import os
import time
import sys
import requests 
import random

from azure.mgmt.common import SubscriptionCloudCredentials
import azure.mgmt.compute
import azure.mgmt.network
import azure.mgmt.resource
import azure.mgmt.storage

import urllib2
import urllib
import time


pwd = 'e9SeflQtHfAxdluDYbhHUJVqeRlr6P4q'
andrewid = 'jiweiz'
lg_dnsname = None
dc_dnsname = None
test_url = None
status = None
storage_account_name = "kiwizhang"
subscripid = "199180a1-588e-49c8-b6e2-d5993c6e71ac"
endpoint_uri = 'https://login.microsoftonline.com/c98a79ca-5a9a-4791-a243-f06afd67464d/oauth2/token'
app_id = '94b2ebbc-beb6-41ae-b703-40f55a9725ec'
app_secret_key = 'painting'
rps_count = 0
dc_status = False

class azure_api:
    def __init__(self, subscripid, endpoint_uri, app_id, app_secret_key):
        self.region = 'eastus'
        # make sure you export these four env variables before you run this code 
        self.subscription_id=subscripid 
        self.AZURE_ENDPOINT_URL=endpoint_uri 
        self.AZURE_APP_ID=app_id 
        self.AZURE_APP_SECRET=app_secret_key 

        # create a token, you need to get create and assign role to an application before use this module
        # check Readme.md in home directory
        self.auth_token = self.get_token_from_client_credentials(endpoint=self.AZURE_ENDPOINT_URL,
            client_id=self.AZURE_APP_ID,
            client_secret=self.AZURE_APP_SECRET)

        # create a cred using the token 
        self.creds = SubscriptionCloudCredentials(self.subscription_id, self.auth_token)

        # now it is the time to manage the resource
        self.compute_client = azure.mgmt.compute.ComputeManagementClient(self.creds)
        self.network_client = azure.mgmt.network.NetworkResourceProviderClient(self.creds)
        self.resource_client = azure.mgmt.resource.ResourceManagementClient(self.creds)

    def get_token_from_client_credentials(self, endpoint, client_id, client_secret):
        payload = {
            'grant_type': 'client_credentials',
            'client_id': client_id,
            'client_secret': client_secret,
            'resource': 'https://management.core.windows.net/',
        }
        response = requests.post(endpoint, data=payload).json()
        return response['access_token']

    # create nic for your vm.
    def create_network_interface(self, network_client, region, group_name, interface_name,
                                 network_name, subnet_name, ip_name):
        # Create or update a virtual network
        # All the nic/ip addresses used by our virtual machines will be put into this virtual network
        result = self.network_client.virtual_networks.create_or_update(
            group_name,
            network_name,
            azure.mgmt.network.VirtualNetwork(
                location=region,
                address_space=azure.mgmt.network.AddressSpace(
                    address_prefixes=[
                        '10.1.0.0/16',
                    ],
                ),
                subnets=[
                    azure.mgmt.network.Subnet(
                        name=subnet_name,
                        address_prefix='10.1.0.0/24',
                    ),
                ],
            ),
        )
    
        result = self.network_client.subnets.get(group_name, network_name, subnet_name)
        subnet = result.subnet
    
        # Create a new ip address resource
        # In azure, we need to create this resource before we create a Network Interface
        result = self.network_client.public_ip_addresses.create_or_update(
            group_name,
            ip_name,
            azure.mgmt.network.PublicIpAddress(
                location=region,
                public_ip_allocation_method='Dynamic',
                idle_timeout_in_minutes=4,
                dns_settings=azure.mgmt.network.networkresourceprovider.PublicIpAddressDnsSettings(
                    domain_name_label=ip_name,  # use ip name as its dns prefix
                ),
            ),
        )
    
        result = self.network_client.public_ip_addresses.get(group_name, ip_name)
        public_ip_id = result.public_ip_address.id
    
        # Create a network interface card using the ip address we just created
        # this nic will be put into the virtual network we just create 
        result = self.network_client.network_interfaces.create_or_update(
            group_name,
            interface_name,
            azure.mgmt.network.NetworkInterface(

                #### updated
                dns_settings=azure.mgmt.network.networkresourceprovider.DnsSettings(
                    dns_servers=[
                            '8.8.8.8'
                        ]
                    ),
                #### updated

                name=interface_name,
                location=region,
                ip_configurations=[
                    azure.mgmt.network.NetworkInterfaceIpConfiguration(
                        name='default',
                        private_ip_allocation_method=azure.mgmt.network.IpAllocationMethod.dynamic,
                        subnet=subnet,
                        public_ip_address=azure.mgmt.network.ResourceId(
                            id=public_ip_id,
                        ),
                    ),
                ],
            ),
        )
    
        result = self.network_client.network_interfaces.get(
            group_name,
            interface_name,
        )

        return result.network_interface.id

    # Create a new vm using the source disk 
    # Return:
    #       IP address
    # Parameters:
    #       group_name:       Group name where the virtual machine is in (except the disk)
    #       storage_name:     Storage account name where the source and target disk are in 
    #       vm_name:          VM name, need to be public unique
    #       region:           Default: "eastus"
    #       machine_size:     Machine Size
    #       image_uri:        URI of the source disk
    #       disk_container_name: Container of the target disk (Create if not exist)
    def create_vm_from_ami(self, group_name="demogroup", storage_name="snsn", vm_name="vmvm",  
            region="",  machine_size=azure.mgmt.compute.VirtualMachineSizeTypes.standard_a0, image_uri="", disk_container_name="vhds"):

        if region == "":
            region = self.region

        virtual_network_name=storage_name
        subnet_name=storage_name

        network_interface_name=vm_name
        public_ip_name=vm_name
        os_disk_name=vm_name
        computer_name=vm_name

        # In our Images, the admin user and password are configured already. Changing the paramenters here won't work
        username="ubuntu"
        password="Cloud@123"

        # 1. Create a resource group
        result = self.resource_client.resource_groups.create_or_update(
            group_name,
            azure.mgmt.resource.ResourceGroup(
               location=region,
            ),
        )

        # print 'created (updated) resource group'

        # 2. Create the network interface using a helper function (defined below)
        nic_id = self.create_network_interface(
            self.network_client,
            region,
            group_name,
            network_interface_name,
            virtual_network_name,
            subnet_name,
            public_ip_name,
        )

        # print 'created virtual network and network interface card'
        
        # print 'creating virtual machine'
        # 3. Create the virtual machine
        result = self.compute_client.virtual_machines.create_or_update(
            group_name,
            azure.mgmt.compute.VirtualMachine(
                location=region,
                name=vm_name,
                os_profile=azure.mgmt.compute.OSProfile(
                    admin_username=username,
                    admin_password=password,
                    computer_name=computer_name,
                ),
                hardware_profile=azure.mgmt.compute.HardwareProfile(
                    virtual_machine_size=machine_size
                ),
                network_profile=azure.mgmt.compute.NetworkProfile(
                    network_interfaces=[
                        azure.mgmt.compute.NetworkInterfaceReference(
                            reference_uri=nic_id,
                        ),
                    ],
                ),
                storage_profile=azure.mgmt.compute.StorageProfile(
                    os_disk=azure.mgmt.compute.OSDisk(
                        caching=azure.mgmt.compute.CachingTypes.none,
                        create_option=azure.mgmt.compute.DiskCreateOptionTypes.from_image,
                        name=os_disk_name,
                        virtual_hard_disk=azure.mgmt.compute.VirtualHardDisk(
                            uri='https://{0}.blob.core.windows.net/{1}/{2}.vhd'.format(
                                storage_name,
                                disk_container_name,
                                os_disk_name,
                            ),
                        ),
                    # AMI uri
                        source_image=azure.mgmt.compute.VirtualHardDisk(
                            uri=image_uri,
                        ),
                        operating_system_type= azure.mgmt.compute.OperatingSystemTypes.linux
                    ),
                ),
            ),
        )
