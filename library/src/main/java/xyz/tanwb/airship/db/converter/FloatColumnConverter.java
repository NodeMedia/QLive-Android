package xyz.tanwb.airship.db.converter;

import android.database.Cursor;

import xyz.tanwb.airship.db.sqlite.ColumnDbType;

public class FloatColumnConverter implements ColumnConverter<Float> {

    @Override
    public Float getFieldValue(final Cursor cursor, int index) {
        return cursor.isNull(index) ? null : cursor.getFloat(index);
    }

    @Override
    public Object fieldValue2DbValue(Float fieldValue) {
        return fieldValue;
    }

    @Override
    public ColumnDbType getColumnDbType() {
        return ColumnDbType.REAL;
    }
}
