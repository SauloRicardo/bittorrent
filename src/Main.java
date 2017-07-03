/**
 * Created on 07/06/17.
 * Arthur Alexsander Martins Teodoro - 0022427
 * Saulo Ricardo Dias Fernandes - 0021581
 * Wesley Henrique Batista Nunes - 0021622
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
                    threadTracker.setPriority(Thread.MIN_PRIORITY);
                    threadTracker.start();
                    System.out.println("O Tracker responsável por este torrent foi criado");

                    Utils.createMetainfo(tracker.getIp(), tracker.getPort(), args[1], args[2]);
                    System.out.println("Arquivo "+args[2]+" criado com sucesso");

                    Par par = new Par(args[2], 6969);

                    createShutDownHook(par);

                    par.carregaPecasArquivo(args[1]);//caso for o par inicial a peca vai pra lista de pecas
                    par.getClienteTracker().getIpsEnxame();//faz uma requisicao no tracker para ter seu ip
                    //incluido na lista

                    par.esperaConexoes();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
            else if(args.length == 1)
            {
                try
                {
                	//============ caso executando na mesma maquina alterar somente aqui o numero do porto ============
                    Par par = new Par(args[0], 6969);

                    createShutDownHook(par);

                    par.setIpParesConectados(par.getClienteTracker().getIpsEnxame());
                    par.conectaComPares();
                    par.esperaConexoes();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void createShutDownHook(Par par)
    {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                par.getClienteTracker().retirarMeuIp();
            }
        }));
    }
}
