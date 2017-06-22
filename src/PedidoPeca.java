import java.io.Serializable;

/**
 * Created by arthur on 17/06/17.
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
