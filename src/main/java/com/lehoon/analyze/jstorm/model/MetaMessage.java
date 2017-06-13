package com.lehoon.analyze.jstorm.model;

import java.io.Serializable;
import java.util.UUID;

/**
 * <p>Title: topology中从spout投递到bolt的序列化对象</p>
 * <p>Description: 解决spout到bolt的参数传递</p>
 * <p>Copyright: Copyright (c) 2017</p>
 * <p>Company: lehoon Co. LTD.</p>
 * <p>Author: lehoon </p>
 * <p>Date: 2017-06-13</p>
 */
public class MetaMessage implements Serializable{
    /**
     * message id
     */
    private UUID messageId = null;

    /**
     * hbase的key，一般都是按照一定的规则生成
     */
    private String key = null;
    /**
     * 写入hbase的内容
     */
    private String content = null;

    public MetaMessage() {
    }

    public MetaMessage(UUID messageId, String key, String content) {
        this.messageId = messageId;
        this.key = key;
        this.content = content;
    }

    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
