import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by arthur on 17/06/17.
 * Classe de conjunto
 */
public class Conjunto<T> implements Serializable
{
    private Set<T> set;

    Conjunto()
    {
        this.set = new ConcurrentSkipListSet<>();
    }

    public boolean add(T value)
    {
        return set.add(value);
    }

    public boolean remove(T value)
    {
        return set.remove(value);
    }

    public Conjunto<T> union(Conjunto<T> set1)
    {
        Conjunto<T> aux = new Conjunto<>();
        for(T i : set)
        {
            aux.add(i);
        }

        for(T i : set1.set)
        {
            aux.add(i);
        }

        return aux;
    }

    public Conjunto<T> intersection(Conjunto<T> set1)
    {
        Conjunto<T> aux = new Conjunto<>();
        for(T i : set)
        {
            if(set1.set.contains(i))
            {
                aux.add(i);
            }
        }

        return aux;
    }

    public boolean contains(T value)
    {
        return set.contains(value);
    }

    public boolean isEmpty()
    {
        return set.isEmpty();
    }

    public void clear()
    {
        set.clear();
    }

    public int size()
    {
        return set.size();
    }

    public Set<T> getSet()
    {
        return set;
    }

    public Object[] toArray()
    {
        return set.toArray();
    }
}
