import java.io.Serializable;

public class Message implements Serializable{
    public static final long serialVersionUID = 1L;
    public static String message="";

    public Message(String message) {
        this.message = message;
    }

    public static String getMessage() {
        return message;
    }
}
