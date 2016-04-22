package cn.com.gwssi.tax.asms.export.controller;

import cn.com.gwssi.tax.asms.export.service.export.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by TianJ on 2016/4/19.
 */
@RestController
@RequestMapping(value = "/discuss")
public class discussController {
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    private ExportService exportService;
    /**
     * 测试导出6万条
     * GET
     * Accept: application/octet-stream
     */
    @RequestMapping(value = "/test",
            method = RequestMethod.GET,
            headers = "Accept=application/octet-stream")
    public ResponseEntity<byte[]> exportTest() throws IOException {
//        List<String> list = jdbcTemplate.queryForList("select id from ASMS.TEST_EXPORT_60000 where id='1ftgtg6459'",String.class);
        List<String> list = new ArrayList<>();
        list.add("1atjtl6999");
        String[] ids = new String[]{};
        ids = list.toArray(ids);
        byte[] bytes = null;

        String tableName = "TEST_EXPORT_60000";
        bytes = exportService.exportByFilter(tableName, "0", ids);
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        String fileName = tableName + ts.getTime() + ".xls"; // 组装附件名称和格式

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        httpHeaders.setContentDispositionFormData("attachment", fileName);

        return new ResponseEntity<byte[]>(bytes, httpHeaders, HttpStatus.CREATED);
    }
}
