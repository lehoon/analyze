package com.lehoon.analyze.jstorm.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import com.lehoon.analyze.jstorm.model.MetaMessage;
import com.lehoon.analyze.store.mysql.MySqlConnectionFactory;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Map;

/**
 * <p>Title: Mysql持久化bolt</p>
 * <p>Description: 持久化到mysql数据库</p>
 * <p>Copyright: CopyRight (c) 2017-2035</p>
 * <p>Company: lehoon Co. LTD.</p>
 * <p>Author: lehoon</p>
 * <p>Date: 2017-06-19</p>
 */
public class MysqlStoreBolt implements IRichBolt {
    /**
     * logger instance
     */
    private static Logger logger = LoggerFactory.getLogger(MysqlStoreBolt.class);

    /**
     * collector instance
     */
    private OutputCollector collector = null;

    /**
     * datasource operate instance
     */
    private QueryRunner queryRunner = null;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.queryRunner = new QueryRunner(MySqlConnectionFactory.getInstance().getDataSource());
    }

    @Override
    public void execute(Tuple input) {
        MetaMessage metaMessage = (MetaMessage) input.getValue(0);
        Object params[] = {metaMessage.getMessageId(), metaMessage.getKey(), metaMessage.getContent()};
        String sql = "insert into logs (msg_id, msg_tag, msg_content) values (?, ?, ?)";
        try {
            queryRunner.update(sql, params);
        } catch (SQLException e) {
            logger.error("insert datasource error:" + e);
        }
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
