package xyz.tanwb.airship.db.sqlite;

import android.text.TextUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import xyz.tanwb.airship.BaseConstants;
import xyz.tanwb.airship.db.DBConstants;
import xyz.tanwb.airship.db.converter.ColumnConverterFactory;
import xyz.tanwb.airship.db.table.ColumnUtils;

public final class WhereBuilder {

    private final List<String> whereItems;

    private WhereBuilder() {
        this.whereItems = new ArrayList<String>();
    }

    /**
     * create new instance
     */
    public static WhereBuilder b() {
        return new WhereBuilder();
    }

    /**
     * create new instance
     *
     * @param columnName columnName
     * @param op         operator: "=","LIKE","IN","BETWEEN"...
     * @param value      value
     */
    public static WhereBuilder b(String columnName, String op, Object value) {
        WhereBuilder result = b();
        result.appendCondition(null, columnName, op, value);
        return result;
    }

    /**
     * add AND condition
     *
     * @param columnName columnName
     * @param op         operator: "=","LIKE","IN","BETWEEN"...
     * @param value      value
     */
    public WhereBuilder and(String columnName, String op, Object value) {
        appendCondition(whereItems.size() == 0 ? null : DBConstants.AND_COND, columnName, op, value);
        return this;
    }

    /**
     * add AND condition
     *
     * @param where expr("[AND] (" + where.toString() + ")")
     */
    public WhereBuilder and(WhereBuilder where) {
        StringBuilder builder = new StringBuilder(DBConstants.SPACE);
        if (whereItems.size() > 0) {
            builder.append(DBConstants.AND_COND).append(DBConstants.SPACE);
        }
        builder.append(DBConstants.LEFT_BRACKETS).append(where.toString()).append(DBConstants.RIGHT_BRACKETS);
        whereItems.add(builder.toString());
        return this;
    }

    /**
     * add OR condition
     *
     * @param columnName columnName
     * @param op         operator: "=","LIKE","IN","BETWEEN"...
     * @param value      value
     */
    public WhereBuilder or(String columnName, String op, Object value) {
        appendCondition(whereItems.size() == 0 ? null : DBConstants.OR_COND, columnName, op, value);
        return this;
    }

    /**
     * add OR condition
     *
     * @param where expr("[OR] (" + where.toString() + ")")
     */
    public WhereBuilder or(WhereBuilder where) {
        StringBuilder builder = new StringBuilder(DBConstants.SPACE);
        if (whereItems.size() > 0) {
            builder.append(DBConstants.OR_COND).append(DBConstants.SPACE);
        }
        builder.append(DBConstants.LEFT_BRACKETS).append(where.toString()).append(DBConstants.RIGHT_BRACKETS);
        whereItems.add(builder.toString());
        return this;
    }

    public WhereBuilder expr(String expr) {
        whereItems.add(DBConstants.SPACE + expr);
        return this;
    }

    public int getWhereItemSize() {
        return whereItems.size();
    }

    @Override
    public String toString() {
        if (whereItems.size() == 0) {
            return BaseConstants.NULL;
        }
        StringBuilder sb = new StringBuilder();
        for (String item : whereItems) {
            sb.append(item);
        }
        return sb.toString();
    }

