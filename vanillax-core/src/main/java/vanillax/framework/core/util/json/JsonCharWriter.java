package vanillax.framework.core.util.json;

import groovy.json.internal.*;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Groovy 내부 클래스를 수정하여 사용한다.
 * 참조 : groovy.json.internal.CharBuf
 */
public class JsonCharWriter extends Writer  {

    private Writer writer;

    public JsonCharWriter(Writer writer) {
        this.writer = writer;
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        this.writer.write(cbuf, off, len);
    }

    public void flush() throws IOException {
        this.writer.flush();
    }

    public void close() throws IOException {
        this.writer.close();
    }

    public final JsonCharWriter add(String str) throws Exception{
        add(FastStringUtils.toCharArray(str));
        return this;
    }

    public final JsonCharWriter addString(String str) throws Exception{
        add(FastStringUtils.toCharArray(str));
        return this;
    }

    public final JsonCharWriter add(int i) throws Exception{
        add(Integer.toString(i));
        return this;
    }

    private Cache<Integer, char[]> icache;

    public final JsonCharWriter addInt(int i) throws Exception{
        switch (i) {
            case 0:
                addChar('0');
                return this;
            case 1:
                addChar('1');
                return this;
            case -1:
                addChar('-');
                addChar('1');
                return this;
        }

        addInt(Integer.valueOf(i));
        return this;
    }

    public final JsonCharWriter addInt(Integer key) throws Exception{
        if (icache == null) {
            icache = new SimpleCache<Integer, char[]>(20, CacheType.LRU);
        }
        char[] chars = icache.get(key);

        if (chars == null) {
            String str = Integer.toString(key);
            chars = FastStringUtils.toCharArray(str);
            icache.put(key, chars);
        }
        addChars(chars);
        return this;
    }

    final char[] trueChars = "true".toCharArray();
    final char[] falseChars = "false".toCharArray();

    public final JsonCharWriter add(boolean b) throws Exception{
        addChars(b ? trueChars : falseChars);
        return this;
    }

    public final JsonCharWriter addBoolean(boolean b) throws Exception{
        add(Boolean.toString(b));
        return this;
    }

    public final JsonCharWriter add(byte i) throws Exception{
        add(Byte.toString(i));
        return this;
    }

    public final JsonCharWriter addByte(byte i) throws Exception{
        addInt(i);
        return this;
    }

    public final JsonCharWriter add(short i)  throws Exception{
        add(Short.toString(i));
        return this;
    }

    public final JsonCharWriter addShort(short i) throws Exception{
        addInt(i);
        return this;
    }

    public final JsonCharWriter add(long l)  throws Exception{
        add(Long.toString(l));
        return this;
    }

    public final JsonCharWriter add(double d) throws Exception{
        add(Double.toString(d));
        return this;
    }

    private Cache<Double, char[]> dcache;

    public final JsonCharWriter addDouble(double d) throws Exception{
        addDouble(Double.valueOf(d));
        return this;
    }

    public final JsonCharWriter addDouble(Double key) throws Exception{
        if (dcache == null) {
            dcache = new SimpleCache<Double, char[]>(20, CacheType.LRU);
        }
        char[] chars = dcache.get(key);

        if (chars == null) {
            String str = Double.toString(key);
            chars = FastStringUtils.toCharArray(str);
            dcache.put(key, chars);
        }

        add(chars);
        return this;
    }

    public final JsonCharWriter add(float d) throws Exception{
        add(Float.toString(d));
        return this;
    }

    private Cache<Float, char[]> fcache;

    public final JsonCharWriter addFloat(float d) throws Exception{
        addFloat(Float.valueOf(d));
        return this;
    }

    public final JsonCharWriter addFloat(Float key) throws Exception{
        if (fcache == null) {
            fcache = new SimpleCache<Float, char[]>(20, CacheType.LRU);
        }
        char[] chars = fcache.get(key);

        if (chars == null) {
            String str = Float.toString(key);
            chars = FastStringUtils.toCharArray(str);
            fcache.put(key, chars);
        }
        add(chars);
        return this;
    }

    public final JsonCharWriter addChar(byte i) throws Exception{
        add((char) i);
        return this;
    }

    public final JsonCharWriter addChar(int i) throws Exception{
        add((char) i);
        return this;
    }

    public final JsonCharWriter addChar(short i) throws Exception{
        add((char) i);
        return this;
    }

    public final JsonCharWriter addChar(final char ch) throws Exception{
        this.writer.write(ch);
        return this;
    }

    public JsonCharWriter addLine(String str) throws Exception{
        add(str.toCharArray());
        add('\n');
        return this;
    }

    public JsonCharWriter addLine(CharSequence str) throws Exception{
        add(str.toString());
        add('\n');
        return this;
    }

    public JsonCharWriter add(char[] chars) throws Exception{
        this.writer.write(chars);
        return this;
    }

    public final JsonCharWriter addChars(char[] chars) throws Exception{
        this.writer.write(chars);
        return this;
    }

    public final JsonCharWriter addQuoted(char[] chars) throws Exception{
        this.writer.write('"');
        this.writer.write(chars);
        this.writer.write('"');
        return this;
    }

