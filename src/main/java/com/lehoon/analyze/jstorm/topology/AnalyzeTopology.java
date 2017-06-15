package com.lehoon.analyze.jstorm.topology;

import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import com.lehoon.analyze.jstorm.bolt.HBaseStoreBolt;
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
    private static Map<Object, Object> stormConf = new HashMap<Object, Object>();

    /**
     * load the program config
     * @param path
     */
    private static void LoadConfig(String path) {
        if (path.endsWith("yaml")) {
            LoadYaml(path);
        } else {
            LoadProperties(path);
        }
    }

    /**
     * load the format of yaml
     * @param path
     */
    private static void LoadYaml(String path) {
        Yaml yaml = new Yaml();

        try {
            InputStream inputStream = new FileInputStream(path);
            stormConf = (HashMap<Object, Object>) yaml.load(inputStream);
            if (null == stormConf || stormConf.isEmpty()) {
                logger.error("配置文件内容为空,请检查配置文件是否正确");
            }
        } catch (FileNotFoundException e) {
            logger.error("指定的配置文件不存在." + path);
        } catch (Exception e) {
            logger.error("读取yaml格式的配置文件失败," + path);
        }
    }

    /**
     * load the format of properties
     * @param path
     */
    private static void LoadProperties(String path) {
        Properties properties = new Properties();

        try {
            InputStream inputStream = new FileInputStream(path);
            properties.load(inputStream);
            stormConf.putAll(properties);
        } catch (FileNotFoundException e) {
            logger.error("指定的配置文件不存在,请检查配置文件," + path);
        } catch (IOException e) {
            logger.error("指定的配置文件错误，请检查配置文件, " + path);
        }
    }

    /**
     * 创建topologyBuilder
     * @return
     */
    private static TopologyBuilder buildTopologyBuilder () {
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("rocketmqSpout", new RocketMQSpout(), 1);
        builder.setBolt("hbaseStoreBolt", new HBaseStoreBolt(), 1).shuffleGrouping("rocketmqSpout");
        return builder;
    }

    /**
     * topology 入口
     * @param args
     * @throws Exception
     */
    public static void main(String [] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Please input storm configuration file.");
            logger.error("Please input storm configuration file.");
            System.exit(-1);
        }

        /**
         * load the configure file
         */
        LoadConfig(args[0]);

        if (stormConf.size() == 0) {
            logger.error("the storm configuration file is error.");
            System.err.println("the storm configuration file is error.");
            System.exit(-2);
        }

        /**
         * debug the configuration file content
         */
        CollectorUtils.dumpMapContent(stormConf);

        TopologyBuilder builder = buildTopologyBuilder();
        Utils.sleep(10);
        StormSubmitter.submitTopology("analyzeTopology", stormConf, builder.createTopology());
    }
}
