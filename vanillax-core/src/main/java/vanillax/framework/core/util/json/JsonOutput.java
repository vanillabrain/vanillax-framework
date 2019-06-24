package vanillax.framework.core.util.json;

import groovy.json.JsonDelegate;
import groovy.json.JsonException;
import groovy.json.JsonLexer;
import groovy.json.JsonToken;
import groovy.json.internal.Chr;
import groovy.lang.Closure;
import groovy.util.Expando;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.beans.Transient;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Groovy 내부 클래스를 수정하여 사용한다.
 * Unicode를 uXXXX 형태가 아닌 문자열 그대로 생성한다.
 * Writer에 문자열을 기록하여 메모리 사용을 줄였다.
 * 참조 : groovy.json.JsonOutput
 */
public class JsonOutput {

    static final char OPEN_BRACKET = '[';
    static final char CLOSE_BRACKET = ']';
    static final char OPEN_BRACE = '{';
    static final char CLOSE_BRACE = '}';
    static final char COLON = ':';
    static final char COMMA = ',';
    static final char SPACE = ' ';
    static final char NEW_LINE = '\n';
    static final char QUOTE = '"';

    private static final char[] EMPTY_STRING_CHARS = Chr.array(QUOTE, QUOTE);

    private static final String NULL_VALUE = "null";
    private static final String JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
//    private static final String DEFAULT_TIMEZONE = "GMT";
    private static final String DEFAULT_TIMEZONE = "Asia/Seoul";

    /**
     * "null" for a null value, or a JSON array representation for a collection, array, iterator or enumeration,
     * or representation for other object.
     */
    public static void toJson(Writer writer, Object object) throws IOException {
        try {
            JsonCharWriter buffer = new JsonCharWriter(writer);
            writeObject(object, buffer); // checking null inside
        }catch (Exception e){
            if( e instanceof IOException){
                throw (IOException)e;
            }
            throw new IOException(e);
        }
    }

    public static String toJson(Object object) throws IOException{
        StringWriter writer = null;
        try {
            writer = new StringWriter();
            JsonCharWriter buffer = new JsonCharWriter(writer);
            writeObject(object, buffer); // checking null inside
        }catch (Exception e){
            if( e instanceof IOException){
                throw (IOException)e;
            }
            throw new IOException(e);
        }
        return writer.toString();
    }

    /**
     * Serializes Number value and writes it into specified jsonCharWriter.
     */
    private static void writeNumber(Class<?> numberClass, Number value, JsonCharWriter jsonCharWriter) throws Exception{
        if (numberClass == Integer.class) {
            jsonCharWriter.addInt((Integer) value);
        } else if (numberClass == Long.class) {
            jsonCharWriter.addLong((Long) value);
        } else if (numberClass == BigInteger.class) {
            jsonCharWriter.addBigInteger((BigInteger) value);
        } else if (numberClass == BigDecimal.class) {
            jsonCharWriter.addBigDecimal((BigDecimal) value);
        } else if (numberClass == Double.class) {
            Double doubleValue = (Double) value;
            if (doubleValue.isInfinite()) {
                throw new JsonException("Number " + value + " can't be serialized as JSON: infinite are not allowed in JSON.");
            }
            if (doubleValue.isNaN()) {
                throw new JsonException("Number " + value + " can't be serialized as JSON: NaN are not allowed in JSON.");
            }

            jsonCharWriter.addDouble(doubleValue);
        } else if (numberClass == Float.class) {
            Float floatValue = (Float) value;
            if (floatValue.isInfinite()) {
                throw new JsonException("Number " + value + " can't be serialized as JSON: infinite are not allowed in JSON.");
            }
            if (floatValue.isNaN()) {
                throw new JsonException("Number " + value + " can't be serialized as JSON: NaN are not allowed in JSON.");
            }

            jsonCharWriter.addFloat(floatValue);
        } else if (numberClass == Byte.class) {
            jsonCharWriter.addByte((Byte) value);
        } else if (numberClass == Short.class) {
            jsonCharWriter.addShort((Short) value);
        } else { // Handle other Number implementations
            jsonCharWriter.addString(value.toString());
        }
    }

