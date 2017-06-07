import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by arthur on 03/06/17.
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

    ClienteTracker(String ipTracker, int portTracker) throws Exception
    {
        this.trackerIp = InetAddress.getByName(ipTracker);
        this.portTracker = portTracker;
        this.socket = new DatagramSocket();
        ClienteTracker.timeOut = 0;
        this.bufferEntrada = new byte[2048];
    }

    private void receiveRequest(DatagramPacket packetSend, DatagramPacket packetReceive)
    {
        try
        {
            socket.send(packetSend);
            socket.setSoTimeout((15 * (int) Math.pow(2, timeOut))*1000);
            socket.receive(packetReceive);
        }
        catch(SocketTimeoutException e)
        {
            ClienteTracker.timeOut++;
            receiveRequest(packetSend, packetReceive);
        } catch(IOException e)
        {
            e.printStackTrace();
        }
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
        String dados = "GET";
        this.bufferSaida = dados.getBytes();

        DatagramPacket request = new DatagramPacket(this.bufferSaida, this.bufferSaida.length, trackerIp, portTracker);

        DatagramPacket resposta = new DatagramPacket(this.bufferEntrada, this.bufferEntrada.length);

        receiveRequest(request, resposta);

        String dadosResposta = new String(resposta.getData()).trim();

        return geraListaIps(dadosResposta);
    }

}
