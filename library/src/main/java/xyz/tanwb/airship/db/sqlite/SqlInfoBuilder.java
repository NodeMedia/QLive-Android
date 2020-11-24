package xyz.tanwb.airship.db.sqlite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import xyz.tanwb.airship.db.DBConstants;
import xyz.tanwb.airship.db.DbException;
import xyz.tanwb.airship.db.KeyValue;
import xyz.tanwb.airship.db.table.ColumnEntity;
import xyz.tanwb.airship.db.table.TableEntity;

/**
 * Sql语句组装类
 * Build "insert", "replace",，"update", "delete" and "create" sql.
 */
public final class SqlInfoBuilder {

    private static final ConcurrentHashMap<TableEntity<?>, String> INSERT_SQL_CACHE = new ConcurrentHashMap<TableEntity<?>, String>();
    private static final ConcurrentHashMap<TableEntity<?>, String> REPLACE_SQL_CACHE = new ConcurrentHashMap<TableEntity<?>, String>();

    //*********************************************** insert sql ***********************************************

    public static SqlInfo buildInsertSqlInfo(TableEntity<?> table, Object entity) throws DbException {

        List<KeyValue> keyValueList = entity2KeyValueList(table, entity);

        if (keyValueList.size() == 0) return null;

        SqlInfo result = new SqlInfo();
        String sql = INSERT_SQL_CACHE.get(table);
        if (sql == null) {
            StringBuilder builder = new StringBuilder();
            builder.append(DBConstants.INSERT).append(DBConstants.INTO).
                    append(DBConstants.DOUBLE_QUOTES).append(table.getName()).append(DBConstants.DOUBLE_QUOTES).
                    append(DBConstants.SPACE).append(DBConstants.LEFT_BRACKETS);
            for (KeyValue kv : keyValueList) {
                builder.append(DBConstants.DOUBLE_QUOTES).append(kv.key).append(DBConstants.DOUBLE_QUOTES).append(DBConstants.COMMA);
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(DBConstants.RIGHT_BRACKETS).append(DBConstants.VALUES).append(DBConstants.LEFT_BRACKETS);

            for (int i = 0; i < keyValueList.size(); i++) {
                builder.append(DBConstants.QUESTION_MARK).append(DBConstants.COMMA);
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(DBConstants.RIGHT_BRACKETS);

            sql = builder.toString();
            result.setSql(sql);
            result.addBindArgs(keyValueList);
            INSERT_SQL_CACHE.put(table, sql);
        } else {
            result.setSql(sql);
            result.addBindArgs(keyValueList);
        }

        return result;
    }

    //*********************************************** replace sql ***********************************************

    public static SqlInfo buildReplaceSqlInfo(TableEntity<?> table, Object entity) throws DbException {

        List<KeyValue> keyValueList = entity2KeyValueList(table, entity);
        if (keyValueList.size() == 0) return null;

        SqlInfo result = new SqlInfo();
        String sql = REPLACE_SQL_CACHE.get(table);
        if (sql == null) {
            StringBuilder builder = new StringBuilder();
            builder.append(DBConstants.REPLACE).append(DBConstants.INTO).
                    append(DBConstants.DOUBLE_QUOTES).append(table.getName()).append(DBConstants.DOUBLE_QUOTES).
                    append(DBConstants.SPACE).append(DBConstants.LEFT_BRACKETS);
            for (KeyValue kv : keyValueList) {
                builder.append(DBConstants.DOUBLE_QUOTES).append(kv.key).append(DBConstants.DOUBLE_QUOTES).append(DBConstants.COMMA);
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(DBConstants.RIGHT_BRACKETS).append(DBConstants.VALUES).append(DBConstants.LEFT_BRACKETS);

            for (int i = 0; i < keyValueList.size(); i++) {
                builder.append(DBConstants.QUESTION_MARK).append(DBConstants.COMMA);
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(DBConstants.RIGHT_BRACKETS);

            sql = builder.toString();
            result.setSql(sql);
            result.addBindArgs(keyValueList);
            REPLACE_SQL_CACHE.put(table, sql);
        } else {
            result.setSql(sql);
            result.addBindArgs(keyValueList);
        }

        return result;
    }

    //*********************************************** delete sql ***********************************************

    public static SqlInfo buildDeleteSqlInfo(TableEntity<?> table, WhereBuilder whereBuilder) throws DbException {
        StringBuilder builder = new StringBuilder();
        builder.append(DBConstants.DELETE).append(DBConstants.FROM);
        builder.append(DBConstants.DOUBLE_QUOTES).append(table.getName()).append(DBConstants.DOUBLE_QUOTES);

        if (whereBuilder != null && whereBuilder.getWhereItemSize() > 0) {
            builder.append(DBConstants.WHERE).append(whereBuilder.toString());
        }

        return new SqlInfo(builder.toString());
    }

    public static SqlInfo buildDeleteSqlInfo(TableEntity<?> table, Object entity) throws DbException {
        ColumnEntity id = table.getId();
        Object idValue = id.getColumnValue(entity);

        if (idValue == null) {
            throw new DbException(String.format(DBConstants.ENTITY_ERROR, table.getEntityType()));
        }

        return buildDeleteSqlInfoById(table, idValue);
    }

    public static SqlInfo buildDeleteSqlInfoById(TableEntity<?> table, Object idValue) throws DbException {
        ColumnEntity id = table.getId();

        if (idValue == null) {
            throw new DbException(String.format(DBConstants.ENTITY_ERROR, table.getEntityType()));
        }

        StringBuilder builder = new StringBuilder();
        builder.append(DBConstants.DELETE).append(DBConstants.FROM);
        builder.append(DBConstants.DOUBLE_QUOTES).append(table.getName()).append(DBConstants.DOUBLE_QUOTES);
        builder.append(DBConstants.WHERE).append(WhereBuilder.b(id.getName(), DBConstants.EQUAL, idValue));

        return new SqlInfo(builder.toString());
    }

    //*********************************************** update sql ***********************************************

    public static SqlInfo buildUpdateSqlInfo(TableEntity<?> table, Object entity, String... updateColumnNames) throws DbException {

        List<KeyValue> keyValueList = entity2KeyValueList(table, entity);

        if (keyValueList.size() == 0) return null;

        HashSet<String> updateColumnNameSet = null;
        if (updateColumnNames != null && updateColumnNames.length > 0) {
            updateColumnNameSet = new HashSet<String>(updateColumnNames.length);
            Collections.addAll(updateColumnNameSet, updateColumnNames);
        }

        ColumnEntity id = table.getId();
        Object idValue = null;
        try {
            idValue = id.getColumnValue(entity);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (idValue == null) {
            throw new DbException(String.format(DBConstants.ENTITY_ERROR, table.getEntityType()));
        }

        SqlInfo result = new SqlInfo();
        StringBuilder builder = new StringBuilder();
        builder.append(DBConstants.UPDATE).append(DBConstants.SPACE);
        builder.append(DBConstants.DOUBLE_QUOTES).append(table.getName()).append(DBConstants.DOUBLE_QUOTES);
        builder.append(DBConstants.SET);
        for (KeyValue kv : keyValueList) {
            if (updateColumnNameSet == null || updateColumnNameSet.contains(kv.key)) {
                builder.append(DBConstants.DOUBLE_QUOTES).append(kv.key).append(DBConstants.DOUBLE_QUOTES).
                        append(DBConstants.EQUAL).append(DBConstants.QUESTION_MARK).append(DBConstants.COMMA);
                result.addBindArg(kv);
            }
        }
        builder.deleteCharAt(builder.length() - 1);

        builder.append(DBConstants.WHERE).append(WhereBuilder.b(id.getName(), DBConstants.EQUAL, idValue));

        result.setSql(builder.toString());
        return result;
    }

    public static SqlInfo buildUpdateSqlInfo(TableEntity<?> table, WhereBuilder whereBuilder, KeyValue... nameValuePairs) throws DbException {
        if (nameValuePairs == null || nameValuePairs.length == 0) return null;

        SqlInfo result = new SqlInfo();
        StringBuilder builder = new StringBuilder();
        builder.append(DBConstants.UPDATE).append(DBConstants.SPACE);
        builder.append(DBConstants.DOUBLE_QUOTES).append(table.getName()).append(DBConstants.DOUBLE_QUOTES);
        builder.append(DBConstants.SET);
        for (KeyValue kv : nameValuePairs) {
            builder.append(DBConstants.DOUBLE_QUOTES).append(kv.key).append(DBConstants.DOUBLE_QUOTES).
                    append(DBConstants.EQUAL).append(DBConstants.QUESTION_MARK).append(DBConstants.COMMA);
            result.addBindArg(kv);
        }
        builder.deleteCharAt(builder.length() - 1);

        if (whereBuilder != null && whereBuilder.getWhereItemSize() > 0) {
            builder.append(DBConstants.WHERE).append(whereBuilder.toString());
        }

        result.setSql(builder.toString());
        return result;
    }

    //*********************************************** others ***********************************************

    public static SqlInfo buildCreateTableSqlInfo(TableEntity<?> table) throws DbException {
        ColumnEntity id = table.getId();

        StringBuilder builder = new StringBuilder();
        builder.append(DBConstants.TABLE_CREATE).append(DBConstants.DOUBLE_QUOTES).append(table.getName()).append(DBConstants.DOUBLE_QUOTES).
                append(DBConstants.SPACE).append(DBConstants.LEFT_BRACKETS).append(DBConstants.SPACE);
        if (id.isAutoId()) {
            builder.append(DBConstants.DOUBLE_QUOTES).append(id.getName()).append(DBConstants.DOUBLE_QUOTES).append(DBConstants.TABLE_AUTOINCREMENT);
        } else {
            builder.append(DBConstants.DOUBLE_QUOTES).append(id.getName()).append(DBConstants.DOUBLE_QUOTES).append(id.getColumnDbType()).append(DBConstants.TABLE_PRIMARY);
        }

        Collection<ColumnEntity> columns = table.getColumnMap().values();
        for (ColumnEntity column : columns) {
            if (column.isId()) continue;
            builder.append(DBConstants.DOUBLE_QUOTES).append(column.getName()).append(DBConstants.DOUBLE_QUOTES);
            builder.append(DBConstants.SPACE).append(column.getColumnDbType());
            builder.append(DBConstants.SPACE).append(column.getProperty());
            builder.append(DBConstants.COMMA);
        }
        builder.deleteCharAt(builder.length() - 1);

        builder.append(DBConstants.SPACE).append(DBConstants.RIGHT_BRACKETS);

        return new SqlInfo(builder.toString());
    }

    public static List<KeyValue> entity2KeyValueList(TableEntity<?> table, Object entity) throws DbException {
        Collection<ColumnEntity> columns = table.getColumnMap().values();
        List<KeyValue> keyValueList = new ArrayList<KeyValue>(columns.size());
        for (ColumnEntity column : columns) {
            KeyValue kv = column2KeyValue(entity, column);
            if (kv != null) {
                keyValueList.add(kv);
            }
        }
        return keyValueList;
    }

    private static KeyValue column2KeyValue(Object entity, ColumnEntity column) throws DbException {
        if (column.isAutoId()) return null;

        String key = column.getName();
        Object value = column.getFieldValue(entity);
        return new KeyValue(key, value);
    }
}
