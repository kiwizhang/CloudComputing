import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
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
import java.text.SimpleDateFormat;
import java.util.*;
import java.math.RoundingMode;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Mapper1 {
	
    public static void main (String args[]) {

    	try { 		
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
        String line;
            //While we have input on stdin
        while((line = br.readLine()) != null){
                //Initialize Tokenizer on string input   	
        		new PrintStream(System.out, true, "UTF-8").println(line.replaceFirst(",","\t"));
        	}
        	
        }

    	} catch (FileNotFoundException e) {
    	 e.printStackTrace();
    	 } catch (IOException e) {
    	 e.printStackTrace();
    	 } catch (ParseException e) {
    	 e.printStackTrace();
    	 } catch (Exception e) {
            e.printStackTrace();
         }

    	
    }


}
