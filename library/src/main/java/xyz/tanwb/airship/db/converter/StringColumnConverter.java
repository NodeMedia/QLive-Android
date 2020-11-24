package xyz.tanwb.airship.db.converter;

import android.database.Cursor;

import xyz.tanwb.airship.db.sqlite.ColumnDbType;

public class StringColumnConverter implements ColumnConverter<String> {

    @Override
    public String getFieldValue(final Cursor cursor, int index) {
        return cursor.isNull(index) ? null : cursor.getString(index);
    }

    @Override
    public Object fieldValue2DbValue(String fieldValue) {
        return fieldValue;
    }

    @Override
    public ColumnDbType getColumnDbType() {
        return ColumnDbType.TEXT;
    }
}
