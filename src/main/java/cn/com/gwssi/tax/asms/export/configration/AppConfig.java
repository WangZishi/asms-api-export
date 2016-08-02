package cn.com.gwssi.tax.asms.export.configration;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Map;

/**
 * @author Thomas Risberg
 */
@Configuration
@EnableTransactionManagement
@ComponentScan
@PropertySource("classpath:jdbc.properties")
class AppConfig {

    @Autowired
    Environment env;

    @Bean
    public DataSource dataSource() {
        BasicDataSource ds = new BasicDataSource();

        ds.setDriverClassName(env.getProperty("jdbc.driverClassName"));
//        ds.setUrl(env.getProperty("jdbc.url"));
//        ds.setUsername(env.getProperty("jdbc.username"));
//        ds.setPassword(env.getProperty("jdbc.password"));

        // 获取系统环境变量
        Map map = System.getenv();
        if (map.containsKey("NODE_ENV")) {
            String nodeEnv = map.get("NODE_ENV").toString();
            switch (nodeEnv) {
                case "dev": {
                    ds.setUrl(env.getProperty("jdbc.dev.url"));
                    ds.setUsername(env.getProperty("jdbc.dev.username"));
                    ds.setPassword(env.getProperty("jdbc.dev.password"));
                }
                break;
                case "test": {
                    ds.setUrl(env.getProperty("jdbc.test.url"));
                    ds.setUsername(env.getProperty("jdbc.test.username"));
                    ds.setPassword(env.getProperty("jdbc.test.password"));
                }
                break;
                case "prod": {
                    ds.setUrl(env.getProperty("jdbc.prod.url"));
                    ds.setUsername(env.getProperty("jdbc.prod.username"));
                    ds.setPassword(env.getProperty("jdbc.prod.password"));
                }
                break;
            }
        }else{
            ds.setUrl(env.getProperty("jdbc.test.url"));
            ds.setUsername(env.getProperty("jdbc.test.username"));
            ds.setPassword(env.getProperty("jdbc.test.password"));
        }
        return ds;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        DataSourceTransactionManager txManager = new DataSourceTransactionManager();
        txManager.setDataSource(dataSource());
        return txManager;
    }
}