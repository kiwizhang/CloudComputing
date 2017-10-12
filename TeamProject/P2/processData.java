import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;


public class processData {
    
    public static void main (String args[]) {
        
        int count = 0;
        
        try {       
            //BufferedReader br = new BufferedReader(new FileReader("/Users/Jiwei/Desktop/cc/TeamProject/P2/outputsorted"));
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
            String line;
            HashMap<String, Integer> map = new HashMap<String, Integer>();
            String oldKey = null;
            //While we have input on stdin
            while((line = br.readLine()) != null) {
                //Initialize Tokenizer on string input
                String[] textArray = line.split("\t");
                String newKey = textArray[1] + "&" + textArray[2];
                if (oldKey == null || newKey.equals(oldKey)) {
                    for (int i = 3; i < textArray.length; ++i) {
                        //System.out.println("word: " + textArray[i]);
                        String[] word_count = textArray[i].split(":");
                        if (map.containsKey(word_count[0])) {
                            map.put(word_count[0], map.get(word_count[0]) + Integer.parseInt(word_count[1]));
                        } else {
                            map.put(word_count[0], Integer.parseInt(word_count[1]));
                        }
                    }
                } else {
                    if (oldKey != null) {
                        //System.out.println("New Key " + newKey);
                        for (Map.Entry<String, Integer> entry : map.entrySet()) {
                            String[] idanddate = oldKey.split("&");
                            System.out.println(idanddate[0] + "\t" + idanddate[1].replace("-" , "") + "\t" + entry.getKey() + "\t" + entry.getValue());
                            count++;
                        }
                    }
                    map  = new HashMap<String, Integer>();
                    for (int i = 3; i < textArray.length; i++) {
                        String[] word_count = textArray[i].split(":");
                            map.put(word_count[0], Integer.parseInt(word_count[1]));
                    }
                }
                oldKey = newKey;
            }
            if (!map.isEmpty()) {
                for (Map.Entry<String, Integer> entry : map.entrySet()) {
                    String[] idanddate = oldKey.split("&");
                    System.out.println(idanddate[0] + "\t" + idanddate[1].replace("-", "") + "\t" + entry.getKey() + "\t" + entry.getValue());
                    count++;
                }
            }
        } catch (FileNotFoundException e) {
           e.printStackTrace();
        } catch (IOException e) {
           e.printStackTrace();
        } 
    }
}
