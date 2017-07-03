import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 17/06/17.
 * Arthur Alexsander Martins Teodoro - 0022427
 * Saulo Ricardo Dias Fernandes - 0021581
 * Wesley Henrique Batista Nunes - 0021622
 * classe para controle da thread que envia as pecas
 */
public class SessaoServidor implements Runnable
{
    private byte[][] pecas;
    private String[] pecasHash;
    private Socket socket;
    private Conjunto<Integer> pecasFaltantesCliente;
    private Conjunto<Integer> pecasObtidas;
    private Semaphore semaphore;

    private ObjectOutputStream saida;
    private ObjectInputStream entrada;

    private boolean terminar;
    private int contEnviosInvalidos;

    SessaoServidor(byte[][] pecas, String[] hash, Socket socket,
                  Conjunto<Integer> pecasObtidas, Semaphore semaphore, ObjectOutputStream saida, ObjectInputStream entrada)
    {
        this.pecas = pecas;
        this.pecasHash = hash;
        this.socket = socket;
        this.pecasFaltantesCliente = new Conjunto<>();
        this.pecasObtidas = pecasObtidas;
        this.semaphore = semaphore;

        this.entrada = entrada;
        this.saida = saida;

        this.terminar = false;
        this.contEnviosInvalidos = 9;
    }

    synchronized int escolhePecaEnviar(Conjunto<Integer> pecasFaltantesCliente)
    {
        Conjunto<Integer> pecasEnviar = pecasFaltantesCliente.intersection(pecasObtidas);
        Object[] pecasId = pecasEnviar.toArray();

        if(pecasId.length == 0)
            return -1;

        int pecaId = ThreadLocalRandom.current().nextInt(0, pecasId.length);

        int pecaEnviar = (Integer) pecasId[pecaId];

        pecasFaltantesCliente.remove(pecaEnviar);

        return pecaEnviar;
    }

    synchronized void enviaPeca() throws Exception
    {

        Object pedido = entrada.readObject();
        if(pedido instanceof PedidoPeca)
        {
            PedidoPeca pedidoPeca = (PedidoPeca) pedido;

            if(pedidoPeca.getPedido() == Utils.SET_PIECE)
            {
                this.pecasFaltantesCliente = pedidoPeca.getPecasFaltantes();
            }

            int idPeca = escolhePecaEnviar(this.pecasFaltantesCliente);

            EnvioPeca envioPeca;

            if(idPeca == -1)
            {
                envioPeca = new EnvioPeca(Utils.PIECE_FALTANTE, new byte[1]);
                contEnviosInvalidos++;
            }
            else
            {
                envioPeca = new EnvioPeca(idPeca, pecas[idPeca]);
                contEnviosInvalidos = 0;
            }

            if(pecasFaltantesCliente.isEmpty() && contEnviosInvalidos == 10)
            {
                terminar = true;
                envioPeca = new EnvioPeca(Utils.FECHAR_SESSA0, new byte[1]);
            }

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
                if(terminar)
                {
                    System.out.println("TERMINANDO SERVIDOR.......");
                    break;
                }
                semaphore.acquire();
                enviaPeca();
            }
            catch(SocketException | EOFException e)
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
