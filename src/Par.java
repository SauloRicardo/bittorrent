import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by arthur on 03/06/17.
 * classe de tratamento do par
 */
public class Par
{
    private List<String> ipParesConectados;
    private List<byte[]> pecas;
    private String metaFileName;
    private List<String> pecasHash;
    private ClienteTracker clienteTracker;
    private String nomeArquivo;
    private boolean[] pecasOk;
    private ServerSocket socket;

    public List<String> getIpParesConectados()
    {
        return ipParesConectados;
    }

    public void setIpParesConectados(List<String> ipParesConectados)
    {
        this.ipParesConectados = ipParesConectados;
    }

    public List<byte[]> getPecas()
    {
        return pecas;
    }

    public void setPecas(List<byte[]> pecas)
    {
        this.pecas = pecas;
    }

    public String getMetaFileName()
    {
        return metaFileName;
    }

    public void setMetaFileName(String metaFileName)
    {
        this.metaFileName = metaFileName;
    }

    public List<String> getPecasHash()
    {
        return pecasHash;
    }

    public void setPecasHash(List<String> pecasHash)
    {
        this.pecasHash = pecasHash;
    }

    public ClienteTracker getClienteTracker()
    {
        return clienteTracker;
    }

    public void setClienteTracker(ClienteTracker clienteTracker)
    {
        this.clienteTracker = clienteTracker;
    }

    public String getNomeArquivo()
    {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo)
    {
        this.nomeArquivo = nomeArquivo;
    }

    private String removeTag(String str)
    {
        String[] split = str.split(":");
        String returnStr = "";
        for(int i = 1; i < split.length; i++)
        {
            returnStr += split[i].trim();
        }
        return returnStr;
    }

    Par(String metaFileName, int port) throws Exception
    {
        socket = new ServerSocket(port);

        pecas = new ArrayList<>();
        pecasHash = new ArrayList<>();
        this.metaFileName = metaFileName;

        BufferedReader bf = new BufferedReader(new FileReader(metaFileName));
        String linha = null;

        String ipTracker;
        int portTracker;

        int fileSize = 0;

        while((linha = bf.readLine()) != null)
        {
            String idValor = linha.split(":")[0];

            //pegar os dados do tracker
            if(idValor.equals("announce"))
            {
                String[] split = linha.split(":")[1].split("-");
                ipTracker = split[0].trim();
                portTracker = Integer.parseInt(split[1].trim());
                this.clienteTracker = new ClienteTracker(ipTracker, portTracker);
            }
            else if(idValor.equals("name"))//pega o nome do arquivo que será criado
            {
                this.nomeArquivo = removeTag(linha);
            }
            else if(idValor.equals("length"))
            {
                fileSize = Integer.parseInt(removeTag(linha));
            }
            else if(idValor.equals("pieces"))
            {
                String pieces = removeTag(linha);
                int quantPecas = (int) Math.ceil(fileSize/(double)Utils.PIECE_SIZE);

                int begin = 0;
                int end = 40;
                for(int i = 0; i < quantPecas; i++)
                {
                    pecasHash.add(pieces.substring(begin, end));
                    begin = begin + 40;
                    end = end + 40;
                }
            }
        }
        this.pecasOk = new boolean[pecasHash.size()];
        bf.close();
    }

    public void carregaPecasArquivo(String nomeArquivo)
    {
        Path path = Paths.get(nomeArquivo);
        File file = path.toFile();

        try
        {
            byte[] bytes = Files.readAllBytes(path);
            int quantPecas =  (int) Math.ceil(file.length()/(double)Utils.PIECE_SIZE);

            for(int i = 0; i < quantPecas; i++)
            {
                byte[] peca = Utils.getPiece(bytes, i*Utils.PIECE_SIZE, (i+1)*Utils.PIECE_SIZE);
                this.pecas.add(peca);
                this.pecasOk[i] = true;
            }

        } catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void esperaConexoes()
    {
        Socket conexao;
        while(true)
        {
            try
            {
                conexao = this.socket.accept();
                SessaoPar sessaoPar = new SessaoPar(pecas, pecasHash, pecasOk, conexao, SessaoPar.ENVIA_PEDE);
                Thread threadSessao = new Thread(sessaoPar);
                threadSessao.start();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void conectaComPares()
    {
        try
        {
            for(String ips : this.ipParesConectados)
            {
                Socket socket = new Socket(ips, 6969);
                SessaoPar sessaoPar = new SessaoPar(pecas, pecasHash, pecasOk, socket, SessaoPar.PEDE_ENVIA);
                Thread threadSessao = new Thread(sessaoPar);
                threadSessao.start();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
