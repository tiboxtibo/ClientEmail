package app;

import java.io.Serializable;

public class Pair implements Serializable {
    private Object obj1;
    private Object obj2;

    public Pair(Object obj1, Object obj2) {
        this.obj1 = obj1;
        this.obj2 = obj2;
    }

    public Object getObj1() {
        return obj1;
    }

    public void setObj1(Object idUtente) {
        this.obj1 = idUtente;
    }

    public Object getObj2() {
        return obj2;
    }

    public void setObj2(Object datiUtente) {
        this.obj2 = datiUtente;
    }


}
