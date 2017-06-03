import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by arthur on 02/06/17.
 * classe do tracker - servidor udp que recebera requisicao sempre que um par
 * quer entrar para um enxame
 */
public class Tracker
{
    private String ip;
    private int port;
    private List<String> listaIps;
    private DatagramSocket socket;
    private byte[] bufferEntrada;
    private byte[] bufferSaida;

    Tracker(int port) throws Exception
    {
        socket = new DatagramSocket(port);
        this.ip = InetAddress.getLocalHost().getHostAddress();
        this.port = port;
        this.listaIps = new ArrayList<>();
        this.bufferEntrada = new byte[2048];
        this.bufferSaida = new byte[2048];
    }

    public String getIp()
    {
        return ip;
    }

    public int getPort()
    {
        return port;
    }

    public List<String> getListaIps()
    {
        return listaIps;
    }

    public void setListaIps(List<String> listaIps)
    {
        this.listaIps = listaIps;
    }

    public DatagramSocket getSocket()
    {
        return socket;
    }

    public void adicionarPar(InetAddress end, int port)
    {
        String add = "";
        add += end.getHostAddress();
        add += "-"+port;

        if(!this.listaIps.contains(add))
            this.listaIps.add(add);
    }

    public String getIps()
    {
        String ret = "";
        for(String s : this.listaIps)
        {
            ret += s + '\t';
        }
        return ret;
    }

    public void iniciarTracker() throws Exception
    {
        DatagramPacket request = new DatagramPacket(this.bufferEntrada, this.bufferEntrada.length);
        DatagramPacket resposta;

        while(true)
        {
            //limpa o buffer de entrada
            for(int i = 0; i < this.bufferEntrada.length; i++)
            {
                this.bufferEntrada[i] = 0;
            }

            //fica na espera de um pedido
            socket.receive(request);

            System.out.println("Recebeu conexao");

            InetAddress endCliente = request.getAddress();
            int portCliente = request.getPort();

            String dados = new String(request.getData()).trim();
            System.out.println("Recebeu: "+dados);

            if(dados.equals("GET"))
            {
                String enviar = this.getIps();
                bufferSaida = enviar.getBytes();

                resposta = new DatagramPacket(bufferSaida, bufferSaida.length, endCliente, portCliente);
                socket.send(resposta);

                this.adicionarPar(endCliente, portCliente);
            }

        }
    }

    public static void main(String[] args)
    {
        try
        {
            Tracker tracker = new Tracker(8989);
            tracker.iniciarTracker();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
