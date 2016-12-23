import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.LinkedList;

public class ServerThread extends Thread {

	BufferedReader inStreamFromClient = null;
	PrintStream outStreamToClient = null;
	Socket communicationSocket = null;
	LinkedList<ServerThread> clients;
	LinkedList<String> offlineClients;
	String name;
	char gender;
	SimpleDateFormat sdf = new SimpleDateFormat("'Date/Time:' dd.MM.yyyy'/'HH:mm:ss'|'");
	boolean nameExist = false;
	static PrintWriter out;
	boolean postojao;

	public ServerThread(Socket socket, LinkedList<ServerThread> clients, LinkedList<String> offlineClients) {
		communicationSocket = socket;
		this.clients = clients;
		this.offlineClients = offlineClients;
	}

	public void run() {
		String message;
		String g = null;
		
		try {
			inStreamFromClient = new BufferedReader(new InputStreamReader(communicationSocket.getInputStream()));
			outStreamToClient = new PrintStream(communicationSocket.getOutputStream());
			
			//text file report
			File file = new File("chat_history.txt");
			if (!file.exists()) 
				file.createNewFile();
			out = new PrintWriter(new BufferedWriter(new FileWriter("chat_history.txt", true)));

			// set name
			outStreamToClient.println("•Type your name: ");
			while (!setClientName(inStreamFromClient.readLine().trim())) {
				if (nameExist) {
					outStreamToClient.println("•Type another name(*this one already exist*): ");
					nameExist = false;
				}
				else
					outStreamToClient.println("•Type your name(*name is required*): ");
			}
			outStreamToClient.println("•Type your gender(M/F): ");
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
					outStreamToClient.println("•Type your gender in valid format(M/F): ");
					g = inStreamFromClient.readLine();
				}
			}
			
			//welcome
			outStreamToClient.println("•••Welcome " + name
					+ ".•••\n•••Type the message you want to send and then choose to whom you want to send it.•••"
					+ "\n•••If you want to leave just type '/exit'.•••");

			//start of messaging process
			while (true) {
				outStreamToClient.println("•Your message: ");
				message = inStreamFromClient.readLine();

				// exit demand
				if (message.startsWith("/exit")) {
					offlineClients.add(this.getClientName());
					outStreamToClient.println("•••Goodbye " + name + ", hope to see you soon!•••");
					break;
				}

				// list of online friends
				if (clients.size() > 1) {
					boolean genderExist = false;
					outStreamToClient.println("•Online friends:");

					for (int i = 0; i < clients.size(); i++) {
						if (this.gender != clients.get(i).gender) {
							outStreamToClient.println("  -" + clients.get(i).getClientName());
							genderExist = true;
						}
					}
					if(genderExist) {
						outStreamToClient.println("•••Type names of receivers with only one comma separating them•••");
					} else if(!genderExist && this.gender == 'M') {
						outStreamToClient.println("•••There are no female persons available for chat•••");
						continue;
					} else {
						outStreamToClient.println("•••There are no male persons available for chat•••");
						continue;
					} 
				}else {
					outStreamToClient.println("•••You are the only person online•••");
					continue;
				}

				//send message
				String rNames = inStreamFromClient.readLine();
				LinkedList<String> receivers = whoisReceiveing(rNames);

				for (String r : receivers) {
					boolean nadjen = false;
					int i = 0;
					do {
						if(r.equalsIgnoreCase(clients.get(i).getClientName())) {
							clients.get(i).outStreamToClient.println("<" + this.getClientName() + "> " + message);
							nadjen = true;
							//writeToHistory
							out.println(sdf.format((new GregorianCalendar()).getTime()) + "•|From:<" + this.getClientName() + "> To:<" 
							+ clients.get(i).getClientName() + "> Message: “"+ message +"”");
							out.flush();
							//deliveryReport
							outStreamToClient.println("•••Message successfully delivered to <" + clients.get(i).getClientName() + ">•••");
							break;
						}
						i++;
					} while(i < clients.size());
				if(!nadjen) {
					postojao = false;
					for(String oc : offlineClients) {
						if(oc.equalsIgnoreCase(r)) {
							outStreamToClient.println("•••User '" + r + "' is now offline•••");
							postojao = true;
							//writeToHistory
							out.println(sdf.format((new GregorianCalendar()).getTime()) + "×|From:<" + this.getClientName() + "> To:<" 
									+ r + "> Message: “"+ message +"” (FAILED->user went offline)");	
							out.flush();
							break;
						}
					}
					if(!postojao) {
					outStreamToClient.println("•••User '" + r + "' does not exist•••");
					}
				}	
				}
			}
			communicationSocket.close();
			if(clients.size() == 1)
			out.close();
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
			if (client != this && client.getClientName().equalsIgnoreCase(testName)) {
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
			listOfReceivers.add(receivers[i].trim());
		}
		return listOfReceivers;
	}
}
