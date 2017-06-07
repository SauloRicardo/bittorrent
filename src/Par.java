import java.io.BufferedReader;
import java.io.FileReader;
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

    Par(String metaFileName) throws Exception
    {
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
        bf.close();
    }

    public static void main(String[] args)
    {
        if(args[0].equals("-i"))
        {
            try
            {
                Tracker tracker = new Tracker(8989);
                Thread treadTracker = new Thread(tracker);
                treadTracker.start();

                Utils.createMetainfo(tracker.getIp(), 8989, args[1], args[2]);
                Par par = new Par(args[2]);
                par.getClienteTracker().getIpsEnxame();//nao seta nada como é a semente inicial, serve somente
                //para colocar seu ip no tracker;
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            try
            {
                Par par = new Par(args[1]);
                par.setIpParesConectados(par.getClienteTracker().getIpsEnxame());
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
