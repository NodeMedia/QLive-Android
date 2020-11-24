package xyz.tanwb.airship.db.converter;

import android.database.Cursor;

import xyz.tanwb.airship.db.sqlite.ColumnDbType;

public class ByteColumnConverter implements ColumnConverter<Byte> {

    @Override
    public Byte getFieldValue(final Cursor cursor, int index) {
        return cursor.isNull(index) ? null : (byte) cursor.getInt(index);
    }

    @Override
    public Object fieldValue2DbValue(Byte fieldValue) {
        return fieldValue;
    }

    @Override
    public ColumnDbType getColumnDbType() {
        return ColumnDbType.INTEGER;
    }
}
