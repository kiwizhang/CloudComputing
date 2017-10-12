import java.io.IOException;
import java.net.ServerSocket;

public class Main {
	private static final int PORT = 80;
	private static DataCenterInstance[] instances;
	private static ServerSocket serverSocket;

	//Update this list with the DNS of your data center instances
	//Remember to put your azure dns here. Don't put IP ADDRESS.
	static {
		instances = new DataCenterInstance[3];
            instances[0] = new DataCenterInstance("First", "http://cc811746vm.eastus.cloudapp.azure.com");
            instances[1] = new DataCenterInstance("Second", "http://cc4615706vm.eastus.cloudapp.azure.com");
            instances[2] = new DataCenterInstance("Third", "http://cc8195322vm.eastus.cloudapp.azure.com");
	}

	public static void main(String[] args) throws IOException {
		initServerSocket();
		LoadBalancer loadBalancer = new LoadBalancer(serverSocket, instances);
		loadBalancer.start();
	}

	/**
	 * Initialize the socket on which the Load Balancer will receive requests from the Load Generator
	 */
	private static void initServerSocket() {
		try {
			serverSocket = new ServerSocket(PORT);
		} catch (IOException e) {
			System.err.println("ERROR: Could not listen on port: " + PORT);
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
