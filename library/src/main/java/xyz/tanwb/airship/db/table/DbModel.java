package xyz.tanwb.airship.db.table;

import android.text.TextUtils;

import java.util.Date;
import java.util.HashMap;

public final class DbModel {

    private HashMap<String, String> dataMap = new HashMap<String, String>();

    public String getString(String columnName) {
        return dataMap.get(columnName);
    }

    public int getInt(String columnName) {
        return Integer.valueOf(dataMap.get(columnName));
    }

    public boolean getBoolean(String columnName) {
        String value = dataMap.get(columnName);
        if (value != null) {
            return value.length() == 1 ? "1".equals(value) : Boolean.valueOf(value);
        }
        return false;
    }

    public double getDouble(String columnName) {
        return Double.valueOf(dataMap.get(columnName));
    }

    public float getFloat(String columnName) {
        return Float.valueOf(dataMap.get(columnName));
    }

    public long getLong(String columnName) {
        return Long.valueOf(dataMap.get(columnName));
    }

    public Date getDate(String columnName) {
        long date = Long.valueOf(dataMap.get(columnName));
        return new Date(date);
    }

    public java.sql.Date getSqlDate(String columnName) {
        long date = Long.valueOf(dataMap.get(columnName));
        return new java.sql.Date(date);
    }

    public void add(String columnName, String valueStr) {
        dataMap.put(columnName, valueStr);
    }

    public HashMap<String, String> getDataMap() {
        return dataMap;
    }

    public boolean isEmpty(String columnName) {
        return TextUtils.isEmpty(dataMap.get(columnName));
    }
}
