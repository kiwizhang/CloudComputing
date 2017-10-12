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
import java.util.*;


public class firstlinks_reducer {
	
    public static void main (String args[]) {
    	
    	int count = 0;
    	
      	try { 		
            //BufferedReader br = new BufferedReader(new FileReader("/Users/Jiwei/Desktop/cc/TeamProject/P2/outputsorted"));
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
            String line;
            ArrayList<String> idlist = new ArrayList<String>();
		    String oldKey = null;
            //While we have input on stdin
            while((line = br.readLine()) != null) {
                //Initialize Tokenizer on string input
        		String[] textArray = line.split(",");
        		String newKey = textArray[0];
        		if (oldKey == null || newKey.equals(oldKey)) {

                    idlist.add(textArray[1]);
        		
        		} else {
        			if (oldKey != null) {
                        //System.out.println("New Key " + newKey);
                        System.out.print(oldKey + "&");
        				for (int i = 0; i < idlist.size() - 1; i++) {
            				System.out.print(idlist.get(i) + ",");
        				}
                        System.out.print(idlist.get(idlist.size()-1));
                        System.out.print("\n");
        			}
        			idlist = new ArrayList<String>();
                    idlist.add(textArray[1]);

        		}
                oldKey = newKey;
            }
            if (idlist.size() != 0) {
                System.out.print(oldKey + "&");
      		    for (int i = 0; i < idlist.size() - 1; i++) {
                    System.out.print(idlist.get(i) + ",");
                }
                System.out.print(idlist.get(idlist.size()-1));
      	    }
        } catch (FileNotFoundException e) {
    	   e.printStackTrace();
    	} catch (IOException e) {
    	   e.printStackTrace();
    	} 
    }
}
