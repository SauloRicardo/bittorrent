import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created on 03/06/17.
 * Arthur Alexsander Martins Teodoro - 0022427
 * Saulo Ricardo Dias Fernandes - 0021581
 * Wesley Henrique Batista Nunes - 0021622
 * classe que realizará a comunicacao com o tracker
 */
public class ClienteTracker
{
    private InetAddress trackerIp;
    private int portTracker;
    private DatagramSocket socket;
    private byte[] bufferEntrada;
    private byte[] bufferSaida;
    private static int timeOut;
    private int portPar;
    private static int contTime;

    ClienteTracker(String ipTracker, int portTracker, int portPar) throws Exception
    {
        this.trackerIp = InetAddress.getByName(ipTracker);
        this.portTracker = portTracker;
        this.socket = new DatagramSocket();
        ClienteTracker.timeOut = 0;
        this.bufferEntrada = new byte[2048];
        this.portPar = portPar;
        contTime = 0;
    }

    private int receiveRequest(DatagramPacket packetSend, DatagramPacket packetReceive)
    {
        int cont = 0;
        while(cont < 4)
        {
            try
            {
                socket.send(packetSend);
                socket.setSoTimeout((15 * (int) Math.pow(2, timeOut)) * 10);
                socket.receive(packetReceive);
                return 0;
            }
            catch(SocketTimeoutException e)
            {
                ClienteTracker.timeOut++;
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            cont++;
        }
        return -1;
    }

    private List<String> geraListaIps(String ips)
    {
        String[] res = ips.split("\t");
        List<String> listaIps = new ArrayList<>();
        Collections.addAll(listaIps, res);
        return listaIps;
    }

    public List<String> getIpsEnxame() throws Exception
    {
        String dados = "GET\t"+portPar;
        this.bufferSaida = dados.getBytes();

        DatagramPacket request = new DatagramPacket(this.bufferSaida, this.bufferSaida.length, trackerIp, portTracker);

        DatagramPacket resposta = new DatagramPacket(this.bufferEntrada, this.bufferEntrada.length);

        if(receiveRequest(request, resposta) != 0)
            return new ArrayList<>();

        String dadosResposta = new String(resposta.getData()).trim();

        return geraListaIps(dadosResposta);
    }

    public void retirarMeuIp()
    {
        String dados = "RETIRAR\t"+portPar;
        this.bufferSaida = dados.getBytes();

        DatagramPacket request = new DatagramPacket(this.bufferSaida, this.bufferSaida.length, trackerIp, portTracker);
        DatagramPacket resposta = new DatagramPacket(this.bufferEntrada, this.bufferEntrada.length);

        receiveRequest(request, resposta);

        String dadosResposta = new String(resposta.getData()).trim();
    }

}