    /**
     * Serializes object and writes it into specified buffer.
     */
    private static void writeObject(Object object, JsonCharWriter buffer) throws Exception{
        if (object == null) {
            buffer.addNull();
        } else {
            Class<?> objectClass = object.getClass();

            if (CharSequence.class.isAssignableFrom(objectClass)) { // Handle String, StringBuilder, GString and other CharSequence implementations
                writeCharSequence((CharSequence) object, buffer);
            } else if (objectClass == Boolean.class) {
                buffer.addBoolean((Boolean) object);
            } else if (Number.class.isAssignableFrom(objectClass)) {
                writeNumber(objectClass, (Number) object, buffer);
            } else if (Date.class.isAssignableFrom(objectClass)) {
                writeDate((Date) object, buffer);
            } else if (Calendar.class.isAssignableFrom(objectClass)) {
                writeDate(((Calendar) object).getTime(), buffer);
            } else if (Map.class.isAssignableFrom(objectClass)) {
                writeMap((Map) object, buffer);
            } else if (Iterable.class.isAssignableFrom(objectClass)) {
                writeIterator(((Iterable<?>) object).iterator(), buffer);
            } else if (Iterator.class.isAssignableFrom(objectClass)) {
                writeIterator((Iterator) object, buffer);
            } else if (objectClass == Character.class) {
                buffer.addJsonEscapedString(Chr.array((Character) object));
            } else if (objectClass == URL.class) {
                buffer.addJsonEscapedString(object.toString());
            } else if (objectClass == UUID.class) {
                buffer.addQuoted(object.toString());
            } else if (objectClass == groovy.json.JsonOutput.JsonUnescaped.class) {
                buffer.add(object.toString());
            } else if (Closure.class.isAssignableFrom(objectClass)) {
                writeMap(JsonDelegate.cloneDelegateAndGetContent((Closure<?>) object), buffer);
            } else if (Expando.class.isAssignableFrom(objectClass)) {
                writeMap(((Expando) object).getProperties(), buffer);
            } else if (Enumeration.class.isAssignableFrom(objectClass)) {
                List<?> list = Collections.list((Enumeration<?>) object);
                writeIterator(list.iterator(), buffer);
            } else if (objectClass.isArray()) {
                writeArray(objectClass, object, buffer);
            } else if (Enum.class.isAssignableFrom(objectClass)) {
                buffer.addQuoted(((Enum<?>) object).name());
            }else if (File.class.isAssignableFrom(objectClass)){
                Map<?, ?> properties = getObjectProperties(object);
                //Clean up all recursive references to File objects
                Iterator<? extends Map.Entry<?, ?>> iterator = properties.entrySet().iterator();
                while(iterator.hasNext()){
                    Map.Entry<?,?> entry = iterator.next();
                    if(entry.getValue() instanceof File){
                        iterator.remove();
                    }
                }

                writeMap(properties, buffer);
            } else {
                Map<?, ?> properties = getObjectProperties(object);
                writeMap(properties, buffer);
            }
        }
    }

    private static Map<?, ?> getObjectProperties(Object object) {
        Map<?, ?> properties = DefaultGroovyMethods.getProperties(object);//getter, setter가 있는 필드가 모두 추출된다
        properties.remove("class");
        properties.remove("declaringClass");
        properties.remove("metaClass");
        //transient modifier가 있거나 getter 메소드가 없거나 getter 메소드에 @Transient 가 있으면 그 필드는 무시한다.
        if(!(object instanceof Map)){
            Iterator<?> iterator = properties.keySet().iterator();
            List<String> exceptList = new ArrayList<>();
            while(iterator.hasNext()){
                String s = (String)iterator.next();
                if(!isJsonExtractingProperty(object.getClass(), s)){
                    exceptList.add(s);
                }
            }
            for(String except:exceptList){
                properties.remove(except);
            }
        }
        return properties;
    }

    /**
     * JSon 문자로 변환하기위한 대상의 필드 인지 확인한다.
     * field에 transient가 정의되어있으면 제외된다.
     * getter가 없으면 제외된다.
     * getter 메소드에 @Transient가 있으면 제외된다
     * @param clazz 검사할 대상의 클래스
     * @param fieldName 추출대상의 필드
     * @return getter가 존재하는 transient 정의가 없는 필드이면 true반환
     */
    private static boolean isJsonExtractingProperty(Class clazz, String fieldName){

        try {
            //transient modifier가 있으면 추출 대상이 아니다
            Field field = clazz.getDeclaredField(fieldName);
            boolean isTransient = Modifier.isTransient(field.getModifiers());
            if (isTransient)
                return false;
        }catch (Exception ignore){
            //do nothing
        }

        try {
            //getter method가 있는지 확인한다.
            //필드가 없더라도 getter가 있으면 추출한다.
            Method[] methods = clazz.getMethods();
            for(Method method:methods){
                String c1 = (fieldName.charAt(0)+"").toUpperCase();
                String s1 = "get"+c1+fieldName.substring(1);//camelCase
                if(method.getName().equals("get"+fieldName) || method.getName().equals(s1)){
                    //@Transient가 있으면 무시한다.
                    if (!method.isAnnotationPresent((Class<? extends Annotation>) Transient.class)){
                        return true;
                    }
                }
            }
        }catch (Exception e){
            //do nothing
//            e.printStackTrace();
        }
        return false;
    }

