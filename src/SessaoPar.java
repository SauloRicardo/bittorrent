import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by arthur on 09/06/17.
 * thread para as sessoes do par
 */
public class SessaoPar implements Runnable
{
    private List<byte[]> pecas;
    private List<String> pecasHash;
    private Socket socket;
    private boolean[] pecasOk;
    private int opcao;
    private boolean seeder;

    public static int PEDE_ENVIA = 1;
    public static int ENVIA_PEDE = 2;

    SessaoPar(List<byte[]> pecas, List<String> hashs, boolean[] pecasOk, Socket socket,
              int op, boolean seeder) throws Exception
    {
        this.pecas = pecas;
        this.pecasHash = hashs;
        this.socket = socket;
        this.pecasOk = pecasOk;
        this.opcao = op;
        this.seeder = seeder;
    }

    synchronized private int getPrimeiraPecaFaltante()
    {
        int indexPeca = 0;
        for(boolean b : this.pecasOk)
        {
            if(!b)
                return indexPeca;
            indexPeca++;
        }
        return -1;
    }

    synchronized private int getRandomPeca()
    {
        if(getPrimeiraPecaFaltante() == -1)
            return -1;

        int valor = ThreadLocalRandom.current().nextInt(0, pecasOk.length);

        while(pecasOk[valor])
        {
            valor = ThreadLocalRandom.current().nextInt(0, pecasOk.length);
        }

        return valor;
    }

    synchronized private void armazenaPeca(byte[] peca, int id) throws Exception
    {
        String sha1Peca = Utils.gerarSHA1(peca);
        if(sha1Peca.equals(this.pecasHash.get(id)))
        {
            this.pecas.remove(id);
            this.pecas.add(id, peca);
            this.pecasOk[id] = true;
            System.out.println("PEDE PECA: Recebeu a peca "+id+" em ordem de "+socket.getInetAddress()+socket.getPort());
        }
    }

    synchronized private void pedePeca() throws Exception
    {
        DataOutputStream saida = new DataOutputStream(socket.getOutputStream());
        DataInputStream entrada = new DataInputStream(socket.getInputStream());
        byte[] bufferEntrada = new byte[Utils.PIECE_SIZE];
        byte[] bufferSaida = new byte[Utils.PIECE_SIZE];

        int pecaPedir = getRandomPeca();
        if(pecaPedir != -1)
        {
            String request = "GET\t"+pecaPedir;
            //bufferSaida = request.getBytes();
            saida.writeUTF(request);

            String resposta;
            entrada.read(bufferEntrada);
            resposta = new String(bufferEntrada).trim();

            if(!resposta.equals("FAIL"))
            {
                //byte[] pecaIdBytes = Arrays.copyOfRange(bufferEntrada, 0, 8);
                //int pecaId = Utils.bytes2int(pecaIdBytes);
                //byte[] peca = Arrays.copyOfRange(bufferEntrada, 8, bufferEntrada.length);

                armazenaPeca(bufferEntrada, pecaPedir);
            }
        }
        else
        {
            String request = "NOTPIECE";
            //bufferSaida = request.trim().getBytes();
            saida.writeUTF(request);

            String resposta;
            entrada.read(bufferEntrada);
            resposta = new String(bufferEntrada);
            resposta.trim();
            System.out.println("PEDE PECA: Acho que recebi tudo");
        }
    }

    synchronized private void enviaPeca() throws Exception
    {
        DataOutputStream saida = new DataOutputStream(socket.getOutputStream());
        DataInputStream entrada = new DataInputStream(socket.getInputStream());
        byte[] bufferEntrada = new byte[Utils.PIECE_SIZE];
        byte[] bufferSaida = new byte[Utils.PIECE_SIZE];

        String pedido = entrada.readUTF();
        //pedido = new String(bufferEntrada);
        pedido.trim();

        if(pedido.contains("GET"))
        {
            int peca = Integer.parseInt(pedido.split("\t")[1]);

            if(pecasOk[peca])
            {
                //bufferSaida = Utils.concat(Utils.integer2bytes(peca), this.pecas.get(peca));
                bufferSaida = this.pecas.get(peca);
                //System.out.println("\tVai enviar a peca "+peca+" com tamanho "+bufferSaida.length);
                saida.write(bufferSaida);
                System.out.println("ENVIOU A PECA: "+peca);
            }
            else
            {
                String enviar = "FAIL";
                bufferSaida = enviar.trim().getBytes();
                saida.write(bufferSaida);
            }
        }
        else
        {
            String request = "ACK NOTPIECE";
            bufferSaida = request.trim().getBytes();
            saida.write(bufferSaida);
        }
    }

    @Override
    public void run()
    {
        while(true)
        {
            try
            {
                if(this.opcao == SessaoPar.PEDE_ENVIA)
                {
                    pedePeca();
                    enviaPeca();
                } else
                {
                    enviaPeca();
                    pedePeca();
                }
            }
            catch(SocketException e)
            {
            	System.out.println("Par "+socket.getInetAddress()+" desconectado");
                break;
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
