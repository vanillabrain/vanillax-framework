/*
 * Copyright (C) 2016 Vanilla Brain, Team - All Rights Reserved
 *
 * This file is part of 'VanillaTopic'
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Vanilla Brain Team and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Vanilla Brain Incorporated
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Vanilla Brain Team.
 */

package vanillax.framework.core.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

/**
 * 문자열 처리 Util
 */
public class StringUtil {

    /**
     * /로 구분되는 문자열을 각 레벨별로 나누어 리스트로 변환한다.
     * aa/bb/cc/dd
     * --
     *   [0] : aa
     *   [1] : aa/bb
     *   [2] : aa/bb/cc
     *   [3] : aa/bb/cc/dd
     * @param path 분리할 문자열 예) "aa/bb/cc/dd"
     * @return 분리한 문자열 리스트
     * @throws Exception
     */
    public static List<String> extractPathArr(String path)throws Exception {
        if(path == null){
            throw new Exception("부적절한 인자 값입니다");
        }
        List<String> list = new ArrayList<String>(10);
        String[] arr = path.split("/");
        int cnt = 0;
        String prevString = "";
        for(String x:arr){
            if(x.trim().equals("")){
                continue;
            }
            if(cnt > 0){
                String p = prevString + "/" + x;
                list.add(p);
                prevString = p;
            }else{
                list.add(x);
                prevString = x;
            }
            cnt++;
        }
        return list;
    }

    public static String unescapeJava(String str){
        return unescapeJava(str, false);
    }

    public static String unescapeJava(String str, boolean escapeDoubleQuote) {
        if(str == null) {
            return null;
        } else {
            try {
                StringWriter ioe = new StringWriter(str.length());
                unescapeJava(ioe, str, escapeDoubleQuote);
                return ioe.toString();
            } catch (IOException var2) {
                throw new RuntimeException(var2);
            }
        }
    }

    public static void unescapeJava(Writer out, String str, boolean escapeDoubleQuote) throws IOException {
        if(out == null) {
            throw new IllegalArgumentException("The Writer must not be null");
        } else if(str != null) {
            int sz = str.length();
            StringBuilder unicode = new StringBuilder(4);
            boolean hadSlash = false;
            boolean inUnicode = false;

            for(int i = 0; i < sz; ++i) {
                char ch = str.charAt(i);
                if(inUnicode) {
                    unicode.append(ch);
                    if(unicode.length() == 4) {
                        try {
                            int nfe = Integer.parseInt(unicode.toString(), 16);
                            out.write((char)nfe);
                            unicode.setLength(0);
                            inUnicode = false;
                            hadSlash = false;
                        } catch (NumberFormatException var9) {
                            throw new RuntimeException("Unable to parse unicode value: " + unicode, var9);
                        }
                    }
                } else if(hadSlash) {
                    hadSlash = false;
                    switch(ch) {
                        case '\"':
                            if(escapeDoubleQuote)
                                out.write('\\');
                            out.write('\"');
                            break;
                        case '\'':
                            out.write('\'');
                            break;
                        case '\\':
                            if(escapeDoubleQuote)
                                out.write('\\');
                            out.write('\\');
                            break;
                        case 'b':
                        case 'f':
                        case 'n':
                        case 'r':
                        case 't':
                            out.write("\\");
                            out.write(ch);
                            break;
                        case 'u':
                            inUnicode = true;
                            break;
                        default:
                            out.write(ch);
                    }
                } else if(ch == '\\') {
                    hadSlash = true;
                } else {
                    out.write(ch);
                }
            }

            if(hadSlash) {
                out.write('\\');
            }

        }
    }

    /**
     * 문자열이 null이거나 빈공간만 있는지 확인한다.
     * @param str 비교문자
     * @return null, 개행문자, 빈공간 문자만 있을 경우 true 반환
     */
    public static boolean isEmpty(String str){
        if(str == null)
            return true;
        if(str.trim().equals(""))
            return true;
        return false;
    }

    /**
     * StrackTrace를 String으로 변환한다.
     * @param traces 특정 시점에서 확인한 StackTrace
     * @return 문자열로 변환된 StackTrace
     */
    public static String stackTraceToString(StackTraceElement[] traces){
        StringBuffer sb = new StringBuffer();
        for(StackTraceElement e:traces){
            sb.append("\tat ").append(e.getClassName()).append(".").append(e.getMethodName());
            sb.append("(").append(e.getFileName()).append(":").append(e.getLineNumber());
            sb.append(")\n");
        }
        return sb.toString();
    }

    public static String errorStackTraceToString(Throwable t){
        return t.getClass().getName() + " " + t.getMessage() +"\n"+ stackTraceToString(t.getStackTrace());
    }

