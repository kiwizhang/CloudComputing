#! /bin/bash
#This is the script to help you deploy kafka+samza cluster on EMR

join_arr() { 
  local IFS="$1"; shift; echo "$*"; 
}

home="/home/hadoop"
cd "$home"
echo "Enter the full path of pem file (ex:/home/hadoop/yourpem.pem):"
read pemfile
if [ ! -f "$pemfile" ]
then
	echo "pem file not found. Please put your pem file under /home/hadoop/ folder."
	exit
else
	chmod 400 "$pemfile"
fi

#Check git
if [ ! -f /usr/bin/git ]
then
	echo -e "\033[34minstall git now...\e[0m"
	sudo yum install git
else
	echo -e "\033[92mgit installed, good!\e[0m"
fi

#Check maven
if [ ! -d apache-maven-3.3.3/ ]
then
	echo -e "\033[34minstall maven now...\e[0m"
	wget http://www.eu.apache.org/dist/maven/maven-3/3.3.3/binaries/apache-maven-3.3.3-bin.tar.gz
	tar -zxf apache-maven-3.3.3-bin.tar.gz
fi
echo -e "\033[92mmaven installed, good!\e[0m"

#Set environment

echo " ">> .bashrc
echo "export HADOOP_YARN_HOME=/usr/lib/hadoop-yarn" >> .bashrc
echo "export PATH=$PATH:/home/hadoop/apache-maven-3.3.3/bin" >> .bashrc
export PATH=$PATH:/home/hadoop/apache-maven-3.3.3/bin
export HADOOP_YARN_HOME=/usr/lib/hadoop-yarn
. .bashrc
sudo curl http://svn.apache.org/viewvc/hadoop/common/trunk/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-tests/src/test/resources/capacity-scheduler.xml?view=co > /etc/hadoop/conf/capacity-scheduler.xml

#Download samza
echo -e "\033[34minstall samza now...\e[0m"
git clone http://git-wip-us.apache.org/repos/asf/samza.git
cd samza
./gradlew clean publishToMavenLocal


#Download hello-samza
cd "$home"
git clone git://git.apache.org/samza-hello-samza.git hello-samza
echo -e "\033[34minstall hello-samza project now...\e[0m"
cd hello-samza
mvn clean package
mkdir -p deploy/samza
tar -xvf ./target/hello-samza-0.10.0-dist.tar.gz -C deploy/samza
hadoop fs -rm /hello-samza-0.10.0-dist.tar.gz
hadoop fs -put ./target/hello-samza-0.10.0-dist.tar.gz /
sed -i '/yarn.package.path=/d' deploy/samza/config/wikipedia-feed.properties
sed -i '/systems.kafka.consumer.zookeeper.connect=/d' deploy/samza/config/wikipedia-feed.properties
sed -i '/systems.kafka.producer.bootstrap.servers=/d' deploy/samza/config/wikipedia-feed.properties
hdfs_path=$(grep -o "hdfs:[^<]*" /etc/hadoop/conf/core-site.xml)
master_ip=$(expr "$hdfs_path" : "hdfs://\([^:]*\):")
echo yarn.package.path="$hdfs_path"/hello-samza-0.10.0-dist.tar.gz >> deploy/samza/config/wikipedia-feed.properties
echo systems.kafka.consumer.zookeeper.connect="$master_ip":2181/ >> deploy/samza/config/wikipedia-feed.properties
echo systems.kafka.producer.bootstrap.servers="$master_ip":9092 >> deploy/samza/config/wikipedia-feed.properties
echo -e "\033[92minstall hello-samza project complete\e[0m"
echo -e "\033[34mstart kafka and zookeeper...\e[0m"
bin/grid install kafka
bin/grid install zookeeper
echo delete.topic.enable=true >> deploy/kafka/config/server.properties
bin/grid start kafka
bin/grid start zookeeper
sudo service iptables save
sudo service iptables stop
sudo chkconfig iptables off
echo -e "\033[92mmaster node complete\e[0m"
# Deploy on all slave nodes
iplist=$(hdfs dfsadmin -report | grep ^Name | cut -f2 -d: | cut -f2 -d' ')
array=($iplist)
count=1
for private_ip in "${array[@]}"
do
    echo -e "\033[34mdeploy kafka for slave node:$private_ip\e[0m"
    scp -o stricthostkeychecking=no -i "$pemfile" "$home"/hello-samza/bin/grid hadoop@"$private_ip":/home/hadoop/
	# install kafka on slave node
	ssh -o stricthostkeychecking=no -i "$pemfile" hadoop@"$private_ip" 'mkdir -p hello-samza/bin;mv grid hello-samza/bin/;cd hello-samza;bin/grid install kafka'
	# configure kafka
	ssh -o stricthostkeychecking=no -i "$pemfile" hadoop@"$private_ip" 'sed -i ''/zookeeper.connect=/d'' /home/hadoop/hello-samza/deploy/kafka/config/server.properties;hdfs_path=$(grep -o "hdfs:[^<]*" /etc/hadoop/conf/core-site.xml);master_ip=$(expr "$hdfs_path" : "hdfs://\([^:]*\):");echo zookeeper.connect="$master_ip":2181 >> /home/hadoop/hello-samza/deploy/kafka/config/server.properties;echo delete.topic.enable=true >> /home/hadoop/hello-samza/deploy/kafka/config/server.properties;'
	ssh -o stricthostkeychecking=no -i "$pemfile" hadoop@"$private_ip" "sed -i ''/broker.id=/d'' /home/hadoop/hello-samza/deploy/kafka/config/server.properties;echo broker.id=$count >> /home/hadoop/hello-samza/deploy/kafka/config/server.properties"
	# start kafka
	ssh -o stricthostkeychecking=no -i "$pemfile" hadoop@"$private_ip" 'cd /home/hadoop/hello-samza;bin/grid start kafka&'
	# iptables
	ssh -o stricthostkeychecking=no -i "$pemfile" hadoop@"$private_ip" 'sudo service iptables save;sudo service iptables stop;sudo chkconfig iptables off'
	count=$((count+1))
done

ownDns=`ifconfig eth0 | grep inet | grep -v inet6 | awk '{print $2}' | cut -d ':' -f2`
ipArray=($iplist)
ipArray+=($ownDns)
brokerArray=( "${ipArray[@]/%/:9092}" )
brokerList=`join_arr , "${brokerArray[@]}"`
echo -e "\033[92mThe IP of the master is: $ownDns\e[0m"
echo -e "\033[92mThe IP list of Samza brokers in the cluster is given below for your reference. Copy it for pasting into .properties file!\e[0m"
echo -e "\033[92m$brokerList\e[0m"
