import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {

	static LinkedList<ServerThread> clients = new LinkedList<ServerThread>();
	private static ServerSocket serverSocket;

	public static void main(String[] args) {
		int port = 23789;

		if (args.length > 0)
			port = Integer.parseInt(args[0]);

		Socket clientSocket = null;

		try {
			serverSocket = new ServerSocket(port);

			while (true) {
				clientSocket = serverSocket.accept();

				clients.add(new ServerThread(clientSocket, clients));
				clients.getLast().start();
				break;
			}

		} catch (IOException e) {
			System.err.println("greska");
		}
	}
}