    /**
     * Serializes any char sequence and writes it into specified buffer.
     */
    private static void writeCharSequence(CharSequence seq, JsonCharWriter buffer) throws Exception{
        if (seq.length() > 0) {
            buffer.addJsonEscapedString(seq.toString());
        } else {
            buffer.addChars(EMPTY_STRING_CHARS);
        }
    }

    /**
     * Serializes date and writes it into specified buffer.
     */
    private static void writeDate(Date date, JsonCharWriter buffer) throws Exception{
        SimpleDateFormat formatter = new SimpleDateFormat(JSON_DATE_FORMAT, Locale.KOREA);
        formatter.setTimeZone(TimeZone.getTimeZone(DEFAULT_TIMEZONE));
        buffer.addQuoted(formatter.format(date));
    }

    /**
     * Serializes array and writes it into specified buffer.
     */
    private static void writeArray(Class<?> arrayClass, Object array, JsonCharWriter buffer) throws Exception{
        buffer.addChar(OPEN_BRACKET);
        if (Object[].class.isAssignableFrom(arrayClass)) {
            Object[] objArray = (Object[]) array;
            if (objArray.length > 0) {
                writeObject(objArray[0], buffer);
                for (int i = 1; i < objArray.length; i++) {
                    buffer.addChar(COMMA);
                    writeObject(objArray[i], buffer);
                }
            }
        } else if (int[].class.isAssignableFrom(arrayClass)) {
            int[] intArray = (int[]) array;
            if (intArray.length > 0) {
                buffer.addInt(intArray[0]);
                for (int i = 1; i < intArray.length; i++) {
                    buffer.addChar(COMMA).addInt(intArray[i]);
                }
            }
        } else if (long[].class.isAssignableFrom(arrayClass)) {
            long[] longArray = (long[]) array;
            if (longArray.length > 0) {
                buffer.addLong(longArray[0]);
                for (int i = 1; i < longArray.length; i++) {
                    buffer.addChar(COMMA).addLong(longArray[i]);
                }
            }
        } else if (boolean[].class.isAssignableFrom(arrayClass)) {
            boolean[] booleanArray = (boolean[]) array;
            if (booleanArray.length > 0) {
                buffer.addBoolean(booleanArray[0]);
                for (int i = 1; i < booleanArray.length; i++) {
                    buffer.addChar(COMMA).addBoolean(booleanArray[i]);
                }
            }
        } else if (char[].class.isAssignableFrom(arrayClass)) {
            char[] charArray = (char[]) array;
            if (charArray.length > 0) {
                buffer.addJsonEscapedString(Chr.array(charArray[0]));
                for (int i = 1; i < charArray.length; i++) {
                    buffer.addChar(COMMA).addJsonEscapedString(Chr.array(charArray[i]));
                }
            }
        } else if (double[].class.isAssignableFrom(arrayClass)) {
            double[] doubleArray = (double[]) array;
            if (doubleArray.length > 0) {
                buffer.addDouble(doubleArray[0]);
                for (int i = 1; i < doubleArray.length; i++) {
                    buffer.addChar(COMMA).addDouble(doubleArray[i]);
                }
            }
        } else if (float[].class.isAssignableFrom(arrayClass)) {
            float[] floatArray = (float[]) array;
            if (floatArray.length > 0) {
                buffer.addFloat(floatArray[0]);
                for (int i = 1; i < floatArray.length; i++) {
                    buffer.addChar(COMMA).addFloat(floatArray[i]);
                }
            }
        } else if (byte[].class.isAssignableFrom(arrayClass)) {
            byte[] byteArray = (byte[]) array;
            if (byteArray.length > 0) {
                buffer.addByte(byteArray[0]);
                for (int i = 1; i < byteArray.length; i++) {
                    buffer.addChar(COMMA).addByte(byteArray[i]);
                }
            }
        } else if (short[].class.isAssignableFrom(arrayClass)) {
            short[] shortArray = (short[]) array;
            if (shortArray.length > 0) {
                buffer.addShort(shortArray[0]);
                for (int i = 1; i < shortArray.length; i++) {
                    buffer.addChar(COMMA).addShort(shortArray[i]);
                }
            }
        }
        buffer.addChar(CLOSE_BRACKET);
    }

    private static final char[] EMPTY_MAP_CHARS = {OPEN_BRACE, CLOSE_BRACE};

