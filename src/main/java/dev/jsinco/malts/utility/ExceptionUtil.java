package dev.jsinco.malts.utility;

import java.sql.SQLException;

public final class ExceptionUtil {

    @FunctionalInterface
    public interface ThrowingSQLException {
        void run() throws SQLException;
    }

    @FunctionalInterface
    public interface ThrowingSQLExceptionWithReturn<T> {
        T run() throws SQLException;
    }

    public static void runWithSQLExceptionHandling(ThrowingSQLException runnable) {
        try {
            runnable.run();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (Throwable t) {
            throw new RuntimeException("An unexpected error occurred", t);
        }
    }

    public static <U> U runWithSQLExceptionHandling(ThrowingSQLExceptionWithReturn<U> supplier) {
        try {
            return supplier.run();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("An unexpected error occurred", t);
        }
    }
}