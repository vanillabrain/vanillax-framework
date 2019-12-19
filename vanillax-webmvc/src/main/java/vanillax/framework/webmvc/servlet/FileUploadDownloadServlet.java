package vanillax.framework.webmvc.servlet;

import vanillax.framework.core.db.TransactionManager;
import vanillax.framework.core.db.monitor.ConnectionMonitor;
import vanillax.framework.core.util.ReflectionUtil;
import vanillax.framework.core.util.StringUtil;
import vanillax.framework.core.util.json.JsonOutput;
import vanillax.framework.webmvc.config.ConfigHelper;
import vanillax.framework.webmvc.exception.BaseException;
import vanillax.framework.webmvc.service.IFilter;
import vanillax.framework.webmvc.service.IService;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Service 호출후 파일다운로드 수행
 */
@WebServlet(name="vanillaWebFile", urlPatterns={"/file/*"})
@MultipartConfig(location = "", maxFileSize = 1024 * 1024 * 10, fileSizeThreshold = 0, maxRequestSize = 1024 * 1024 * 20)
public class FileUploadDownloadServlet extends MvcServletBase {
    private static final Logger log = Logger.getLogger(FileUploadDownloadServlet.class.getName());

    public void init() throws ServletException {
        // Do required initialization
        super.init();
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        String method = request.getMethod();
        String s = request.getPathInfo();
        if(method.equals("GET")){
            String fileDownloadUri = ConfigHelper.get("file.download.uri","/fileDownload");
            fileDownloadUri = fileDownloadUri.trim();
            if(!fileDownloadUri.startsWith("/")){
                fileDownloadUri = "/"+fileDownloadUri;
            }

            if(!s.startsWith(fileDownloadUri)){
                throw new ServletException("Wrong request");
            }
            doGetDownload(request, response);
        }else if(method.equals("POST")){
            String fileUploadUri = ConfigHelper.get("file.upload.uri","/fileUpload");
            fileUploadUri = fileUploadUri.trim();
            if(!fileUploadUri.startsWith("/")){
                fileUploadUri = "/"+fileUploadUri;
            }
            if(!s.startsWith(fileUploadUri)){
                throw new ServletException("Wrong request");
            }
            doPostUpload(request, response);
        }
    }

