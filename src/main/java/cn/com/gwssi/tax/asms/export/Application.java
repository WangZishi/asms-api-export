package cn.com.gwssi.tax.asms.export;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@SpringBootApplication
@RestController
public class Application {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @RequestMapping("/")
    public String home() {
        String a = "2222";
        String b  = "2222";
//        if ('2' == '2')
//        String r = jdbcTemplate.query(
//                "select * from asms_application",
//                (rs, rowNum) -> rs.getString("cscid"))
//                .toString();
//        return "Hello Docker World" + r;
//        List<Map<String,Object>> result = jdbcTemplate.queryForList("select * from asms_application");
        return "hello";
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}