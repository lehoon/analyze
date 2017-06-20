package com.lehoon.analyze.rocketmq;

import com.alibaba.jstorm.utils.JStormUtils;
import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerOrderly;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Title: rocketmq消费者工厂</p>
 * <p>Description: 缓冲消费者对象</p>
 * <p>Copyright: Copyright (c) 2017</p>
 * <p>Company: lehoon Co. LTD.</p>
 * <p>Author: lehoon </p>
 * <p>Date: 2017-06-13</p>
 */
public class ConsumerFactory {
    private final static Logger logger = LoggerFactory.getLogger(ConsumerFactory.class);

    /**
     * 消费者缓冲池
     */
    private static Map<String, DefaultMQPushConsumer> consumers = new ConcurrentHashMap<String, DefaultMQPushConsumer>();

    /**
     * create and get the consumer
     * @param config
     * @param listener
     * @return
     * @throws MQClientException
     */
    public static synchronized DefaultMQPushConsumer getConsumer(RocketMQConfig config, MessageListenerOrderly listener) throws MQClientException {
        Set<String> topicSet = config.getTopicSet();
        String tagName = config.getRocketmq_tag();
        String groupName = config.getRocketmq_groupname();

        StringBuilder consumerKeys = new StringBuilder();
        for (String topic : topicSet) {
            consumerKeys.append(topic);
        }

        consumerKeys.append("@");
        consumerKeys.append(groupName);

        String consumerKey = consumerKeys.toString();
        DefaultMQPushConsumer consumer = consumers.get(consumerKey);

        if (null != consumer) {
            //一个consumer不支持在2个线程上同时使用，因为注册的处理对象是一个，只能是不同的groupname+topic的consumer
            logger.error("RocketMQ Consumer is exits, so one consumer don't support run more than one process.");
            return null;
        }

        /**
         * create the consumer instance
         */
        consumer = new DefaultMQPushConsumer(groupName);

        if (null != consumer) {
            /**
             * 集群消费模式
             */
            consumer.setMessageModel(MessageModel.CLUSTERING);
            /**
             * 实例名 组+进程名
             */
            consumer.setInstanceName(config.getRocketmq_groupname() + "@" + JStormUtils.process_pid());
            /**
             * nameserv地址
             */
            consumer.setNamesrvAddr(config.getRocketmq_namesrvaddr());

            for (String topic : topicSet) {
                consumer.subscribe(topic, tagName);
            }

            consumer.setConsumeMessageBatchMaxSize(config.getPullBatchSize());
            consumer.registerMessageListener(listener);
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            consumer.start();
            consumers.put(consumerKey, consumer);
            return consumer;
        }

        return null;
    }
}
