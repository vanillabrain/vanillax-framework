package vanillax.framework.batch.script;

import javax.xml.bind.DatatypeConverter;

public class StringTest {
    public static void main(String[] args) {
        byte[] arr = {(byte)0x12,(byte)0xAB,(byte)0x12,(byte)0xCD,(byte)0x12,(byte)0xEF,(byte)0xAA};
        String s = DatatypeConverter.printHexBinary(arr);
        System.out.println(s);
        System.out.println(String.format("0x%02X", arr[1]));
    }
}
