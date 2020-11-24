package xyz.tanwb.airship.db.table;

import android.database.Cursor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import xyz.tanwb.airship.db.DbException;
import xyz.tanwb.airship.db.annotation.Column;
import xyz.tanwb.airship.db.converter.ColumnConverter;
import xyz.tanwb.airship.db.converter.ColumnConverterFactory;
import xyz.tanwb.airship.db.sqlite.ColumnDbType;

public final class ColumnEntity {

    protected final String name;

    protected final Method getMethod;
    protected final Method setMethod;

    protected final Field columnField;
    protected final ColumnConverter columnConverter;

    private final String property;
    private final boolean isId;
    private final boolean isAutoId;

    public ColumnEntity(Class<?> entityType, Field field, Column column) {
        field.setAccessible(true);

        this.columnField = field;
        this.name = column.name();
        this.property = column.property();
        this.isId = column.isId();

        Class<?> fieldType = field.getType();
        this.isAutoId = this.isId && column.autoGen() && ColumnUtils.isAutoIdType(fieldType);
        this.columnConverter = ColumnConverterFactory.getColumnConverter(fieldType);

        this.getMethod = ColumnUtils.findGetMethod(entityType, field);
        if (this.getMethod != null && !this.getMethod.isAccessible()) {
            this.getMethod.setAccessible(true);
        }
        this.setMethod = ColumnUtils.findSetMethod(entityType, field);
        if (this.setMethod != null && !this.setMethod.isAccessible()) {
            this.setMethod.setAccessible(true);
        }
    }

    public void setValueFromCursor(Object entity, Cursor cursor, int index) throws DbException {
        Object value = columnConverter.getFieldValue(cursor, index);
        if (value == null) return;
        try {
            if (setMethod != null) {
                setMethod.invoke(entity, value);
            } else {
                this.columnField.set(entity, value);
            }
        } catch (Exception e) {
            throw new DbException(e.getMessage());
        }
    }

    public Object getColumnValue(Object entity) throws DbException {
        Object fieldValue = getFieldValue(entity);
        if (this.isAutoId && (fieldValue.equals(0L) || fieldValue.equals(0))) {
            return null;
        }
        return columnConverter.fieldValue2DbValue(fieldValue);
    }

    public void setAutoIdValue(Object entity, long value) throws DbException {
        Object idValue = value;
        if (ColumnUtils.isInteger(columnField.getType())) {
            idValue = (int) value;
        }
        try {
            if (setMethod != null) {
                setMethod.invoke(entity, idValue);
            } else {
                this.columnField.set(entity, idValue);
            }
        } catch (Exception e) {
            throw new DbException(e.getMessage());
        }
    }

    public Object getFieldValue(Object entity) throws DbException {
        Object fieldValue = null;
        try {
            if (entity != null) {
                if (getMethod != null) {
                    fieldValue = getMethod.invoke(entity);
                } else {
                    fieldValue = this.columnField.get(entity);
                }
            }
        } catch (Exception e) {
            throw new DbException(e.getMessage());
        }
        return fieldValue;
    }

    public String getName() {
        return name;
    }

    public String getProperty() {
        return property;
    }

    public boolean isId() {
        return isId;
    }

    public boolean isAutoId() {
        return isAutoId;
    }

    public Field getColumnField() {
        return columnField;
    }

    public ColumnConverter getColumnConverter() {
        return columnConverter;
    }

    public ColumnDbType getColumnDbType() {
        return columnConverter.getColumnDbType();
    }

    @Override
    public String toString() {
        return name;
    }
}
