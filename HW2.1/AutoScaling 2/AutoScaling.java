import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.net.*;


import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.CreateAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.CreateLaunchConfigurationRequest;
import com.amazonaws.services.autoscaling.model.CreateOrUpdateTagsRequest;
import com.amazonaws.services.autoscaling.model.DeleteAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.InstanceMonitoring;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsRequest;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyRequest;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.ComparisonOperator;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.amazonaws.services.cloudwatch.model.Statistic;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.IpPermission;

import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.AddTagsRequest;
import com.amazonaws.services.elasticloadbalancing.model.ConfigureHealthCheckRequest;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.DeleteLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.HealthCheck;
import com.amazonaws.services.elasticloadbalancing.model.Listener;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;

public class AutoScaling {
	

	static String keyName = "15619demo";
    static String lgImage = "ami-8ac4e9e0";
    static String lgType = "m3.medium";
    static String dcImage = "ami-349fbb5e";
    static String dcType = "m3.large";
    static AmazonEC2Client ec2;
    static String groupNameLg = "HW21-sg-lg";
    static String groupNameElb = "HW21-sg-elb";
    static String elbName = "HW21-ELB";
    static String configName = "HW21config";
    static String asgName = "HW21sgp";
    static String policyName = "HW21policy";
    static String price = "0.019";
    static AmazonAutoScaling asClient;
    static AmazonElasticLoadBalancingClient elb;
    

	
	
	public static String launch_ec2(String imageId, String instanceType, String groupName) {
		//Load the Properties File with AWS Credentials
				
				//Create an Amazon EC2 Client
//				AmazonEC2Client ec2 = new AmazonEC2Client(bawsc);
				//Create Instance Request
				RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
				//Configure Instance Request
				runInstancesRequest.withImageId(imageId)
				.withInstanceType(instanceType)
				.withMinCount(1)
				.withMaxCount(1)
				.withKeyName(keyName)
		        .withSecurityGroupIds(groupName);


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
				return instance.getInstanceId();
	}
	
