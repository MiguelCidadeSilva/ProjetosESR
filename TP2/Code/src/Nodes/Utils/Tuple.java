package Nodes.Utils;
public class Tuple<V1,V2>
{
    private V1 value1;
    private V2 value2;

    public Tuple(V1 value1, V2 value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public V1 getValue1() {
        return value1;
    }

    public V2 getValue2() {
        return value2;
    }

    public V1 setValue1(V1 value) {
        V1 old = this.value1;
        this.value1 = value;
        return old;
    }

    public V2 setValue2(V2 value) {
        V2 old = this.value2;
        this.value2 = value;
        return old;
    }

    @Override
    public String toString() {
        return "(" + value1 + "," +  value2  + ")";
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return value1.equals(tuple.value1) && value2.equals(tuple.value2);
    }
}
