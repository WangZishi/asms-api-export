package cn.com.gwssi.tax.asms.export.controller;

import cn.com.gwssi.tax.asms.export.service.export.Print201Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;

/**
 * Created by TianJ on 2016/7/25.
 */
@RestController
@RequestMapping(value = "/api/v2/excel/print201")
public class print201Controller {
    @Autowired
    private Print201Service print201Service;
    @RequestMapping(
            method = RequestMethod.POST,
            headers = "Content-Type=application/x-www-form-urlencoded")
    public ResponseEntity<byte[]> print201(@RequestBody String str) throws IOException {
        String appStr = java.net.URLDecoder.decode(str, "UTF-8");
        appStr = appStr.substring(4);
        HashMap application = new ObjectMapper().readValue(appStr, HashMap.class);
        byte[] bytes = print201Service.print201(application);
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        String fileName = ts.getTime() + ".xls"; // 组装附件名称和格式

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        httpHeaders.setContentDispositionFormData("attachment", fileName);

        return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.CREATED);

    }
}
