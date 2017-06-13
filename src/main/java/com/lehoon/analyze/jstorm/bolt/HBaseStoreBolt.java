package com.lehoon.analyze.jstorm.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import com.lehoon.analyze.jstorm.model.MetaMessage;
import com.lehoon.analyze.store.hbase.HBaseConnectionFactory;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
    private String getZookeeperPort = null;

    /**
     * hbase连接对象
     */
    private Connection connection = null;

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
        this.getZookeeperPort = (String) map.get("hbase.zookeeper.port");

        /**
         * 获取hbase的连接对象
         */
        this.connection = HBaseConnectionFactory.getConnection(zookeeperAddr, getZookeeperPort);

        /**
         * 获取失败，打印错误信息
         */
        if (null == this.connection) {
            logger.error("获取hbase的连接失败，调用参数[hbase.zookeeper.address=" + this.zookeeperAddr + "; [hbase.zookeeper.port]=" + this.getZookeeperPort);
        }
    }

    @Override
    public void execute(Tuple tuple) {
        MetaMessage message = (MetaMessage) tuple.getValue(0);

        /**
         * 消息合法性校验
         */
        if (null == message) {
            logger.error("投递的消息失效，需要给确认该tuple，以免再次投递到bolt。");
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
            table = this.connection.getTable(TableName.valueOf("sms_hisoty"));

            if (null != table) {
                Put put = new Put(Bytes.toBytes(message.getKey()));
                put.addColumn(Bytes.toBytes("message"), Bytes.toBytes("body"), Bytes.toBytes(message.getContent()));
                table.put(put);
                this.collector.ack(tuple);
            } else {
                this.collector.fail(tuple);
            }
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
        HBaseConnectionFactory.closeConnection(this.zookeeperAddr, this.getZookeeperPort);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
