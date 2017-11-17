import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

final class ChatServer {
    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;


    private ChatServer(int port) {
        this.port = port;
    }

    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */
    private void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while(true) {
                Socket socket = serverSocket.accept();
                Runnable r = new ClientThread(socket, uniqueId++);
                Thread t = new Thread(r);
                clients.add((ClientThread) r);
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private synchronized void broadcast(String message){
        message = getDate()+" "+message;
        for(int i = 0; i<clients.size(); i++){
            ClientThread cli = clients.get(i);
            cli.writeMessage(message);
        }
        System.out.println(message);
    }
    private synchronized void directMessage(String message, String username){
        message = getDate()+" "+message;
        boolean found = false;
        for(int i = 0; i<clients.size(); i++){
            ClientThread cli = clients.get(i);
            if(cli.username.equals(username)) {
                found = true;
                cli.writeMessage(message);
            }
        }
        if(found)
            System.out.println(message);
        else
            System.out.println("Person not found.");
    }
    private String getDate(){
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(date);
    }

    private synchronized void remove(int id){
        clients.remove(id);
    }
    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        int portNumber = 1500;
        if(args.length>0)
            portNumber=Integer.parseInt(args[0]);
        ChatServer server = new ChatServer(portNumber);
        server.start();
    }

    /*
     * This is a private class inside of the ChatServer
     * A new thread will be created to run this every time a new client connects.
     */
    private final class ClientThread implements Runnable {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        ChatMessage cm;

        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        private void close() throws IOException{
            sInput.close();
            sOutput.close();
            socket.close();
        }
        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {
            // Read the username sent to you by client
            System.out.println(getDate()+ " "+ username+" just connected.");
            boolean x = true;
            while(x) {
                try {
                    cm = (ChatMessage) sInput.readObject();
                    if (cm.getType() == 1) {
                        x = false;
                        if(clients.size()>=id+1) {
                            broadcast(clients.get(id).username+" disconnected with a LOGOUT " +
                                    "message.");
                            remove(id);
                        }
                    }
                    else if(cm.getType() == 0){
                        broadcast(username+": "+cm.getMessage());
                    }
                    else if(cm.getType() == 2){
                        directMessage(username+"->"+cm.getRecipient()+": "+cm.getMessage(), cm.getRecipient());
                    }
                } catch (IOException | ClassNotFoundException e) {
                    if(clients.size()>=id+1) {
                        broadcast(username+" has disconnected.");
                        remove(id);
                    }
                    x = false;
                }
            }
        }
        private boolean writeMessage(String msg){
            try {
                sOutput.writeObject(msg);
            }
            catch(IOException e){
                return false;
            }
            return !socket.isConnected();
        }
    }

}
