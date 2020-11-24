package xyz.tanwb.airship.bean;

/**
 * JSON包装类
 *
 * @param <T>
 */
public class AbsT<T> extends Abs {

    /**
     * JSON具体内容
     */
    public T target;

    /**
     * JSON具体内容
     */
    public T result;

    @Override
    public boolean isNotEmpty() {
        return target != null || result != null;
    }

}
