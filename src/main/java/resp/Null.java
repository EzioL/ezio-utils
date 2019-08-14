package resp;

import java.io.Serializable;

/**
 * @creed: Here be dragons !
 * @author: Ezio
 * @Time: 2019-08-14 11:23
 */
public final class Null implements Serializable {
    public static final Null NULL = new Null();
    private Null(){}
}
