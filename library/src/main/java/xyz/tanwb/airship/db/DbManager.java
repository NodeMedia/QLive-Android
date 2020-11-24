package xyz.tanwb.airship.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import xyz.tanwb.airship.App;
import xyz.tanwb.airship.BaseConstants;
import xyz.tanwb.airship.db.sqlite.SqlInfo;
import xyz.tanwb.airship.db.sqlite.SqlInfoBuilder;
import xyz.tanwb.airship.db.table.ColumnEntity;
import xyz.tanwb.airship.db.table.DbModel;
import xyz.tanwb.airship.db.table.TableEntity;
import xyz.tanwb.airship.rxjava.RxBus;

/**
 * Database API
 */
public final class DbManager implements Closeable {

    private static HashMap<DBConfig, DbManager> daoMap = new HashMap<DBConfig, DbManager>();

    private HashMap<Class<?>, TableEntity<?>> tableMap = new HashMap<Class<?>, TableEntity<?>>();
    private boolean allowTransaction;

    private SQLiteDatabase database;
    private DBConfig daoConfig;

    private DbManager(DBConfig config) {
        if (config == null) {
            throw new NullPointerException(DBConstants.DBCONFIG_ERROR);
        }
        this.daoConfig = config;
        File dbDir = config.getDbDir();
        if (dbDir != null && (dbDir.exists() || dbDir.mkdirs())) {
            File dbFile = new File(dbDir, config.getDbName());
            this.database = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
        } else {
            this.database = App.app().openOrCreateDatabase(config.getDbName(), 0, null);
        }
        // 开启WAL, 对写入加速提升巨大
        this.database.enableWriteAheadLogging();
        if (config.getDbOpenListener() != null) {
            config.getDbOpenListener().onDbOpened(this);
        }
    }

