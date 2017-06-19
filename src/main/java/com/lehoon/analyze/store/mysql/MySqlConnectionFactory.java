package com.lehoon.analyze.store.mysql;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.jstorm.utils.JStormUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * <p>Title: Mysql连接维护工厂</p>
 * <p>Description: mysql资源工厂</p>
 * <p>Copyright: CopyRight (c) 2017-2035</p>
 * <p>Company: lehoon Co. LTD.</p>
 * <p>Author: lehoon</p>
 * <p>Date: 2017-06-19</p>
 */
public class MySqlConnectionFactory {
    private final Logger logger = LoggerFactory.getLogger(MySqlConnectionFactory.class);

    /**
     * instance
     */
    private volatile static MySqlConnectionFactory instance = null;

    /**
     * datasource instance
     */
    private DruidDataSource dataSource = null;

    /**
     * get instance of mysql connection
     * @return
     */
    public static MySqlConnectionFactory getInstance() {
        if (null == instance) {
            synchronized (MySqlConnectionFactory.class) {
                if (null == instance) {
                    instance = new MySqlConnectionFactory();
                }
            }
        }
        return instance;
    }

    /**
     * 构造函数
     */
    private MySqlConnectionFactory() {
        Properties properties = new Properties();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties");

        try {
            properties.load(inputStream);
            String driverClass = properties.getProperty("druid.driverClassName");
            String jdbcUrl = properties.getProperty("druid.url");
            String username = properties.getProperty("druid.username");
            String password = properties.getProperty("druid.password");
            int initialSize = JStormUtils.parseInt(properties.getProperty("druid.initialSize"), 10);
            int maxActive = JStormUtils.parseInt(properties.getProperty("druid.maxActive"), 30);
            int minIdel = JStormUtils.parseInt(properties.getProperty("druid.minIdle"), 10);
            int maxWait = JStormUtils.parseInt(properties.getProperty("druid.maxWait"), 60000);
            int timeBetweenEvictionRunsMills = JStormUtils.parseInt(properties.getProperty("druid.timeBetweenEvictionRunsMillis"), 60000);
            int minEvictableIdleTimeMillis = JStormUtils.parseInt(properties.getProperty("druid.minEvictableIdleTimeMillis"), 300000);
            String validationQuery = properties.getProperty("druid.validationQuery");
            boolean testWhileIdle = JStormUtils.parseBoolean(properties.getProperty("druid.testWhileIdle"), true);
            boolean testOnBorrow = JStormUtils.parseBoolean(properties.getProperty("druid.testOnBorrow"), false);
            boolean testOnReturn = JStormUtils.parseBoolean(properties.getProperty("druid.testOnReturn"), false);
            boolean poolPreparedStatements = JStormUtils.parseBoolean(properties.getProperty("druid.poolPreparedStatements"), true);
            int maxPoolPreparedStatementPerConnectionSize = JStormUtils.parseInt(properties.getProperty("druid.maxPoolPreparedStatementPerConnectionSize"), 20);

            /**
             * set datasource paramer
             */
            this.dataSource = new DruidDataSource();
            this.dataSource.setDriverClassName(driverClass);
            this.dataSource.setUrl(jdbcUrl);
            this.dataSource.setUsername(username);
            this.dataSource.setPassword(password);
            this.dataSource.setInitialSize(initialSize);
            this.dataSource.setMaxActive(maxActive);
            this.dataSource.setMinIdle(minIdel);
            this.dataSource.setMaxWait(maxWait);
            this.dataSource.setTimeBetweenConnectErrorMillis(timeBetweenEvictionRunsMills);
            this.dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
            this.dataSource.setValidationQuery(validationQuery);
            this.dataSource.setTestWhileIdle(testWhileIdle);
            this.dataSource.setTestOnBorrow(testOnBorrow);
            this.dataSource.setTestOnReturn(testOnReturn);
            this.dataSource.setPoolPreparedStatements(poolPreparedStatements);
            this.dataSource.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
        } catch (IOException e) {
            logger.error("Create Mysql Datasource Pool Exception:" + e);
        }
    }

    /**
     * return datasource
     * @return
     */
    public DataSource getDataSource() {
        return this.dataSource;
    }

    public void destory() {
        if (null != this.dataSource) {
            this.dataSource.close();
        }
    }
}
