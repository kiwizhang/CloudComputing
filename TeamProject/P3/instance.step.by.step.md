# 1. login to mysql, username: root, password: root;

# 2. create database: create database `15619`;

# 3. repalce DNS in App.java, using sudo

# 4. redirect port request:
sudo iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to-ports 8080
sudo iptables -t nat -I OUTPUT -p tcp -d 127.0.0.1 --dport 80 -j REDIRECT --to-ports 8080

# 5. restore database, cd to database backup folder:
mysql -uroot -p 15619 < database_backup_20160329.sql

# 6. compile project: sudo mvn compile

# 7. launch server: sudo mvn exec:java