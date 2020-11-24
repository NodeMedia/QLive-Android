package xyz.tanwb.airship.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * 网络请求错误编码
 */
public class ErrorCode {

    public static final Map<String, String> ERRORMAP = new HashMap<>();

    static {
        ERRORMAP.put("-3", "参数错误");
        ERRORMAP.put("-1", "请求失败,请稍后再试.");
        ERRORMAP.put("0", "数据为空");
        ERRORMAP.put("1", "请求成功");
        ERRORMAP.put("20000", "接口签名验证失败");
        ERRORMAP.put("30000", "密码错误");
    }
}
