import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

/**
 * Created by arthur on 02/06/17.
 * classe com códigos que poderam ser utilizados várias vezes
 */
public class Utils
{
    public static final int PIECE_SIZE = 262144;

    public static String byteArray2Hex(final byte[] hash)
    {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    public static String gerarSHA1(byte[] convertme) throws NoSuchAlgorithmException
    {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return byteArray2Hex(md.digest(convertme));
    }

    public static byte[] getPiece(byte[] bytes, int begin, int end)
    {
        if(begin+end > bytes.length)
            end = bytes.length;
        byte[] piece = new byte[end-begin];
        int index = 0;
        for(int i = begin; i < end; i++)
        {
            piece[index] = bytes[i];
            index++;
        }
        return piece;
    }

    public static void createMetainfo(String trackerIp, int trackerPort, String filePath, String torrentPath) throws Exception
    {
        Path path = Paths.get(filePath);
        File file = path.toFile();

        BufferedWriter fileOut = new BufferedWriter(new FileWriter(torrentPath));

        byte[] bytes = Files.readAllBytes(path);

        fileOut.append(String.format("announce: "+trackerIp+"-"+trackerPort+"\n"));
        fileOut.append("name: "+file.getName()+"\n");
        fileOut.append("length: "+file.length()+"\n");
        fileOut.append("piece length: "+Utils.PIECE_SIZE+"\n");

        //gera a lista de hash de cada peca
        String hashs = "";
        int inicio = 0;
        int fim = Utils.PIECE_SIZE;

        while(inicio < bytes.length)
        {
            byte[] piece = getPiece(bytes, inicio, fim);
            inicio += Utils.PIECE_SIZE;
            fim += Utils.PIECE_SIZE;
            hashs = hashs + Utils.gerarSHA1(piece);
        }
        fileOut.append("pieces: "+hashs+"\n");
        fileOut.close();
    }
}
