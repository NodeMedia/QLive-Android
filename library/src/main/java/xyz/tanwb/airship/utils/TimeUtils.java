package xyz.tanwb.airship.utils;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {

    public static final String YMDHMS = "yyyy/MM/dd HH:mm:ss";
    public static final String YMDHM = "yyyy/MM/dd HH:mm";
    public static final String YMDE = "yyyy/MM/dd E";
    public static final String YMD = "yyyy/MM/dd";
    public static final String EHM = "E HH:mm";
    public static final String DHM = "dd日 HH:mm";
    public static final String HMS = "HH:mm:ss";
    public static final String HM = "HH:mm";
    public static final String E = "E";
    public static final String MONTH = "月前";
    public static final String WEEL = "周前";
    public static final String DAY = "天前";
    public static final String HOUR = "小时前";
    public static final String MINUTE = "分钟前";
    public static final String UNKNOW = "Unknow";

    @SuppressLint({"SimpleDateFormat"})
    public static String format(String format, Date date) {
        return format != null && date != null ? (new SimpleDateFormat(format, Locale.CHINA)).format(date) : UNKNOW;
    }

    public static String format(String format, String timeMillis) {
        return format(format, getTimeMillis(timeMillis));
    }

    public static String format(String format, long timeMillis) {
        return format(format, getTimeMillis(timeMillis));
    }

    public static String ymdhms(String timeMillis) {
        return format(YMDHMS, timeMillis);
    }

    public static String ymdhms(long timeMillis) {
        return format(YMDHMS, timeMillis);
    }

    public static String ymdhm(String timeMillis) {
        return format(YMDHM, timeMillis);
    }

    public static String ymdhm(long timeMillis) {
        return format(YMDHM, timeMillis);
    }

    public static String ymde(String timeMillis) {
        return format(YMDE, timeMillis);
    }

    public static String ymde(long timeMillis) {
        return format(YMDE, timeMillis);
    }

    public static String ymd(String timeMillis) {
        return format(YMD, timeMillis);
    }

    public static String ymd(long timeMillis) {
        return format(YMD, timeMillis);
    }

    public static String ehm(String timeMillis) {
        return format(EHM, timeMillis);
    }

    public static String ehm(long timeMillis) {
        return format(EHM, timeMillis);
    }

    public static String hms(String timeMillis) {
        return format(HMS, timeMillis);
    }

    public static String hms(long timeMillis) {
        return format(HMS, timeMillis);
    }

    public static String hm(String timeMillis) {
        return format(HM, timeMillis);
    }

    public static String hm(long timeMillis) {
        return format(HM, timeMillis);
    }

    public static String e(String timeMillis) {
        return format(E, timeMillis);
    }

    public static String e(long timeMillis) {
        return format(E, timeMillis);
    }

    public static String transformDate(Object timeMillis) {
        try {
            if (timeMillis != null) {
                long time;
                if (timeMillis instanceof String) {
                    time = Long.parseLong(timeMillis.toString());
                } else {
                    time = (long) timeMillis;
                }

                if (time == 0L) {
                    return UNKNOW;
                }

                Calendar currentCalendar = Calendar.getInstance();
                currentCalendar.setTimeInMillis(System.currentTimeMillis());
                int currentYear = currentCalendar.get(Calendar.YEAR);
                int currentMonth = currentCalendar.get(Calendar.MONTH);
                int currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);
                Calendar targetCalendar = Calendar.getInstance();
                targetCalendar.setTimeInMillis(time);
                int targetYear = targetCalendar.get(Calendar.YEAR);
                int targetMonth = targetCalendar.get(Calendar.MONTH);
                int targetDay = targetCalendar.get(Calendar.DAY_OF_MONTH);
                String format;
                if (currentDay < 3 && currentMonth - targetMonth == 1 && targetDay > 29) {
                    format = EHM;
                } else if (currentYear == targetYear && currentMonth == targetMonth) {
                    switch (currentDay - targetDay) {
                        case 0:
                            format = HM;
                            break;
                        case 1:
                            format = EHM;
                            break;
                        case 2:
                            format = EHM;
                            break;
                        default:
                            format = DHM;
                    }
                } else {
                    format = YMDHM;
                }
                return format(format, time);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return UNKNOW;
    }

    public static String getDateDesc(Date time) {
        if (time == null) return UNKNOW;
        String timeContent;
        Long ss = System.currentTimeMillis() - time.getTime();
        Long minute = ss / 60000;
        if (minute < 1) {
            minute = 1L;
        }
        if (minute >= 60) {
            Long hour = minute / 60;
            if (hour >= 24) {
                if (hour > 720) {
                    timeContent = (hour / 720) + MONTH;
                } else if (hour > 168 && hour <= 720) {
                    timeContent = (hour / 168) + WEEL;
                } else {
                    timeContent = (hour / 24) + DAY;
                }
            } else {
                timeContent = hour + HOUR;
            }
        } else {
            timeContent = minute + MINUTE;
        }
        return timeContent;
    }

    private static Date getTimeMillis(String timeMillis) {
        try {
            return getTimeMillis(Long.parseLong(timeMillis));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Date getTimeMillis(long timeMillis) {
        try {
            if (timeMillis != 0L) {
                return new Date(timeMillis);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
