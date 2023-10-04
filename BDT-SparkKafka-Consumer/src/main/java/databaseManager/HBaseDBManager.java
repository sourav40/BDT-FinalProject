package databaseManager;

import java.io.IOException;
import java.util.*;

import model.Tweet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.io.compress.Compression.Algorithm;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.api.java.JavaRDD;

public class HBaseDBManager 
{
	private Configuration hbaseConfig;
	private int rowkeyAnalysis=0;
	private final String TABLE_NAME="tbl_tweet_analysis";
	int count=0;
	public HBaseDBManager() throws IOException
	{
		this.hbaseConfig = HBaseConfiguration.create();
		this.DefaultValues();
		this.rowkeyAnalysis=this.GetMaxRownum();
	}

	private void DefaultValues() throws IOException
	{
		try (Connection connection = ConnectionFactory.createConnection(this.hbaseConfig);
				Admin admin = connection.getAdmin())
		{
			HTableDescriptor table = new HTableDescriptor(TableName.valueOf("tbl_keywords"));
			table.addFamily(new HColumnDescriptor("type_fam").setCompressionType(Algorithm.NONE));
			table.addFamily(new HColumnDescriptor("keywords_fam"));
			
			if (admin.tableExists(table.getTableName()))
			{
				admin.disableTable(table.getTableName());
				admin.deleteTable(table.getTableName());
			}
			if (!admin.tableExists(table.getTableName()))
			{
				admin.createTable(table);
				Table tbl = connection.getTable(TableName.valueOf("tbl_keywords"));
				Put put1 = new Put(Bytes.toBytes("1"));
				put1.addColumn(Bytes.toBytes("type_fam"),Bytes.toBytes("type"),Bytes.toBytes("SiliconValley"));
				put1.addColumn(Bytes.toBytes("keywords_fam"),Bytes.toBytes("keywords"),Bytes.toBytes("SiliconValley"));
				tbl.put(put1);
				
				Put put2 = new Put(Bytes.toBytes("2"));
				put2.addColumn(Bytes.toBytes("type_fam"),Bytes.toBytes("type"),Bytes.toBytes("Elon Musk"));
				put2.addColumn(Bytes.toBytes("keywords_fam"),Bytes.toBytes("keywords"),Bytes.toBytes("Elon Musk"));
				tbl.put(put2);
				
				Put put3 = new Put(Bytes.toBytes("3"));
				put3.addColumn(Bytes.toBytes("type_fam"),Bytes.toBytes("type"),Bytes.toBytes("Telsa"));
				put3.addColumn(Bytes.toBytes("keywords_fam"),Bytes.toBytes("keywords"),Bytes.toBytes("Telsa"));
				tbl.put(put3);
				
				Put put4 = new Put(Bytes.toBytes("4"));
				put4.addColumn(Bytes.toBytes("type_fam"),Bytes.toBytes("type"),Bytes.toBytes("Miami"));
				put4.addColumn(Bytes.toBytes("keywords_fam"),Bytes.toBytes("keywords"),Bytes.toBytes("Miami"));
				tbl.put(put4);
				
				Put put5 = new Put(Bytes.toBytes("5"));
				put5.addColumn(Bytes.toBytes("type_fam"),Bytes.toBytes("type"),Bytes.toBytes("Twitter"));
				put5.addColumn(Bytes.toBytes("keywords_fam"),Bytes.toBytes("keywords"),Bytes.toBytes("Twitter"));
				tbl.put(put5);
				
				Put put6 = new Put(Bytes.toBytes("6"));
				put6.addColumn(Bytes.toBytes("type_fam"),Bytes.toBytes("type"),Bytes.toBytes("Chat GPT"));
				put6.addColumn(Bytes.toBytes("keywords_fam"),Bytes.toBytes("keywords"),Bytes.toBytes("Chat GTP"));
				tbl.put(put6);

				Put put7 = new Put(Bytes.toBytes("7"));
				put7.addColumn(Bytes.toBytes("type_fam"),Bytes.toBytes("type"),Bytes.toBytes("Big Data"));
				put7.addColumn(Bytes.toBytes("keywords_fam"),Bytes.toBytes("keywords"),Bytes.toBytes("Big Data"));
				tbl.put(put7);

				Put put8 = new Put(Bytes.toBytes("8"));
				put8.addColumn(Bytes.toBytes("type_fam"),Bytes.toBytes("type"),Bytes.toBytes("NFL"));
				put8.addColumn(Bytes.toBytes("keywords_fam"),Bytes.toBytes("keywords"),Bytes.toBytes("NFL"));
				tbl.put(put8);
				
				tbl.close();
			}
		}
	}

