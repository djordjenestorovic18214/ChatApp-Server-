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
	String name;
	char gender;
	boolean nameExist = false;

	public ServerThread(Socket socket, LinkedList<ServerThread> clients) {
		communicationSocket = socket;
		this.clients = clients;
	}

	public String getClientName() {
		return this.name;
	}

	public boolean setClientName(String testName) {
		if (testName == null || testName.isEmpty())
			return false;

		for (ServerThread client : clients) {
			if (client != this && client.getClientName().equals(testName)) {
				nameExist = true;
				return false;
			}
		}

		name = testName;
		return true;
	}

	public void run() {
		String text;
		String g = null;

		try {
			inStreamFromClient = new BufferedReader(new InputStreamReader(communicationSocket.getInputStream()));
			outStreamToClient = new PrintStream(communicationSocket.getOutputStream());

			outStreamToClient.println("Type your name: ");
			while (!setClientName(inStreamFromClient.readLine())) {
				if (nameExist)
					outStreamToClient.println("Type another name(*this one already exist*): ");
				else
					outStreamToClient.println("Type your name(*name is required*): ");
			}

			outStreamToClient.println("Type your gender(M/F): ");
			g = inStreamFromClient.readLine();

			while (true) {
				if (g.startsWith("m") || g.startsWith("M")) {
					gender = 'M';
					break;
				} else if (g.startsWith("f") || g.startsWith("F")) {
					gender = 'F';
					break;
				} else {
					outStreamToClient.println("Type your gender in valid format(M/F): ");
					g = inStreamFromClient.readLine();
				}
			}

			outStreamToClient.println("Welcome " + name
					+ ".\nType the message you want to send and then choose who you want to receive your message."
					+ "\nIf you want to leave just type '/exit'.");

			while (true) {
				outStreamToClient.println("Your message: ");

				text = inStreamFromClient.readLine();

				if (text.startsWith("/exit")) {
					outStreamToClient.println("***Goodbye " + name + ", hope to see you soon!***");
					break;
				}
				if (clients.size() > 1) {
					outStreamToClient.println("Online friends:");

					for (int i = 1; i < clients.size(); i++) {
						outStreamToClient.println(i + ". " + clients.get(i - 1).getClientName());
					}
				} else {
					outStreamToClient.println("You are the only person online.");
				}
			}

			communicationSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		clients.remove(this);

	}
}
