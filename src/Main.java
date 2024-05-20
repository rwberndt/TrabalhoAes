import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Digite a chave (bytes separados por ','):");
        String inputKey = scanner.nextLine();

        byte[] key = ByteUtils.stringToByteArray(inputKey);

        System.out.println("Digite o caminho completo para o arquivo a ser criptografado (ex.: /Users/leeonardoo/Documents/t1.pdf):");
        String inputPath = scanner.nextLine();

        System.out.println("Digite o caminho completo de saída para o arquivo criptografado (ex.: /Users/leeonardoo/Documents/file.enc):");
        String outputPath = scanner.nextLine();

        File inputFile = new File(inputPath);
        byte[] fileBytes = new byte[0];

        try (FileInputStream inputStream = new FileInputStream(inputFile)) {
            fileBytes = inputStream.readAllBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }

        File outputFile = new File(outputPath);
        File parentFile = outputFile.getParentFile();

        if (parentFile != null) {
            parentFile.mkdirs();
        }

        AESAlgorithm aes = new AESAlgorithm();
        int[] expandedKeys = aes.getExpandedKey(key);
        byte[] encrypted = aes.cipher(fileBytes, expandedKeys);

        new FileOutputStream(outputFile).write(encrypted);
        System.out.println("Arquivo ciptografado salvo");
    }

}
