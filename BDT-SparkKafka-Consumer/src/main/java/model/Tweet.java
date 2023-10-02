package model;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import scala.Tuple2;


public class Tweet implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 154265L;
	public String user="";
	public List<Tuple2<String,String>> keyword;
	
	public Tweet()
	{
		this.keyword=new ArrayList<Tuple2<String,String>>(); 
	}
	
	public String GetStatement()
	{
		if(this.keyword.isEmpty())
			return "";
		
		return this.keyword.stream().map(t -> t._1()).collect(Collectors.joining(","));
	}
	
	public String GetFoundKeywords()
	{
		if(this.keyword.isEmpty())
			return "";
		
		return this.keyword.stream().map(t -> t._2()).collect(Collectors.joining(","));
	}
	
	@Override
	public String toString()
	{
		return this.user+"|"+this.GetStatement()+"|"+this.GetFoundKeywords();
	}
}
