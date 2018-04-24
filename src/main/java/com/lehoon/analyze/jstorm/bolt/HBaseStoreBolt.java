package com.lehoon.analyze.jstorm.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import com.lehoon.analyze.jstorm.model.MetaMessage;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>Title: hbase持久存储bolt</p>
 * <p>Description: 实现数据持久化到hbase中</p>
 * <p>Copyright: Copyright (c) 2017</p>
 * <p>Company: lehoon Co. LTD.</p>
 * <p>Author: lehoon </p>
 * <p>Date: 2017-06-13</p>
 */
public class HBaseStoreBolt implements IRichBolt{
    /**
     * logger instance
     */
    private final static Logger logger = LoggerFactory.getLogger(HBaseStoreBolt.class);

    /**
     * output collector
     */
    private OutputCollector collector = null;

    /**
     * hbase的zookeeper地址
     */
    private String zookeeperAddr = null;

    /**
     * hbase的zookeeper端口
     */
    private String zookeeperPort = null;

    /**
     * hbase连接对象
     */
    private Connection connection = null;
    
    /**
     * batch size of message, once insert into hbase
     */
    private static List<MetaMessage> messageList = new ArrayList<>(128);

    /**
     * bolt初始化，jstorm在创建bolt的时候，会调用该初始化方法
     * @param map      配置参数
     * @param topologyContext   topology的上下文对象
     * @param collector
     */
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        this.collector = collector;

        /**
         * 初始化hbase的配置参数
         */
        this.zookeeperAddr = (String) map.get("hbase.zookeeper.address");
        this.zookeeperPort = (String) map.get("hbase.zookeeper.port");

        Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", zookeeperAddr);
        configuration.set("hbase.zookeeper.property.clientPort", zookeeperPort);
        configuration.set("hbase.client.retries.number", "3");
        configuration.set("hbase.client.pause", "50");
        configuration.set("hbase.rpc.timeout", "2000");
        configuration.set("hbase.client.oprtation.timeout", "3000");
        configuration.set("hbase.client.scanner.timeout.period", "10000");
        
        /**
         * 获取hbase的连接对象
         */
        try{
        	connection = ConnectionFactory.createConnection(configuration);;
        } catch(IOException e) {
        	logger.error("获取Hbase连接失败. " + e.toString());
        }

        /**
         * 获取失败，打印错误信息
         */
        if (null == connection) {
            logger.error("获取hbase的连接失败，调用参数[hbase.zookeeper.address=" + this.zookeeperAddr + "; [hbase.zookeeper.port]=" + this.zookeeperPort);
        }
    }

    @Override
    public void execute(Tuple tuple) {
        if (null == connection) {
            logger.error("object hbase client connection is null.");
            this.collector.fail(tuple);
            return;
        }
        
        MetaMessage message = (MetaMessage) tuple.getValue(0);
        
        /**
         * 消息合法性校验
         */
        if (null == message) {
            logger.error("投递的消息失效，需要给确认该tuple，以免再次投递到bolt。");
            this.collector.ack(tuple);
            return;
        }

        messageList.add(message);
        
        if(messageList.size() < 128) {
            this.collector.ack(tuple);
            return;
        }
        
        /**
         * hbase的tbale对象，数据增删改查主要在table上完成
         * 注意：table是一个轻量级的对象，非线程安全，
         * 所以尽量每个事务操作都需要重新冲connection获取新的table
         */
        Table table = null;

        try {
            table = connection.getTable(TableName.valueOf("sms_hisoty"));
        } catch (IOException e) {
            logger.error("获取sms_hisoty 对象失败." + e);
        }

        if (null == table) {
            logger.error("object hbase client table is null.");
            return;
        }

        /**
         * 批量提交的put对象
         */
        List<Put> putList = new ArrayList<Put>(128);
        
        for(MetaMessage msg : messageList) {
            Put put = new Put(Bytes.toBytes(msg.getKey()));
            put.addColumn(Bytes.toBytes("message"), Bytes.toBytes("body"), Bytes.toBytes(msg.getContent()));
            putList.add(put);
        }
        
        try {
            table.put(putList);
        } catch (IOException e) {
            logger.error("数据持久化到hbase失败,详情请参考" + e.fillInStackTrace());
        } finally {
            if (null != table) {
                try {
                    table.close();
                } catch (IOException e) {
                    logger.error(e.fillInStackTrace().toString());
                }
            }
        }
    }

    @Override
    public void cleanup() {
        /**
         * 清理资源
         */
        if(connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
                logger.error(e.getMessage());
            }
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
