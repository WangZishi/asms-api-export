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

/**
 * Created by TianJ on 2016/7/12.
 */
@RestController
@RequestMapping(value = "/api/v2/excel/expert-review")
public class expertController {
    @Autowired
    private ExportService exportService;
    /**
     * 导出预约专家名单
     * Post
     */
    @RequestMapping(
            value = "/expert",
            method = RequestMethod.POST,
            headers = "Content-Type=application/x-www-form-urlencoded")
    public ResponseEntity<byte[]> exportExperts(@RequestBody String str) throws IOException {
        String idsStr = java.net.URLDecoder.decode(str, "UTF-8");
        idsStr = idsStr.substring(5,idsStr.length()-1);
        String[] idsTemp = idsStr.split(",");
        String[] ids = new String[idsTemp.length];
        for(int i=0;i<idsTemp.length;i++){
            ids[i] = idsTemp[i].substring(1,idsTemp[i].length()-1);
        }
        String tableName = "V_EXP_EXPERT";
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
    /**
     * 导出评审结果汇总
     * Post
     */
    @RequestMapping(
            value = "/review",
            method = RequestMethod.POST,
            headers = "Content-Type=application/x-www-form-urlencoded")
    public ResponseEntity<byte[]> exportReviewResults(@RequestBody String str) throws IOException {
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
        switch (type){
            case "score":tableName = "V_EXP_REVIEW_SCORE";break;
            case "conclusion":tableName = "V_EXP_REVIEW_CONCLUSION";break;
            case "combine":tableName="V_EXP_REVIEW_COMBINE";break;
            default:tableName = "V_EXP_REVIEW_SCORE";break;
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