# cc15619p21lgv10-osDisk.f6be8828-8cab-45ae-a611-904aeeef3c9e.vhd
# https://kiwizhang.blob.core.windows.net/system/Microsoft.Compute/Images/vhds/cc15619p21lgv10-osDisk.f6be8828-8cab-45ae-a611-904aeeef3c9e.vhd

# "https://cmucc.blob.core.windows.net/public/Microsoft.Compute/Images/vhds/cc15619p21dcv5-osDisk.e27faca3-f177-40ea-a740-9a1838326ae6.vhd"
        # Get the ip address
        while True:
            try:
                result = self.network_client.public_ip_addresses.get(group_name, public_ip_name)
                break
            except:
                # print 'getting ip error, recall it now'
                pass
            
        print('VM {0} available at IP: {1}'.format(vm_name, result.public_ip_address.ip_address))
        return result.public_ip_address.ip_address

def help_book():
    print 'usage: python azure_demo_create_vm_from_ami.py STORAGE_ACCOUNT_NAME SUBSCRIPTION_ID ENDPOINT_URI APPLICATION_ID APPLICATION_SECRET_KEY'
        
# def demo_dc():
#     if 6 != len(sys.argv):
#         help_book()
#         return 
    
#     # print 'Your source os disk should be in storage account[{0}]. \n Your subscription id is {1} \n Your Endpoint uri is {2} \n Your application id is {3} \n your application secret key is {4} \n Please make sure you input the correct parameters for these!'.format(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5])

#     api = azure_api(sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5])

#     time_stamp = int(time.time()) % 10000
#     rand_stamp = random.randint(1, 1000)

#     pse_uniqname = "cc"+str(time_stamp)+str(rand_stamp)

