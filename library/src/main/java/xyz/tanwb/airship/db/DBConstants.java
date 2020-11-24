package xyz.tanwb.airship.db;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import xyz.tanwb.airship.BaseConstants;

/**
 * 数据库相关静态值
 */
public class DBConstants extends BaseConstants {

    public static final String INTEGER = "INTEGER";
    public static final String REAL = "REAL";
    public static final String TEXT = "TEXT";
    public static final String BLOB = "BLOB";

    @StringDef({INTEGER, REAL, TEXT, BLOB})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ColumnDbType {
    }

    public static final String MRTHOD_SET = "set";
    public static final String MRTHOD_GET = "get";
    public static final String MRTHOD_IS = "is";

    public static final String SQL_QUERY_TABLE = "SELECT name FROM sqlite_master WHERE type='table' AND name<>'sqlite_sequence'";
    public static final String SQL_QUERY_COUNT = "SELECT COUNT(*) AS c FROM sqlite_master WHERE type = 'table' AND name = '%s'";
    public static final String SQL_LAST_INCREMENTID = "SELECT seq FROM sqlite_sequence WHERE name = '%s' LIMIT 1";
    public static final String SQL_LAST_INSERTID = "SELECT last_insert_rowid() from %s";

    public static final String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS ";
    public static final String TABLE_AUTOINCREMENT = " INTEGER PRIMARY KEY AUTOINCREMENT, ";
    public static final String TABLE_PRIMARY = " PRIMARY KEY, ";
    public static final String TABLE_DROP = "DROP TABLE ";
    public static final String TABLE_ALTER = "ALTER TABLE ";
    public static final String COLUMN = " ADD COLUMN ";

    public static final String INSERT = "INSERT";
    public static final String REPLACE = "REPLACE";
    public static final String DELETE = "DELETE";
    public static final String UPDATE = "UPDATE";
    public static final String SELECT = "SELECT";
    public static final String INTO = " INTO ";
    public static final String FROM = " FROM ";
    public static final String VALUES = " VALUES ";
    public static final String WHERE = " WHERE ";
    public static final String SET = " SET ";
    public static final String AND_COND = " AND ";
    public static final String OR_COND = "OR";
    public static final String IN_COND = "IN";
    public static final String BETWEEN_COND = "BETWEEN";
    public static final String LIKE_COND = "LIKE";
    public static final String GROUP_BY = " GROUP BY ";
    public static final String HAVING = " HAVING ";
    public static final String ORDER_BY = " ORDER BY ";
    public static final String LIMIT = " LIMIT ";
    public static final String OFFSET = " OFFSET ";
    public static final String DESC = "DESC";
    public static final String ASC = "ASC";
    public static final String START = " * ";
    public static final String SINGLE = "'";
    public static final String DOUBLE = "''";
    public static final String ISNULL = " IS NULL";
    public static final String ISNOTNULL = " IS NOT NULL";
    public static final String DATANULL = " NULL";

    public static final String ENTITY_ERROR = "this entity[%s]'s id value is null";
    public static final String VALUE_ARRAY = "value must be an Array or an Iterable.";
    public static final String VALUE_TWOITEMS = "value must have tow items.";
    public static final String DBCONFIG_ERROR = "daoConfig may not be null";
    public static final String SAVE_ERROR = "saveBindingId error, transaction will not commit!";

}
