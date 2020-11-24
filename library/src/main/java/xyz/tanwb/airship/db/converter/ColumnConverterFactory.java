package xyz.tanwb.airship.db.converter;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import xyz.tanwb.airship.db.sqlite.ColumnDbType;

public final class ColumnConverterFactory {

    private static final ConcurrentHashMap<String, ColumnConverter> COLUMNMAP;

    public static ColumnConverter getColumnConverter(Class columnType) {
        ColumnConverter result = null;
        if (COLUMNMAP.containsKey(columnType.getName())) {
            result = COLUMNMAP.get(columnType.getName());
        } else if (ColumnConverter.class.isAssignableFrom(columnType)) {
            ColumnConverter columnConverter = null;
            try {
                columnConverter = (ColumnConverter) columnType.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (columnConverter != null) {
                COLUMNMAP.put(columnType.getName(), columnConverter);
            }
            result = columnConverter;
        }

        if (result == null) {
            throw new RuntimeException("Database Column Not Support: " + columnType.getName() + ", please impl ColumnConverter or use ColumnConverterFactory#registerColumnConverter(...)");
        }

        return result;
    }

    public static ColumnDbType getDbColumnType(Class columnType) {
        ColumnConverter converter = getColumnConverter(columnType);
        return converter.getColumnDbType();
    }

    public static void registerColumnConverter(Class columnType, ColumnConverter columnConverter) {
        COLUMNMAP.put(columnType.getName(), columnConverter);
    }

    public static boolean isSupportColumnConverter(Class columnType) {
        if (COLUMNMAP.containsKey(columnType.getName())) {
            return true;
        } else if (ColumnConverter.class.isAssignableFrom(columnType)) {
            ColumnConverter columnConverter = null;
            try {
                columnConverter = (ColumnConverter) columnType.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (columnConverter != null) {
                COLUMNMAP.put(columnType.getName(), columnConverter);
            }
            return columnConverter == null;
        }
        return false;
    }

    static {
        COLUMNMAP = new ConcurrentHashMap<String, ColumnConverter>();

        BooleanColumnConverter booleanColumnConverter = new BooleanColumnConverter();
        COLUMNMAP.put(boolean.class.getName(), booleanColumnConverter);
        COLUMNMAP.put(Boolean.class.getName(), booleanColumnConverter);

        ByteArrayColumnConverter byteArrayColumnConverter = new ByteArrayColumnConverter();
        COLUMNMAP.put(byte[].class.getName(), byteArrayColumnConverter);

        ByteColumnConverter byteColumnConverter = new ByteColumnConverter();
        COLUMNMAP.put(byte.class.getName(), byteColumnConverter);
        COLUMNMAP.put(Byte.class.getName(), byteColumnConverter);

        CharColumnConverter charColumnConverter = new CharColumnConverter();
        COLUMNMAP.put(char.class.getName(), charColumnConverter);
        COLUMNMAP.put(Character.class.getName(), charColumnConverter);

        DateColumnConverter dateColumnConverter = new DateColumnConverter();
        COLUMNMAP.put(Date.class.getName(), dateColumnConverter);

        DoubleColumnConverter doubleColumnConverter = new DoubleColumnConverter();
        COLUMNMAP.put(double.class.getName(), doubleColumnConverter);
        COLUMNMAP.put(Double.class.getName(), doubleColumnConverter);

        FloatColumnConverter floatColumnConverter = new FloatColumnConverter();
        COLUMNMAP.put(float.class.getName(), floatColumnConverter);
        COLUMNMAP.put(Float.class.getName(), floatColumnConverter);

        IntegerColumnConverter integerColumnConverter = new IntegerColumnConverter();
        COLUMNMAP.put(int.class.getName(), integerColumnConverter);
        COLUMNMAP.put(Integer.class.getName(), integerColumnConverter);

        LongColumnConverter longColumnConverter = new LongColumnConverter();
        COLUMNMAP.put(long.class.getName(), longColumnConverter);
        COLUMNMAP.put(Long.class.getName(), longColumnConverter);

        ShortColumnConverter shortColumnConverter = new ShortColumnConverter();
        COLUMNMAP.put(short.class.getName(), shortColumnConverter);
        COLUMNMAP.put(Short.class.getName(), shortColumnConverter);

        SqlDateColumnConverter sqlDateColumnConverter = new SqlDateColumnConverter();
        COLUMNMAP.put(java.sql.Date.class.getName(), sqlDateColumnConverter);

        StringColumnConverter stringColumnConverter = new StringColumnConverter();
        COLUMNMAP.put(String.class.getName(), stringColumnConverter);
    }
}
