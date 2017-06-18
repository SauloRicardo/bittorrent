import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created by arthur on 17/06/17.
 * Classe para sessao que pede as pecas
 */
public class SessaoCliente implements Runnable
{
    private byte[][] pecas;
    private String[] pecasHash;
    private Socket socket;
    private Conjunto<Integer> pecasFaltantes;
    private Conjunto<Integer> pecasObtidas;
    private Semaphore semaphore;
    private boolean seeder;
    private String nomeArquivo;

    SessaoCliente(byte[][] pecas, String[] hash, Socket socket, Conjunto<Integer> pecasFaltantes,
                  Conjunto<Integer> pecasObtidas, Semaphore semaphore, boolean seeder, String nomeArquivo)
    {
        this.pecas = pecas;
        this.pecasHash = hash;
        this.socket = socket;
        this.pecasFaltantes = pecasFaltantes;
        this.pecasObtidas = pecasObtidas;
        this.semaphore = semaphore;
        this.seeder = seeder;
        this.nomeArquivo = nomeArquivo;
    }

    synchronized private void armazenaPeca(byte[] peca, int id) throws Exception
    {
        String sha1Peca = Utils.gerarSHA1(peca);
        if(sha1Peca.equals(this.pecasHash[id]))
        {
            this.pecas[id] = peca;
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

            if(pecasFaltantes.isEmpty() && !seeder)
            {
                Utils.escreveArquivo(nomeArquivo, pecas);
                seeder = true;
            }
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
