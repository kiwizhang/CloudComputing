import java.util.LinkedHashMap;
import java.util.Map;


public class CacheUtil {
    private final static int CACHE_SIZE = 500000;
    @SuppressWarnings("serial")
    private LinkedHashMap<String, TweetBean> cacheMap = new LinkedHashMap<String, TweetBean>(CACHE_SIZE, .75F, true) {
        protected boolean removeEldestEntry(Map.Entry<String, TweetBean> eldest) {
            return size() > CACHE_SIZE;
        }
    };
    /**
     * check to see if the cache contain the specified tweet id.
     * @param tweetId
     * @return true or false
     */
    public boolean contains(String tweetId) {
        return cacheMap.containsKey(tweetId);
    }
    /**
     * get specified field, if the cache does not contain the tweet id then return empty string.
     * @param tweetId
     * @param field
     * @return the value of specific field or an empty string.
     */
    public String read(String tweetId, String field) {
        if (cacheMap.containsKey(tweetId)) {
            TweetBean tweet = cacheMap.get(tweetId);
            return tweet.get(field);
        } else {
            return "";
        }
    }
    /**
     * update the cache with tweet id, field and payload.
     * @param tweetId
     * @param fields
     * @param payload
     */
    public void update(String tweetId, String fields, String payload) {
        TweetBean tweet = null;
        if (cacheMap.containsKey(tweetId)) {
            tweet = cacheMap.get(tweetId);
        } else {
            tweet = new TweetBean();
        }
        String[] fieldsList = fields.split(",");
        String[] payloadList = payload.split(",");
        if (fieldsList.length < payloadList.length) {
            try {
                throw new Exception("Exception from caching, number of fileds is less than payload's");
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        } else if (fieldsList.length > payloadList.length) {
            if (payload.charAt(payload.length() - 1) != ',') {
                try {
                    throw new Exception("Exception from caching, number of fileds and payload does not match");
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            } else {
                String[] temp = new String[fieldsList.length];
                for (int i = 0; i < temp.length; i ++) {
                    temp[i] = i < payloadList.length ? payloadList[i] : "";
                }
                payloadList = temp;
            }
        }
        tweet.setTweetId(tweetId);
        for (int i = 0; i < fieldsList.length; i ++) {
            tweet.set(fieldsList[i], payloadList[i]);
        }
        cacheMap.put(tweetId, tweet);
    }
}
