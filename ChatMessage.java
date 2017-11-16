import java.io.Serializable;

final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;
    private int type;
    private String message;
    public ChatMessage(int type, String message){
        this.type = type;
        this.message = message;
    }
    public int getType(){
        return type;
    }
    public String getMessage(){
        return message;
    }
}