    public final JsonCharWriter addJsonEscapedString(String jsonString) throws Exception{
        char[] charArray = FastStringUtils.toCharArray(jsonString);
        return addJsonEscapedString(charArray);
    }

    private static boolean hasAnyJSONControlOrUnicodeChars(int c) {
        /* Anything less than space is a control character. */
        if (c < 30) {
            return true;
        /* 34 is double quote. */
        } else if (c == 34) {
            return true;
        } else if (c == 92) {
            return true;
        } else if (c < ' ' || c > 126) {
            return true;
        }

        return false;
    }

    private static boolean hasAnyJSONControlChars(final char[] charArray) {
        int index = 0;
        char c;
        while (true) {
            c = charArray[index];
            if (hasAnyJSONControlOrUnicodeChars(c)) {
                return true;
            }
            if (++index >= charArray.length) return false;
        }
    }

    public final JsonCharWriter addJsonEscapedString(final char[] charArray) throws Exception{
        if (charArray.length == 0) return this;
        if (hasAnyJSONControlChars(charArray)) {
            return doAddJsonEscapedString(charArray);
        } else {
            return this.addQuoted(charArray);
        }
    }

    private JsonCharWriter doAddJsonEscapedString(char[] charArray) throws Exception{
        this.writer.write('"');
        int index = 0;
        while (true) {
            char c = charArray[index];
            if (hasAnyJSONControlOrUnicodeChars(c)) {
                switch (c) {
                    case '\"':
                        this.writer.write("\\\"");
                        break;
                    case '\\':
                        this.writer.write("\\\\");
                        break;
                    case '\b':
                        this.writer.write("\\b");
                        break;
                    case '\f':
                        this.writer.write("\\f");
                        break;
                    case '\n':
                        this.writer.write("\\n");
                        break;
                    case '\r':
                        this.writer.write("\\r");
                        break;
                    case '\t':
                        this.writer.write("\\t");
                        break;
                    default: //여기를 수정했다. unicode그대로 입력되게 한다.
                        this.writer.write(c);
                }
            } else {
                this.writer.write(c);
            }

            if (++index >= charArray.length) break;
        }
        this.writer.write('"');

        return this;
    }

    public final JsonCharWriter addJsonFieldName(String str) throws Exception{
        return addJsonFieldName(FastStringUtils.toCharArray(str));
    }

    private static final char[] EMPTY_STRING_CHARS = Chr.array('"', '"');

    public final JsonCharWriter addJsonFieldName(char[] chars) throws Exception{
        if (chars.length > 0) {
            addJsonEscapedString(chars);
        } else {
            addChars(EMPTY_STRING_CHARS);
        }
        addChar(':');
        return this;
    }

    public final JsonCharWriter addQuoted(String str) throws Exception{
        final char[] chars = FastStringUtils.toCharArray(str);
        addQuoted(chars);
        return this;
    }

    public JsonCharWriter add(char[] chars, final int length) throws Exception{
        this.writer.write(chars, 0, length);
        return this;
    }

    public JsonCharWriter add(byte[] chars) throws Exception{
        this.writer.write(new String(chars, "UTF-8"));
        return this;
    }

    public JsonCharWriter add(byte[] bytes, int start, int end) throws Exception{
        this.writer.write(new String(bytes, start, end, "UTF-8"));
        return this;
    }

    public final JsonCharWriter add(char ch) throws Exception{
        this.writer.write(ch);
        return this;
    }

    static final char[] nullChars = "null".toCharArray();

    public final void addNull() throws Exception{
        this.add(nullChars);
    }

    private Cache<BigDecimal, char[]> bigDCache;

    public JsonCharWriter addBigDecimal(BigDecimal key) throws Exception{
        if (bigDCache == null) {
            bigDCache = new SimpleCache<BigDecimal, char[]>(20, CacheType.LRU);
        }
        char[] chars = bigDCache.get(key);

        if (chars == null) {
            String str = key.toString();
            chars = FastStringUtils.toCharArray(str);
            bigDCache.put(key, chars);
        }

        add(chars);

        return this;
    }

    private Cache<BigInteger, char[]> bigICache;

    public JsonCharWriter addBigInteger(BigInteger key) throws Exception{
        if (bigICache == null) {
            bigICache = new SimpleCache<BigInteger, char[]>(20, CacheType.LRU);
        }
        char[] chars = bigICache.get(key);

        if (chars == null) {
            String str = key.toString();
            chars = FastStringUtils.toCharArray(str);
            bigICache.put(key, chars);
        }

        add(chars);

        return this;
    }

    private Cache<Long, char[]> lcache;

    public final JsonCharWriter addLong(long l) throws Exception{
        addLong(Long.valueOf(l));
        return this;
    }

    public final JsonCharWriter addLong(Long key) throws Exception{
        if (lcache == null) {
            lcache = new SimpleCache<Long, char[]>(20, CacheType.LRU);
        }
        char[] chars = lcache.get(key);

        if (chars == null) {
            String str = Long.toString(key);
            chars = FastStringUtils.toCharArray(str);
            lcache.put(key, chars);
        }

        add(chars);

        return this;
    }

}

