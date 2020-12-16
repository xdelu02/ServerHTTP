package DeLuca.serverHTTP;

import java.net.ServerSocket;
import java.net.Socket;


public class ServerHttp {
	
	private ServerSocket serverSocket;
	private Socket socket;
	private final int port = 3000;

    public static void main( String[] args ) throws Exception {
    	new ServerHttp().start();
    }
    
    private void start() {
    	try {
    		System.out.println("Server sta ascoltando su porta " +port +"...\n");
			serverSocket = new ServerSocket(port);
			
			while(true) {
				socket = serverSocket.accept();
				System.out.println("Nuova richiesta da: "+ socket);
				ClientHandler ch = new ClientHandler(socket);
				Thread t = new Thread(ch);
				t.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
