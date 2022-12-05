package app;

import java.io.Serializable;

public class Pair implements Serializable {
    //TODO da completare bene la descrizione per cosa viene usato
    private Object obj1;//viene usato come id dell'istruzione che dobbiamo compiere con la mail nell'obj2
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
