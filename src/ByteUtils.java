import java.util.ArrayList;
import java.util.List;

public class ByteUtils {

    private ByteUtils() {
    }

    public static byte[] stringToByteArray(String input) {
        String[] entranceToStringArray = input.split(",");
        int[] integerArray = parseStringArrayToIntArray(entranceToStringArray);

        byte[] byteArray = new byte[integerArray.length];

        for (int i = 0; i < entranceToStringArray.length; i++) {
            byte newByte = (byte) integerArray[i];
            byteArray[i] = newByte;

        }

        return byteArray;
    }

    public static int toInt(byte b1, byte b2, byte b3, byte b4) {
        int integerw = 0;
        integerw ^= ((int) b1) << 24;
        integerw ^= (((int) b2) & 0x000000ff) << 16;
        integerw ^= (((int) b3) & 0x000000ff) << 8;
        integerw ^= (((int) b4) & 0x000000ff);
        return integerw;
    }

    public static byte getAesByte(int value, int iByte) {
        return (byte) ((value >>> (iByte * 8)) & 0x000000ff);
    }

    public static byte getBit(byte value, int i) {
        final byte[] bitMasks = {(byte) 0x01, (byte) 0x02, (byte) 0x04, (byte) 0x08, (byte) 0x10, (byte) 0x20,
                (byte) 0x40, (byte) 0x80};
        byte bit = (byte) (value & bitMasks[i]);
        return (byte) ((byte) (bit >> i) & (byte) 0x01);
    }

    private static int[] parseStringArrayToIntArray(String[] stringArray) {

        int[] result = new int[stringArray.length];

        for (int i = 0; i < stringArray.length; i++) {
            result[i] = Integer.parseInt(stringArray[i]);
        }

        return result;
    }

}
