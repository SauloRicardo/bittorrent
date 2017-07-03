import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created on 17/06/17.
 * Arthur Alexsander Martins Teodoro - 0022427
 * Saulo Ricardo Dias Fernandes - 0021581
 * Wesley Henrique Batista Nunes - 0021622
 * classe para controle do par
 */
public class Par
{
    private List<String> ipParesConectados;
    private byte[][] pecas;
    private String[] pecasHash;
    private String metaFileName;
    private ClienteTracker clienteTracker;
    private String nomeArquivo;
    private Conjunto<Integer> pecasFaltantes;
    private Conjunto<Integer> pecasObtidas;
    private ServerSocket socket;
    private Boolean seeder;

    Par(String metaFileName, int port) throws Exception
    {
        socket = new ServerSocket(port);
        this.seeder = false;

        this.metaFileName = metaFileName;
        this.pecasFaltantes = new Conjunto<>();
        this.pecasObtidas = new Conjunto<>();

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
                this.clienteTracker = new ClienteTracker(ipTracker, portTracker, port);
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
                this.pecas = new byte[quantPecas][Utils.PIECE_SIZE];
                this.pecasHash = new String[quantPecas];

                int begin = 0;
                int end = 40;
                for(int i = 0; i < quantPecas; i++)
                {
                    pecasHash[i] = pieces.substring(begin, end);
                    begin = begin + 40;
                    end = end + 40;
                }
            }
        }

        for(int i = 0; i < this.pecasHash.length; i++)
        {
            this.pecasFaltantes.add(i);
        }
        bf.close();
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

    public void carregaPecasArquivo(String nomeArquivo)
    {
        Path path = Paths.get(nomeArquivo);
        File file = path.toFile();

        try
        {
            byte[] bytes = Files.readAllBytes(path);
            int quantPecas = (int) Math.ceil(file.length()/(double)Utils.PIECE_SIZE);

            for(int i = 0; i < quantPecas; i++)
            {
                byte[] peca = Utils.getPiece(bytes, i*Utils.PIECE_SIZE, (i+1)*Utils.PIECE_SIZE);
                this.pecas[i] = peca;
                this.pecasFaltantes.remove(i);
                this.pecasObtidas.add(i);
            }
            this.seeder = true;

        } catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void conectaComPares() throws Exception
    {
        ConectPeers parConect = new ConectPeers();
        parConect.start();
    }

    public void esperaConexoes()
    {
        Socket conexao;
        while(true)
        {
            try
            {
                conexao = socket.accept();

                ObjectOutputStream dos = new ObjectOutputStream(conexao.getOutputStream());
                ObjectInputStream dis = new ObjectInputStream(conexao.getInputStream());


                Semaphore semaphore = new Semaphore(1, true);

                SessaoCliente sessaoCliente = new SessaoCliente(pecas, pecasHash, conexao,
                        pecasFaltantes, pecasObtidas, semaphore, seeder, nomeArquivo, dos, dis);
                Thread threadCliente = new Thread(sessaoCliente);
                threadCliente.setPriority(3);
                threadCliente.start();

                SessaoServidor sessaoServidor = new SessaoServidor(pecas, pecasHash, conexao,
                        pecasObtidas, semaphore, dos, dis);
                Thread threadServidor = new Thread(sessaoServidor);
                threadServidor.start();

            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public List<String> getIpParesConectados()
    {
        return ipParesConectados;
    }

    public void setIpParesConectados(List<String> ipParesConectados)
    {
        this.ipParesConectados = ipParesConectados;
    }

    public byte[][] getPecas()
    {
        return pecas;
    }

    public void setPecas(byte[][] pecas)
    {
        this.pecas = pecas;
    }

    public String[] getPecasHash()
    {
        return pecasHash;
    }

    public void setPecasHash(String[] pecasHash)
    {
        this.pecasHash = pecasHash;
    }

    public String getMetaFileName()
    {
        return metaFileName;
    }

    public void setMetaFileName(String metaFileName)
    {
        this.metaFileName = metaFileName;
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

    public Conjunto<Integer> getPecasFaltantes()
    {
        return pecasFaltantes;
    }

    public void setPecasFaltantes(Conjunto<Integer> pecasFaltantes)
    {
        this.pecasFaltantes = pecasFaltantes;
    }

    public ServerSocket getSocket()
    {
        return socket;
    }

    public void setSocket(ServerSocket socket)
    {
        this.socket = socket;
    }

    public boolean isSeeder()
    {
        return seeder;
    }

    public void setSeeder(boolean seeder)
    {
        this.seeder = seeder;
    }

    class ConectPeers extends Thread
    {
        @Override
        public void run()
        {
            for(String ip : ipParesConectados)
            {
                try
                {
                    String ipEnd = ip.split("-")[0];
                    int port = Integer.parseInt(ip.split("-")[1]);

                    Socket socket = new Socket(ipEnd, port);

                    ObjectOutputStream dos = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream dis = new ObjectInputStream(socket.getInputStream());

                    Semaphore semaphore = new Semaphore(1, true);

                    SessaoServidor sessaoServidor = new SessaoServidor(pecas, pecasHash, socket,
                            pecasObtidas, semaphore, dos, dis);
                    Thread threadServidor = new Thread(sessaoServidor);
                    threadServidor.setPriority(3);
                    threadServidor.start();

                    SessaoCliente sessaoCliente = new SessaoCliente(pecas, pecasHash, socket,
                            pecasFaltantes, pecasObtidas, semaphore, seeder, nomeArquivo, dos, dis);
                    Thread threadCliente = new Thread(sessaoCliente);
                    threadCliente.start();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

}
