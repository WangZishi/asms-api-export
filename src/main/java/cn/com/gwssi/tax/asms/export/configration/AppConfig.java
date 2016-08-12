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
        // 获取系统环境变量
        Map map = System.getenv();
        if (map.containsKey("JDBC_URL") && map.containsKey("JDBC_USERNAME") && map.containsKey("JDBC_PASSWORD")) {
            String jdbcUrl = map.get("JDBC_URL").toString();
            String jdbcUsername = map.get("JDBC_USERNAME").toString();
            String jdbcPassword = map.get("JDBC_PASSWORD").toString();
            ds.setUrl(jdbcUrl);
            ds.setUsername(jdbcUsername);
            ds.setPassword(jdbcPassword);
        }else{
            ds.setUrl(env.getProperty("jdbc.dev.url"));
            ds.setUsername(env.getProperty("jdbc.dev.username"));
            ds.setPassword(env.getProperty("jdbc.dev.password"));
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