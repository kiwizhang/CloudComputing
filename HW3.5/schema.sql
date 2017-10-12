-- Optimizations:
-- 1. Stored the data as parquet format --
-- 2. Partition table lineorder_opt by lo_discount, as it is the selected field for query 1--
-- 3. Partition table dwdate_opt by d_year, as it is the selected field for query 3 --

-- steps: 
-- 1. create new table
-- 2. insert data from old table


-- start impala_create_table_unoptimized
CREATE EXTERNAL TABLE part_opt (
	p_partkey INT, 
	p_name STRING, 
	p_mfgr STRING, 
	p_category STRING, 
	p_brand1 STRING, 
	p_color STRING, 
	p_type STRING, 
	p_size INT, 
	p_container STRING)
STORED AS PARQUET;

INSERT OVERWRITE part_opt
SELECT 
	p_partkey, 
	p_name, 
	p_mfgr, 
	p_category, 
	p_brand1, 
	p_color, 
	p_type, 
	p_size, 
	p_container
FROM part;



CREATE EXTERNAL TABLE supplier_opt (
	s_suppkey   INT,
	s_name STRING,
	s_address STRING,
	s_city STRING,
	s_nation STRING,
	s_region STRING,
	s_phone STRING)
STORED AS PARQUET;

INSERT OVERWRITE supplier_opt
SELECT 
	s_suppkey,
	s_name,
	s_address,
	s_city,
	s_nation,
	s_region,
	s_phone
FROM supplier;


CREATE EXTERNAL TABLE customer_opt(
	c_custkey INT,
	c_name STRING,
	c_address  STRING,
	c_city STRING,
	c_nation STRING,
	c_region STRING,
	c_phone STRING,
	c_mktsegment STRING)
STORED AS PARQUET;

INSERT OVERWRITE customer_opt
SELECT 
	c_custkey,
	c_name,
	c_address,
	c_city,
	c_nation,
	c_region,
	c_phone,
	c_mktsegment
FROM customer;


CREATE EXTERNAL TABLE dwdate_opt(
	d_datekey INT,
	d_date STRING,
	d_dayofweek STRING,
	d_month STRING,
	d_yearmonthnum INT,
	d_yearmonth STRING,
	d_daynuminweek INT,
	d_daynuminmonth INT,
	d_daynuminyear INT,
	d_monthnuminyear INT,
	d_weeknuminyear INT,
	d_sellingseason STRING,
	d_lastdayinweekfl STRING,
	d_lastdayinmonthfl STRING,
	d_holidayfl STRING,
	d_weekdayfl STRING)
PARTITIONED BY (d_year INT)
STORED AS PARQUET;

INSERT INTO dwdate_opt
PARTITION (d_year)
SELECT 
	d_datekey,
	d_date,
	d_dayofweek,
	d_month,
	d_yearmonthnum,
	d_yearmonth,
	d_daynuminweek,
	d_daynuminmonth,
	d_daynuminyear,
	d_monthnuminyear,
	d_weeknuminyear,
	d_sellingseason,
	d_lastdayinweekfl,
	d_lastdayinmonthfl,
	d_holidayfl,
	d_weekdayfl,
	d_year
FROM dwdate;



CREATE EXTERNAL TABLE lineorder_opt(
	lo_orderkey INT,
	lo_linenumber INT,
	lo_custkey INT,
	lo_partkey INT,
	lo_suppkey INT,
	lo_orderdate INT,
	lo_orderpriority STRING,
	lo_shippriority STRING,
	lo_quantity INT,
	lo_extendedprice INT,
	lo_ordertotalprice INT,
	lo_revenue INT,
	lo_supplycost INT,
	lo_tax INT,
	lo_commitdate INT,
	lo_shipmode STRING)
PARTITIONED BY (lo_discount INT)
STORED AS PARQUET


INSERT INTO lineorder_opt
PARTITION (lo_discount)
SELECT lo_orderkey, lo_linenumber, lo_custkey, lo_partkey,lo_suppkey, lo_orderdate, 
lo_orderpriority, lo_shippriority, lo_quantity, lo_extendedprice, lo_ordertotalprice, lo_revenue, 
lo_supplycost, lo_tax, lo_commitdate, lo_shipmode, lo_discount
FROM lineorder;
-- end impala_create_table_unoptimized