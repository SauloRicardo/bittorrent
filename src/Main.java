/**
 * Created by arthur on 07/06/17.
 * classe principal do programa, com o código cli
 */
public class Main
{
    public static void main(String[] args)
    {
        if(args.length < 1)//caso nao seja passado nada por parrâmetro
        {
            System.err.println("ERRO - Chamada inválida");
            System.exit(-1);
        }
        else
        {
            if(args[0].equals("-i") && args.length == 3)//o usuario esta criando
            {
                try
                {
                    Tracker tracker = new Tracker(8989);
                    Thread threadTracker = new Thread(tracker);
                    threadTracker.start();
                    System.out.println("O Tracker responsável por este torrent foi criado");

                    Utils.createMetainfo(tracker.getIp(), tracker.getPort(), args[1], args[2]);
                    System.out.println("Arquivo "+args[2]+" criado com sucesso");

                    Par par = new Par(args[2]);
                    par.getClienteTracker().getIpsEnxame();//faz uma requisicao no tracker para ter seu ip
                    //incluido na lista

                    System.out.println("Sistema semeando com sucesso");
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