    private void doGetDownload(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        IService service = null;
        Map<String, Object> data = new HashMap<String,Object>();
        Object json = null;
        Object result = null;
        boolean onError = false;
        List<IFilter> filterList = null;
        try{
            String method = request.getMethod();
            service = this.searchService(request);
            json = readContent(request);//String을 GroovyJson Object로 변환한다.
            data.put("_input", json);
            this.setBaseData(data, request, response, service.getId());

            //Filter초기화
            filterList = makeFilterList();
            //Filter 전처리
            data = filterPreprocess(filterList, data);

            if(method.equals("GET")) {
                // URI path에 마지막이 ID로 입력되는 경우 기본으로는 findOne을 호출하고 ID값이 입력되지 않은 경우 기본적으로 findMany를 호출한다.
                // 단, findOne, findMany 함수가 정의되어있을 경우에만 호출한다.
                // 기본호출은 find()함수이다.
                int findType = 0;//default
                if(StringUtil.isEmpty((String)data.get("_path"))){
                    if(ReflectionUtil.findMethod(service.getClass(),"findMany") != null){
                        findType = 1;//findMany
                    }
                }else{
                    if(ReflectionUtil.findMethod(service.getClass(),"findOne") != null){
                        findType = 2;//findOne
                    }
                }
                switch (findType){
                    case 1:
                        result = service.findMany(data);
                        break;
                    case 2:
                        result = service.findOne(data);
                        break;
                    default:
                        result = service.find(data);
                        break;
                }
            } else {
                String errMsg1 = localStrings.getString("http.method_not_implemented");
                Map<String, String> errorMap = new LinkedHashMap<String, String>();
                errorMap.put("_result", "ERROR");
                errorMap.put("message", errMsg1);
                result = errorMap;
                response.setStatus(500);
                onError = true;
            }

            //Filter 후처리. 전처리 역순. 오류가 발생하지 않을경우만 처리.
            if(!onError){
                result = filterPostprocess(filterList, result);
            }
        }catch(Throwable e){
            String stackTrace = StringUtil.errorStackTraceToString(e);
            log.warning("Error occurred during file downloading : " + stackTrace);//파일 다운로드중 오류가 발생했습니다
            Map<String, String> errorMap = new LinkedHashMap<String, String>();
            errorMap.put("_result", "ERROR");
            errorMap.put("message", e.getMessage());
            if(e instanceof BaseException){
                BaseException be = (BaseException)e;
                errorMap.put("detail", be.getDetail());
                errorMap.put("code", be.getCode());
            }
            if(ConfigHelper.getBoolean("response.stackTrace", false)){
                errorMap.put("stackTrace", stackTrace);
            }
            result = errorMap;
            response.setStatus(424);//메소드 실패
            onError = true;
        }finally {
            TransactionManager.getInstance().clearTxSession();//Transaction 관련 데이터를 초기화한다.
            ConnectionMonitor.getInstance().onThreadFinished();//Thread종료시 close되지 않은 Connection이 있는지 검사한다.
            if(filterList != null)
                filterList.clear(); //필터 정리
        }

        if(onError){
            response.setContentType("application/json; charset=UTF-8");
            JsonOutput.toJson(response.getWriter(), result);
            response.flushBuffer();
            return;
        }

        //File 밀어넣기
        Map<String,Object> map = ((Map<String,Object>) result);
        Map<String,Object> m = (Map<String,Object>)map.get("resultObject");
        String fileExt = (String)m.get("fileExt");
        File downloadFile = (File)m.get("downloadFile");
        String filename = (String)m.get("fileName");
        if(isImage(fileExt)){
            response.setContentType("image/"+fileExt);
        }else{
            response.setContentType("application/octet-stream;");
            //파일명처리
            String userAgent = request.getHeader("User-Agent");

            // attachment; 가 붙으면 IE의 경우 무조건 다운로드창이 뜬다. 상황에 따라 써야한다.
            if (userAgent != null && userAgent.indexOf("MSIE 5.5") > -1) { // MS IE 5.5 이하
                response.setHeader("Content-Disposition", "filename=" + URLEncoder.encode(filename, "UTF-8") + ";");
            } else if (userAgent != null && userAgent.indexOf("MSIE") > -1) { // MS IE (보통은 6.x 이상 가정)
                response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8") + ";");
            } else { // 모질라나 오페라
                response.setHeader("Content-Disposition", "attachment; filename=" + new String(filename.getBytes("UTF-8"), "latin1") + ";");
            }
        }

        response.setContentLength((int)downloadFile.length());

        BufferedInputStream fin = null;
        BufferedOutputStream outs = null;
        byte[] buffer = new byte[1024];

        try {
            fin = new BufferedInputStream(new FileInputStream(downloadFile));
            outs = new BufferedOutputStream(response.getOutputStream());
            int read = 0;

            while ((read = fin.read(buffer)) != -1) {
                outs.write(buffer, 0, read);
            }
            outs.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {fin.close();} catch (Exception ex1){}
            try {outs.close();} catch (Exception ex1){}
        } // end of try/catch
    }

