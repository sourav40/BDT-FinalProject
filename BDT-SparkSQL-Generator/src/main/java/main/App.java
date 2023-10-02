package main;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

import model.Tweet;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.AnalysisException;

import databaseManager.HBaseReader;

public class App {

public static void main(String[] args) throws AnalysisException, IOException {

	SparkConf conf= new SparkConf().setAppName("SparkSQL").setMaster("local[*]");
	JavaSparkContext sc=new JavaSparkContext(conf);
 	SparkSession spark = SparkSession
      .builder()
      .appName("SparkSQL2")
      .config(conf)
      .getOrCreate();

    showTweetAnalysis(sc,spark);
    spark.stop();
    sc.close();
  }
	
  private static void showTweetAnalysis(JavaSparkContext sc,SparkSession spark) throws IOException {

    JavaRDD<Tweet> tweetsRDD=sc.parallelize(new HBaseReader().GetTweetAnalysis());
    
    String schemaString = "key user tweet_analysis keyword";
    
    List<StructField> fields = new ArrayList<>();
    for (String fieldName : schemaString.split(" ")) 
    {
    	StructField field = DataTypes.createStructField(fieldName, DataTypes.StringType, true);
    	fields.add(field);
    }
    StructType schema = DataTypes.createStructType(fields);

    JavaRDD<Row> rowRDD = tweetsRDD.map((Function<Tweet, Row>) record -> 
    {
    	if (record.key !=null) {
    		return RowFactory.create(record.key , record.user,record.GetStatement().isEmpty()?"General":record.GetStatement(),record.GetFoundKeywords());
    	}
		return null;
    });

    Dataset<Row> dataFrame = spark.createDataFrame(rowRDD, schema);
    dataFrame.createOrReplaceTempView("tweets");

    Dataset<Row> tweetResult = spark.sql("SELECT key,user,tweet_analysis as tweet_type,keyword FROM tweets WHERE key != 'NULL'");
    tweetResult.show(50);

    Dataset<Row> tweetCount = spark.sql("SELECT tweet_analysis as tweet_type,count(*) as count FROM tweets group by tweet_analysis");
    tweetCount.show(50);
    
    tweetResult.write().mode("append").option("header","true").csv("hdfs://localhost/user/cloudera/SparkTableResult");
    tweetCount.write().mode("append").option("header","true").csv("hdfs://localhost/user/cloudera/SparkTableCount");
    
  }
}

