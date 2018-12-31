package vanillax.framework.core.db.monitor.print;

import javax.xml.bind.DatatypeConverter;
import java.util.List;

public class ResultSetPrinter {

    private StringBuffer resultSetDataTable = null;
    private static final int MAX_ROWS_PRINTABLE = 1000;
    private static final int MAX_COL_SPACE = 100;
    private static final String LINE_SEP = System.getProperty("line.separator");

    public ResultSetPrinter() {
        resultSetDataTable = new StringBuffer();
    }

    public String getResultSetToPrint(ResultSetCollector resultSetCollector) {
        this.resultSetDataTable.append(LINE_SEP);
        this.resultSetDataTable.append("rows : "+resultSetCollector.getRows().size() +" / " + resultSetCollector.getTotalRows());
        this.resultSetDataTable.append(LINE_SEP);

        int columnCount = resultSetCollector.getColumnCount();
        int maxLength[] = new int[columnCount];
        //칼럼의 최대 길기 구하기
        //칼럼 헤더와 값을 모두 열어서 길이를 확인한다.
        //한글인 경우 2자리로 계산한다.
        List<List<Object>> rows = resultSetCollector.getRows();
        adjustStr(rows);
        for (int column = 1; column <= columnCount; column++) {
            int maxCharCnt = countLength(resultSetCollector.getColumnName(column));
            for(int i = 0; i < rows.size() && i < MAX_ROWS_PRINTABLE;i++){
                List<Object> row = rows.get(i);
                int length = countLength(row.get(column -1).toString());
//                System.out.println(" row.get(" +(column -1)+").toString() :" +row.get(column -1).toString() +" / length : "+length);
                maxCharCnt = Math.max(maxCharCnt, length);
            }
            maxLength[column - 1] = maxCharCnt;
        }
        if (resultSetCollector.getRows() != null) {
            for (List<Object> printRow : resultSetCollector.getRows()) {
                int colIndex = 0;
                for (Object v : printRow) {
                    if (v != null) {
                        int length = v.toString().length();
                        if (length > maxLength[colIndex]) {
                            maxLength[colIndex] = length;
                        }
                    }
                    colIndex++;
                }
            }
        }
        for (int column = 1; column <= columnCount; column++) {
            maxLength[column - 1] = maxLength[column - 1] + 1;
        }
        //첫줄 출력 : +----------+-----------+
        this.resultSetDataTable.append("+");

        for (int column = 1; column <= columnCount; column++) {
            this.resultSetDataTable.append(padRight("-", maxLength[column - 1]).replaceAll(" ", "-") + "+");
        }
        this.resultSetDataTable.append(LINE_SEP);
        this.resultSetDataTable.append("|");//column 헤더 출력 : |column1  |column2 |
        for (int column = 1; column <= columnCount; column++) {
            this.resultSetDataTable.append(padRight(resultSetCollector.getColumnName(column), maxLength[column - 1]) + "|");
        }
        this.resultSetDataTable.append(LINE_SEP);
        this.resultSetDataTable.append("+");//column 헤더 아랫줄 출력 : +----------+-----------+
        for (int column = 1; column <= columnCount; column++) {
            this.resultSetDataTable.append(padRight("-", maxLength[column - 1]).replaceAll(" ", "-") + "+");
        }
        this.resultSetDataTable.append(LINE_SEP);
        if (resultSetCollector.getRows() != null) {//row 별 데이터 출력
            int cnt = 0;
            for (List<Object> printRow : resultSetCollector.getRows()) {
                int colIndex = 0;
                this.resultSetDataTable.append("|");
                for (Object v : printRow) {
                    this.resultSetDataTable.append(padRight(v == null ? "(null)" : v.toString(), maxLength[colIndex]) + "|");
                    colIndex++;
                }
                this.resultSetDataTable.append(LINE_SEP);
                if(++cnt >= MAX_ROWS_PRINTABLE) //최대 출력치를 벗어나면 더 이상 출력하지 않는다.
                    break;
            }
        }
        this.resultSetDataTable.append("+");
        for (int column = 1; column <= columnCount; column++) {
            this.resultSetDataTable.append(padRight("-", maxLength[column - 1]).replaceAll(" ", "-") + "+");
        }

        this.resultSetDataTable.append(LINE_SEP);

        resultSetCollector.reset();

        return this.resultSetDataTable.toString() ;

    }

    private static String padRight(String s, int n) {
        n = n - (countLength(s) - s.length());//한글이 있는 경우 다시 글수를 줄여준다.
        return String.format("%1$-" + n + "s", s);
    }

    private static int countLength(String str){
        if(str == null){
            return "(null)".length();
        }
        int cnt = 0;

        for(char c : str.toCharArray()){
            if(c > 256){ //한글일 경우 두 칸으로 간주한다.
                cnt = cnt + 2;
            }else{
                cnt++;
            }
        }
        return cnt;
    }

    /**
     * 개행문자를 \r\n형태의 문자열로 바꿔주고 최대 문자열이 넘어갈 경우 ...축약시켜주는 함수.
     **/
    private static void adjustStr(List<List<Object>> rows){
        int cnt = 0;
        for(List<Object> row : rows){
            int idx = 0;
            for(Object obj : row){

                if(obj != null && obj instanceof Byte){
                    obj = String.format("0x%02X",((Byte)obj).byteValue());
                }
                if(obj != null && obj instanceof Byte[]){
                    Byte[] arr = (Byte[])obj;
                    if(arr.length == 0){
                        obj = "";
                    }else{
                        byte[] bytes = new byte[arr.length];
                        for(int i = 0; i < arr.length; i++) bytes[i] = arr[i];
                        obj = DatatypeConverter.printHexBinary(bytes);
                    }
                }

                if(obj != null && obj instanceof String){
                    String s = (String)obj;
                    int charCnt = 0;
                    StringBuffer sb = new StringBuffer();
                    for(char c : s.toCharArray()){
                        if(c == '\r'){
                            sb.append("\\r");
                            charCnt++;
                        }else if(c == '\n'){
                            sb.append("\\n");
                            charCnt++;
                        }else if(c == '\t'){
                            sb.append("    ");
                            charCnt = charCnt+3;
                        }else{
                            sb.append(c);
                        }
                        if(c > 256){ //한글일 경우 두 칸으로 간주한다.
                            charCnt++;
                        }
                        charCnt++;

                        if(charCnt == MAX_COL_SPACE - 3){
                            sb.append("...");
                            break;
                        }else if(charCnt >= MAX_COL_SPACE - 2){
                            sb.append("..");
                            break;
                        }
                    }//..s.toCharArray()
                    row.set(idx, sb.toString());
                }
                idx++;
            }//..row
            if(++cnt >= MAX_ROWS_PRINTABLE) //최대 출력치를 벗어나면 더 이상 출력하지 않는다.
                break;
        }//..rows

    }
}
