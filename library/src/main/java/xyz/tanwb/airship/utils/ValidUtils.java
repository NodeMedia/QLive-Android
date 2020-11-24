package xyz.tanwb.airship.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidUtils {

    /**
     * 验证字符串是否是邮箱
     *
     * @param params 字符串参数
     */
    public static boolean isEmail(String params) {
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(params);
        return m.matches();
    }

    /**
     * 验证字符串中是否包含@字符
     *
     * @param params 字符串参数
     */
    public static boolean isEmailValid(String params) {
        return params.contains("@");
    }

    /**
     * 验证字符串是否是手机号
     *
     * @param params 字符串参数
     */
    public static boolean isMobileNO(String params) {
        String str = "^((13[0-9])|(15[^4,\\D])|(17[^4,\\D])|(18[0-9]))\\d{8}$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(params);
        return m.matches();
    }

    /**
     * 验证字符串是否全为数字
     *
     * @param params 字符串参数
     */
    public static boolean isNumber(String params) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher match = pattern.matcher(params);
        return match.matches();
    }

    /**
     * 验证字符串是否符合价格格式
     *
     * @param params 字符串参数
     */
    public static boolean isPrice(String params) {
        Pattern pattern = Pattern.compile("\\d{1,10}(\\.\\d{1,2})?$");
        Matcher matcher = pattern.matcher(params);
        return matcher.matches();
    }

    /**
     * 判断是否是网址
     */
    public static boolean isLinkAvailable(String link) {
        Pattern pattern = Pattern.compile("^(http://|https://)?((?:[A-Za-z0-9]+-[A-Za-z0-9]+|[A-Za-z0-9]+)\\.)+([A-Za-z]+)[/\\?\\:]?.*$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(link);
        return matcher.matches();
    }

    /**
     * 判断是否是银行卡号
     */
    public static boolean isBankCard(String cardId) {
        String regx = "\\d{16}|\\d{19}";
        return cardId.matches(regx);
    }

    /**
     * 判断是否为身份证
     */
    public static boolean isCard(String idStr) {
        String reg15 = "^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$";
        String reg18 = "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9]|X)$";
        return idStr.matches(reg15) || idStr.matches(reg18);
    }

}
