package org.unitedata.consumer;

/**
 * @author: hushi
 * @create: 2019/03/13
 */
public class TaskToolException extends RuntimeException{
    public TaskToolException() {
    }

    public TaskToolException(String message) {
        super(message);
    }

    public TaskToolException(Throwable cause) {
        super(cause);
    }

    public TaskToolException(String message, Throwable cause) {
        super(message, cause);
    }
}
