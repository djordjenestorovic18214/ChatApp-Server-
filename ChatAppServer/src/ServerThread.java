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

	public void run() {
		String message;
		String g = null;

		try {
			inStreamFromClient = new BufferedReader(new InputStreamReader(communicationSocket.getInputStream()));
			outStreamToClient = new PrintStream(communicationSocket.getOutputStream());

			// set name
			outStreamToClient.println("Type your name: ");
			while (!setClientName(inStreamFromClient.readLine().trim())) {
				if (nameExist) {
					outStreamToClient.println("Type another name(*this one already exist*): ");
					nameExist = false;
				}
				else
					outStreamToClient.println("Type your name(*name is required*): ");
			}

			outStreamToClient.println("Type your gender(M/F): ");
			g = inStreamFromClient.readLine();
			// set gender
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
					+ ".\nType the message you want to send and then choose to whom you want to send it."
					+ "\nIf you want to leave just type '/exit'.");

			while (true) {
				outStreamToClient.println("Your message: ");

				message = inStreamFromClient.readLine();

				// exit
				if (message.startsWith("/exit")) {
					outStreamToClient.println("***Goodbye " + name + ", hope to see you soon!***");
					break;
				}

				// write online friends
				if (clients.size() > 1) {
					boolean genderExist = false;
					outStreamToClient.println("Online friends:");

					for (int i = 0; i < clients.size(); i++) {
						if (this.gender != clients.get(i).gender) {
							outStreamToClient.println("•" + clients.get(i).getClientName());
							genderExist = true;
						}
					}
					if(genderExist) {
						outStreamToClient.println("Type names of receivers with only one comma separating them:");
					} else if(!genderExist && this.gender == 'M') {
						outStreamToClient.println("There are no female persons available for chat...");
						continue;
					} else {
						outStreamToClient.println("There are no male persons available for chat...");
						continue;
					} 
				}else {
					outStreamToClient.println("You are the only person online...");
					continue;
				}

				//send message
				boolean nadjen = false;
				while(!nadjen) {
				String rNames = inStreamFromClient.readLine();
				LinkedList<String> receivers = whoisReceiveing(rNames);

				for (String r : receivers) {
					int i = 0;
					do {
						if(r.equalsIgnoreCase(clients.get(i).getClientName())) {
							clients.get(i).outStreamToClient.println("<" + this.getClientName() + "> " + message);
							nadjen = true;
							break;
						}
						i++;
					} while(i < clients.size());
				}
				if(!nadjen)
					outStreamToClient.println("Person not found! Type name of receivers with only one comma separating them:");
				}
			}

			communicationSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		clients.remove(this);
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

	private LinkedList<String> whoisReceiveing(String rNames) {
		String[] receivers = rNames.split(",");

		LinkedList<String> listOfReceivers = new LinkedList<String>();

		for (int i = 0; i < receivers.length; i++) {
			listOfReceivers.add(receivers[i]);
		}
		return listOfReceivers;
	}
}
