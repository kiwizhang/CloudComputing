import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.sql.PreparedStatement;

public class TweetDAO {
    private Connection con = null;
    private Connection conBoneCP = null;
    private Connection conC3P0 = null;
    private String tableName = "q3";

    public TweetDAO() {
        try {
	    this.con = DAOFactory.createConnectionBoneCP();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String q4Put(String tweetid, String fields, String payload) {
	    String flag = null;
        String sql = null;

        String[] flds = fields.split(",");
        int len = flds.length;
        String[] values = new String[len];
        String[] vals = payload.split(",");
        int index = 0;
        for (String v : vals) {
            values[index] = v;
            index++;
        }
        for (int i = index; i < len; ++i) {
            values[i] = "";
        }

        sql = "INSERT INTO q4 (tweetid," + fields + ") VALUES (?," ;
        for (int i = 0; i < len; ++i) {
            sql = sql + "?,";
        }
        sql = sql.substring(0, sql.length() - 1);
        sql = sql + ") ON DUPLICATE KEY UPDATE ";
        for (int i = 0; i < len; ++i) {
            sql = sql + flds[i] + "=?,";
        }
        sql = sql.substring(0, sql.length() - 1);

        //System.out.println(sql);
        try {
	       //System.out.print("*** inside try, connection is closed? " + this.con.isClosed());

	        this.con = this.con.isClosed() ? DAOFactory.createConnectionBoneCP() : this.con;
            PreparedStatement stmt = this.con.prepareStatement(sql);
            stmt.setString(1, tweetid);

            for (int i = 1; i <= len; ++i) {
                stmt.setString(i+1, values[i-1]);
            }
            for (int i = 1; i <= len; ++i) {
                stmt.setString(len+i+1, values[i-1]);
            }
            //System.out.println(stmt.toString());
	       //System.out.println("got statement");
            stmt.execute();
            //System.out.println("Inserted into table");
            stmt.close();
	       flag = "success";
        } catch (Exception e) {
	       e.printStackTrace();
	       flag = "exception";
	        //System.out.println("Q4 Put Excetpion");
            //return "exception";
        } finally {
            // if (this.con != null) {
            //     try {
            //         this.con.close();
            //     } catch (SQLException e1) {
            //         e1.printStackTrace();
            //     }
            // }
        }
        return flag;
    }

    public String q4Get(String tweetid, String fields) {
        String result = "";
        try {
            String sql = "select " + fields + " from q4 where tweetid = " + tweetid;
            //System.out.println(sql);
            Statement stmt = this.con.createStatement();
            stmt.execute(sql);
            String[] flds = fields.split(",");
            ResultSet rs = stmt.getResultSet();
            if (rs.next()) {
                for (String f : flds) {
                    result = result + rs.getString(f);
                }
            }
            //System.out.println("Finish getting from database");
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // if (this.con != null) {
            //     try {
            //         this.con.close();
            //     } catch (SQLException e1) {
            //         e1.printStackTrace();
            //     }
            // }
        }
        return result.replaceAll(" ", "+");
    }
    /**
     * This method is used for Q2.
     */
    public String getTweetStringByUseridAndHashtag(String userId, String hashtag) {
	String tweets = null;
        try {
            String sql = "select tweet_text from q2 where id = binary\'" + userId + "&"+ hashtag + "\';";
            Statement stmt = this.con.createStatement();
            stmt.execute(sql);
            ResultSet rs = stmt.getResultSet();
            if (rs.next()) {
		tweets = rs.getString("tweet_text");
            }
            rs.close();
            stmt.close();
	    return tweets;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
	    if (this.con != null) {
                try {
                    this.con.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
	}
        return tweets;
    }
    /**
     * This method is used for Q3.
     */
    public String getwordcount(String sd, String ed, String su, String eu, String words) {
        Map<String, Integer> wordcountmap = new HashMap<String, Integer>();
        String[] wordslist = words.split(",");
        wordcountmap.put(wordslist[0], 0);
        wordcountmap.put(wordslist[1], 0);
        wordcountmap.put(wordslist[2], 0);
        sd = sd.replace("-", "");
        ed = ed.replace("-", "");
        try {
            String sql = String.format("select words from %s where user_id>=%s and user_id<=%s and date>=%s and date<=%s", this.tableName, su, eu, sd, ed);
            Statement stmt = this.con.createStatement();
            stmt.execute(sql);
            ResultSet rs = stmt.getResultSet();
            while (rs.next()) {
                String text = rs.getString("words");
                String[] list = text.split(" +");
                for (String s : list) {
                    String[] wc = s.split(":");
                    if (wordcountmap.containsKey(wc[0])) {
                        wordcountmap.put(wc[0], wordcountmap.get(wc[0]) + Integer.parseInt(wc[1]));
                    }
                }
            }
            rs.close();
            stmt.close();
            DAOFactory.connectionPool.add(this.con);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (this.con != null) {
                try {
                    this.con.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        }
        StringBuilder result = new StringBuilder();
        for (String s : wordslist) {
            result.append(s).append(":").append(wordcountmap.get(s)).append("\n");
        }
        return result.toString();
    }
}
