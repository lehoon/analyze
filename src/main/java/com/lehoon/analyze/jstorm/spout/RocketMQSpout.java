package com.lehoon.analyze.jstorm.spout;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import com.alibaba.jstorm.utils.JStormUtils;
import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerOrderly;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.lehoon.analyze.jstorm.model.MetaMessage;
import com.lehoon.analyze.rocketmq.ConsumerFactory;
import com.lehoon.analyze.rocketmq.RocketMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <p>Title: 从rocketmq消费数据</p>
 * <p>Description: 通过rocketmq的consumer来拉取数据</p>
 * <p>Copyright: Copyright (c) 2017</p>
 * <p>Company:  lehoon LTD.</p>
 * <p>Author: lehoon </p>
 * <p>Date: 2017-06-13</p>
 */
public class RocketMQSpout implements IRichSpout, MessageListenerOrderly {
    /**
     * logger instance
     */
    private final static Logger logger = LoggerFactory.getLogger(RocketMQSpout.class);

    /**
     * rocketmq 的消费者对象
     */
    private transient DefaultMQPushConsumer consumer = null;

    /**
     * id
     */
    private String spoutId = null;

    /**
     * collector
     */
    private SpoutOutputCollector collector = null;

    /**
     * rocketmq消息缓存队列
     */
    private LinkedBlockingQueue<MessageExt> messageList = new LinkedBlockingQueue<MessageExt>();

    /**
     * 待反馈的tuple映射
     */
    private Map<UUID, MetaMessage> tupleMap = new ConcurrentHashMap<UUID, MetaMessage>();

    @Override
    public void open(Map conf, TopologyContext topologyContext, SpoutOutputCollector collector) {
        this.collector = collector;
        this.spoutId = topologyContext.getThisComponentId() + ":" + topologyContext.getThisTaskId();

        RocketMQConfig rocketMQConfig = new RocketMQConfig();
        rocketMQConfig.setRocketmq_namesrvaddr(conf.get(RocketMQConfig.MQ_NAMESERVER_FIELD_NAME).toString());
        rocketMQConfig.setRocketmq_groupname(conf.get(RocketMQConfig.MQ_GROUP_FIELD_NAME).toString());
        rocketMQConfig.setRocketmq_tag(conf.get(RocketMQConfig.MQ_TAG_FIELD_NAME).toString());
        rocketMQConfig.addTopic(conf.get(RocketMQConfig.MQ_TOPIC_FIELD_NAME).toString());
        rocketMQConfig.setPullBatchSize(JStormUtils.parseInt(conf.get(RocketMQConfig.MQ_PULL_BATCHSIZE_NAME), 32));
        rocketMQConfig.setPullThreadNumMax(JStormUtils.parseInt(conf.get(RocketMQConfig.MQ_PULL_THREADMAXSIZE_NAME), 4));
        rocketMQConfig.setPullThreadNumMin(JStormUtils.parseInt(conf.get(RocketMQConfig.MQ_PULL_THREADMINSIZE_NAME), 1));
        rocketMQConfig.setPullInterval(JStormUtils.parseInt(conf.get(RocketMQConfig.MQ_PULL_INTERVAL_NAME), 1));

        /**
         * 打印rocketmq的配置参数
         */
        logger.debug(rocketMQConfig.toString());

        try {
            this.consumer = ConsumerFactory.getConsumer(rocketMQConfig, this);
        } catch (MQClientException e) {
            logger.error("获取rocketmq的消费者对象失败, " + e.fillInStackTrace());
        }

        if (null == this.consumer) {
            logger.error(spoutId + " 在当前的workder中，已经有一个spout线程运行了，请确保rocketmqSpout在一个workder上并行度为1。");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            break;
                        }

                        logger.error(spoutId + " 在当前的workder中，已经有一个spout线程运行了，请确保rocketmqSpout在一个workder上并行度为1。");
                    }
                }
            });
        }
    }

    @Override
    public void close() {
        if (null != this.consumer) {
            this.consumer.shutdown();
        }
    }

    @Override
    public void activate() {
        if (null != this.consumer) {
            this.consumer.resume();
        }
    }

    @Override
    public void deactivate() {
        if (null != this.consumer) {
            this.consumer.suspend();
        }
    }

    @Override
    public void nextTuple() {
        MessageExt msg = null;

        try {
            msg = this.messageList.take();
        } catch (InterruptedException e) {
            logger.error("获取消息失败," + e.fillInStackTrace());
        }

        if (null == msg) {
            return;
        }

        MetaMessage message = new MetaMessage();
        message.setMessageId(UUID.randomUUID());
        message.setContent(new String(msg.getBody()));
        //演示使用，系统使用的时候，请使用预定义的key格式
        message.setKey(msg.getTags() + "_" + System.currentTimeMillis());

        /**
         * 放入待ack容器中
         */
        tupleMap.put(message.getMessageId(), message);

        /**
         * 投递消息
         */
        this.collector.emit(new Values(message));
    }

    @Override
    public void ack(Object key) {
        if (this.tupleMap.containsKey(key)) {
            this.tupleMap.remove(key);
        }
    }

    @Override
    public void fail(Object key) {
        if (this.tupleMap.containsKey(key)) {
            MetaMessage message = this.tupleMap.get(key);
            this.collector.emit(new Values(message));
            logger.error("消息处理失败，重新投递消息" + message.toString());
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("MetaMessage"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

    @Override
    public ConsumeOrderlyStatus consumeMessage(List<MessageExt> list, ConsumeOrderlyContext consumeOrderlyContext) {
        consumeOrderlyContext.setAutoCommit(true);

        for (MessageExt msg : list) {
            messageList.offer(msg);
        }

        return ConsumeOrderlyStatus.SUCCESS;
    }
}
