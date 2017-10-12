import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadBalancer {
	private static final int THREAD_POOL_SIZE = 4;
	private final ServerSocket socket;
	private final DataCenterInstance[] instances;

	public LoadBalancer(ServerSocket socket, DataCenterInstance[] instances) {
		this.socket = socket;
		this.instances = instances;
	}

	// Complete this function
	public void start() throws IOException {
		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		int count = 0;
		int small = 0, mid = 1, large = 2;
		while (true) {
			// After every 20 requests, check the cpu for each data center. 
			//Get the data center instance with lowest cpu usage
			double[] array = new double[3];
			if (count % 300 == 0) {								
				array[0] = getCPU(instances[0]);
				array[1] = getCPU(instances[1]);
				array[2] = getCPU(instances[2]);					
				small = array[0] < array[1]? 0: 1;
				small = array[small] < array[2]? small: 2;
				large = array[0] < array[1]? 1: 0;
				large = array[large] < array[2]? 2: large;
				for (int i = 0; i < array.length; i++) {
					if (i != small && i != large) {
						mid = i;
					}
				}
//				swap(instances, 0, minindex);


			}
			
			Runnable requestHandler1 = new RequestHandler(socket.accept(), instances[small]);
	        executorService.execute(requestHandler1);
	        Runnable requestHandler2 = new RequestHandler(socket.accept(), instances[mid]);
	        executorService.execute(requestHandler2);
	      
			for (int i = 0; i < instances.length; i++) {	

				Runnable requestHandler = new RequestHandler(socket.accept(), instances[i]);
		        executorService.execute(requestHandler);	
				
			}
	        count++;


			//send the request to the data center with mininum cpu usage
		

			
		}
	}
	
	public static double getCPU(DataCenterInstance instance) {
		String url = instance.getUrl() + ":8080/info/cpu";
		String text = "";
		while (!isNumeric(text)) {
		    text = getText(url).split("<body>")[1].split("</body>")[0];
		}
		return Double.parseDouble(text);	
	}
	
	public static String getText(String url) {
        StringBuilder response = new StringBuilder();
    	
    	try{
    		URL website = new URL(url);
            URLConnection connection = website.openConnection();
            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(
                                        connection.getInputStream()));

            String inputLine;

            while ((inputLine = in.readLine()) != null) 
                response.append(inputLine);

            in.close();

            
    	}catch (MalformedURLException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}catch(ProtocolException e) {
    		e.printStackTrace();
    	}
    	catch (IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	return response.toString();
        
    }
	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    double d = Double.parseDouble(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}
}
