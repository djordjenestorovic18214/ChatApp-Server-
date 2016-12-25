import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {

	static LinkedList<ServerThread> clients = new LinkedList<ServerThread>();
	static LinkedList<String> offlineClients = new LinkedList<String>();
	private static ServerSocket serverSocket;

	public static void main(String[] args) {
		// port
		int port = 23789;

		if (args.length > 0)
			port = Integer.parseInt(args[0]);

		// tcp
		Socket clientSocket = null;

		try {
			serverSocket = new ServerSocket(port);
			DatagramSocket datagramSocket = new DatagramSocket(port);
			
			while (true) {
				clientSocket = serverSocket.accept();

				clients.add(new ServerThread(clientSocket, datagramSocket, clients, offlineClients));
				clients.getLast().start();
			}

		} catch (IOException e) {
			System.err.println(e);
		}
	}
}