    private void appendCondition(String conj, String columnName, String op, Object value) {
        StringBuilder builder = new StringBuilder();

        if (whereItems.size() > 0) {
            builder.append(DBConstants.SPACE);
        }

        // append conj
        if (!TextUtils.isEmpty(conj)) {
            builder.append(conj).append(DBConstants.SPACE);
        }

        // append columnName
        builder.append(DBConstants.DOUBLE_QUOTES).append(columnName).append(DBConstants.DOUBLE_QUOTES);

        // convert op
        if (DBConstants.EQUALS.equals(op)) {
            op = DBConstants.EQUAL;
        } else if (DBConstants.NOTEQUAL.equals(op)) {
            op = DBConstants.BARRING;
        }

        // append op & value
        if (value == null) {
            if (DBConstants.EQUAL.equals(op)) {
                builder.append(DBConstants.ISNULL);
            } else if (DBConstants.BARRING.equals(op)) {
                builder.append(DBConstants.ISNOTNULL);
            } else {
                builder.append(DBConstants.SPACE).append(op).append(DBConstants.DATANULL);
            }
        } else {
            builder.append(DBConstants.SPACE).append(op).append(DBConstants.SPACE);

            if (DBConstants.IN_COND.equalsIgnoreCase(op)) {
                Iterable<?> items = null;
                if (value instanceof Iterable) {
                    items = (Iterable<?>) value;
                } else if (value.getClass().isArray()) {
                    int len = Array.getLength(value);
                    List<Object> arrayList = new ArrayList<Object>(len);
                    for (int i = 0; i < len; i++) {
                        arrayList.add(Array.get(value, i));
                    }
                    items = arrayList;
                }
                if (items != null) {
                    StringBuilder inSb = new StringBuilder(DBConstants.LEFT_BRACKETS);
                    for (Object item : items) {
                        Object itemColValue = ColumnUtils.convert2DbValueIfNeeded(item);
                        if (ColumnDbType.TEXT.equals(ColumnConverterFactory.getDbColumnType(itemColValue.getClass()))) {
                            String valueStr = itemColValue.toString();
                            if (valueStr.indexOf(DBConstants.SINGLE_QUOTES) != -1) {
                                valueStr = valueStr.replace(DBConstants.SINGLE, DBConstants.DOUBLE);
                            }
                            inSb.append(DBConstants.SINGLE).append(valueStr).append(DBConstants.SINGLE);
                        } else {
                            inSb.append(itemColValue);
                        }
                        inSb.append(DBConstants.COMMA);
                    }
                    inSb.deleteCharAt(inSb.length() - 1);
                    inSb.append(DBConstants.RIGHT_BRACKETS);
                    builder.append(inSb.toString());
                } else {
                    throw new IllegalArgumentException(DBConstants.VALUE_ARRAY);
                }
            } else if (DBConstants.BETWEEN_COND.equalsIgnoreCase(op)) {
                Iterable<?> items = null;
                if (value instanceof Iterable) {
                    items = (Iterable<?>) value;
                } else if (value.getClass().isArray()) {
                    int len = Array.getLength(value);
                    List<Object> arrayList = new ArrayList<Object>(len);
                    for (int i = 0; i < len; i++) {
                        arrayList.add(Array.get(value, i));
                    }
                    items = arrayList;
                }
                if (items != null) {
                    Iterator<?> iterator = items.iterator();
                    if (!iterator.hasNext()) {
                        throw new IllegalArgumentException(DBConstants.VALUE_TWOITEMS);
                    }
                    Object start = iterator.next();
                    if (!iterator.hasNext()) {
                        throw new IllegalArgumentException(DBConstants.VALUE_TWOITEMS);
                    }
                    Object end = iterator.next();

                    Object startColValue = ColumnUtils.convert2DbValueIfNeeded(start);
                    Object endColValue = ColumnUtils.convert2DbValueIfNeeded(end);

                    if (ColumnDbType.TEXT.equals(ColumnConverterFactory.getDbColumnType(startColValue.getClass()))) {
                        String startStr = startColValue.toString();
                        if (startStr.indexOf(DBConstants.SINGLE_QUOTES) != -1) {
                            startStr = startStr.replace(DBConstants.SINGLE, DBConstants.DOUBLE);
                        }
                        String endStr = endColValue.toString();
                        if (endStr.indexOf(DBConstants.SINGLE_QUOTES) != -1) {
                            endStr = endStr.replace(DBConstants.SINGLE, DBConstants.DOUBLE);
                        }
                        builder.append(DBConstants.SINGLE).append(startStr).append(DBConstants.SINGLE);
                        builder.append(DBConstants.AND_COND);
                        builder.append(DBConstants.SINGLE).append(endStr).append(DBConstants.SINGLE);
                    } else {
                        builder.append(startColValue);
                        builder.append(DBConstants.AND_COND);
                        builder.append(endColValue);
                    }
                } else {
                    throw new IllegalArgumentException(DBConstants.VALUE_ARRAY);
                }
            } else {
                value = ColumnUtils.convert2DbValueIfNeeded(value);
                if (ColumnDbType.TEXT.equals(ColumnConverterFactory.getDbColumnType(value.getClass()))) {
                    String valueStr = value.toString();
                    if (valueStr.indexOf(DBConstants.SINGLE_QUOTES) != -1) {
                        valueStr = valueStr.replace(DBConstants.SINGLE, DBConstants.DOUBLE);
                    }
                    builder.append(DBConstants.SINGLE).append(valueStr).append(DBConstants.SINGLE);
                } else {
                    builder.append(value);
                }
            }
        }
        whereItems.add(builder.toString());
    }
}
