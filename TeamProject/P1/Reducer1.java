import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
 
public class Reducer1 {
 
    public static void main (String args[]) {    

        String currentId = null;  
        int count = 0;
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//            BufferedReader br = new BufferedReader(new FileReader("/Users/Jiwei/Desktop/cc/TeamProject/P1/mapper_result"));

            String line;            
            
            //While we have input on stdin
            while((line = br.readLine()) != null){
            	StringBuilder sb = new StringBuilder();
                String[] split_result = line.split("\t");
                String id = split_result[0];
                if (currentId != null && currentId.equals(id)) {
                	continue;
                } else {
                	if (currentId != null && split_result.length == 2) {
                		sb.append(currentId).append("\t");
                		String[] splitArray = split_result[1].split("&&");
                	
                		if (splitArray != null && splitArray.length > 0) {
                			for (String item: splitArray) {
                				sb.append(item).append("\t");
                			}
                		}
                		System.out.println(sb.toString());
                		count++;
                	}
            		currentId = id;

                }
            }
            System.out.println(count);
        } catch (IOException e1) {
			e1.printStackTrace();
		}   
    }            	                                     
           
}