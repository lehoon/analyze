package com.lehoon.analyze.store.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>Title: hbase的连接创建工厂</p>
 * <p>Description: 实现hbase的连接缓存</p>
 * <p>Copyright: Copyright (c) 2017</p>
 * <p>Company: lehoon Co. LTD.</p>
 * <p>Author: lehoon </p>
 * <p>Date: 2017-06-13</p>
 */
public class HBaseConnectionFactory {
    private final static Logger logger = LoggerFactory.getLogger(HBaseConnectionFactory.class);

    /**
     * hbase连接缓存
     */
    private static Map<String, HBaseConnection> connections = new ConcurrentHashMap<String, HBaseConnection>();

    /**
     * 根据zookeeper的信息获取hbase的连接对象，复用的是connection
     * @param zookeeperAddr
     * @param zookeeperPort
     * @return
     */
    public synchronized static Connection getConnection(String zookeeperAddr, String zookeeperPort) {
        logger.info("zookeeper addr " + zookeeperAddr + "  zookeeper port " + zookeeperPort);
        StringBuilder sb = new StringBuilder(zookeeperAddr);
        sb.append(":").append(zookeeperPort);

        String connectionKey = sb.toString();
        HBaseConnection hbaseConnection = connections.get(connectionKey);

        if (null != hbaseConnection) {
            hbaseConnection.ref.incrementAndGet();
            return hbaseConnection.connection;
        }

        Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", zookeeperAddr);
        configuration.set("hbase.zookeeper.property.clientPort", zookeeperPort);

        Connection connection = null;
        try {
            connection = ConnectionFactory.createConnection(configuration);
        } catch (IOException e) {
            logger.error("create hbase client instance error, reason " + e.getMessage());
        }

        if (null != connection) {
            hbaseConnection = new HBaseConnection();
            hbaseConnection.connection = connection;
            hbaseConnection.ref.incrementAndGet();
            connections.put(connectionKey, hbaseConnection);
        }

        return connection;
    }

    /**
     * 回收hbase的connection对象
     * @param zookeeperAddr
     * @param zookeeperPort
     */
    public synchronized static void closeConnection(String zookeeperAddr, String zookeeperPort) {
        logger.info("close connection by zookeeper addr " + zookeeperAddr + "  zookeeper port " + zookeeperPort);
        StringBuilder sb = new StringBuilder(zookeeperAddr);
        sb.append(":").append(zookeeperPort);

        String connectionKey = sb.toString();
        HBaseConnection connection = connections.get(connectionKey);

        if (null != connection) {
            return;
        }

        if (connection.ref.decrementAndGet() > 0) {
            return;
        }

        try {
            connection.connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        connections.remove(connectionKey);
    }

    /**
     * hbase的引用对象
     */
    static class HBaseConnection {
        Connection connection = null;
        AtomicInteger ref = new AtomicInteger(0);

        public HBaseConnection() {
        }
    }
}
