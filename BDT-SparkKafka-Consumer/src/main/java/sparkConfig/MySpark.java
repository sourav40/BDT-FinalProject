package sparkConfig;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import model.Tweet;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFunction;

import databaseManager.HBaseDBManager;
import scala.Tuple2;

public class MySpark {
	static int counter = 1;
	private JavaSparkContext sc;
	private HBaseDBManager db;
	private HashMap<String, String> mapKeywords;

	public MySpark() throws IOException {
		this.sc = new JavaSparkContext("local[*]", "MySpark", new SparkConf());
		this.db = new HBaseDBManager();
		this.mapKeywords = this.db.GetKeywords();
	}

	public void Process(String key, List<String> l) throws IOException {
		System.out.println("consumed message -->" + l);
		JavaRDD<String> list = this.sc.parallelize(l)
				.flatMap(line -> Arrays.asList(line.toUpperCase().split("RT")))
				.filter(line -> !line.isEmpty());
		JavaPairRDD<String, Tweet> tweetResults = list.mapToPair(
				new MyPairFunction(key, this.mapKeywords)).sortByKey();
		System.out.println("key-->" + key);
		System.out.println("value-->" + tweetResults.values());
		this.db.WriteTweetAnalysis(key, tweetResults.values());
	}

	static class MyPairFunction implements PairFunction<String, String, Tweet>,
			Serializable {
		private String key;
		private HashMap<String, String> map;

		public MyPairFunction(String k, HashMap<String, String> m) {
			this.key = k;
			this.map = m;
		}

		private static final long serialVersionUID = 1L;

		@Override
		public Tuple2<String, Tweet> call(String _line) throws Exception {
			String line = _line.toLowerCase();
			Tweet tweet = new Tweet();
			if (line.contains("@") && line.contains(":")) {
				int pos1 = line.indexOf("@");
				int pos2 = line.indexOf(":");
				tweet.user = line.substring(pos1, pos2);
			}

			for (String x : this.map.keySet()) {
				for (String s : this.map.get(x).split(",")) {
					if (line.contains(s.toLowerCase())) {
						tweet.keyword.add(new Tuple2<String, String>(x, s));
					}
				}
			}
			if (tweet.keyword.isEmpty()) {
				tweet.keyword.add(new Tuple2<String, String>("General", ""));
			}
			return new Tuple2<String, Tweet>(this.key, tweet);
		}

	}
}