	public HashMap<String,String> GetKeywords() throws IOException
	{
		HashMap<String,String> map=new HashMap<String,String>();
		
		try (Connection connection = ConnectionFactory.createConnection(this.hbaseConfig))
		{
			Table tbl = connection.getTable(TableName.valueOf("tbl_keywords"));
			Scan scan = new Scan();
			scan.setCacheBlocks(false);
			scan.setCaching(10000);
			scan.setMaxVersions(10);
			ResultScanner scanner = tbl.getScanner(scan);
			for (Result result = scanner.next(); result != null; result = scanner.next()) 
			{
				String type="";
				String keywords="";
				for (Cell cell : result.rawCells()) 
				{
					String family = Bytes.toString(CellUtil.cloneFamily(cell));
					String column = Bytes.toString(CellUtil.cloneQualifier(cell));
					if(family.equalsIgnoreCase("type_fam") && column.equalsIgnoreCase("type"))
					{
						type=Bytes.toString(CellUtil.cloneValue(cell));
					}
					else if(family.equalsIgnoreCase("keywords_fam") && column.equalsIgnoreCase("keywords"))
					{
						keywords=Bytes.toString(CellUtil.cloneValue(cell));
					}
				}
				
				if(!map.containsKey(type))
				{
					map.put(type, keywords);
				}
				else
				{
					map.replace(type, map.get(type)+","+keywords);
				}
			}
		}
		
		return map;
	}

	public void WriteTweetAnalysis(String key,JavaRDD<Tweet> rdd) throws IOException
	{
		System.out.println("testing......");
		try (Connection connection = ConnectionFactory.createConnection(this.hbaseConfig);
				Admin admin = connection.getAdmin())
		{
			HTableDescriptor table = new HTableDescriptor(TableName.valueOf(TABLE_NAME));
			table.addFamily(new HColumnDescriptor("key_fam").setCompressionType(Algorithm.NONE));
			table.addFamily(new HColumnDescriptor("tweet_fam"));
			if (!admin.tableExists(table.getTableName()))
			{
				admin.createTable(table);
			}
			
			System.out.println("here -----> admin");
		
			Table tbl = connection.getTable(TableName.valueOf(TABLE_NAME));
			System.out.println("is null --> "+rdd == null);
			System.out.println("rdd value -->"+rdd.count());
//			int count=0;
			for(Tweet tw:rdd.collect())
			{
				System.out.println("value berfor saving -->"+tw.user + ":"+tw.GetFoundKeywords()+":"+tw.GetStatement());
				Put put = new Put(Bytes.toBytes(String.valueOf(++this.rowkeyAnalysis)));
				put.addColumn(Bytes.toBytes("key_fam"),Bytes.toBytes("key"),Bytes.toBytes(key));
				put.addColumn(Bytes.toBytes("key_fam"),Bytes.toBytes("user"),Bytes.toBytes(tw.user));
				put.addColumn(Bytes.toBytes("tweet_fam"),Bytes.toBytes("tweet_analysis"),Bytes.toBytes(tw.GetStatement()));
				put.addColumn(Bytes.toBytes("tweet_fam"),Bytes.toBytes("keyword"),Bytes.toBytes(tw.GetFoundKeywords()));
				tbl.put(put);
				count++;
			}
			tbl.close();
			
			System.out.println("tbl_tweet_analysis written rows count:" + count);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings({ "finally", "deprecation" })
	private int GetMaxRownum()
	{
		try
		{
			@SuppressWarnings("resource")
			Result result=new HTable(this.hbaseConfig,TABLE_NAME).getRowOrBefore(Bytes.toBytes("9999"),Bytes.toBytes(""));
			return Integer.parseInt(Bytes.toString(result.getRow()));
		}
		catch(Exception ex){}
		finally {return 0;}
	}
	
	
}
