# HbasePlugin
这是基于hbase2.0一个支持注解式增删改查的hbase-plugin。
### 功能特性
1. 对hbase进行增删改查。
2. 支持hbase自动建表
3. 根据配置或默认配置支持hbase自动分区.

### 快速开始
1. 添加hbase-plugin依赖
	+ Latest Version（最新版本）：1.0
	+ maven依赖
		
	``` xml
		<dependency>
            <groupId>io.github.lcf-wmz</groupId>
            <artifactId>hbase-plugin-spring-boot-starter</artifactId>
            <version>Latest Version</version>
        </dependency>
	```
2. 使用注解定义entity.(2种定义自动分区的方式)
	- 方式1：通过配置分隔字符串（regionSplitKeys）自动分区
	``` java
	@HbaseTable(table = "test:goods",regionSplitKeys = {"g"})
	public class Goods {
		//商品id
		@HbaseRowKey
		private String id;
		//商品名称
		@HbaseField(qualifier = "goodsName")
		private String name;
		//商品单价
		@HbaseField
		private double price;
		//图片链接
		@HbaseField
		private String picUrl;
		//上市日期
		@HbaseField
		private LocalDate timeToMarket;
		//商品介绍
		@HbaseField(family = @HbaseFamily(family = "g",expire="90 DAYS") )
		private String produce;
	}
	```
	
	- 方式2：通过配置分区数（regionNum）自定分区
	``` java
	@HbaseTable(table = "test:order",regionNum = 10)
	@Data
	public class Order implements IOrder{

		//订单id
		@HbaseRowKey
		private String id;
		//交易时间
		@HbaseField
		private LocalDateTime tranTime;
		//用户id
		@HbaseField
		private String userId;
		//商品id
		@HbaseField
		private String goodsId;
		//商品数量
		@HbaseField
		private long num;
		//商品单价
		@HbaseField
		private double price;
		//支付金额
		@HbaseField
		private double payAmount;
		//优惠金额
		@HbaseField
		private double discountAmount;

	}
	```
3. 使用(详情请查看源码example)
	``` java
	@Resource
    private HbaseAccessor hbaseAccessor;

    public void test() throws IOException {
		Order order =  new Order();
		String id = UUID.randomUUID().toString().replace("-","");
		order.setId(id);
		order.setNum(15);
		order.setPrice(10);
		order.setTranTime(LocalDateTime.now());
		//增、改
		hbaseAccessor.put(order);
		//查询所有列值
		Order queryOrder = hbaseAccessor.get(id,Order.class);
		//查询指定列值
		queryOrder = hbaseAccessor.get(id,Order.class,Order::getNum);
		//删除所有列记录
		hbaseAccessor.delete(id,Order.class);
		//删除整定列记录
		hbaseAccessor.delete(id,Order.class,Order::getNum);
	}
	```
4. 关键注解说明
 + @HbaseTable 注解在entity类上
 ``` java
 @HbaseTable(table = "test:goods",regionSplitKeys = {"g"})
 ```
 ```
  table="test:goods"，表示hbase库中namespace为test的goods表
  regionSplitKeys = {"g"}，表示以'g'作为分区分隔符
 ```
 ``` java
 @HbaseTable(table = "test:order",regionNum = 10)
  regionNum = 10,表示分区数为10
 ```
  ``` java
 @HbaseTable(table = "user")
 ```
 ```
 表示namespace为default的user表,且分区数默认为1
 ```
  + @HbaseRowKey 可以注解在entity类属性、get方法、set方法以及is方法上
 ``` java
 @HbaseRowKey
 private String id;
 ```
 ```
 @HbaseRowKey
 public String getId(){
 	return id;
 };
  ```
 ```
 @HbaseRowKey
 public String setId(){
 	return id;
 };
  ```
  ```
 @HbaseRowKey
 public String isId(){
 	return id;
 };
 ```
 + @HbaseFamily 与@HbaseField结合使用，详情查看@HbaseField注解说明
 
 + @HbaseField 可以注解在entity类属性、get方法、set方法以及is方法上
	 ``` java
	@HbaseField(qualifier="goodsProduce",family = @HbaseFamily(family = "g",expire="90 DAYS") )
	private String produce;
	 ```
 ```
 表示：属性produce对应hbase表中的column为goodsProduce，列簇为'g',该列的过期时间为90天。
  ```
   ``` java
	@HbaseField
	private double price;
  ```
 ```
 表示：属性price对应hbase表中的column为price，默认列簇为'f',该列的默认过期时间为15天，默认的压缩方式为SNAPPY。
  ```
 
5. 其他
 + 有使用问题可以邮箱咨询
 + 邮箱 1154535007@qq.com

	
