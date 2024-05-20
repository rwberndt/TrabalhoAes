public class AESAlgorithm {

    public static int numberOfKeys = 4;
    public static int numberOfBlocks = 4;
    public static int rounds = 10;

    public byte[] cipher(byte[] bytesMessage, int[] wordsKeyExpansion) {
        long messageSize = bytesMessage.length;
        byte[] bytesPacket = new byte[bytesMessage.length + 12];
        bytesPacket[0] = (byte) (0xFFL & messageSize);
        bytesPacket[1] = (byte) ((0xFF00L & messageSize) >> 8);
        bytesPacket[2] = (byte) ((0xFF0000L & messageSize) >> 16);
        bytesPacket[3] = (byte) ((0xFF000000L & messageSize) >> 24);
        bytesPacket[4] = (byte) ((0xFF00000000L & messageSize) >> 32);
        bytesPacket[5] = (byte) ((0xFF0000000000L & messageSize) >> 40);
        bytesPacket[6] = (byte) ((0xFF000000000000L & messageSize) >> 48);
        bytesPacket[7] = (byte) ((0xFF00000000000000L & messageSize) >> 56);
        bytesPacket[8] = (byte) (0xFF & 1);
        bytesPacket[9] = (byte) ((0x00FF & 1) >> 8);
        bytesPacket[10] = (byte) ((0x0000FF & 1) >> 16);
        bytesPacket[11] = (byte) ((0x000000FF & 1) >> 24);

        System.arraycopy(bytesMessage, 0, bytesPacket, 12, bytesMessage.length);

        CryptoBlock block = new CryptoBlock(bytesPacket);
        CryptoBlock encryptedBlock = new CryptoBlock(block.getCryptoDataLength());

        byte[][] out;
        int blockSize = block.getSize();
        int blockSizeForOut = blockSize - 1;

        for (int i = 0; i < blockSize; i++) {
            out = this.cipher(block.getBlockAt(i), wordsKeyExpansion);
            System.out.println("Criptografando bloco " + i + " de " + blockSizeForOut);
            encryptedBlock.addNewBlock(out);
        }

        System.out.println("Escrevendo o arquivo de saída...");
        return encryptedBlock.getCryptoBytes();
    }

    public void expandKey(byte[] key, int[] word) {
        int temporary;
        int index = 0;

        while (index < numberOfKeys) {
            word[index] = ByteUtils.toInt(key[4 * index], key[4 * index + 1], key[4 * index + 2], key[4 * index + 3]);
            index++;
        }

        index = numberOfKeys;


        while (index < numberOfBlocks * (rounds + 1)) {
            temporary = word[index - 1];
            if (index % numberOfKeys == 0) {
                temporary = subWord(rotWord(temporary)) ^ Tables.rcon[index / numberOfKeys];
            } else if (numberOfKeys > 6 && index % numberOfKeys == 4) {
                temporary = subWord(temporary);
            }
            word[index] = word[index - numberOfKeys] ^ temporary;
            index++;
        }
    }

    public int[] getExpandedKey(byte[] key) {
        int[] word = new int[numberOfBlocks * (rounds + 1)];
        expandKey(key, word);
        return word;
    }

    private byte[][] cipher(byte[][] bytesMessage, int[] wordsKeyExpansion) {
        byte[][] currentBlockState;
        currentBlockState = bytesMessage;
        currentBlockState = addRoundKey(currentBlockState, wordsKeyExpansion, 0);

        for (int round = 1; round <= rounds - 1; round++) {
            currentBlockState = subBytes(currentBlockState);
            currentBlockState = shiftRows(currentBlockState);
            currentBlockState = mixColumns(currentBlockState);
            currentBlockState = addRoundKey(currentBlockState, wordsKeyExpansion, round * numberOfBlocks);
        }
        currentBlockState = subBytes(currentBlockState);
        currentBlockState = shiftRows(currentBlockState);
        currentBlockState = addRoundKey(currentBlockState, wordsKeyExpansion, rounds * numberOfBlocks);
        return currentBlockState;
    }

    private static int subWord(int word) {
        int newWord = 0;
        newWord ^= (int) sboxTransform((byte) (word >>> 24)) & 0x000000ff;
        newWord <<= 8;

        newWord ^= (int) sboxTransform((byte) ((word & 0xff0000) >>> 16)) & 0x000000ff;
        newWord <<= 8;

        newWord ^= (int) sboxTransform((byte) ((word & 0xff00) >>> 8)) & 0x000000ff;
        newWord <<= 8;

        newWord ^= (int) sboxTransform((byte) (word & 0xff)) & 0x000000ff;

        return newWord;
    }

    private static int rotWord(int word) {
        return (word << 8) ^ ((word >> 24) & 0x000000ff);
    }

    private byte[][] mixColumns(byte[][] state) {
        byte[][] newState = new byte[state.length][state[0].length];
        for (int c = 0; c < numberOfBlocks; c++) {
            newState[0][c] = xor(galoi(state[0][c], 0x02), galoi(state[1][c], 0x03), state[2][c], state[3][c]);
            newState[1][c] = xor(state[0][c], galoi(state[1][c], 0x02), galoi(state[2][c], 0x03), state[3][c]);
            newState[2][c] = xor(state[0][c], state[1][c], galoi(state[2][c], 0x02), galoi(state[3][c], 0x03));
            newState[3][c] = xor(galoi(state[0][c], 0x03), state[1][c], state[2][c], galoi(state[3][c], 0x02));
        }
        return newState;
    }

    private byte xor(byte b1, byte b2, byte b3, byte b4) {
        byte bResult = 0;
        bResult ^= b1;
        bResult ^= b2;
        bResult ^= b3;
        bResult ^= b4;
        return bResult;
    }

    private byte[][] addRoundKey(byte[][] state, int[] w, int l) {
        byte[][] newState = new byte[state.length][state[0].length];
        for (int c = 0; c < numberOfBlocks; c++) {
            newState[0][c] = (byte) (state[0][c] ^ ByteUtils.getAesByte(w[l + c], 3));
            newState[1][c] = (byte) (state[1][c] ^ ByteUtils.getAesByte(w[l + c], 2));
            newState[2][c] = (byte) (state[2][c] ^ ByteUtils.getAesByte(w[l + c], 1));
            newState[3][c] = (byte) (state[3][c] ^ ByteUtils.getAesByte(w[l + c], 0));
        }
        return newState;
    }

    private static byte[][] subBytes(byte[][] state) {
        for (int i = 0; i < state.length; i++)
            for (int j = 0; j < state[i].length; j++)
                state[i][j] = sboxTransform(state[i][j]);
        return state;
    }

    private static byte sboxTransform(byte value) {
        byte bUpper, bLower;
        bUpper = (byte) ((byte) (value >> 4) & 0x0f);
        bLower = (byte) (value & 0x0f);
        return (byte) Tables.sbox[bUpper][bLower];
    }

    private byte[][] shiftRows(byte[][] state) {
        byte[][] stateNew = new byte[state.length][state[0].length];

        stateNew[0] = state[0];
        for (int r = 1; r < state.length; r++)
            for (int c = 0; c < state[r].length; c++)
                stateNew[r][c] = state[r][(c + shift(r, numberOfBlocks)) % numberOfBlocks];

        return stateNew;
    }

    private static int shift(int r, int iNb) {
        return r;
    }


    private static byte xtime(byte value) {
        int iResult;
        iResult = (value & 0x000000ff) * 2;
        return (byte) (((iResult & 0x100) != 0) ? iResult ^ 0x11b : iResult);
    }

    private static byte galoi(int v1, int v2) {
        return galoi((byte) v1, (byte) v2);
    }

    private static byte galoi(byte v1, byte v2) {
        byte[] bytes = new byte[8];
        byte result = 0;
        bytes[0] = v1;
        for (int i = 1; i < bytes.length; i++) {
            bytes[i] = xtime(bytes[i - 1]);
        }
        for (int i = 0; i < bytes.length; i++) {
            if (ByteUtils.getBit(v2, i) != 1) {
                bytes[i] = 0;
            }
            result ^= bytes[i];
        }
        return result;
    }

}