    public static String createSecurityGroup(String groupName) {
        Region usEast1 = Region.getRegion(Regions.US_EAST_1);
        Properties properties = new Properties();
		try {
			properties.load(AutoScaling.class.getResourceAsStream("AwsCredentials.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BasicAWSCredentials bawsc = new BasicAWSCredentials(properties.getProperty("aws_access_key_id"), properties.getProperty("aws_secret_access_key"));
		ec2 = new AmazonEC2Client(bawsc);
        ec2.setRegion(usEast1);
        String groupId = null;
        try {
            CreateSecurityGroupRequest securityGroupRequest = new CreateSecurityGroupRequest(
                    groupName, groupName + " by kiwi");
            CreateSecurityGroupResult result = ec2
                    .createSecurityGroup(securityGroupRequest);
            System.out.println(String.format("Security group created: [%s]",
                    result.getGroupId()));
            groupId = result.getGroupId();
            CreateTagsRequest createTagsRequest = new CreateTagsRequest();
            createTagsRequest.withResources(groupId);
            createTagsRequest.withTags(new Tag("Project", "2.1"));
            ec2.createTags(createTagsRequest);
        } catch (AmazonServiceException ase) {
            System.out.println(ase.getMessage());
        }
        String ipAddr = "0.0.0.0/0";
        
        // Create a range that you would like to populate.

        List<String> ipRanges = Collections.singletonList(ipAddr);
        IpPermission ipPermission = new IpPermission()
                .withIpProtocol("-1")
                .withFromPort(new Integer(0))
                .withToPort(new Integer(65535))
                .withIpRanges(ipRanges);
        
        List<IpPermission> ipPermissions = Collections.singletonList(ipPermission);
        try {
            AuthorizeSecurityGroupIngressRequest ingressRequest = new AuthorizeSecurityGroupIngressRequest(
                    groupName, ipPermissions);
            ec2.authorizeSecurityGroupIngress(ingressRequest);
            System.out.println(String.format("Ingress port authroized: [%s]",
                    ipPermissions.toString()));
        } catch (AmazonServiceException ase) {
            System.out.println(ase.getMessage());
        }
        return groupId;
    }
	
    public static String getPublicInstanceDNS(String instanceId) {
        String dns = null;
        do {
            DescribeInstancesRequest describeRequest =  new DescribeInstancesRequest();
            DescribeInstancesResult describeResult = ec2.describeInstances(describeRequest);
            List<Reservation> reservations = describeResult.getReservations();
            for (Reservation reservationItem: reservations) {
                List<Instance> instances = reservationItem.getInstances();
                for (Instance instanceItem: instances) {
                    if (instanceItem.getInstanceId().equals((instanceId))) {
                        return instanceItem.getPublicDnsName();
                    }
                }
            }
        } while (dns == null || dns.length() == 0);
        return dns;
    }
	
    public static String createELB(String elbName, String groupId, String healthCheckPage, String instanceId) throws InterruptedException {
    	Properties properties = new Properties();
		try {
			properties.load(AutoScaling.class.getResourceAsStream("AwsCredentials.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BasicAWSCredentials bawsc = new BasicAWSCredentials(properties.getProperty("aws_access_key_id"), properties.getProperty("aws_secret_access_key"));
    	elb = new AmazonElasticLoadBalancingClient(bawsc);       
        CreateLoadBalancerRequest createLBRequest = new CreateLoadBalancerRequest();
        createLBRequest.setLoadBalancerName(elbName);
        List<Listener> listeners = new ArrayList<Listener>(1);
        listeners.add(new Listener("HTTP", 80, 80));
        createLBRequest.setListeners(listeners);
        createLBRequest.withAvailabilityZones("us-east-1d", "us-east-1b");
        createLBRequest.withSecurityGroups(groupId);        
        CreateLoadBalancerResult createLBResult = elb.createLoadBalancer(createLBRequest);
        System.out.println("elb is created");
        String elbDns = createLBResult.getDNSName();         
        AddTagsRequest addTagsRequest = new AddTagsRequest();
        
        addTagsRequest.withLoadBalancerNames(elbName);
        com.amazonaws.services.elasticloadbalancing.model.Tag tag = new com.amazonaws.services.elasticloadbalancing.model.Tag();
        tag.setKey("Project");
        tag.setValue("2.1");
        addTagsRequest.withTags(tag);
        elb.addTags(addTagsRequest);        
        ConfigureHealthCheckRequest healthCheckRequest = new ConfigureHealthCheckRequest();
        healthCheckRequest.withLoadBalancerName(elbName);
        HealthCheck healthCheck = new HealthCheck().
                withHealthyThreshold(4).
                withUnhealthyThreshold(2).
                withTimeout(5).
                withInterval(50).
                withTarget(healthCheckPage);
        healthCheckRequest.setHealthCheck(healthCheck);
        elb.configureHealthCheck(healthCheckRequest);        
        com.amazonaws.services.elasticloadbalancing.model.Instance instance = new com.amazonaws.services.elasticloadbalancing.model.Instance().withInstanceId(instanceId);
        RegisterInstancesWithLoadBalancerRequest registerInstanceRequest = new RegisterInstancesWithLoadBalancerRequest().
                withInstances(instance).
                withLoadBalancerName(elbName);
        elb.registerInstancesWithLoadBalancer(registerInstanceRequest);
        Thread.sleep(18000);
        System.out.println("wait 3 minS for the elb to be ready");
        return elbDns;
    }
    
    public static void createAutoScaling(String configName, String asgName, String policyName, String imageId, String instanceType, String price, String groupName, String elbName) {
        asClient = new AmazonAutoScalingClient();
        AmazonCloudWatchClient cloudWatchClient = new AmazonCloudWatchClient();
        
//        create launch configuration
        InstanceMonitoring monitoring = new InstanceMonitoring();
        monitoring.setEnabled(Boolean.TRUE);
        CreateLaunchConfigurationRequest lcRequest = new CreateLaunchConfigurationRequest()
                .withLaunchConfigurationName(configName)
                .withImageId(imageId)
                .withInstanceType(instanceType)
                .withSecurityGroups(groupName)
                .withInstanceMonitoring(monitoring)
                .withSpotPrice(price)
                .withKeyName(keyName);
        asClient.createLaunchConfiguration(lcRequest);
        System.out.println("created launch configuration.");
        
//        create auto scaling group
        CreateAutoScalingGroupRequest asgRequest = new CreateAutoScalingGroupRequest()
                .withAutoScalingGroupName(asgName)
                .withLaunchConfigurationName(configName)
                .withAvailabilityZones("us-east-1d", "us-east-1b")
                .withMinSize(2)
                .withMaxSize(3)
                .withLoadBalancerNames(elbName)
                .withHealthCheckType("ELB")
                .withHealthCheckGracePeriod(60)
                .withNewInstancesProtectedFromScaleIn(Boolean.TRUE);
        asClient.createAutoScalingGroup(asgRequest);
        com.amazonaws.services.autoscaling.model.Tag tag = new com.amazonaws.services.autoscaling.model.Tag()
                .withKey("Project")
                .withValue("2.1")
                .withPropagateAtLaunch(Boolean.TRUE)
                .withResourceType("auto-scaling-group")
                .withResourceId(asgName);
        CreateOrUpdateTagsRequest tagRequest = new CreateOrUpdateTagsRequest().withTags(tag);
        asClient.createOrUpdateTags(tagRequest);
        System.out.println("created auto scaling group.");
        
//        create scaling policy
        PutScalingPolicyRequest policyRequest = new PutScalingPolicyRequest()
                .withAutoScalingGroupName(asgName)
                .withPolicyName(policyName)
                .withScalingAdjustment(1)
                .withAdjustmentType("ChangeInCapacity");
        asClient.putScalingPolicy(policyRequest);
        System.out.println("created scaling policy.");

        //create increasing cloud watch alarm
        Dimension dimension = new Dimension();
        dimension.setName("AutoScalingGroupName");
        dimension.setValue(asgName);
        PutMetricAlarmRequest upRequest = new PutMetricAlarmRequest()
                .withAlarmName("Increase size")
                .withMetricName("Hight-CPU-Utilization")
                .withDimensions(dimension)
                .withNamespace("AWS/EC2")
                .withComparisonOperator(ComparisonOperator.GreaterThanThreshold)
                .withStatistic(Statistic.Average)
                .withUnit(StandardUnit.Percent)
                .withThreshold(65d)
                .withPeriod(60)
                .withEvaluationPeriods(2);
        //create decreasing alarm
        PutMetricAlarmRequest downRequest = new PutMetricAlarmRequest()
        .withAlarmName("decrease size")
        .withMetricName("Low-CPU-Utilization")
        .withDimensions(dimension)
        .withNamespace("AWS/EC2")
        .withComparisonOperator(ComparisonOperator.LessThanThreshold)
        .withStatistic(Statistic.Average)
        .withUnit(StandardUnit.Percent)
        .withThreshold(35d)
        .withPeriod(60)
        .withEvaluationPeriods(2);
        cloudWatchClient.putMetricAlarm(upRequest);
        cloudWatchClient.putMetricAlarm(downRequest);
        System.out.println("created cloud watch alarm.");
    }
    
	public static void sendRequest(String url) {    	
    	while (true) {
    		try{
        		URL website = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) website.openConnection();
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();
                System.out.println("Response Code : " + responseCode);
                if (responseCode != 200) {
                    System.out.println("resubmit.");
                    continue;
                } else {
                    return;
                }         
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
    	}
    }
		
	public static String getText(String url) {
        StringBuilder response = new StringBuilder();
    	
    	while(true) {
    		try{
        		URL website = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) website.openConnection();
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();
                System.out.println("responsecode:" + responseCode);
                if (responseCode != 200) {
                    System.out.println("resubmit.");
                    continue;
                } else {
                	BufferedReader in = new BufferedReader(
                            new InputStreamReader(
                                connection.getInputStream()));

                	String inputLine;

                	while ((inputLine = in.readLine()) != null) 
                	response.append(inputLine);

                	in.close();
                	return response.toString();

                } 
                
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
    	}
    	
        
    }

	
	public static void main(String[] args) throws Exception {
		String groupIdLg = createSecurityGroup(groupNameLg);
	    String groupIdElb = createSecurityGroup(groupNameElb);
	    //create lg
	    String lgId = launch_ec2(lgImage, lgType, groupNameLg);
	    //launch dc
	    String dcId = launch_ec2(dcImage, dcType, groupNameElb);
        Thread.sleep(100000);

	    //get dns names
	    String lgDns = getPublicInstanceDNS(lgId);
	    System.out.println(lgDns);
        String dcDns = getPublicInstanceDNS(dcId);
        // launch elb
        String healthCheckPage = "HTTP:80/heartbeat?lg=" + lgDns;
        String elbDns = createELB(elbName, groupIdElb, healthCheckPage, dcId);
        
        createAutoScaling(configName, asgName, policyName, dcImage, dcType, price, groupNameElb, elbName);
        // create launch configuration, asg, policy and cloud watch alarm
        System.out.println("created elb: " + elbDns);
        
        // get andrewid and submissionpwd
        Properties properties = new Properties();
		try {
			properties.load(AutoScaling.class.getResourceAsStream("AwsCredentials.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String andrewid = properties.getProperty("andrewid");
		String pwd= properties.getProperty("pwd");
       //submit id and pwd
      String submitUrl = String.format("http://%s/password?passwd=%s&andrewId=%s", lgDns, pwd, andrewid);
      System.out.println(submitUrl);
      sendRequest(submitUrl);
      
      //warm up
      String warmUpUrl = String.format("http://%s/warmup?dns=%s", lgDns, elbDns);
      sendRequest(warmUpUrl);
      System.out.println("wait 15 minutes to warm up");
      Thread.sleep(930000);
      System.out.println("warm up done");
      //junior test
      String juniorUrl = String.format("http://%s/junior?dns=%s", lgDns, elbDns);
      sendRequest(juniorUrl);
      System.out.println("wait 48 minutes for the test");
      Thread.sleep(2880000);
      System.out.println("test is completed");
      //get test id
      String logUrl = String.format("http://%s/log", lgDns);
      String content = getText(logUrl);
      String[] contentArray = content.split(".log'>test.");
      String testId = contentArray[contentArray.length - 1].split(".log<")[0];   
      System.out.println("test id: " + testId);
		
      
      //terminate asg
      
//      asClient.deleteAutoScalingGroup( new DeleteAutoScalingGroupRequest()
//    		  .withAutoScalingGroupName(asgName)
//    		  .withForceDelete( true ) );
//     // terminate elb
//      elb.deleteLoadBalancer(new DeleteLoadBalancerRequest()
//    		  .withLoadBalancerName(elbName));
//      // terminate all ec2 instances other than load generator 
//      DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
//	  List<Reservation> reservations = describeInstancesRequest.getReservations();
//	  List<String> instanceIds = new ArrayList<String>();
//	    for (Reservation reservation : reservations) {
//	      for (Instance instance : reservation.getInstances()) {
//	    	  instanceIds.add(instance.getInstanceId());
//	      }
//	    }
//      instanceIds.remove(lgId);
//      TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest(instanceIds);
//      ec2.terminateInstances(terminateRequest);
//      
//      //terminate security group
//      DeleteSecurityGroupRequest deleteLgSecurityGroup = new DeleteSecurityGroupRequest("HW21-sg-lg");
//      DeleteSecurityGroupRequest deleteElbSecurityGroup = new DeleteSecurityGroupRequest("HW21-sg-elb");
//      ec2.deleteSecurityGroup(deleteLgSecurityGroup);
//      ec2.deleteSecurityGroup(deleteElbSecurityGroup);

		
	}
}

