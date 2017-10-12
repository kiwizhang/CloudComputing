package cc.cmu.edu.minisite;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JOptionPane;

import org.json.JSONObject;

import org.json.JSONArray;

public class ProfileServlet extends HttpServlet {
	
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_NAME = "flykiwi";
    private static final String URL = "jdbc:mysql://flykiwi.clnmtgvkqmer.us-east-1.rds.amazonaws.com/" + DB_NAME;

    private static final String DB_USER = "flykiwi";
    private static final String DB_PWD = "painting";
    private List<Connection> connectionPool = new ArrayList<Connection>();


//    private static Connection conn;

    public ProfileServlet() {
        /*
            Your initialization code goes here
        */
    }
    



    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) 
            throws ServletException, IOException {
        JSONObject result = new JSONObject();

        String id = request.getParameter("id");
        String pwd = request.getParameter("pwd");

        /*
            Task 1:
            This query simulates the login process of a user, 
            and tests whether your backend system is functioning properly. 
            Your web application will receive a pair of UserID and Password, 
            and you need to check in your backend database to see if the 
	    UserID and Password is a valid pair. 
            You should construct your response accordingly:

            If YES, send back the user's Name and Profile Image URL.
            If NOT, set Name as "Unauthorized" and Profile Image URL as "#".
        */
        Statement st1 = null;
        String sql1 = null;
        Statement st2 = null;
        String sql2 = null;
        Connection conn = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;
        
        try {
        	 Class.forName(JDBC_DRIVER);
             conn = DriverManager.getConnection(URL, DB_USER, DB_PWD);
            
            if (id != null && pwd != null) {
            	st1 = conn.createStatement();
                sql1 = "Select * from users Where UserId='" + id + "' and password='" + pwd + "'";
                rs1 = st1.executeQuery(sql1);
                if (rs1.next()) {
                	st2 = conn.createStatement();
                    sql2 = "Select * from userprofile Where UserId='" + id + "'";
                    rs2 = st2.executeQuery(sql2);
                    while (rs2.next()) {
                    	String username = rs2.getString("UserName");
                        String url = rs2.getString("url");
                        result.put("name", username);
                        result.put("profile", url);
                    }
                    

                	
                } else {
                    System.out.println("This user does not exist.");
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }catch (ClassNotFoundException e) { 
        	e.printStackTrace();
        }finally {
            if (st1 != null) {
                try {
                    st1.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (st2 != null) {
                try {
                    st2.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) { /* ignored */}
            }
            
        }

            // You can also validate user by result size if its comes zero user is invalid else user is valid

        

        PrintWriter writer = response.getWriter();
        writer.write(String.format("returnRes(%s)", result.toString()));
        writer.close();
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
    

}
