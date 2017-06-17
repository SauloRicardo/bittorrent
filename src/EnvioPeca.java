import java.io.Serializable;

/**
 * Created by arthur on 17/06/17.
 */
public class EnvioPeca implements Serializable
{
    private int idPeca;
    private byte[] peca;

    EnvioPeca(int idPeca, byte[] peca)
    {
        this.idPeca = idPeca;
        this.peca = peca;
    }

    public int getIdPeca()
    {
        return idPeca;
    }

    public void setIdPeca(int idPeca)
    {
        this.idPeca = idPeca;
    }

    public byte[] getPeca()
    {
        return peca;
    }

    public void setPeca(byte[] peca)
    {
        this.peca = peca;
    }
}
