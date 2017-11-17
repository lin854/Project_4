import java.io.Serializable;

final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;
    private int type; //0 = general; 1 = logout; 2 = DM
    private String message;
    private String username;
    public ChatMessage(int type, String message, String username){
        this.type = type;
        this.message = message;
        this.username = username;
    }
    public int getType(){
        return type;
    }
    public String getMessage(){
        return message;
    }
    public String getRecipient(){return username;}
}
