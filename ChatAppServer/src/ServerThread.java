import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.LinkedList;

public class ServerThread extends Thread {

	BufferedReader inStreamFromClient = null;
	PrintStream outStreamToClient = null;
	Socket communicationSocket = null;
	LinkedList<ServerThread> clients;

	public ServerThread(Socket socket, LinkedList<ServerThread> clients) {
		communicationSocket = socket;
		this.clients = clients;
	}

	public void run() {
		String text;
		String name;
		String gender;

		try {
			inStreamFromClient = new BufferedReader(new InputStreamReader(communicationSocket.getInputStream()));
			outStreamToClient = new PrintStream(communicationSocket.getOutputStream());

			outStreamToClient.println("Type your name: ");
			name = inStreamFromClient.readLine();

			while (true) {
				outStreamToClient.println("Type your gender(M/F): ");
				if (inStreamFromClient.readLine() == "M" || inStreamFromClient.readLine() == "F"
						|| inStreamFromClient.readLine() == "m" || inStreamFromClient.readLine() == "f") {
					gender = inStreamFromClient.readLine();
					break;
				}

				outStreamToClient.println("Welcome " + name
						+ "./nType the message you want to send and then choose who will receive your message./nIf you want to leave just type '/exit'.");
			}

			while (true) {
				outStreamToClient.println("Your message: ");

				text = inStreamFromClient.readLine();

				if (text.startsWith("/exit"))
					break;

				outStreamToClient.println("Type ordinal number of receiver(s): ");

			}

			outStreamToClient.println("Goodbye " + name + ", hope to see you soon!");

			communicationSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
