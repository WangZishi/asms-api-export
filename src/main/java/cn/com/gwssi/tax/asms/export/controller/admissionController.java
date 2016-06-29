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
 * Created by TianJ on 2016/6/15.
 */
@RestController
@RequestMapping(value = "/api/v2/excel/admission")
public class admissionController {
    @Autowired
    private ExportService exportService;
    /**
     * 导出新生录取名单
     * Post
     */
    @RequestMapping(
            method = RequestMethod.POST,
            headers = "Content-Type=application/x-www-form-urlencoded")
    public ResponseEntity<byte[]> exportAdmission(@RequestBody String str) throws IOException {
        String cscIdsStr = java.net.URLDecoder.decode(str, "UTF-8");
        cscIdsStr = cscIdsStr.substring(7);
        ArrayList content = new ObjectMapper().readValue(cscIdsStr,ArrayList.class);
        String[] tableNames = new String[content.size()]; //视图名称
        String[] titles = new String[content.size()]; //各sheet页标题
        String[] sheetTitles = new String[content.size()]; //各sheet页名称
        List<List<String>> idsList = new ArrayList<>(); //各sheet页对应应导出的cscIds
        String[] Row2Col2s = new String[content.size()]; //各sheet页第二行第二列对应内容
        for(int i=0;i<content.size();i++){
            HashMap c = (HashMap) content.get(i);
            Integer year = Integer.parseInt(c.get("year").toString());
            Integer nextYear = year+1;
            String title = year + "~" + nextYear + "学年度" + c.get("dispatch") + "-" + c.get("projectname") + "新生录取名单";
            titles[i] = title;
            String sheetTitle = year + "" + c.get("dispatch") + "-" + c.get("projectname");
            sheetTitles[i] = sheetTitle;
            tableNames[i] = "asms.v_stu_lqmd";
            List cscIds = (List)c.get("cscIds");
            idsList.add(cscIds);
            String subTitle = c.get("dispatch") + "-" + c.get("projectname");
            Row2Col2s[i] = subTitle;
        }
        byte[] bytes = null;
        //打印设置
        PrintConfig printConfig = new PrintConfig();
        printConfig.LANDSCAPE = true; // 打印方向，true：横向，false：纵向
        printConfig.PAPERSIZE = HSSFPrintSetup.A4_PAPERSIZE;
        printConfig.SCALE = (short) 90;

        bytes = exportService.exportByFilter(tableNames, "0", idsList,titles,sheetTitles,Row2Col2s,printConfig);
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        String fileName = ts.getTime() + ".xls"; // 组装附件名称和格式

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        httpHeaders.setContentDispositionFormData("attachment", fileName);

        return new ResponseEntity<byte[]>(bytes, httpHeaders, HttpStatus.CREATED);
    }
}
