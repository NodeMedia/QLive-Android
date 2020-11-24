package xyz.tanwb.airship.bean;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * 基础Json包装
 */
public class Abs implements Serializable {

    /**
     * 错误编码
     */
    public String errCode;
    /**
     * 错误原因 (result复用参数)
     */
    public String message;
    /**
     * 是否成功 (result复用参数)
     */
    public boolean success;
    /**
     * 会话ID
     */
    public String userAttrId;
    /**
     * 是否异步回调 (result参数)
     */
    public String invokeId;

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return success && isNotEmpty();
    }

    /**
     * 根据返回的Code判断是否成功
     */
    public boolean isCodeSuccess() {
        return !TextUtils.isEmpty(this.errCode) && "1".equals(this.errCode) && isNotEmpty();
    }

    /**
     * 是否不为空
     */
    public boolean isNotEmpty() {
        return true;
    }

    /**
     * 获取消息内容
     */
    public String getMsg() {
        return message != null ? message : getCodeMeg(errCode);
    }

    /**
     * 根据Code获取消息内容
     */
    public String getCodeMeg(String code) {
        if (TextUtils.isEmpty(code)) {
            return ErrorCode.ERRORMAP.get("-1");
        }
        String errorMsg = ErrorCode.ERRORMAP.get(code);
        if (TextUtils.isEmpty(errorMsg)) {
            return getCodeMeg(null);
        }
        return errorMsg;
    }
}
