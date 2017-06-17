import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created by arthur on 17/06/17.
 * classe para controle do par
 */
public class Par
{
    private List<String> ipParesConectados;
    private List<byte[]> pecas;
    private List<String> pecasHash;
    private String metaFileName;
    private ClienteTracker clienteTracker;
    private String nomeArquivo;
    private Conjunto<Integer> pecasFaltantes;
    private Conjunto<Integer> pecasObtidas;
    private ServerSocket socket;
    private boolean seeder;

    Par(String metaFileName, int port) throws Exception
    {
        socket = new ServerSocket(port);
        this.seeder = false;

        pecas = new ArrayList<>();
        pecasHash = new ArrayList<>();
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

        for(int i = 0; i < this.pecasHash.size(); i++)
        {
            this.pecasFaltantes.add(i);
            this.pecas.add(new byte[1]);
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
            this.pecas.clear();
            byte[] bytes = Files.readAllBytes(path);
            int quantPecas = (int) Math.ceil(file.length()/(double)Utils.PIECE_SIZE);

            for(int i = 0; i < quantPecas; i++)
            {
                byte[] peca = Utils.getPiece(bytes, i*Utils.PIECE_SIZE, (i+1)*Utils.PIECE_SIZE);
                this.pecas.add(peca);
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
        for(String ip : this.ipParesConectados)
        {
            Socket socket = new Socket(ip, 6969);

            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            DataInputStream dis = new DataInputStream(socket.getInputStream());

            boolean parSemente = dis.readBoolean();
            dos.writeBoolean(seeder);

            Semaphore semaphore = new Semaphore(1, true);

            if(!parSemente)
            {
                SessaoServidor sessaoServidor = new SessaoServidor(pecas, pecasHash, socket,
                        pecasFaltantes, pecasObtidas, semaphore);
                Thread threadServidor = new Thread(sessaoServidor);
                threadServidor.start();
            }

            SessaoCliente sessaoCliente = new SessaoCliente(pecas, pecasHash, socket,
                    pecasFaltantes, pecasObtidas, semaphore, seeder);
            Thread threadCliente = new Thread(sessaoCliente);
            threadCliente.start();
        }
    }

    public void esperaConexoes()
    {
        Socket conexao;
        while(true)
        {
            try
            {
                conexao = socket.accept();

                DataOutputStream dos = new DataOutputStream(conexao.getOutputStream());
                DataInputStream dis = new DataInputStream(conexao.getInputStream());

                dos.writeBoolean(seeder);
                boolean parSemente = dis.readBoolean();

                Semaphore semaphore = new Semaphore(1, true);

                SessaoCliente sessaoCliente = new SessaoCliente(pecas, pecasHash, conexao,
                        pecasFaltantes, pecasObtidas, semaphore, seeder);
                Thread threadCliente = new Thread(sessaoCliente);
                threadCliente.start();

                if(!parSemente)
                {
                    SessaoServidor sessaoServidor = new SessaoServidor(pecas, pecasHash, conexao,
                            pecasFaltantes, pecasObtidas, semaphore);
                    Thread threadServidor = new Thread(sessaoServidor);
                    threadServidor.start();

                }
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

    public List<byte[]> getPecas()
    {
        return pecas;
    }

    public void setPecas(List<byte[]> pecas)
    {
        this.pecas = pecas;
    }

    public List<String> getPecasHash()
    {
        return pecasHash;
    }

    public void setPecasHash(List<String> pecasHash)
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

}
