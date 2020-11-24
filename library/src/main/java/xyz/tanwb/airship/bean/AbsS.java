package xyz.tanwb.airship.bean;

import android.text.TextUtils;

/**
 * String Json包装类
 */
public class AbsS extends Abs {

    /**
     * JSON具体内容
     */
    public String target;

    /**
     * JSON具体内容
     */
    public String result;

    public AbsS() {
    }

    public AbsS(String data) {
        this("1", null, data);
    }

    public AbsS(String errCode, String message) {
        this(errCode, message, null);
    }

    public AbsS(String errCode, String message, String data) {
        this.errCode = errCode;
        this.message = message;
        this.target = data;
        this.result = data;
    }

    @Override
    public boolean isNotEmpty() {
        return !TextUtils.isEmpty(target) || !TextUtils.isEmpty(result);
    }

}