    /**
     * Serializes map and writes it into specified buffer.
     */
    private static void writeMap(Map<?, ?> map, JsonCharWriter buffer) throws Exception{
        if (!map.isEmpty()) {
            buffer.addChar(OPEN_BRACE);
            boolean firstItem = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() == null) {
                    throw new IllegalArgumentException("Maps with null keys can\'t be converted to JSON");
                }

                if (!firstItem) {
                    buffer.addChar(COMMA);
                } else {
                    firstItem = false;
                }

                buffer.addJsonFieldName(entry.getKey().toString());
                writeObject(entry.getValue(), buffer);
            }
            buffer.addChar(CLOSE_BRACE);
        } else {
            buffer.addChars(EMPTY_MAP_CHARS);
        }
    }

    private static final char[] EMPTY_LIST_CHARS = {OPEN_BRACKET, CLOSE_BRACKET};

    /**
     * Serializes iterator and writes it into specified buffer.
     */
    private static void writeIterator(Iterator<?> iterator, JsonCharWriter buffer) throws Exception{
        if (iterator.hasNext()) {
            buffer.addChar(OPEN_BRACKET);
            Object it = iterator.next();
            writeObject(it, buffer);
            while (iterator.hasNext()) {
                it = iterator.next();
                buffer.addChar(COMMA);
                writeObject(it, buffer);
            }
            buffer.addChar(CLOSE_BRACKET);
        } else {
            buffer.addChars(EMPTY_LIST_CHARS);
        }
    }

    /**
     * Pretty print a JSON payload.
     *
     * @param jsonPayload
     * @return a pretty representation of JSON payload.
     */
    public static String prettyPrint(String jsonPayload) throws Exception{
        int indentSize = 0;
        // Just a guess that the pretty view will take a 20 percent more than original.
        StringWriter writer = new StringWriter((int) (jsonPayload.length() * 0.2));
        final JsonCharWriter jsonCharWriter = new JsonCharWriter(writer);

        JsonLexer lexer = new JsonLexer(new StringReader(jsonPayload));
        // Will store already created indents.
        Map<Integer, char[]> indentCache = new HashMap<Integer, char[]>();
        while (lexer.hasNext()) {
            JsonToken token = lexer.next();
            switch (token.getType()) {
                case OPEN_CURLY:
                    indentSize += 4;
                    jsonCharWriter.addChars(Chr.array(OPEN_BRACE, NEW_LINE)).addChars(getIndent(indentSize, indentCache));

                    break;
                case CLOSE_CURLY:
                    indentSize -= 4;
                    jsonCharWriter.addChar(NEW_LINE);
                    if (indentSize > 0) {
                        jsonCharWriter.addChars(getIndent(indentSize, indentCache));
                    }
                    jsonCharWriter.addChar(CLOSE_BRACE);

                    break;
                case OPEN_BRACKET:
                    indentSize += 4;
                    jsonCharWriter.addChars(Chr.array(OPEN_BRACKET, NEW_LINE)).addChars(getIndent(indentSize, indentCache));

                    break;
                case CLOSE_BRACKET:
                    indentSize -= 4;
                    jsonCharWriter.addChar(NEW_LINE);
                    if (indentSize > 0) {
                        jsonCharWriter.addChars(getIndent(indentSize, indentCache));
                    }
                    jsonCharWriter.addChar(CLOSE_BRACKET);

                    break;
                case COMMA:
                    jsonCharWriter.addChars(Chr.array(COMMA, NEW_LINE)).addChars(getIndent(indentSize, indentCache));

                    break;
                case COLON:
                    jsonCharWriter.addChars(Chr.array(COLON, SPACE));

                    break;
                case STRING:
                    String textStr = token.getText();
                    String textWithoutQuotes = textStr.substring(1, textStr.length() - 1);
                    if (textWithoutQuotes.length() > 0) {
                        jsonCharWriter.addJsonEscapedString(textWithoutQuotes);
                    } else {
                        jsonCharWriter.addQuoted(Chr.array());
                    }

                    break;
                default:
                    jsonCharWriter.addString(token.getText());
            }
        }

        return writer.toString();
    }

    /**
     * Creates new indent if it not exists in the indent cache.
     *
     * @return indent with the specified size.
     */
    private static char[] getIndent(int indentSize, Map<Integer, char[]> indentCache) {
        char[] indent = indentCache.get(indentSize);
        if (indent == null) {
            indent = new char[indentSize];
            Arrays.fill(indent, SPACE);
            indentCache.put(indentSize, indent);
        }

        return indent;
    }


}
