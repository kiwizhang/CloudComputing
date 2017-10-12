import java.awt.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Predictor2 {

  public static class SortMapper
       extends Mapper<Object, Text, Text, Text>{

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();
    private Map<String, String> map = new HashMap<String, String>();
    private String prev = null;
    private int prevcount;
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      String line = value.toString();     
      String[] lineArray = line.split("\t");
      String phrase = lineArray[0];
      String count = lineArray[1];
      if (lineArray[0].split(" ").length > 1) {
    	  String keyword = line.substring(0, phrase.lastIndexOf(" ")).trim();
    	  String lastword = line.substring(phrase.lastIndexOf(" ")+1).trim();
    	  word.set(keyword);
		  context.write(word, new Text(lastword + "&" + count));
      }
    }
  }

  public static class TextReducer
       extends TableReducer<Text,Text,ImmutableBytesWritable> {
    
    private class pair {
    	public String oneword;
    	public int count;
    	
    	private pair(String oneword, int count) {
    		this.oneword = oneword;
    		this.count = count;
    	}
    }

    public void reduce(Text key, Iterable<Text> values,
                       Context context
                       ) throws IOException, InterruptedException {
    	ArrayList<pair> pairs = new ArrayList<pair>();
        for (Text val : values) {
          String[] split = val.toString().split("&");
          pairs.add(new pair(split[0], Integer.parseInt(split[1])));
        }
        
        Collections.sort(pairs,new Comparator<pair>(){
            public int compare(pair s1,pair s2){
                  // Write your logic here.
            	if (s1.count == s2.count) {
					return s1.oneword.compareTo(s2.oneword);
				}
            	return s2.count - s1.count;
            }});
        String resultString = "";
        
        Configuration conf = context.getConfiguration();
        int param = Integer.parseInt(conf.get("topn"));

        for (int j = 0; j < pairs.size() && j < param; j++) {
            resultString += pairs.get(j).oneword + "&" + pairs.get(j).count + ",";
        }
        resultString = resultString.substring(0, resultString.length()-1);        
		
        Put put = new Put(key.toString().trim().getBytes());//initial Put
        //add to column family word
        put.add(Bytes.toBytes("words"), key.getBytes(), Bytes.toBytes(resultString.toString()));
        context.write(new ImmutableBytesWritable(key.getBytes()), put);    
         
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    conf.set("topn", args[1]);
    Job job = Job.getInstance(conf, "predictor");
    job.setJarByClass(Predictor2.class);
    job.setMapperClass(SortMapper.class);
    job.setCombinerClass(TextReducer.class);
    job.setReducerClass(TextReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}