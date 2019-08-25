package com.xiaoyu.hbase1;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.io.compress.Compression.Algorithm;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestHBase {

	Configuration conf = null;
    Connection conn = null;
    //表的管理对象
    Admin admin = null;
    Table table = null;
    //创建表的对象
    TableName tableName = TableName.valueOf("phone");
    
	@Before
	public void init() throws IOException {
		 //创建配置文件对象
        conf = HBaseConfiguration.create();
        //加载zookeeper的配置
        conf.set("hbase.zookeeper.quorum","node01,node02,node03");
        //获取连接
        conn = ConnectionFactory.createConnection(conf);
        //获取对象
        admin = conn.getAdmin();
        //获取数据操作对象
        table = conn.getTable(tableName);
	}
	
	@Test
    public void createTable() throws IOException {
        //定义表描述对象
        TableDescriptorBuilder tableDescriptorBuilder = TableDescriptorBuilder.newBuilder(tableName);
        //定义列族描述对象
        ColumnFamilyDescriptorBuilder columnFamilyDescriptorBuilder = ColumnFamilyDescriptorBuilder.newBuilder("cf".getBytes());
        //添加列族信息给表
        tableDescriptorBuilder.setColumnFamily(columnFamilyDescriptorBuilder.build());
        if(admin.tableExists(tableName)){
            //禁用表
            admin.disableTable(tableName);
            admin.deleteTable(tableName);
        }
        //创建表
        admin.createTable(tableDescriptorBuilder.build());
	}
	
	 @Test
	 public void insert() throws IOException {
	        Put put = new Put(Bytes.toBytes("2222"));
	        put.addColumn(Bytes.toBytes("cf"),Bytes.toBytes("name"),Bytes.toBytes("lisi"));
	        put.addColumn(Bytes.toBytes("cf"),Bytes.toBytes("age"),Bytes.toBytes("341"));
	        put.addColumn(Bytes.toBytes("cf"),Bytes.toBytes("sex"),Bytes.toBytes("women"));
	        table.put(put);
	}
	 
	 @Test
	 public void get() throws IOException {
	        Get get = new Get(Bytes.toBytes("1111"));
	        //在服务端做数据过滤，挑选出符合需求的列
	        get.addColumn(Bytes.toBytes("cf"),Bytes.toBytes("name"));
	        get.addColumn(Bytes.toBytes("cf"),Bytes.toBytes("age"));
	        get.addColumn(Bytes.toBytes("cf"),Bytes.toBytes("sex"));
	        Result result = table.get(get);
	        
	        Cell cell1 = result.getColumnLatestCell(Bytes.toBytes("cf"),Bytes.toBytes("name"));
	        Cell cell2 = result.getColumnLatestCell(Bytes.toBytes("cf"),Bytes.toBytes("age"));
	        Cell cell3 = result.getColumnLatestCell(Bytes.toBytes("cf"),Bytes.toBytes("sex"));
	        
	        String name = Bytes.toString(CellUtil.cloneValue(cell1));
	        String age = Bytes.toString(CellUtil.cloneValue(cell2));
	        String sex = Bytes.toString(CellUtil.cloneValue(cell3));
	        
	        System.out.println("name:"+name+" age:"+age+" sex:"+sex);
	        
	 }
	 
	  @Test
	  public void scan() throws IOException {
	        Scan scan = new Scan();
//	        scan.withStartRow();
//	        scan.withStopRow();
	        ResultScanner rss = table.getScanner(scan);
	        for (Result rs: rss) {
	            Cell cell1 = rs.getColumnLatestCell(Bytes.toBytes("cf"),Bytes.toBytes("name"));
	            Cell cell2 = rs.getColumnLatestCell(Bytes.toBytes("cf"),Bytes.toBytes("age"));
	            Cell cell3 = rs.getColumnLatestCell(Bytes.toBytes("cf"),Bytes.toBytes("sex"));
	            String name = Bytes.toString(CellUtil.cloneValue(cell1));
	            String age = Bytes.toString(CellUtil.cloneValue(cell2));
	            String sex = Bytes.toString(CellUtil.cloneValue(cell3));
	            
	            System.out.println(name);
	            System.out.println(age);
	            System.out.println(sex);
	        }
	   }

	  
	  SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

	    private String getDate(String s) {
	        return s+String.format("%02d%02d%02d%02d%02d",random.nextInt(12)+1,random.nextInt(31),random.nextInt(24),random.nextInt(60),random.nextInt(60));
	    }

	    Random random = new Random();
	    public String getNumber(String str){
	        return str+String.format("%08d",random.nextInt(99999999));

	    }
	    
	  /**
	          * 查询某一个用户3月份的通话记录
	    */
	   @Test
	   public void scanByCondition() throws Exception {
	        Scan scan = new Scan();
	        String startRow = "15883348450_"+(Long.MAX_VALUE-sdf.parse("20190331000000").getTime());
	        String stopRow = "15883348450_"+(Long.MAX_VALUE-sdf.parse("20190301000000").getTime());
	        scan.withStartRow(Bytes.toBytes(startRow));
	        scan.withStopRow(Bytes.toBytes(stopRow));
	        ResultScanner rss = table.getScanner(scan);
	        for (Result rs:rss) {
	            System.out.print(Bytes.toString(CellUtil.cloneValue(rs.getColumnLatestCell(Bytes.toBytes("cf"),Bytes.toBytes("dnum")))));
	            System.out.print("--"+Bytes.toString(CellUtil.cloneValue(rs.getColumnLatestCell(Bytes.toBytes("cf"),Bytes.toBytes("type")))));
	            System.out.print("--"+Bytes.toString(CellUtil.cloneValue(rs.getColumnLatestCell(Bytes.toBytes("cf"),Bytes.toBytes("length")))));
	            System.out.println("--"+Bytes.toString(CellUtil.cloneValue(rs.getColumnLatestCell(Bytes.toBytes("cf"),Bytes.toBytes("date")))));
	        }
	    }
	    
	  @Test
	  public void getType() throws IOException {
	        Scan scan = new Scan();
	        //创建过滤器集合
	        FilterList filters = new FilterList(FilterList.Operator.MUST_PASS_ALL);
	        //创建过滤器
	        SingleColumnValueFilter filter1 = new SingleColumnValueFilter(Bytes.toBytes("cf"),
	        		Bytes.toBytes("type"),CompareOperator.EQUAL,Bytes.toBytes("1"));
	        filters.addFilter(filter1);
	        //前缀过滤器
	        PrefixFilter filter2 = new PrefixFilter(Bytes.toBytes("15883348450"));
	        filters.addFilter(filter2);
	        scan.setFilter(filters);
	        
	        ResultScanner rss = table.getScanner(scan);
	        for (Result rs:rss) {
	            System.out.print(Bytes.toString(CellUtil.cloneValue(rs.getColumnLatestCell(Bytes.toBytes("cf"),Bytes.toBytes("dnum")))));
	            System.out.print("--"+Bytes.toString(CellUtil.cloneValue(rs.getColumnLatestCell(Bytes.toBytes("cf"),Bytes.toBytes("type")))));
	            System.out.print("--"+Bytes.toString(CellUtil.cloneValue(rs.getColumnLatestCell(Bytes.toBytes("cf"),Bytes.toBytes("length")))));
	            System.out.println("--"+Bytes.toString(CellUtil.cloneValue(rs.getColumnLatestCell(Bytes.toBytes("cf"),Bytes.toBytes("date")))));
	        }
	}
	  
	 
	  
	@After
	public void destory(){
		try {
            table.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            admin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            conn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }      
	}   
	
}
