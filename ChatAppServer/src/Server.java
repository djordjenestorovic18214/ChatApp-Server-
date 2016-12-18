import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {

	static LinkedList<ServerThread> clients = new LinkedList<ServerThread>();

	public static void main(String[] args) {
		int port = 2003;

		if (args.length > 0)
			port = Integer.parseInt(args[0]);

		Socket clientSocket = null;

		try {
			ServerSocket serverSocket = new ServerSocket(port);

			while (true) {
				clientSocket = serverSocket.accept();

				clients.add(new ServerThread(clientSocket, clients));
				clients.getLast().start();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
