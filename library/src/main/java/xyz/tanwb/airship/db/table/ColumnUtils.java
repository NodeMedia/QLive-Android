package xyz.tanwb.airship.db.table;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;

import xyz.tanwb.airship.db.DBConstants;
import xyz.tanwb.airship.db.converter.ColumnConverter;
import xyz.tanwb.airship.db.converter.ColumnConverterFactory;

public final class ColumnUtils {

    private static final HashSet<Class<?>> BOOLEAN_TYPES = new HashSet<Class<?>>(2);
    private static final HashSet<Class<?>> INTEGER_TYPES = new HashSet<Class<?>>(2);
    private static final HashSet<Class<?>> AUTO_INCREMENT_TYPES = new HashSet<Class<?>>(4);

    static {
        BOOLEAN_TYPES.add(boolean.class);
        BOOLEAN_TYPES.add(Boolean.class);

        INTEGER_TYPES.add(int.class);
        INTEGER_TYPES.add(Integer.class);

        AUTO_INCREMENT_TYPES.addAll(INTEGER_TYPES);
        AUTO_INCREMENT_TYPES.add(long.class);
        AUTO_INCREMENT_TYPES.add(Long.class);
    }

    public static boolean isAutoIdType(Class<?> fieldType) {
        return AUTO_INCREMENT_TYPES.contains(fieldType);
    }

    public static boolean isInteger(Class<?> fieldType) {
        return INTEGER_TYPES.contains(fieldType);
    }

    public static boolean isBoolean(Class<?> fieldType) {
        return BOOLEAN_TYPES.contains(fieldType);
    }

    public static Object convert2DbValueIfNeeded(final Object value) {
        Object result = value;
        if (value != null) {
            Class<?> valueType = value.getClass();
            ColumnConverter converter = ColumnConverterFactory.getColumnConverter(valueType);
            result = converter.fieldValue2DbValue(value);
        }
        return result;
    }

    static Method findGetMethod(Class<?> entityType, Field field) {
        if (Object.class.equals(entityType)) return null;

        String fieldName = field.getName();
        Method getMethod = null;
        if (isBoolean(field.getType())) {
            getMethod = findBooleanGetMethod(entityType, fieldName);
        }
        if (getMethod == null) {
            String methodName = DBConstants.MRTHOD_GET + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            try {
                getMethod = entityType.getDeclaredMethod(methodName);
            } catch (NoSuchMethodException ignore) {
            }
        }

        if (getMethod == null) {
            return findGetMethod(entityType.getSuperclass(), field);
        }
        return getMethod;
    }

    static Method findSetMethod(Class<?> entityType, Field field) {
        if (Object.class.equals(entityType)) return null;

        String fieldName = field.getName();
        Class<?> fieldType = field.getType();
        Method setMethod = null;
        if (isBoolean(fieldType)) {
            setMethod = findBooleanSetMethod(entityType, fieldName, fieldType);
        }
        if (setMethod == null) {
            String methodName = DBConstants.MRTHOD_SET + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            try {
                setMethod = entityType.getDeclaredMethod(methodName, fieldType);
            } catch (NoSuchMethodException ignore) {
            }
        }

        if (setMethod == null) {
            return findSetMethod(entityType.getSuperclass(), field);
        }
        return setMethod;
    }

    private static Method findBooleanGetMethod(Class<?> entityType, final String fieldName) {
        String methodName;
        if (fieldName.startsWith(DBConstants.MRTHOD_IS)) {
            methodName = fieldName;
        } else {
            methodName = DBConstants.MRTHOD_IS + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        }
        try {
            return entityType.getDeclaredMethod(methodName);
        } catch (NoSuchMethodException ignore) {
        }
        return null;
    }

    private static Method findBooleanSetMethod(Class<?> entityType, final String fieldName, Class<?> fieldType) {
        String methodName;
        if (fieldName.startsWith(DBConstants.MRTHOD_IS)) {
            methodName = DBConstants.MRTHOD_SET + fieldName.substring(2, 3).toUpperCase() + fieldName.substring(3);
        } else {
            methodName = DBConstants.MRTHOD_SET + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        }
        try {
            return entityType.getDeclaredMethod(methodName, fieldType);
        } catch (NoSuchMethodException ignore) {
        }
        return null;
    }

}
