package xyz.tanwb.airship.db.sqlite;

public enum ColumnDbType {

    INTEGER("INTEGER"), REAL("REAL"), TEXT("TEXT"), BLOB("BLOB");

    private String value;

    ColumnDbType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
