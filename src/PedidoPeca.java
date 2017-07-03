import java.io.Serializable;

/**
 * Created on 17/06/17.
 * Arthur Alexsander Martins Teodoro - 0022427
 * Saulo Ricardo Dias Fernandes - 0021581
 * Wesley Henrique Batista Nunes - 0021622
 * Classe correspondente ao pacote de pedido de uma peca
 */
public class PedidoPeca implements Serializable
{
    private short pedido;
    private Conjunto<Integer> pecasFaltantes;

    PedidoPeca(short pedido, Conjunto<Integer> pecas)
    {
        this.pedido = pedido;
        this.pecasFaltantes = pecas;
    }

    public short getPedido()
    {
        return pedido;
    }

    public void setPedido(short pedido)
    {
        this.pedido = pedido;
    }

    public Conjunto<Integer> getPecasFaltantes()
    {
        return pecasFaltantes;
    }

    public void setPecasFaltantes(Conjunto<Integer> pecasFaltantes)
    {
        this.pecasFaltantes = pecasFaltantes;
    }
}