#     # storage_account_name=

#     image_uri = "https://kiwizhang.blob.core.windows.net/system/Microsoft.Compute/Images/vhds/cc15619p21dcv5-osDisk.e27faca3-f177-40ea-a740-9a1838326ae6.vhd"
#     group_name_for_new_vm = pse_uniqname+"group"  # specify your new group name
#     vm_name = pse_uniqname + "vm"    # specify your vm name (should be public unique)

#     api.create_vm_from_ami(storage_name=storage_account_name, vm_name=vm_name, group_name=group_name_for_new_vm, image_uri=image_uri, machine_size="Standard_A1")

def demo_lg():
    # if 6 != len(sys.argv):
    #     help_book()
    #     return 
    
    # print 'Your source os disk should be in storage account[{0}]. \n Your subscription id is {1} \n Your Endpoint uri is {2} \n Your application id is {3} \n your application secret key is {4} \n Please make sure you input the correct parameters for these!'.format(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5])

    api = azure_api(sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5])

    time_stamp = int(time.time()) % 10000
    rand_stamp = random.randint(1, 1000)

    pse_uniqname = "cc"+str(time_stamp)+str(rand_stamp)

    storage_account_name=sys.argv[1]

    image_uri = "https://kiwizhang.blob.core.windows.net/system/Microsoft.Compute/Images/vhds/cc15619p21lgv10-osDisk.f6be8828-8cab-45ae-a611-904aeeef3c9e.vhd"
    group_name_for_new_vm = pse_uniqname+"group"  # specify your new group name
    vm_name = pse_uniqname + "vm"    # specify your vm name (should be public unique)

    api.create_vm_from_ami(storage_name=storage_account_name, vm_name=vm_name, group_name=group_name_for_new_vm, image_uri=image_uri, machine_size="Standard_D1")
    global lg_dnsname
    lg_dnsname = vm_name + '.eastus.cloudapp.azure.com'
    # lg_dnsname = urllib.quote(lg_dnsname)

    submit_url = 'http://' + lg_dnsname + '/password?passwd=' + pwd + '&andrewid=' + andrewid


    req = urllib2.Request(submit_url)
    response = urllib2.urlopen(req)
    the_page = response.read()
    while (the_page.startswith("Invalid")):
        req = urllib2.Request(submit_url)
        response = urllib2.urlopen(req)
        the_page = response.read()
    # print the_page

def demo_dc():
    # if 6 != len(sys.argv):
    #     help_book()
    #     return 
    
    # print 'Your source os disk should be in storage account[{0}]. \n Your subscription id is {1} \n Your Endpoint uri is {2} \n Your application id is {3} \n your application secret key is {4} \n Please make sure you input the correct parameters for these!'.format(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5])

    api = azure_api(subscripid, endpoint_uri, app_id, app_secret_key)

    time_stamp = int(time.time()) % 10000
    rand_stamp = random.randint(1, 1000)

    pse_uniqname = "cc"+str(time_stamp)+str(rand_stamp)

    # storage_account_name=sys.argv[1]

    image_uri = "https://kiwizhang.blob.core.windows.net/system/Microsoft.Compute/Images/vhds/cc15619p21dcv5-osDisk.e27faca3-f177-40ea-a740-9a1838326ae6.vhd"
    group_name_for_new_vm = pse_uniqname+"group"  # specify your new group name
    vm_name = pse_uniqname + "vm"    # specify your vm name (should be public unique)

    api.create_vm_from_ami(storage_name=storage_account_name, vm_name=vm_name, group_name=group_name_for_new_vm, image_uri=image_uri, machine_size="Standard_A1")
    global dc_dnsname
    dc_dnsname = vm_name + '.eastus.cloudapp.azure.com'

def launch_dc():
    demo_dc()
    global test_url
    global lg_dnsname
    global dc_dnsname
    lg_dnsname = urllib.quote(lg_dnsname)
    dc_dnsname = urllib.quote(dc_dnsname)
    global dc_status
    if dc_status == False:
        test_url = 'http://' + lg_dnsname + '/test/horizontal?dns=' + dc_dnsname
    else:
        test_url = 'http://' + lg_dnsname + '/test/horizontal/add?dns=' + dc_dnsname
    dc_status = True
    req = urllib2.Request(test_url)
    response = urllib2.urlopen(req)
    the_page = response.read()

    pageline = pagecontent.split('\n')

    for i in reversed(xrange(len(pageline))):
        if pageline[len(pageline) - 2].split()[1] == "finished":
            global status
            status = "finished"
            return
        if pageline[i].startswith("[Minute"):
            global rps_count
            rps_count = rps_count + pageline[i+1].split("=")[1]
            break

if __name__ == '__main__':
    demo_lg()
    while (rps_count < 3000 and status != "finished"):
        time.sleep(100)
        launch_dc()
    print 'success!'


