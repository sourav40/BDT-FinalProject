package databaseManager;

import java.io.IOException;
import java.util.*;

import model.Tweet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;


public class HBaseReader 
{
	private Configuration hbaseConfig;
	private final String TABLE_NAME="tbl_tweet_analysis";
	public HBaseReader()
	{
		this.hbaseConfig = HBaseConfiguration.create();
	}

	public List<Tweet> GetTweetAnalysis() throws IOException
	{
		List<Tweet> tweetList=new ArrayList<Tweet>();
		
		try (Connection connection = ConnectionFactory.createConnection(this.hbaseConfig))
		{
			Table table = connection.getTable(TableName.valueOf(TABLE_NAME));
			Scan scan = new Scan();
			scan.setCacheBlocks(false);
			scan.setCaching(10000);
			scan.setMaxVersions(10);
			ResultScanner scanner = table.getScanner(scan);
			for (Result result = scanner.next(); result != null; result = scanner.next()) 
			{
				Tweet tweet=new Tweet();
				for (Cell cell : result.rawCells()) 
				{
					String family = Bytes.toString(CellUtil.cloneFamily(cell));
					String column = Bytes.toString(CellUtil.cloneQualifier(cell));
					if(family.equalsIgnoreCase("key_fam"))
					{
						if(column.equalsIgnoreCase("key"))
							tweet.key=Bytes.toString(CellUtil.cloneValue(cell));
						else if(column.equalsIgnoreCase("user"))
							tweet.user=Bytes.toString(CellUtil.cloneValue(cell));
					}
					else if(family.equalsIgnoreCase("tweet_fam"))
					{
						if(column.equalsIgnoreCase("tweet_analysis"))
						{
							tweet.PutStatements(Bytes.toString(CellUtil.cloneValue(cell)));
						}
						else if(column.equalsIgnoreCase("keyword"))
							tweet.PutKeywords(Bytes.toString(CellUtil.cloneValue(cell)));
					}
				}
				
				if(!tweet.key.isEmpty() && !tweet.user.isEmpty())
					tweetList.add(tweet);
			}
		}
		
		return tweetList;
	}
}
