package me.jfro.minecraft;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: jerome
 * Date: 8/28/11
 * Time: 8:55 PM
 * exception for data provider classes
 */
public class DataProviderException extends IOException {
    public DataProviderException() {
        super();
    }

    public DataProviderException(String message) {
        super(message);
    }

    public DataProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataProviderException(Throwable cause) {
        super(cause);
    }
}
