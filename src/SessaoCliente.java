import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created by arthur on 17/06/17.
 * Classe para sessao que pede as pecas
 */
public class SessaoCliente implements Runnable
{
    private List<byte[]> pecas;
    private List<String> pecasHash;
    private Socket socket;
    private Conjunto<Integer> pecasFaltantes;
    private Conjunto<Integer> pecasObtidas;
    private Semaphore semaphore;
    private boolean seeder;

    SessaoCliente(List<byte[]> pecas, List<String> hash, Socket socket, Conjunto<Integer> pecasFaltantes,
                  Conjunto<Integer> pecasObtidas, Semaphore semaphore, boolean seeder)
    {
        this.pecas = pecas;
        this.pecasHash = hash;
        this.socket = socket;
        this.pecasFaltantes = pecasFaltantes;
        this.pecasObtidas = pecasObtidas;
        this.semaphore = semaphore;
        this.seeder = seeder;
    }

    synchronized private void armazenaPeca(byte[] peca, int id) throws Exception
    {
        String sha1Peca = Utils.gerarSHA1(peca);
        if(sha1Peca.equals(this.pecasHash.get(id)))
        {
            this.pecas.remove(id);
            this.pecas.add(id, peca);
            this.pecasFaltantes.remove(id);
            this.pecasObtidas.add(id);
            System.out.println("PEDE PECA: Recebeu a peca "+id+" em ordem de "+socket.getInetAddress()+" "+socket.getPort());
        }
    }

    synchronized private void pedePeca() throws Exception
    {
        ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());

        PedidoPeca pedidoPeca = new PedidoPeca("GET", pecasFaltantes);

        saida.writeObject(pedidoPeca);

        Object resposta = entrada.readObject();
        if(resposta instanceof EnvioPeca)
        {
            EnvioPeca peca = (EnvioPeca) resposta;

            if(peca.getIdPeca() != -1)
                armazenaPeca(peca.getPeca(), peca.getIdPeca());

            if(pecasFaltantes.isEmpty())
                seeder = true;
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
                pedePeca();
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
