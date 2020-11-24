package xyz.tanwb.airship.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 分页信息
 *
 * @param <T>
 */
public class PageInfo<T> implements Serializable {

    /**
     * 当前页数
     */
    public int pageNo;

    /**
     * 总页数
     */
    public int totalPageNo;

    /**
     * 页大小
     */
    public int pageSize;

    /**
     * 当前结果集数量
     */
    public int size;

    /**
     * 所有结果集的总数量
     */
    public int totalCount;

    /**
     * 结果列表,其中的数据类型由具体的业务定义
     */
    public List<T> result;

}
