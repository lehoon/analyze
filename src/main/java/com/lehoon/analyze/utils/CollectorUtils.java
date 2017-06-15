package com.lehoon.analyze.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <p>Title: 集合处理工具类</p>
 * <p>Description: 集合工具</p>
 * <p>Copyright: Copyright (c) 2017</p>
 * <p>Company: lehoon Co. LTD.</p>
 * <p>Author: lehoon </p>
 * <p>Date: 2017-06-13</p>
 */
public class CollectorUtils {
    private final static Logger logger = LoggerFactory.getLogger(CollectorUtils.class);

    /**
     * dump the map key and value
     * @param map
     */
    public static void dumpMapContent(Map<Object, Object> map) {
        Set<Object> keys = map.keySet();

        Iterator<Object> iterator = keys.iterator();
        while (iterator.hasNext()) {
            Object key = iterator.next();
            logger.info("map key=" + key + ", content=" + map.get(key));
        }
    }
}
