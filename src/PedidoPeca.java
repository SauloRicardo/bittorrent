import java.io.Serializable;

/**
 * Created by arthur on 17/06/17.
 */
public class PedidoPeca implements Serializable
{
    private String pedido;
    private Conjunto<Integer> pecasFaltantes;

    PedidoPeca(String pedido, Conjunto<Integer> pecas)
    {
        this.pedido = pedido;
        this.pecasFaltantes = pecas;
    }

    public String getPedido()
    {
        return pedido;
    }

    public void setPedido(String pedido)
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
