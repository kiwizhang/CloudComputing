import java.util.HashMap;


public class TweetBean {
    private static String[] fields = {"tweetid", "userid", "username", "timestamp", "text", "hashtag", "ip", "coordinates", "repliedby", "reply_count", "mentioned", "mentionedby", "mentioned_count", "favoritedby", "favorite_count", "useragent", "filter_level", "lang"};
    private static HashMap<String, String> map = new HashMap<String, String>();
    public String get(String field) {
        return !validField(field) || map.get(field) == null ? "" : map.get(field);
    }
    public void set(String field, String value) {
        if (validField(field)) {
            map.put(field, value.replaceAll("\\s+", "+"));
        }
    }
    public void setTweetId(String tweetid) {
        map.put("tweetid", tweetid);
    }
    private boolean validField(String field) {
        for (String item: fields) {
            if (item.equals(field)) {
                return true;
            }
        }
        return false;
    }
}
