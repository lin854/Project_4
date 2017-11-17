import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;

final class ChatClient {
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private final String server;
    private final String username;
    private final int port;

    private ChatClient(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }

    /*
     * This starts the Chat Client
     */
    private boolean start() {
        // Create a socket
        try {
            socket = new Socket(server, port);
        } catch(ConnectException e){
            System.out.println("Server not found.");
            return false;
        }catch (IOException e) {
            e.printStackTrace();
        }

        // Create your input and output streams
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // This thread will listen from the server for incoming messages
        Runnable r = new ListenFromServer();
        Thread t = new Thread(r);
        t.start();

        // After starting, send the clients username to the server.
        try {
            sOutput.writeObject(username);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


    /*
     * This method is used to send a ChatMessage Objects to the server
     */
    private void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     * To start the Client use one of the following command
     * > java ChatClient
     * > java ChatClient username
     * > java ChatClient username portNumber
     * > java ChatClient username portNumber serverAddress
     *
     * If the portNumber is not specified 1500 should be used
     * If the serverAddress is not specified "localHost" should be used
     * If the username is not specified "Anonymous" should be used
     */
    public static void main(String[] args) {
        // Get proper arguments and override defaults
        String username = args[0];
        int portNumber = 1500;
        String serverAddress = "localHost";
        if(args.length>1) {
            portNumber = Integer.parseInt(args[1]);
            if(args.length>2)
                serverAddress = args[2];
        }

        // Create your client and start it
        ChatClient client = new ChatClient(serverAddress, portNumber, username);
        boolean x = client.start();
        Scanner sc = new Scanner(System.in);
        while(x) {
            System.out.print("> ");
            String message = sc.nextLine();
            int type = 0;
            String user = "";
            if(message.toLowerCase().equals("/logout")) {
                type = 1;
            }
            else if(message.startsWith("/msg ")){
                message = message.replaceFirst("/msg ", "");
                user = message.substring(0, message.indexOf(" "));
                message = message.substring(message.indexOf(" ")+1);
                type = 2;
                if(user.equals(username)||user.equals("")) {
                    user = "";
                    type = 0;
                    System.out.println("When you try to message yourself, you will message to the whole " +
                            "server like normal");
                }
            }
            ChatMessage msg = new ChatMessage(type, message, user);
            client.sendMessage(msg);
            if(type == 1){
                x=false;
                client.close();
            }
        }
    }

    /*
     * This is a private class inside of the ChatClient
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     */
    private final class ListenFromServer implements Runnable {
        public void run() {
            try {
                while(!socket.isClosed()) {
                    String msg = (String) sInput.readObject();
                    System.out.println(msg);
                    System.out.print("> ");
                }
            } catch (IOException | ClassNotFoundException e) {
                close();
            }
        }
    }
    private boolean close(){
        try {
            sInput.close();
            sOutput.close();
            socket.close();
            return true;
        }
        catch(Exception e){
            return false;
        }
    }
}
