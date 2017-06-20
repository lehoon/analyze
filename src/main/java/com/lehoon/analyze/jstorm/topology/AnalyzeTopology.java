package com.lehoon.analyze.jstorm.topology;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import com.alibaba.jstorm.utils.JStormUtils;
import com.lehoon.analyze.jstorm.bolt.HBaseStoreBolt;
import com.lehoon.analyze.jstorm.bolt.MysqlStoreBolt;
import com.lehoon.analyze.jstorm.spout.RocketMQSpout;
import com.lehoon.analyze.utils.CollectorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * <p>Title: topology入口</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2017</p>
 * <p>Company: lehoon Co. LTD.</p>
 * <p>Author: lehoon </p>
 * <p>Date: 2017-06-13</p>
 */
public class AnalyzeTopology {
    private final static Logger logger = LoggerFactory.getLogger(AnalyzeTopology.class);

    /**
     * 配置参数
     */
    private static Map<String, Object> stormConf = new HashMap<String, Object>();

    /**
     * load the format of yaml
     */
    private static void LoadConfig() {
        Yaml yaml = new Yaml();

        try {
            InputStream inputStream = new FileInputStream("analyze.yaml");
            stormConf = (HashMap<String, Object>) yaml.load(inputStream);
            if (null == stormConf || stormConf.isEmpty()) {
                logger.error("配置文件内容为空,请检查配置文件是否正确");
            }
        } catch (FileNotFoundException e) {
            logger.error("指定的配置文件不存在.");
        } catch (Exception e) {
            logger.error("读取yaml格式的配置文件失败");
        }
    }

    /**
     * 创建topologyBuilder
     * @return
     */
    private static TopologyBuilder buildTopologyBuilder () {
        TopologyBuilder builder = new TopologyBuilder();

        /**
         * 获取topology中工作节点的并行数参数,默认1
         */
        int rocketmqSpoutHInt = JStormUtils.parseInt(stormConf.get("topology.rocketmqspout.parallelism_hint"), 1);
        int hbaseStoreBoltHInt = JStormUtils.parseInt(stormConf.get("topology.hbasebolt.parallelism_hint"), 1);
        int mysqlStoreBoltHInt = JStormUtils.parseInt(stormConf.get("topology.mysqlbolt.parallelism_hint"), 1);

        builder.setSpout("rocketmqSpout", new RocketMQSpout(), rocketmqSpoutHInt);
        builder.setBolt("hbaseStoreBolt", new HBaseStoreBolt(), hbaseStoreBoltHInt).localOrShuffleGrouping("rocketmqSpout");
        builder.setBolt("mysqlStoreBolt", new MysqlStoreBolt(), mysqlStoreBoltHInt).localOrShuffleGrouping("rocketmqSpout");
        return builder;
    }

    /**
     * topology 入口
     * @param args
     * @throws Exception
     */
    public static void main(String [] args) throws Exception {
        /**
         * load the configure file
         */
        LoadConfig();

        if (stormConf.size() == 0) {
            logger.error("the storm configuration file is error.");
            System.err.println("the storm configuration file is error.");
            System.exit(-2);
        }

        /**
         * debug the configuration file content
         */
        CollectorUtils.dumpMapContent(stormConf);

        Config config = new Config();
        config.putAll(stormConf);

        /**
         * 在配置文件中配置该worker数目
         */
        //config.setNumWorkers(1);

        TopologyBuilder builder = buildTopologyBuilder();
        Utils.sleep(10);
        StormSubmitter.submitTopology("analyzeTopology", stormConf, builder.createTopology());
    }
}
