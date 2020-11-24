package xyz.tanwb.airship.db;

import android.database.Cursor;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import xyz.tanwb.airship.db.sqlite.WhereBuilder;
import xyz.tanwb.airship.db.table.DbModel;

public final class DbModelSelector {

    private String[] columnExpressions;
    private String groupByColumnName;
    private WhereBuilder having;

    private Selector<?> selector;

    private DbModelSelector(xyz.tanwb.airship.db.table.TableEntity<?> table) {
        selector = Selector.from(table);
    }

    protected DbModelSelector(Selector<?> selector, String groupByColumnName) {
        this.selector = selector;
        this.groupByColumnName = groupByColumnName;
    }

    protected DbModelSelector(Selector<?> selector, String[] columnExpressions) {
        this.selector = selector;
        this.columnExpressions = columnExpressions;
    }

    public static DbModelSelector from(xyz.tanwb.airship.db.table.TableEntity<?> table) {
        return new DbModelSelector(table);
    }

    public DbModelSelector where(WhereBuilder whereBuilder) {
        selector.where(whereBuilder);
        return this;
    }

    public DbModelSelector where(String columnName, String op, Object value) {
        selector.where(columnName, op, value);
        return this;
    }

    public DbModelSelector and(String columnName, String op, Object value) {
        selector.and(columnName, op, value);
        return this;
    }

    public DbModelSelector and(WhereBuilder where) {
        selector.and(where);
        return this;
    }

    public DbModelSelector or(String columnName, String op, Object value) {
        selector.or(columnName, op, value);
        return this;
    }

    public DbModelSelector or(WhereBuilder where) {
        selector.or(where);
        return this;
    }

    public DbModelSelector expr(String expr) {
        selector.expr(expr);
        return this;
    }

    public DbModelSelector groupBy(String columnName) {
        this.groupByColumnName = columnName;
        return this;
    }

    public DbModelSelector having(WhereBuilder whereBuilder) {
        this.having = whereBuilder;
        return this;
    }

    public DbModelSelector select(String... columnExpressions) {
        this.columnExpressions = columnExpressions;
        return this;
    }

    public DbModelSelector orderBy(String columnName) {
        selector.orderBy(columnName);
        return this;
    }

    public DbModelSelector orderBy(String columnName, boolean desc) {
        selector.orderBy(columnName, desc);
        return this;
    }

    public DbModelSelector limit(int limit) {
        selector.limit(limit);
        return this;
    }

    public DbModelSelector offset(int offset) {
        selector.offset(offset);
        return this;
    }

    public xyz.tanwb.airship.db.table.TableEntity<?> getTable() {
        return selector.getTable();
    }

    public DbModel findFirst() throws DbException {
        xyz.tanwb.airship.db.table.TableEntity<?> table = selector.getTable();
        if (!table.tableIsExist()) return null;

        this.limit(1);
        Cursor cursor = table.getDb().execQuery(this.toString());
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    return CursorUtils.getDbModel(cursor);
                }
            } catch (Exception e) {
                throw new DbException(e);
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    public List<DbModel> findAll() throws DbException {
        xyz.tanwb.airship.db.table.TableEntity<?> table = selector.getTable();
        if (!table.tableIsExist()) return null;

        List<DbModel> result = null;

        Cursor cursor = table.getDb().execQuery(this.toString());
        if (cursor != null) {
            try {
                result = new ArrayList<DbModel>();
                while (cursor.moveToNext()) {
                    DbModel entity = CursorUtils.getDbModel(cursor);
                    result.add(entity);
                }
            } catch (Exception e) {
                throw new DbException(e);
            } finally {
                cursor.close();
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(DBConstants.SELECT);
        if (columnExpressions != null && columnExpressions.length > 0) {
            for (String columnExpression : columnExpressions) {
                result.append(columnExpression);
                result.append(DBConstants.COMMA);
            }
            result.deleteCharAt(result.length() - 1);
        } else {
            if (!TextUtils.isEmpty(groupByColumnName)) {
                result.append(groupByColumnName);
            } else {
                result.append(DBConstants.START);
            }
        }
        result.append(DBConstants.FROM).append(DBConstants.DOUBLE_QUOTES).append(selector.getTable().getName()).append(DBConstants.DOUBLE_QUOTES);
        WhereBuilder whereBuilder = selector.getWhereBuilder();
        if (whereBuilder != null && whereBuilder.getWhereItemSize() > 0) {
            result.append(DBConstants.WHERE).append(whereBuilder.toString());
        }
        if (!TextUtils.isEmpty(groupByColumnName)) {
            result.append(DBConstants.GROUP_BY).append(DBConstants.DOUBLE_QUOTES).append(groupByColumnName).append(DBConstants.DOUBLE_QUOTES);
            if (having != null && having.getWhereItemSize() > 0) {
                result.append(DBConstants.HAVING).append(having.toString());
            }
        }
        List<Selector.OrderBy> orderByList = selector.getOrderByList();
        if (orderByList != null && orderByList.size() > 0) {
            for (int i = 0; i < orderByList.size(); i++) {
                result.append(DBConstants.ORDER_BY).append(orderByList.get(i).toString()).append(DBConstants.COMMA);
            }
            result.deleteCharAt(result.length() - 1);
        }
        if (selector.getLimit() > 0) {
            result.append(DBConstants.LIMIT).append(selector.getLimit());
            result.append(DBConstants.OFFSET).append(selector.getOffset());
        }
        return result.toString();
    }
}
