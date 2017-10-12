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
import java.util.Random;

public class shuffleData {

	public static void main(String args[]) {

		int count = 0;

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));

			String line;
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			String oldKey = null;
			Random random = new Random();
			// While we have input on stdin
			while ((line = br.readLine()) != null) {
				// Initialize Tokenizer on string input
				String[] textArray = line.split("\t");
				String newDate = textArray[2].replace(textArray[2].charAt(5), (char)(random.nextInt(9) + 1));
				newDate = newDate.replace(textArray[2].charAt(7), (char)(random.nextInt(9) + 1));


				line.replace(textArray[2], newDate);
				System.out.println(line);
			
			}
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}

}
}
