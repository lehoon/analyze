package com.lehoon.analyze.rocketmq;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2017</p>
 * <p>Company: TXBDS Nav Co. LTD.</p>
 * <p>Author: lehoon </p>
 * <p>Date: 2017-06-13</p>
 */
public class RocketMQConfig {

    /**
     * name server
     */
    public final static String MQ_NAMESERVER_FIELD_NAME = "rocketmq.namesrv";

    /**
     * group name
     */
    public final static String MQ_GROUP_FIELD_NAME = "rocketmq.group";

    /**
     * topic name
     */
    public final static String MQ_TOPIC_FIELD_NAME = "rocketmq.topic";

    /**
     * tag name
     */
    public final static String MQ_TAG_FIELD_NAME = "rocketmq.tag";

    /**
     * batch pull message size
     */
    public final static String MQ_PULL_BATCHSIZE_NAME = "rocketmq.pullbatchsize";

    /**
     * pull thread max size
     */
    public final static String MQ_PULL_THREADMAXSIZE_NAME = "rocketmq.pullthreadmax";

    /**
     * pull thread min size
     */
    public final static String MQ_PULL_THREADMINSIZE_NAME = "rocketmq.pullthreadmin";

    /**
     * pull interval time
     */
    public final static String MQ_PULL_INTERVAL_NAME = "rocketmq.pullinterval";

    /**
     * topic set
     */
    private Set<String> topicSet = new ConcurrentSkipListSet<String>();

    /**
     * default rocketmq namesrv address
     */
    private String rocketmq_namesrvaddr = "192.168.1.182:9876";

    /**
     * default rocket group name
     */
    private String rocketmq_groupname = "";

    /**
     * default rocketmq tag
     */
    private String rocketmq_tag = "*";

    private int PullBatchSize = 0;

    private int PullThreadNumMax = 0;

    private int PullThreadNumMin = 0;

    /**
     * rocketmq message pull time
     */
    private int PullInterval = 0;

    public RocketMQConfig() {
    }

    public String getRocketmq_namesrvaddr() {
        return rocketmq_namesrvaddr;
    }

    public void setRocketmq_namesrvaddr(String rocketmq_namesrvaddr) {
        this.rocketmq_namesrvaddr = rocketmq_namesrvaddr;
    }

    public String getRocketmq_groupname() {
        return rocketmq_groupname;
    }

    public void setRocketmq_groupname(String rocketmq_groupname) {
        this.rocketmq_groupname = rocketmq_groupname;
    }

    public String getRocketmq_tag() {
        return rocketmq_tag;
    }

    public void setRocketmq_tag(String rocketmq_tag) {
        this.rocketmq_tag = rocketmq_tag;
    }

    public int getPullBatchSize() {
        return PullBatchSize;
    }

    public void setPullBatchSize(int pullBatchSize) {
        PullBatchSize = pullBatchSize;
    }

    public int getPullThreadNumMax() {
        return PullThreadNumMax;
    }

    public void setPullThreadNumMax(int pullThreadNumMax) {
        PullThreadNumMax = pullThreadNumMax;
    }

    public int getPullThreadNumMin() {
        return PullThreadNumMin;
    }

    public void setPullThreadNumMin(int pullThreadNumMin) {
        PullThreadNumMin = pullThreadNumMin;
    }

    public int getPullInterval() {
        return PullInterval;
    }

    public void setPullInterval(int pullInterval) {
        PullInterval = pullInterval;
    }

    public void addTopic(String topic) {
        topicSet.add(topic);
    }

    public Set<String> getTopicSet() {
        return topicSet;
    }

    @Override
    public String toString() {
        return "RocketMQConfig{" +
                "topicSet=" + topicSet +
                ", rocketmq_namesrvaddr='" + rocketmq_namesrvaddr + '\'' +
                ", rocketmq_groupname='" + rocketmq_groupname + '\'' +
                ", rocketmq_tag='" + rocketmq_tag + '\'' +
                ", PullBatchSize=" + PullBatchSize +
                ", PullThreadNumMax=" + PullThreadNumMax +
                ", PullThreadNumMin=" + PullThreadNumMin +
                ", PullInterval=" + PullInterval +
                '}';
    }
}
