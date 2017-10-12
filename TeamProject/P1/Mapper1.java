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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Mapper1 {
	
    public static void main (String args[]) {
    	
    	DecimalFormat f = new DecimalFormat("##.000");
    	String[] stopWordsList = {"a","able","about","across","after","all","almost","also","am","among","an","and","any","are","as","at","be","because","been","but","by","can","cannot","could","did","do","does","either","else","ever","every","for","from","get","got","had","has","have","he","her","hers","him","his","how","however","i","if","in","into","is","it","its","just","least","let","likely","may","me","might","most","must","my","neither","nor","not","of","off","often","on","only","or","other","our","own","rather","said","say","says","she","should","since","so","some","than","that","the","their","them","then","there","these","they","this","tis","to","too","twas","us","wants","was","we","were","what","when","where","which","while","who","whom","why","will","with","would","yet","you","your"};
    	//stopWordSet to store stop words
    	HashSet<String> stopWordSet = new HashSet<String>();
    	for (String word: stopWordsList) {
    		stopWordSet.add(word);
    	}
    	HashSet<String>	censorWords = new HashSet<String>();
    	String lineCensor;
        try {
        BufferedReader brCensor = new BufferedReader(new FileReader("/Users/Jiwei/Desktop/cc/TeamProject/P1/censorWords"));

        while((lineCensor = brCensor.readLine()) != null){
				censorWords.add(lineCensor.trim());
        } 
		} catch (IOException e1) {
			e1.printStackTrace();
		}   
    	
    	// scoreMap to store word and its sentiment score
    	HashMap<String, String> scoreMap = new HashMap<String, String>();
    	String lineScore;
        try {
        BufferedReader brScore = new BufferedReader(new FileReader("/Users/Jiwei/Desktop/cc/TeamProject/P1/sentimentScore"));

        while((lineScore = brScore.readLine()) != null){
				scoreMap.put(lineScore.split("\t")[0], lineScore.split("\t")[1]);
        } 
		} catch (IOException e1) {
			e1.printStackTrace();
		}    	
    	JSONParser parser = new JSONParser();
    	
    	//idset to store tweet_id; when it is full, the oldest id will be cleared
    	HashSet<String> idset = new HashSet<String>();
    	try { 		
        BufferedReader br = new BufferedReader(new FileReader("/Users/Jiwei/Desktop/cc/TeamProject/P1/part-00000"));
        String line;
            //While we have input on stdin
        while((line = br.readLine()) != null){
                //Initialize Tokenizer on string input
        	double sentimentScore = 0;
        	int EWC = 0;
        	double density = 0;
        	Object obj = parser.parse(line);
        	JSONObject jsonObject = (JSONObject) obj;
        	String id = (String) jsonObject.get("id_str");

        	String created_at = (String) jsonObject.get("created_at");
        	String text = (String) jsonObject.get("text");
        	JSONObject user = (JSONObject) jsonObject.get("user");
        	String userid = (String) user.get("id_str");
        	

        	if (jsonObject == null || id == null || id.length() == 0 || idset.contains(id) || created_at == null || created_at.length() == 0 || text == null || text.length() == 0) {
        		continue;

        	} else {
        		idset.add(id);
        		String[] textArray = text.trim().split("[^a-zA-Z0-9]+");
        		for (int i = 0; i < textArray.length; i++) {
        			String oneWord = textArray[i].toLowerCase();
        			if (oneWord.equals("") || stopWordSet.contains(oneWord)) {
        				continue; 
        			} else {
        				EWC++;
        				if (scoreMap.containsKey(oneWord)) {
        					sentimentScore = sentimentScore + Integer.parseInt(scoreMap.get(oneWord));
        				}
        				if (censorWords.contains(oneWord)) {
        					char[] oneWordArray = oneWord.toCharArray();
        					for (int j = 1; j < oneWordArray.length - 1; j++) {
        						oneWordArray[j] ='*';
        					}
        					text = text.replaceAll(oneWord, String.valueOf(oneWordArray));
        				}
        				
        			}     			
        		}
        		text = text.replaceAll("\t", " ");
				text = text.replaceAll("\n", " ");
				text = text.replaceAll("&&", " ");
        		density = sentimentScore / EWC ;
        		String densityScore = f.format(density);
        		StringBuilder outputSb = new StringBuilder();
            	outputSb.append(userid).append("&&").append(created_at).append("&&").append(densityScore).append("&&").append(text);
            	String[] tagtextArray = text.split("#");
            	//append the hashtags
            	if (tagtextArray != null && tagtextArray.length != 0) {
            		outputSb.append("&&");
            		for (String tagtext: tagtextArray) {
            			String[] tagArray = tagtext.split("[\\p{Punct}\\s]+");
            			if (tagArray != null && tagArray.length > 0) {
            				outputSb.append(" " + tagArray[0]);
            			} else {
            				//if the text end with hashtag
            				outputSb.append(" " + tagtext);
            			}
                		
                	}
            	}
            	
        		System.out.println(id+'\t'+outputSb.toString());
        	}
        	
        }

    	} catch (FileNotFoundException e) {
    	 e.printStackTrace();
    	 } catch (IOException e) {
    	 e.printStackTrace();
    	 } catch (ParseException e) {
    	 e.printStackTrace();
    	 }

    	
    }


}
