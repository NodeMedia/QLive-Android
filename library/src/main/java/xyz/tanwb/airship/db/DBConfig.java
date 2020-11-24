package xyz.tanwb.airship.db;

import android.text.TextUtils;

import java.io.File;

import xyz.tanwb.airship.App;

public class DBConfig {

    public static DBConfig dbConfig;

    private File dbDir;
    private String dbName = App.getAppName();
    private int dbVersion = 1;
    private DbUpgradeListener dbUpgradeListener;
    private DbOpenListener dbOpenListener;
    private TableCreateListener tableCreateListener;

    public static synchronized DBConfig init() {
        if (dbConfig == null) {
            dbConfig = new DBConfig();
        }
        return dbConfig;
    }

    public DBConfig setDbDir(File dbDir) {
        this.dbDir = dbDir;
        return this;
    }

    public DBConfig setDbName(String dbName) {
        if (!TextUtils.isEmpty(dbName)) {
            this.dbName = dbName;
        }
        return this;
    }

    public DBConfig setDbVersion(int dbVersion) {
        this.dbVersion = dbVersion;
        return this;
    }

    public DBConfig setDbOpenListener(DbOpenListener dbOpenListener) {
        this.dbOpenListener = dbOpenListener;
        return this;
    }

    public DBConfig setDbUpgradeListener(DbUpgradeListener dbUpgradeListener) {
        this.dbUpgradeListener = dbUpgradeListener;
        return this;
    }

    public DBConfig setTableCreateListener(TableCreateListener tableCreateListener) {
        this.tableCreateListener = tableCreateListener;
        return this;
    }

    public File getDbDir() {
        return dbDir;
    }

    public String getDbName() {
        return dbName;
    }

    public int getDbVersion() {
        return dbVersion;
    }

    public DbOpenListener getDbOpenListener() {
        return dbOpenListener;
    }

    public DbUpgradeListener getDbUpgradeListener() {
        return dbUpgradeListener;
    }

    public TableCreateListener getTableCreateListener() {
        return tableCreateListener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DBConfig daoConfig = (DBConfig) o;

        return dbName.equals(daoConfig.dbName) && (dbDir == null ? daoConfig.dbDir == null : dbDir.equals(daoConfig.dbDir));
    }

    @Override
    public int hashCode() {
        int result = dbName.hashCode();
        result = 31 * result + (dbDir != null ? dbDir.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.valueOf(dbDir) + File.separator + dbName;
    }

    public interface DbOpenListener {
        void onDbOpened(DbManager db);
    }

    public interface DbUpgradeListener {
        void onUpgrade(DbManager db, int oldVersion, int newVersion);
    }

    interface TableCreateListener {
        void onTableCreated(DbManager db, xyz.tanwb.airship.db.table.TableEntity<?> table);
    }
}
