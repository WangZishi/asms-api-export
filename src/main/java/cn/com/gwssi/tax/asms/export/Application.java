package cn.com.gwssi.tax.asms.export;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Application {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @RequestMapping("/")
    public String home() {
        String r = jdbcTemplate.query(
                "select * from asms_application",
                (rs, rowNum) -> rs.getString("cscid"))
                .toString();
        return "Hello Docker World" + r;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}