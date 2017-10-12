import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import java.beans.PropertyVetoException;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DAOFactory extends AbstractDAOFactory {
    public static final String DRIVER = "com.mysql.jdbc.Driver";
    public static final String DBURL = "jdbc:mysql://localhost:3306/15619?useUnicode=true&characterEncoding=utf8";
    public static List<Connection> connectionPool = new ArrayList<Connection>();
    public static BoneCP connectionPoolBoneCP = null;
    public static BoneCPConfig config = null;
    private static ComboPooledDataSource dataSource = null;

    public static Connection createConnection() throws Exception {
        if (connectionPool.size() > 0) {
            return connectionPool.remove(connectionPool.size() - 1);
        }
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println("Cannot Find MySQL Driver.");
            throw new Exception(e);
        }
        try {
            return (Connection) DriverManager.getConnection(DBURL, "root", "root");
        } catch (SQLException e) {
            System.out.println("Cannot Get Connection.");
            throw new Exception(e);
        }
    }

    public static Connection createConnectionBoneCP() throws Exception {
	if (connectionPoolBoneCP != null) {
	    return connectionPoolBoneCP.getConnection();
	}
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println("Cannot Find MySQL Driver.");
            throw new Exception(e);
        }
        try {
	    config = new BoneCPConfig();
	    config.setJdbcUrl(DBURL);
	    config.setUsername("root");
	    config.setPassword("root");
	    config.setMinConnectionsPerPartition(10);
            config.setMaxConnectionsPerPartition(50);
	    config.setPartitionCount(10);
	    connectionPoolBoneCP = new BoneCP(config);
	    System.out.println("BoneCP config successfully");		
	    return connectionPoolBoneCP.getConnection();
        } catch (SQLException e) {
            System.out.println("Cannot Get Connection. - BoneCP");
            throw new Exception(e);
        }

    }

    public static Connection createConnectionC3P0() throws Exception {
	if (dataSource != null) {
	    return dataSource.getConnection();
	}
	dataSource = new ComboPooledDataSource();
	try {
	    dataSource.setDriverClass(DRIVER);
	} catch (PropertyVetoException e) {
	    e.printStackTrace();
	}
	dataSource.setJdbcUrl(DBURL);
	dataSource.setUser("root");
	dataSource.setPassword("root");
	dataSource.setMinPoolSize(5);
	dataSource.setAcquireIncrement(5);
	dataSource.setMaxPoolSize(20);
	return dataSource.getConnection();
    }
    @Override
    public TweetDAO getTweetDAO() {
        return new TweetDAO();
    }
}
