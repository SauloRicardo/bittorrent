import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Semaphore;

/**
 * Created on 17/06/17.
 * Arthur Alexsander Martins Teodoro - 0022427
 * Saulo Ricardo Dias Fernandes - 0021581
 * Wesley Henrique Batista Nunes - 0021622
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
    private Boolean seeder;
    private String nomeArquivo;
    private boolean arquivoEscrito;

    private ObjectOutputStream saida;
    private ObjectInputStream entrada;

    private int timeOutReevio;
    private boolean terminar;

    SessaoCliente(byte[][] pecas, String[] hash, Socket socket, Conjunto<Integer> pecasFaltantes,
                  Conjunto<Integer> pecasObtidas, Semaphore semaphore, boolean seeder, String nomeArquivo,
                  ObjectOutputStream saida, ObjectInputStream entrada)
    {
        this.pecas = pecas;
        this.pecasHash = hash;
        this.socket = socket;
        this.pecasFaltantes = pecasFaltantes;
        this.pecasObtidas = pecasObtidas;
        this.semaphore = semaphore;
        this.seeder = seeder;
        this.nomeArquivo = nomeArquivo;
        this.arquivoEscrito = false;

        this.entrada = entrada;
        this.saida = saida;

        this.timeOutReevio = 0;
        this.terminar = false;
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
        PedidoPeca pedidoPeca;

        if(timeOutReevio == 0)
        {
            pedidoPeca = new PedidoPeca(Utils.SET_PIECE, pecasFaltantes);
            timeOutReevio = 50;
        }
        else
            pedidoPeca = new PedidoPeca(Utils.GET_PIECE, null);

        saida.writeObject(pedidoPeca);

        Object resposta = entrada.readObject();
        if(resposta instanceof EnvioPeca)
        {
            EnvioPeca peca = (EnvioPeca) resposta;

            if(peca.getIdPeca() >= 0)
                armazenaPeca(peca.getPeca(), peca.getIdPeca());
            else if(peca.getIdPeca() == Utils.FECHAR_SESSA0)
            {
                terminar = true;
            }

            if(pecasFaltantes.isEmpty() && !seeder  && !arquivoEscrito)
            {
                long t1 = System.currentTimeMillis();
                Utils.escreveArquivo(nomeArquivo, pecas);
                seeder = true;
                arquivoEscrito = true;
            }
            timeOutReevio--;
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
                    System.out.println("TERMINANDO CLIENTE.......");
                    break;
                }
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