    public static synchronized DbManager getInstance() {
        DBConfig dbConfig = DBConfig.init();
        DbManager dbManager = daoMap.get(dbConfig);
        if (dbManager == null) {
            dbManager = new DbManager(dbConfig);
            daoMap.put(dbConfig, dbManager);
        } else {
            dbManager.daoConfig = dbConfig;
        }

        // update the database if needed
        SQLiteDatabase database = dbManager.database;
        int oldVersion = database.getVersion();
        int newVersion = dbConfig.getDbVersion();
        if (oldVersion != newVersion) {
            if (oldVersion != 0) {
                DBConfig.DbUpgradeListener upgradeListener = dbConfig.getDbUpgradeListener();
                if (upgradeListener != null) {
                    upgradeListener.onUpgrade(dbManager, oldVersion, newVersion);
                } else {
                    try {
                        dbManager.dropDb();
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                }
            }
            database.setVersion(newVersion);
        }
        return dbManager;
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }

    public DBConfig getDaoConfig() {
        return daoConfig;
    }

    public DbManager setAllowTransaction(boolean allowTransaction) {
        this.allowTransaction = allowTransaction;
        return this;
    }

    /**
     * Save data
     */
    public void save(Object entity) throws DbException {
        try {
            beginTransaction();

            if (entity instanceof List) {
                List<?> entities = (List<?>) entity;
                if (entities.isEmpty()) return;
                TableEntity<?> table = this.getTable(entities.get(0).getClass());
                createTableIfNotExist(table);
                for (Object item : entities) {
                    execNonQuery(SqlInfoBuilder.buildInsertSqlInfo(table, item));
                }
            } else {
                TableEntity<?> table = this.getTable(entity.getClass());
                createTableIfNotExist(table);
                execNonQuery(SqlInfoBuilder.buildInsertSqlInfo(table, entity));
            }

            setTransactionSuccessful();
            // EventBus.getDefault().post(new EventBusInfo(entity.getClass().getName()));
            RxBus.getInstance().post(entity.getClass().getName());
        } finally {
            endTransaction();
        }
    }

    /**
     * Save BindingId data
     */
    public boolean saveBindingId(Object entity) throws DbException {
        boolean result = false;
        try {
            beginTransaction();

            if (entity instanceof List) {
                List<?> entities = (List<?>) entity;
                if (entities.isEmpty()) return false;
                TableEntity<?> table = this.getTable(entities.get(0).getClass());
                createTableIfNotExist(table);
                for (Object item : entities) {
                    if (!saveBindingIdWithoutTransaction(table, item)) {
                        throw new DbException(DBConstants.SAVE_ERROR);
                    }
                }
            } else {
                TableEntity<?> table = this.getTable(entity.getClass());
                createTableIfNotExist(table);
                result = saveBindingIdWithoutTransaction(table, entity);
            }

            setTransactionSuccessful();
            // EventBus.getDefault().post(new EventBusInfo(entity.getClass().getName()));
            RxBus.getInstance().post(entity.getClass().getName());
        } finally {
            endTransaction();
        }
        return result;
    }

    /**
     * Save Or Update data
     */
    public void saveOrUpdate(Object entity) throws DbException {
        try {
            beginTransaction();

            if (entity instanceof List) {
                List<?> entities = (List<?>) entity;
                if (entities.isEmpty()) return;

                TableEntity<?> table = this.getTable(entities.get(0).getClass());
                createTableIfNotExist(table);
                for (Object item : entities) {
                    saveOrUpdateWithoutTransaction(table, item);
                }
            } else {
                TableEntity<?> table = this.getTable(entity.getClass());
                createTableIfNotExist(table);
                saveOrUpdateWithoutTransaction(table, entity);
            }

            setTransactionSuccessful();
            // EventBus.getDefault().post(new EventBusInfo(entity.getClass().getName()));
            RxBus.getInstance().post(entity.getClass().getName());
        } finally {
            endTransaction();
        }
    }

    /**
     * Replace data
     */
    public void replace(Object entity) throws DbException {
        try {
            beginTransaction();

            if (entity instanceof List) {
                List<?> entities = (List<?>) entity;
                if (entities.isEmpty()) return;
                TableEntity<?> table = this.getTable(entities.get(0).getClass());
                createTableIfNotExist(table);
                for (Object item : entities) {
                    execNonQuery(SqlInfoBuilder.buildReplaceSqlInfo(table, item));
                }
            } else {
                TableEntity<?> table = this.getTable(entity.getClass());
                createTableIfNotExist(table);
                execNonQuery(SqlInfoBuilder.buildReplaceSqlInfo(table, entity));
            }

            setTransactionSuccessful();
            // EventBus.getDefault().post(new EventBusInfo(entity.getClass().getName()));
            RxBus.getInstance().post(entity.getClass().getName());
        } finally {
            endTransaction();
        }
    }

    private void createTableIfNotExist(TableEntity<?> table) throws DbException {
        if (!table.tableIsExist()) {
            synchronized (table.getClass()) {
                if (!table.tableIsExist()) {
                    SqlInfo sqlInfo = SqlInfoBuilder.buildCreateTableSqlInfo(table);
                    execNonQuery(sqlInfo);
                    String execAfterTableCreated = table.getOnCreated();
                    if (!TextUtils.isEmpty(execAfterTableCreated)) {
                        execNonQuery(execAfterTableCreated);
                    }
                    table.setCheckedDatabase(true);
                }
            }
        }
    }

    /**
     * Delete data based on ID
     */
    public void deleteById(Class<?> entityType, Object idValue) throws DbException {
        TableEntity<?> table = this.getTable(entityType);
        if (!table.tableIsExist()) return;
        try {
            beginTransaction();

            execNonQuery(SqlInfoBuilder.buildDeleteSqlInfoById(table, idValue));

            setTransactionSuccessful();
            // EventBus.getDefault().post(new EventBusInfo(entityType.getName()));
            RxBus.getInstance().post(entityType.getName());
        } finally {
            endTransaction();
        }
    }

    /**
     * Delete data based on entity
     */
    public void delete(Object entity) throws DbException {
        try {
            beginTransaction();

            if (entity instanceof List) {
                List<?> entities = (List<?>) entity;
                if (entities.isEmpty()) return;
                TableEntity<?> table = this.getTable(entities.get(0).getClass());
                if (!table.tableIsExist()) return;
                for (Object item : entities) {
                    execNonQuery(SqlInfoBuilder.buildDeleteSqlInfo(table, item));
                }
            } else {
                TableEntity<?> table = this.getTable(entity.getClass());
                if (!table.tableIsExist()) return;
                execNonQuery(SqlInfoBuilder.buildDeleteSqlInfo(table, entity));
            }

            setTransactionSuccessful();
            // EventBus.getDefault().post(new EventBusInfo(entity.getClass().getName()));
            RxBus.getInstance().post(entity.getClass().getName());
        } finally {
            endTransaction();
        }
    }

    /**
     * Delete data based on class
     */
    public void delete(Class<?> entityType) throws DbException {
        delete(entityType, null);
    }

    /**
     * Delete data according to the condition
     */
    public int delete(Class<?> entityType, xyz.tanwb.airship.db.sqlite.WhereBuilder whereBuilder) throws DbException {
        TableEntity<?> table = this.getTable(entityType);
        if (!table.tableIsExist()) return 0;
        int result = 0;
        try {
            beginTransaction();

            result = executeUpdateDelete(SqlInfoBuilder.buildDeleteSqlInfo(table, whereBuilder));

            setTransactionSuccessful();
            // EventBus.getDefault().post(new EventBusInfo(entityType.getName()));
            RxBus.getInstance().post(entityType.getName());
        } finally {
            endTransaction();
        }
        return result;
    }

    /**
     * Data to modify the column name specified in the table
     */
    public void update(Object entity, String... updateColumnNames) throws DbException {
        try {
            beginTransaction();

            if (entity instanceof List) {
                List<?> entities = (List<?>) entity;
                if (entities.isEmpty()) return;
                TableEntity<?> table = this.getTable(entities.get(0).getClass());
                if (!table.tableIsExist()) return;
                for (Object item : entities) {
                    execNonQuery(SqlInfoBuilder.buildUpdateSqlInfo(table, item, updateColumnNames));
                }
            } else {
                TableEntity<?> table = this.getTable(entity.getClass());
                if (!table.tableIsExist()) return;
                execNonQuery(SqlInfoBuilder.buildUpdateSqlInfo(table, entity, updateColumnNames));
            }

            setTransactionSuccessful();
            // EventBus.getDefault().post(new EventBusInfo(entity.getClass().getName()));
            RxBus.getInstance().post(entity.getClass().getName());
        } finally {
            endTransaction();
        }
    }

    /**
     * Data for the specified column name in the condition modification table
     */
    public int update(Class<?> entityType, xyz.tanwb.airship.db.sqlite.WhereBuilder whereBuilder, KeyValue... nameValuePairs) throws DbException {
        TableEntity<?> table = this.getTable(entityType);
        if (!table.tableIsExist()) return 0;

        int result = 0;
        try {
            beginTransaction();

            result = executeUpdateDelete(SqlInfoBuilder.buildUpdateSqlInfo(table, whereBuilder, nameValuePairs));

            setTransactionSuccessful();
            // EventBus.getDefault().post(new EventBusInfo(entityType.getName()));
            RxBus.getInstance().post(entityType.getName());
        } finally {
            endTransaction();
        }

        return result;
    }

    /**
     * Search data according to ID
     */
    public <T> T findById(Class<T> entityType, Object idValue) throws DbException {
        TableEntity<T> table = this.getTable(entityType);
        if (!table.tableIsExist()) return null;

        Selector selector = Selector.from(table).where(table.getId().getName(), BaseConstants.EQUAL, idValue);

        String sql = selector.limit(1).toString();
        Cursor cursor = execQuery(sql);
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    return CursorUtils.getEntity(table, cursor);
                }
            } catch (Exception e) {
                throw new DbException(e);
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * Lookup table first data
     */
    public <T> T findFirst(Class<T> entityType) throws DbException {
        return this.selector(entityType).findFirst();
    }

    /**
     * Lookup table all data
     */
    public <T> List<T> findAll(Class<T> entityType) throws DbException {
        return this.selector(entityType).findAll();
    }

    /**
     * selector table data
     */
    public <T> Selector<T> selector(Class<T> entityType) throws DbException {
        return Selector.from(this.getTable(entityType));
    }

    /**
     * Lookup table first data
     */
    public DbModel findDbModelFirst(SqlInfo sqlInfo) throws DbException {
        Cursor cursor = execQuery(sqlInfo);
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


    /**
     * Lookup table all data
     */
    public List<DbModel> findDbModelAll(SqlInfo sqlInfo) throws DbException {
        List<DbModel> dbModelList = new ArrayList<DbModel>();

        Cursor cursor = execQuery(sqlInfo);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    dbModelList.add(CursorUtils.getDbModel(cursor));
                }
            } catch (Exception e) {
                throw new DbException(e);
            } finally {
                cursor.close();
            }
        }
        return dbModelList;
    }

    private void saveOrUpdateWithoutTransaction(TableEntity<?> table, Object entity) throws DbException {
        ColumnEntity id = table.getId();
        if (id.isAutoId()) {
            if (id.getColumnValue(entity) != null) {
                execNonQuery(SqlInfoBuilder.buildUpdateSqlInfo(table, entity));
            } else {
                saveBindingIdWithoutTransaction(table, entity);
            }
        } else {
            execNonQuery(SqlInfoBuilder.buildReplaceSqlInfo(table, entity));
        }
    }

    private boolean saveBindingIdWithoutTransaction(TableEntity<?> table, Object entity) throws DbException {
        ColumnEntity id = table.getId();
        if (id.isAutoId()) {
            execNonQuery(SqlInfoBuilder.buildInsertSqlInfo(table, entity));
            long idValue = getLastAutoIncrementId(table.getName());
            if (idValue == -1) {
                return false;
            }
            id.setAutoIdValue(entity, idValue);
            return true;
        } else {
            execNonQuery(SqlInfoBuilder.buildInsertSqlInfo(table, entity));
            return true;
        }
    }

    private long getLastAutoIncrementId(String tableName) throws DbException {
        long id = -1;
        Cursor cursor = execQuery(String.format(DBConstants.SQL_LAST_INCREMENTID, tableName));
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    id = cursor.getLong(0);
                }
            } catch (Exception e) {
                throw new DbException(e);
            } finally {
                cursor.close();
            }
        }
        return id;
    }

    private void beginTransaction() {
        if (allowTransaction) {
            getDatabase().beginTransaction();
        }
    }

    private void setTransactionSuccessful() {
        if (allowTransaction) {
            getDatabase().setTransactionSuccessful();
        }
    }

    private void endTransaction() {
        if (allowTransaction) {
            getDatabase().endTransaction();
        }
    }

    /**
     * Get table info
     */
    public <T> TableEntity<T> getTable(Class<T> entityType) throws DbException {
        synchronized (tableMap) {
            TableEntity<T> table = (TableEntity<T>) tableMap.get(entityType);
            if (table == null) {
                try {
                    table = new TableEntity<T>(this, entityType);
                } catch (Exception ex) {
                    throw new DbException(ex);
                }
                tableMap.put(entityType, table);
            }

            return table;
        }
    }

    /**
     * Delete table
     */
    public void dropTable(Class<?> entityType) throws DbException {
        TableEntity<?> table = this.getTable(entityType);
        if (!table.tableIsExist()) return;
        execNonQuery(DBConstants.TABLE_DROP + DBConstants.DOUBLE_QUOTES + table.getName() + DBConstants.DOUBLE_QUOTES);
        table.setCheckedDatabase(false);
        tableMap.remove(entityType);
    }

    /**
     * Add a column to a table
     */
    public void addColumn(Class<?> entityType, String column) throws DbException {
        TableEntity<?> table = this.getTable(entityType);
        ColumnEntity col = table.getColumnMap().get(column);
        if (col != null) {
            StringBuilder builder = new StringBuilder();
            builder.append(DBConstants.TABLE_ALTER).append(DBConstants.DOUBLE_QUOTES).append(table.getName()).append(DBConstants.DOUBLE_QUOTES).
                    append(DBConstants.COLUMN).append(DBConstants.DOUBLE_QUOTES).append(col.getName()).append(DBConstants.DOUBLE_QUOTES).
                    append(DBConstants.SPACE).append(col.getColumnDbType()).append(DBConstants.SPACE).append(col.getProperty());
            execNonQuery(builder.toString());
        }
    }

    /**
     * Delete database
     */
    public void dropDb() throws DbException {
        Cursor cursor = execQuery(DBConstants.SQL_QUERY_TABLE);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String tableName = cursor.getString(0);
                    execNonQuery(DBConstants.TABLE_DROP + tableName);
                }

                synchronized (tableMap) {
                    for (TableEntity<?> table : tableMap.values()) {
                        table.setCheckedDatabase(false);
                    }
                    tableMap.clear();
                }
            } catch (Exception e) {
                throw new DbException(e);
            } finally {
                cursor.close();
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (daoMap.containsKey(getDaoConfig())) {
            daoMap.remove(getDaoConfig());
            getDatabase().close();
        }
    }

    public int executeUpdateDelete(SqlInfo sqlInfo) throws DbException {
        SQLiteStatement statement = null;
        try {
            statement = sqlInfo.buildStatement(getDatabase());
            return statement.executeUpdateDelete();
        } catch (Exception e) {
            throw new DbException(e);
        } finally {
            if (statement != null) {
                statement.releaseReference();
            }
        }
    }

    public int executeUpdateDelete(String sql) throws DbException {
        SQLiteStatement statement = null;
        try {
            statement = getDatabase().compileStatement(sql);
            return statement.executeUpdateDelete();
        } catch (Exception e) {
            throw new DbException(e);
        } finally {
            if (statement != null) {
                statement.releaseReference();
            }
        }
    }

    public void execNonQuery(SqlInfo sqlInfo) throws DbException {
        SQLiteStatement statement = null;
        try {
            statement = sqlInfo.buildStatement(getDatabase());
            statement.execute();
        } catch (Exception e) {
            throw new DbException(e);
        } finally {
            if (statement != null) {
                statement.releaseReference();
            }
        }
    }

    public void execNonQuery(String sql) throws DbException {
        try {
            getDatabase().execSQL(sql);
        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    public Cursor execQuery(SqlInfo sqlInfo) throws DbException {
        try {
            return getDatabase().rawQuery(sqlInfo.getSql(), sqlInfo.getBindArgsAsStrArray());
        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    public Cursor execQuery(String sql) throws DbException {
        try {
            return getDatabase().rawQuery(sql, null);
        } catch (Exception e) {
            throw new DbException(e);
        }
    }

    public int getLastInsertRowid(Class<?> entityType) throws DbException {
        TableEntity<?> table = this.getTable(entityType);
        Cursor cursor = execQuery(String.format(DBConstants.SQL_LAST_INSERTID, table.getName()));
        int id = -1;
        if (cursor != null) {
            try {
                if (cursor.moveToNext()) {
                    id = cursor.getInt(0);
                }
            } catch (Exception e) {
                throw new DbException(e);
            } finally {
                cursor.close();
            }
        }
        return id;
    }

}
