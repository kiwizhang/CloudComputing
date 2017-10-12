import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.net.*;
import java.io.*;


import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsRequest;
import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.LaunchSpecification;
import com.amazonaws.services.ec2.model.RequestSpotInstancesRequest;
import com.amazonaws.services.ec2.model.RequestSpotInstancesResult;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.SpotInstanceRequest;
import com.amazonaws.services.ec2.model.Tag;

public class Horizontal_Scaling {
	
	static String lg_dnsname;
	static boolean dc_status = false;
	static String log_url;
	static String testid_url;
	static String dc_dnsname;
	static double rps_count;
    static int responseCode;
	
	
	public static void launch_lg() {
		//Load the Properties File with AWS Credentials
		//Load the Properties File with AWS Credentials
		Properties properties = new Properties();
		try {
			properties.load(Horizontal_Scaling.class.getResourceAsStream("AwsCredentials.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BasicAWSCredentials bawsc = new BasicAWSCredentials(properties.getProperty("aws_access_key_id"), properties.getProperty("aws_secret_access_key"));
		//Create an Amazon EC2 Client
		AmazonEC2Client ec2 = new AmazonEC2Client(bawsc);
		//Create Instance Request
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
		//Configure Instance Request
		runInstancesRequest.withImageId("ami-8ac4e9e0")
		.withInstanceType("m3.medium")
		.withMinCount(1)
		.withMaxCount(1)
		.withKeyName("15619demo")
		.withSecurityGroups("kiwifly");

		//Launch Instance
		RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);   
		//Return the Object Reference of the Instance just Launched
		Instance instance=runInstancesResult.getReservation().getInstances().get(0);
		
		//Add tags
		CreateTagsRequest createTagsRequest = new CreateTagsRequest();
		createTagsRequest.withResources(instance.getInstanceId())
						.withTags(new Tag("Project", "2.1"));
		ec2.createTags(createTagsRequest);
				
				// print the_page
				System.out.print(instance.getInstanceId());
				try {
					TimeUnit.SECONDS.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				lg_dnsname = getInstancePublicDnsName(instance.getInstanceId());
				System.out.println(lg_dnsname);
				String pwd = properties.getProperty("pwd");
				String andrewid = properties.getProperty("andrewid");
				String submit_url = "http://" + lg_dnsname + "/password?passwd=" + pwd + "&andrewId=" + andrewid;
				System.out.println(submit_url);
				String submit_url_result = null;
				while (responseCode != 200 || submit_url_result == null) {
					submit_url_result = getText(submit_url);
				}
				
				System.out.println("submit lg url");
		
	}
	
	public static void launch_dc() {
		//Load the Properties File with AWS Credentials
				Properties properties = new Properties();
				try {
					properties.load(Horizontal_Scaling.class.getResourceAsStream("AwsCredentials.properties"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				BasicAWSCredentials bawsc = new BasicAWSCredentials(properties.getProperty("aws_access_key_id"), properties.getProperty("aws_secret_access_key"));
				//Create an Amazon EC2 Client
				AmazonEC2Client ec2 = new AmazonEC2Client(bawsc);
				//Create Instance Request
				RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
				//Configure Instance Request
				runInstancesRequest.withImageId("ami-349fbb5e")
				.withInstanceType("m3.medium")
				.withMinCount(1)
				.withMaxCount(1)
				.withKeyName("15619demo")
				.withSecurityGroups("kiwifly");

				//Launch Instance
				RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);   
				//Return the Object Reference of the Instance just Launched
				Instance instance=runInstancesResult.getReservation().getInstances().get(0);
				
				//Add tags
				CreateTagsRequest createTagsRequest = new CreateTagsRequest();
				createTagsRequest.withResources(instance.getInstanceId())
								.withTags(new Tag("Project", "2.1"));
				ec2.createTags(createTagsRequest);
				System.out.println("Just launched an dc instance with ID:" + instance.getInstanceId());
				try {
					TimeUnit.SECONDS.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dc_dnsname = getInstancePublicDnsName(instance.getInstanceId());
				String test_url;
				if (dc_status == false) {
					test_url = "http://" + lg_dnsname + "/test/horizontal?dns=" + dc_dnsname;
				} else {
					test_url = "http://" + lg_dnsname + "/test/horizontal/add?dns=" + dc_dnsname;
				}
				System.out.println("test_url:" + test_url);
				String test_url_result = null;
				while (responseCode != 200 || test_url_result == null) {
					test_url_result = getText(test_url);
				}
				
				System.out.println("submit test url");
				System.out.println("test_url_result:" + test_url_result);
				dc_status = true;
				
				testid_url = "http://" + lg_dnsname + "/log";
				System.out.println("testid_url: " + testid_url);
				String testid_url_result = null;
				while (testid_url_result == null) {
					testid_url_result = getText(testid_url);
				}
				System.out.println("log_url_result: " + testid_url_result);
				int split_length = testid_url_result.split("test.").length;
				String test_id = testid_url_result.split("test.")[split_length - 1].split(".log")[0];
				System.out.println(test_id);
				log_url = "http://" + lg_dnsname + "/log?name=test." + test_id + ".log";

				
		
	}
	
	static String getInstancePublicDnsName(String instanceId) {
		Properties properties = new Properties();
		try {
			properties.load(Horizontal_Scaling.class.getResourceAsStream("AwsCredentials.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BasicAWSCredentials bawsc = new BasicAWSCredentials(properties.getProperty("aws_access_key_id"), properties.getProperty("aws_secret_access_key"));
		//Create an Amazon EC2 Client
		AmazonEC2Client ec2 = new AmazonEC2Client(bawsc);
	    DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
	    List<Reservation> reservations = describeInstancesRequest.getReservations();
	    Set<Instance> allInstances = new HashSet<Instance>();
	    for (Reservation reservation : reservations) {
	      for (Instance instance : reservation.getInstances()) {
	        if (instance.getInstanceId().equals(instanceId))
	          return instance.getPublicDnsName();
	      }
	    }
	    return null;
	}
	
	public static String getText(String url) {
        StringBuilder response = new StringBuilder();
    	
    	try{
    		URL website = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) website.openConnection();
            connection.setRequestMethod("GET");
            responseCode = connection.getResponseCode();
            System.out.println("responsecode:" + responseCode);
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
		

  
	
	public static void main(String[] args) throws Exception {
//		RunInstance s = new RunInstance();
		launch_lg();
		ArrayList<String> urllist = new ArrayList<>();
		while (rps_count < 4000) {
			try {
				TimeUnit.SECONDS.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			launch_dc();
			
			urllist.add(log_url);
        	rps_count = 0;
	        for(String log_url_specific : urllist) {
	        	System.out.println("log_url_specific:" + log_url_specific);
	        	String log_url_content = null;
	    		while (responseCode != 200 || log_url_content == null || log_url_content.indexOf("[Minute") == -1) {
	    			log_url_content = getText(log_url_specific);
	    
	    		}
	    		System.out.println("log_url_content:" + log_url_content);
    			int split_length = log_url_content.split("=").length;
    			rps_count = rps_count + Double.parseDouble(log_url_content.split("=")[split_length - 1].trim());
    			System.out.println("rps_count:" + rps_count);
	                
	                
	        }
		}
		System.out.println("succeed");
		
        
		
		
	}
}

