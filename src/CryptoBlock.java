import java.util.ArrayList;
import java.util.List;

public class CryptoBlock {

    private List<byte[][]> blockBytes;
    private long cryptoDataLength = 0;

    public CryptoBlock(long originalDataLength) {
        blockBytes = new ArrayList<>();
        cryptoDataLength = originalDataLength;
    }

    public CryptoBlock(byte[] bytesMessage) {
        initBlockWith(bytesMessage);
    }

    public int getSize() {
        return blockBytes.size();
    }

    public long getCryptoDataLength() {
        return cryptoDataLength;
    }

    public byte[][] getBlockAt(int blockIndex) {
        return blockBytes.get(blockIndex);
    }

    public void addNewBlock(byte[][] block) {
        blockBytes.add(block);
    }

    public byte[] getCryptoBytes() {
        int Nb = AESAlgorithm.numberOfBlocks;

        int blockSize = 4 * Nb;
        byte[] bytes = new byte[blockSize * this.blockBytes.size()];

        int index = 0;
        for (byte[][] block : blockBytes) {
            for (int i = 0; i < blockSize; i++) {
                int row = i % 4;
                int column = (i / 4) % 4;
                bytes[index + i] = block[column][row];
            }
            index += blockSize;
        }
        return bytes;
    }

    private void initBlockWith(byte[] bytesMessage) {
        int Nb = AESAlgorithm.numberOfBlocks;
        blockBytes = new ArrayList<>((bytesMessage.length % (4 * Nb)) + 1);

        int row;
        int column;
        int index;

        byte[][] block = new byte[4][Nb];
        block[0][0] = bytesMessage[0];

        cryptoDataLength = bytesMessage.length;

        boolean blockAlreadyExists = false;

        for (index = 1; index < bytesMessage.length; index++) {
            if (index % (4 * Nb) == 0) {
                blockBytes.add(block);
                blockAlreadyExists = true;
                block = new byte[4][Nb];
            }
            row = index % Nb;
            column = (index / 4) % Nb;
            block[column][row] = bytesMessage[index];
            blockAlreadyExists = false;
        }

        if (!blockAlreadyExists)
            blockBytes.add(block);
    }

}