    private void doPostUpload(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        IService service = null;
        Map<String, Object> data = new HashMap<String,Object>();
        Object json = null;
        Object result = null;
        boolean onError = false;
        List<IFilter> filterList = null;
        String UPLOAD_DIRECTORY = ConfigHelper.get("file.upload.directory");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("/yyyy/MM/dd");
        long MAX_FILE_SIZE = 10*1024*1024;//10M
        long MAX_REQUEST_SIZE = 10*1024*1024;//10M

        try{
            String method = request.getMethod();
            service = this.searchService(request);
            this.setBaseData(data, request, response, service.getId());

            //Filter초기화
            filterList = makeFilterList();
            //Filter 전처리
            data = filterPreprocess(filterList, data);

            //process only if its multipart content
            if(request.getContentType().indexOf("multipart") < 0){
                throw new Exception("Not file upload format.");
            }

            String dateDir = simpleDateFormat.format(new Date(System.currentTimeMillis()));

            String uploadPath = UPLOAD_DIRECTORY + dateDir;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            List<Map<String, String>> fileList = new ArrayList<>(8);
            for (Part part : request.getParts()) {
                String fileName = getFileName(part);
                if("".equals(fileName)){
                    continue;
                }

                String filePath = uploadPath + "/" + fileName;
                int idxDot = fileName.lastIndexOf(".");
                String ext = extractExt(fileName);

                String fileNamePre = idxDot < 0 ? fileName : fileName.substring(0, idxDot);
                //본래 파일명을 숨길것인지 확인한다
                if(ConfigHelper.getBoolean("file.hide.upload.filename",false)){
                    fileNamePre ="F" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(System.currentTimeMillis());
                    filePath = uploadPath + "/" + fileNamePre + (ext == null ? "": "."+ext);
                }

                File storeFile = new File(filePath);
                for(int i = 1; storeFile.exists() && i <= 1000;i++){
                    String newFileName = fileNamePre+"("+i+")";
                    if(ext != null)
                        newFileName = newFileName + "." + ext;
                    storeFile = new File(new File(uploadPath), newFileName);
                }
                part.write(storeFile.getAbsolutePath());
                Map<String, String> fileInfo = new LinkedHashMap<>();
                fileInfo.put("fileName", fileName);
                fileInfo.put("fileExt", ext);
                fileInfo.put("filePath", dateDir + "/" + storeFile.getName());
                fileList.add(fileInfo);
            }

            //fileName, fileExt, filePath
            data.put("uploadFileList", fileList);
            result = service.insert(data);

            //Filter 후처리. 전처리 역순. 오류가 발생하지 않을경우만 처리.
            if(!onError){
                result = filterPostprocess(filterList, result);
            }
        }catch(Throwable e){
            String stackTrace = StringUtil.errorStackTraceToString(e);
            log.warning("Error occurred during file uploading : " + stackTrace);//파일 업로드중 오류가 발생했습니다
            Map<String, String> errorMap = new LinkedHashMap<String, String>();
            errorMap.put("_result", "ERROR");
            errorMap.put("message", e.getMessage());
            if(e instanceof BaseException){
                BaseException be = (BaseException)e;
                errorMap.put("detail", be.getDetail());
                errorMap.put("code", be.getCode());
            }
            if(ConfigHelper.getBoolean("response.stackTrace", false)){
                errorMap.put("stackTrace", stackTrace);
            }
            result = errorMap;
            response.setStatus(424);//메소드 실패
            onError = true;
        }finally {
            TransactionManager.getInstance().clearTxSession();//Transaction 관련 데이터를 초기화한다.
            ConnectionMonitor.getInstance().onThreadFinished();//Thread종료시 close되지 않은 Connection이 있는지 검사한다.
            if(filterList != null)
                filterList.clear(); //필터 정리
        }
        response.setContentType("application/json; charset=UTF-8");
        JsonOutput.toJson(response.getWriter(), result);
        response.flushBuffer();
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    private boolean isImage(String ext){
        if(ext == null)
            return false;
        String[] imgArr = {"jpg","jpeg","png","tiff","btm"};
        for(String imgExt : imgArr){
            if(ext.equalsIgnoreCase(imgExt)){
                return true;
            }
        }
        return false;
    }

    private String extractExt(String fileName){
        if(fileName == null || fileName.indexOf(".") <0)
            return  null;
        return fileName.substring(fileName.lastIndexOf(".")+1, fileName.length());
    }

    /**
     * Utility method to get file name from HTTP header content-disposition
     */
    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
//        System.out.println("content-disposition header= "+contentDisp);
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length()-1);
            }
        }
        return "";
    }

}
