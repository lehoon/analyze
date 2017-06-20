package com.lehoon.analyze.store.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Title: Redis连接管理</p>
 * <p>Description: Redis池化</p>
 * <p>Copyright: CopyRight (c) 2017-2035</p>
 * <p>Company: lehoon Co. LTD.</p>
 * <p>Author: lehoon</p>
 * <p>Date: 2017-06-19</p>
 */
public class JRedisConnectionFactory {
    /**
     * logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(JRedisConnectionFactory.class);

    /**
     * redis pool factory instance
     */
    private static JRedisConnectionFactory instance = null;

    public synchronized JRedisConnectionFactory getInstance() {
        if (null == instance) {
            synchronized (JRedisConnectionFactory.class) {
                if (null == instance) {
                    instance = new JRedisConnectionFactory();
                }
            }
        }
        return instance;
    }



}
