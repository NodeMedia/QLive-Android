package xyz.tanwb.airship.db.converter;

import android.database.Cursor;

import xyz.tanwb.airship.db.sqlite.ColumnDbType;

public class LongColumnConverter implements ColumnConverter<Long> {

    @Override
    public Long getFieldValue(final Cursor cursor, int index) {
        return cursor.isNull(index) ? null : cursor.getLong(index);
    }

    @Override
    public Object fieldValue2DbValue(Long fieldValue) {
        return fieldValue;
    }

    @Override
    public ColumnDbType getColumnDbType() {
        return ColumnDbType.INTEGER;
    }
}
