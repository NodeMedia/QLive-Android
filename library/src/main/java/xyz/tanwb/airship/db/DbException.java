package xyz.tanwb.airship.db;

public class DbException extends Exception {
    private static final long serialVersionUID = 1L;

    public DbException() {
    }

    public DbException(String detailMessage) {
        super(detailMessage);
    }

    public DbException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        this.initCause(throwable);
    }

    public DbException(Throwable throwable) {
        super(throwable);
        this.initCause(throwable);
    }

}
