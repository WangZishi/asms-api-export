package cn.com.gwssi.tax.asms.export.controller;

import cn.com.gwssi.tax.asms.export.domain.PrintConfig;
import cn.com.gwssi.tax.asms.export.service.export.ExportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by TianJ on 2016/6/29.
 */
@RestController
@RequestMapping(value = "/api/v2/excel/report")
public class reportController {
    @Autowired
    private ExportService exportService;
    /**
     * 导出上报清单
     * Post
     */
    @RequestMapping(
            method = RequestMethod.POST,
            headers = "Content-Type=application/x-www-form-urlencoded")
    public ResponseEntity<byte[]> exportAdmission(@RequestBody String str) throws IOException {
        String idsStr = java.net.URLDecoder.decode(str, "UTF-8");
        idsStr = idsStr.substring(4);
        HashMap map = new ObjectMapper().readValue(idsStr,HashMap.class);
        String type = (String)map.get("type");
        List idslist = (ArrayList)map.get("ids");
        String[] ids = new String[idslist.size()];
        for(int i=0;i<idslist.size();i++){
            ids[i] = (String)idslist.get(i);
        }
        String tableName = "";
        if("school".equals(type)){
            tableName = "v_exp_report_school"; //视图名称
        }else{
            tableName = "v_exp_report"; //视图名称
        }
        byte[] bytes = null;
        //打印设置
        PrintConfig printConfig = new PrintConfig();
        printConfig.LANDSCAPE = true; // 打印方向，true：横向，false：纵向
        printConfig.PAPERSIZE = HSSFPrintSetup.A4_PAPERSIZE;
        printConfig.SCALE = (short) 90;

        bytes = exportService.exportByFilter(tableName, "0",ids,printConfig);
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        String fileName = ts.getTime() + ".xls"; // 组装附件名称和格式

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        httpHeaders.setContentDispositionFormData("attachment", fileName);

        return new ResponseEntity<byte[]>(bytes, httpHeaders, HttpStatus.CREATED);
    }

}
