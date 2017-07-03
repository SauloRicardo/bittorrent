import java.io.Serializable;

/**
 * Created on 17/06/17.
 * Arthur Alexsander Martins Teodoro - 0022427
 * Saulo Ricardo Dias Fernandes - 0021581
 * Wesley Henrique Batista Nunes - 0021622
 * Classe correspondente ao pacote de envio de pecas
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
