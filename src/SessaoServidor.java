import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by arthur on 17/06/17.
 * classe para controle da thread que envia as pecas
 */
public class SessaoServidor implements Runnable
{
    private List<byte[]> pecas;
    private List<String> pecasHash;
    private Socket socket;
    private Conjunto<Integer> pecasFaltantes;
    private Conjunto<Integer> pecasObtidas;
    private Semaphore semaphore;

    SessaoServidor(List<byte[]> pecas, List<String> hash, Socket socket, Conjunto<Integer> pecasFaltantes,
                  Conjunto<Integer> pecasObtidas, Semaphore semaphore)
    {
        this.pecas = pecas;
        this.pecasHash = hash;
        this.socket = socket;
        this.pecasFaltantes = pecasFaltantes;
        this.pecasObtidas = pecasObtidas;
        this.semaphore = semaphore;
    }

    synchronized int escolhePecaEnviar(Conjunto<Integer> pecasFaltantesCliente)
    {
        Conjunto<Integer> pecasEnviar = pecasFaltantesCliente.intersection(pecasObtidas);
        Object[] pecasId = pecasEnviar.toArray();

        if(pecasId.length == 0)
            return -1;

        int pecaId = ThreadLocalRandom.current().nextInt(0, pecasId.length);

        return (Integer) pecasId[pecaId];
    }

    synchronized void enviaPeca() throws Exception
    {
        ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());

        Object pedido = entrada.readObject();
        if(pedido instanceof PedidoPeca)
        {
            PedidoPeca pedidoPeca = (PedidoPeca) pedido;

            int idPeca = escolhePecaEnviar(pedidoPeca.getPecasFaltantes());

            EnvioPeca envioPeca;

            if(idPeca == -1)
                envioPeca = new EnvioPeca(-1, new byte[1]);
            else
                envioPeca = new EnvioPeca(idPeca, pecas.get(idPeca));

            saida.writeObject(envioPeca);
            System.out.println("ENVIA PECA: Enviou a peca "+idPeca+" para "+socket.getInetAddress()+" "+socket.getPort());
        }
    }

    @Override
    public void run()
    {
        while(true)
        {
            try
            {
                semaphore.acquire();
                enviaPeca();
            }
            catch(SocketException e)
            {
                System.out.println("Par "+socket.getInetAddress()+"-"+socket.getPort()+" desconectado");
                semaphore.release();
                break;
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                semaphore.release();
            }
        }
    }
}
