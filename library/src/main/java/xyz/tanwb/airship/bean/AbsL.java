package xyz.tanwb.airship.bean;

import java.util.List;

/**
 * List Json包装类
 *
 * @param <T>
 */
public class AbsL<T> extends Abs {

    /**
     * JSON具体内容
     */
    public List<T> target;

    /**
     * JSON具体内容
     */
    public List<T> result;

    @Override
    public boolean isNotEmpty() {
        return (target != null && !target.isEmpty()) || (result != null && !result.isEmpty());
    }
}
