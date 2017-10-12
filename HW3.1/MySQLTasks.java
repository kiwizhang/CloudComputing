import java.sql.*;

public class MySQLTasks {

    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_NAME = "song_db";
    private static final String URL = "jdbc:mysql://localhost/" + DB_NAME;

    private static final String DB_USER = "root";
    private static final String DB_PWD = "db15319root";

    private static Connection conn;

    /**
     * You should complete the missing parts in the following method. Feel free to add helper functions if necessary.
     *
     * For all questions, output your answer in one single line, i.e. use System.out.print().
     *
     * @param args The arguments for main method.
     */
    public static void main(String[] args) {
        try {
            initializeConnection();
            // This argument should be used to determine the piece(s) of your code to run.
            String runOption = args[0];
            switch (runOption) {
                // Run the demo function.
                case "demo":
                    demo();
                    break;
                // Load data from the csv files into corresponding tables.
                case "load_data":
                    loadData();
                    break;
                // Answer question 7.
                case "q7":
                    q7();
                    break;
                // Answer question 8.
                case "q8":
                    // For q8, there should be an args[1] which is the name (NOT field) of your intended database index.
                    q8(args[1]);
                    break;
                // Answer question 9.
                case "q9":
                    q9();
                    break;
                // Answer question 10.
                case "q10":
                    q10();
                    break;
                // Answer question 11.
                case "q11":
                    q11();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Initializes database connection.
     *
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private static void initializeConnection() throws ClassNotFoundException, SQLException {
        Class.forName(JDBC_DRIVER);
        conn = DriverManager.getConnection(URL, DB_USER, DB_PWD);
    }

    /**
     * JDBC usage demo. The following function will print the row count of the "songs" table.
     * Table must exists before this function is called.
     */
    private static void demo() {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            String tableName = "songs";
            String sql = "SELECT count(*) AS cnt FROM " + tableName;
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                int rowCount = rs.getInt("cnt");
                System.out.println("Total number of lines in " + tableName + " is: " + rowCount);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Load data.
     * 
     * This method should load data from csv files into corresponding tables.
     * Complete this method with your own implementation.
     * 
     * You are allowed to make changes such as modifying method name, parameter list and/or return type.
     */
    private static void loadData() {
    	Statement stmt;
		try {
			stmt = conn.createStatement();
	    	stmt.execute("LOAD DATA LOCAL INFILE 'million_songs_metadata.csv'"
	    			+ "INTO TABLE songs"
	    			+ "FIELDS TERMINATED BY ','"
	    			+ "ENCLOSED BY '\"'LINES TERMINATED BY '\n");

	    	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }

    /**
     * Question 7.
     * 
     * This method should execute a SQL query and print the trackid of the song with the maximum duration.
     * If there are multiple answers, simply print any one of them. Do NOT hardcode your answer.
     * 
     * You are allowed to make changes such as modifying method name, parameter list and/or return type.
     */
    private static void q7() {
    	Statement stmt;
		try {
			stmt = conn.createStatement();
	    	ResultSet rs = stmt.executeQuery("SELECT track_id FROM songs WHERE duration = (SELECT max(duration) FROM songs)");
	    	while(rs.next()) {
	    		System.out.println(rs.getString("track_id"));
	    	}
	    
	    	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * Question 8.
     * 
     * A database index is a data structure that improves the speed of data retrieval.
     * Identify the field that will improve the performance of your query in question 7
     * and create a database index on that field. A custom index name is needed to create an index.
     * 
     * You are allowed to make changes such as modifying method name, parameter list and/or return type.
     *
     * @param indexName The name of your index (this is NOT the field on which your index will be created).
     */
    private static void q8(String indexName) {
    	Statement stmt;
		try {
			stmt = conn.createStatement();
	    	stmt.execute("CREATE INDEX " + indexName +" ON songs(duration)");

	    	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * Question 9.
     * 
     * This method should execute a SQL query and return the trackid of the song with the maximum duration.
     * If there are multiple answers, simply print any one of them. Do NOT hardcode your answer.
     * 
     * This is the same query as Question 7. Do you see any difference in performance?
     * 
     * You are allowed to make changes such as modifying method name, parameter list and/or return type.
     */
    private static void q9() {
    	Statement stmt;
		try {
			stmt = conn.createStatement();
	    	ResultSet rs = stmt.executeQuery("SELECT track_id FROM songs WHERE duration = (SELECT max(duration) FROM songs)");
	    	while(rs.next()) {
	    		System.out.println(rs.getString("track_id"));
	    	}
	    	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * Question 10.
     * 
     * Write the SQL query that returns all matches (across any column), similar to the command grep -P 'The Beatles' | wc -l:
     * Do NOT hardcode your answer.
     * 
     * You are allowed to make changes such as modifying method name, parameter list and/or return type.
     */
    private static void q10() {
    
    	Statement stmt;
		try {
			stmt = conn.createStatement();
			String query = "SELECT count(*) as countNumber FROM songs WHERE (title LIKE BINARY '%The Beatles%' OR `release` LIKE BINARY '%The Beatles%' OR artist_name LIKE BINARY '%The Beatles%')";
				
	    	ResultSet rs = stmt.executeQuery(query);
	    	while(rs.next()) {
	    		System.out.println(rs.getInt("countNumber"));
	    	}      

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * Question 11.
     * 
     * Which artist has the third-most number of rows in table songs? The output should be the name of the artist.
     * Please use artist_id as the unique identifier of the artist.
     * If there are multiple answers, simply print any one of them. Do NOT hardcode your answer.
     * 
     * You are allowed to make changes such as modifying method name, parameter list and/or return type.
     */
    private static void q11() {
    	
    	Statement stmt;
		try {
			stmt = conn.createStatement();
	    	ResultSet rs = stmt.executeQuery("SELECT artist_name FROM songs GROUP BY artist_id ORDER BY COUNT(artist_id) DESC LIMIT 1 offset 2");
	    	while(rs.next()) {
	    		System.out.println(rs.getString("artist_name"));
	    	} 

	    	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
