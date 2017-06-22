import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Formatter;

/**
 * Created by arthur on 02/06/17.
 * classe com códigos que poderam ser utilizados várias vezes
 */
public class Utils
{
    public static final int PIECE_SIZE = 32768;
    public static final short GET_PIECE = 0;
    public static final short SET_PIECE = 1;
    public static final int PIECE_FALTANTE = -1;
    public static final int FECHAR_SESSA0 = -2;

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
        if(end > bytes.length)
            end = bytes.length;
        byte[] piece = new byte[end-begin];
        int index = 0;
        /*for(int i = begin; i < end; i++)
        {
            piece[index] = bytes[i];
            index++;
        }*/
        System.arraycopy(bytes, begin, piece, 0, piece.length);
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
        int quantPecas = 0;

        while(inicio < bytes.length)
        {
            byte[] piece = getPiece(bytes, inicio, fim);
            inicio += Utils.PIECE_SIZE;
            fim += Utils.PIECE_SIZE;
            hashs = hashs + Utils.gerarSHA1(piece);
            quantPecas++;
        }
        fileOut.append("pieces: "+hashs+"\n");
        fileOut.close();
    }

    public static String getIpAdrress()
    {
        String ip = "";
        try
        {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements())
            {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements())
                {
                    InetAddress addr = addresses.nextElement();
                    ip = addr.getHostAddress();
                    if(addr.getClass() == Inet4Address.class)
                        return ip;
                }
            }
        }
        catch (SocketException e)
        {
            throw new RuntimeException(e);
        }
        return ip;
    }

    public static byte[] integer2bytes(int value)
    {
        Formatter formatter = new Formatter();
        formatter.format("%02x", value);
        String numeroHex = formatter.toString();
        String zeros = "";

        for(int i = 0; i < 8-numeroHex.length(); i++)
        {
            zeros += "0";
        }

        return zeros.concat(numeroHex).getBytes();
    }

    public static int bytes2int(byte[] bytes)
    {
        String byteStr = new String(bytes);
        return Integer.parseInt(byteStr, 16);
    }

    public static byte[] concat(byte[] a, byte[] b)
    {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c = new byte[aLen+bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    public static void escreveArquivo(String nomeArquivo, byte[][] pecas)
    {
        String caminho = nomeArquivo;
        int cont = 1;
        Path path = Paths.get(caminho);
        File file = path.toFile();

        while(file.exists())
        {
            caminho = nomeArquivo + "("+cont+")";
            path = Paths.get(caminho);
            file = path.toFile();

            cont++;
        }

        try
        {
            FileOutputStream fos = new FileOutputStream(file);

            for(int i = 0; i < pecas.length; i++)
            {
                byte[] peca = pecas[i];
                fos.write(peca);
            }

            fos.close();
        } catch(IOException e)
        {
            e.printStackTrace();
        }

    }
}