    public static String makeUid(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    /**
     * SQL in문에서 사용하기 위해 list내에 있는 key값을 뽑아서 ,로 구분한 문자열을 생성한다.
     * @param list
     * @param key
     * @return
     */
    public static String makeInString(List<Map<String,Object>> list, String key){
        if(list == null || key == null)
            return null;
        StringBuffer sb = new StringBuffer();
        int cnt = 0;
        for(Map<String,Object> x:list){
            Object o = x.get(key);
            if(o == null)
                continue;
            if(o instanceof String || o instanceof Date ){
                sb.append("'").append(o.toString()).append("'");
            }else{
                sb.append(o.toString());
            }
            if(list.size() - 1 > cnt++ ){
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /**
     * SQL문장내에 :xxx 형태의 SQL parameter가 있는 확인한다.
     * 주석문이 있을 경우 주석내 :xxx는 무시한다.
     * @param sql SQL문장.
     * @return :xxx 형태의 파라미터가 있는경우 true
     */
    public static boolean hasSqlParameter(String sql){
        if(sql == null)
            return false;
        char prev = 0;
        boolean lineComment = false;
        boolean starComment = false;
        for(int i=0; i < sql.length(); i++){
            char c = sql.charAt(i);
//            System.out.println(" prev : "+prev+" c : "+c+" lineComment : "+lineComment+" starComment : "+starComment);
            if(!lineComment && !starComment && prev == '/' && c =='/'){
                lineComment = true;
                continue;
            }
            if(!lineComment && !starComment && prev == '-' && c =='-'){
                lineComment = true;
                continue;
            }
            if(!lineComment && !starComment  && prev == '/' && c =='*'){
                starComment = true;
                continue;
            }
            if(lineComment && !starComment && c =='\n'){
                lineComment = false;
                continue;
            }
            if(starComment && prev =='*' && c =='/'){
                lineComment = false;
                starComment = false;
                continue;
            }
            if(!lineComment && !starComment && prev ==':'){
                if(c == '_')
                    return true;
                if(c >= 'a' && c<='z')
                    return true;
                if(c >= 'A' && c<='Z')
                    return true;
            }
            prev = c;
        }
        return false;
    }

    public static String excludeComment(String str){
        if(str == null)
            return null;
        StringBuffer sb = new StringBuffer();

        char prev = 0;
        boolean lineComment = false;
        boolean starComment = false;
        for(char c:str.toCharArray()){
//            System.out.println(" prev : "+prev+" c : "+c+" lineComment : "+lineComment+" starComment : "+starComment);
            if(!lineComment && !starComment && prev == '/' && c =='/'){
                lineComment = true;
                continue;
            }
            if(!lineComment && !starComment  && prev == '/' && c =='*'){
                starComment = true;
                continue;
            }
            if(lineComment && !starComment && c =='\n'){
                lineComment = false;
                continue;
            }
            if(starComment && prev =='*' && c =='/'){
                lineComment = false;
                starComment = false;
                continue;
            }
            if(!lineComment && !starComment){
                sb.append(c);
            }
            prev = c;
        }

        return sb.toString();

    }

    public static boolean strmatch(String text, String pattern) {
        return text.matches(pattern.replace("?", ".?").replace("*", ".*?"));
    }

    public static boolean match(String str, String pattern) {
        int n=str.length();
        int m=pattern.length();
        // empty pattern can only match with
        // empty string
        if (m == 0)
            return (n == 0);

        // lookup table for storing results of
        // subproblems
        boolean[][] lookup = new boolean[n + 1][m + 1];

        // initailze lookup table to false
        for(int i = 0; i < n + 1; i++)
            Arrays.fill(lookup[i], false);


        // empty pattern can match with empty string
        lookup[0][0] = true;

        // Only '*' can match with empty string
        for (int j = 1; j <= m; j++)
            if (pattern.charAt(j - 1) == '*')
                lookup[0][j] = lookup[0][j - 1];

        // fill the table in bottom-up fashion
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                // Two cases if we see a '*'
                // a) We ignore '*'' character and move
                //    to next  character in the pattern,
                //     i.e., '*' indicates an empty sequence.
                // b) '*' character matches with ith
                //     character in input
                if (pattern.charAt(j - 1) == '*')
                    lookup[i][j] = lookup[i][j - 1] || lookup[i - 1][j];

                    // Current characters are considered as
                    // matching in two cases
                    // (a) current character of pattern is '?'
                    // (b) characters actually match
                else if (pattern.charAt(j - 1) == '?' || str.charAt(i - 1) == pattern.charAt(j - 1))
                    lookup[i][j] = lookup[i - 1][j - 1];

                    // If characters don't match
                else lookup[i][j] = false;
            }
        }

        return lookup[n][m];
    }


}
