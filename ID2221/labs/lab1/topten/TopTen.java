package id2221.topten;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;

public class TopTen {
    // This helper function parses the stackoverflow into a Map for us.
    public static Map<String, String> transformXmlToMap(String xml) {
    	Map<String, String> map = new HashMap<String, String>();
    	try {
    	    String[] tokens = xml.trim().substring(5, xml.trim().length() - 3).split("\"");

    	    for (int i = 0; i < tokens.length - 1; i += 2) {
        		String key = tokens[i].trim();
        		String val = tokens[i + 1];
        		map.put(key.substring(0, key.length() - 1), val);
    	    }
    	} catch (StringIndexOutOfBoundsException e) {
    	    System.err.println(xml);
    	}

    	return map;
    }

    public static class TopTenMapper extends Mapper<Object, Text, NullWritable, Text> {
      	// Stores a map of user reputation to the record
      	TreeMap<Integer, Text> repToRecordMap = new TreeMap<Integer, Text>();

      	public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
      	    Map<String, String> parsed = TopTen.transformXmlToMap(value.toString());
      	    if (parsed == null) {
      		      return;
      	    }

      	    String userId = parsed.get("AccountId");
      	    String reputation = parsed.get("Reputation");
      	    if (userId == null || reputation == null) {
      		      return;
      	    }

      	    // Add this record to our map with the reputation as the key
      	    repToRecordMap.put(Integer.parseInt(reputation), new Text(value));

      	    // If we have more than ten records, remove the one with the lowest reputation.
      	    if (repToRecordMap.size() > 10) {
      		      repToRecordMap.remove(repToRecordMap.firstKey());
      	    }
    	   }

    	protected void cleanup(Context context) throws IOException, InterruptedException {
    	    // Output our ten records to the reducers with a null key
    	    for (Text t : repToRecordMap.values()) {
    		      context.write(NullWritable.get(), t);
    	    }
	     }
  }

    public static class TopTenReducer extends TableReducer<NullWritable, Text, NullWritable> {
    	// Stores a map of user reputation to the record
    	// Overloads the comparator to order the reputations in descending order
    	private TreeMap<Integer, Text> repToRecordMap = new TreeMap<Integer, Text>();

    	public void reduce(NullWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
    	    for (Text value : values) {
        		Map<String, String> parsed = TopTen.transformXmlToMap(value.toString());
        		String reputation = parsed.get("Reputation");

        		// Add this record to our map with the reputation as the key
        		repToRecordMap.put(Integer.parseInt(reputation), new Text(value));

        		// If we have more than ten records, remove the one with the lowest reputation.
        		if (repToRecordMap.size() > 10) {
        		    repToRecordMap.remove(repToRecordMap.firstKey());
        		}
    	    }

          // HBase operations
    	    for (Text t : repToRecordMap.descendingMap().values()) {
        		Map<String, String> parsed = TopTen.transformXmlToMap(t.toString());
        		String rep = parsed.get("Reputation");
        		String id = parsed.get("AccountId");

        		Put insHBase = new Put(rep.getBytes());

        		// insert sum value to hbase
        		insHBase.addColumn(Bytes.toBytes("info"), Bytes.toBytes("rep"), Bytes.toBytes(rep));
        		insHBase.addColumn(Bytes.toBytes("info"), Bytes.toBytes("id"), Bytes.toBytes(id));

        		// write data to Hbase table
        		context.write(null, insHBase);
    	    }
    	}
    }

    public static void main(String[] args) throws Exception {
    	Configuration conf = HBaseConfiguration.create();

    	Job job = Job.getInstance(conf, "TopTen");
    	job.setJarByClass(TopTen.class);
    	job.setMapperClass(TopTenMapper.class);
    	job.setReducerClass(TopTenReducer.class);

    	TableMapReduceUtil.initTableReducerJob("topten", TopTenReducer.class, job);

    	job.setOutputKeyClass(NullWritable.class);
    	job.setOutputValueClass(Text.class);

    	job.setNumReduceTasks(1);

    	FileInputFormat.addInputPath(job, new Path(args[0]));
    	System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
