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
import java.util.*;

/**
 * Created by TianJ on 2016/7/23.
 */
@RestController
@RequestMapping(value = "/api/v2/excel/distribution")
public class distributionController {
    @Autowired
    private ExportService exportService;

    /**
     * 导出分配名单
     * Post
     */
    @RequestMapping(
            method = RequestMethod.POST,
            headers = "Content-Type=application/x-www-form-urlencoded")
    public ResponseEntity<byte[]> exportAdmission(@RequestBody String str) throws IOException {
        String cscIdsStr = java.net.URLDecoder.decode(str, "UTF-8");
        cscIdsStr = cscIdsStr.substring(7);
        HashMap content = new ObjectMapper().readValue(cscIdsStr, HashMap.class);
        String[] tableNames = new String[content.size()]; //视图名称
        String[] titles = new String[content.size()]; //各sheet页标题
        String[] sheetTitles = new String[content.size()]; //各sheet页名称
        List<List<String>> idsList = new ArrayList<>(); //各sheet页对应应导出的cscIds
        String[] Row2Col2s = new String[content.size()]; //各sheet页第二行第二列对应内容

        Set<Map.Entry<String, ArrayList>> entrySet = content.entrySet();
        Iterator<Map.Entry<String, ArrayList>> iterator = entrySet.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Map.Entry<String, ArrayList> entry = iterator.next();
            String schoolAndyear = entry.getKey();
            String[] arr = schoolAndyear.split("&");
            String school = arr[0];
            int year = Integer.parseInt(arr[1]);
            int nextYear = year + 1;
            ArrayList<String> cscIds = entry.getValue();
            titles[i] = year + "~" + nextYear + "学年度来华新生商请和分配名单";
            sheetTitles[i] = year + school;
            tableNames[i] = "asms.V_EXP_DISTRIBUTE";
            idsList.add(cscIds);
            Row2Col2s[i] = "校名：" + school;
            i++;
        }
        byte[] bytes = null;
        //打印设置
        PrintConfig printConfig = new PrintConfig();
        printConfig.LANDSCAPE = true; // 打印方向，true：横向，false：纵向
        printConfig.PAPERSIZE = HSSFPrintSetup.A4_PAPERSIZE;
        printConfig.SCALE = (short) 90;

        bytes = exportService.exportByFilter(tableNames, "0", idsList, titles, sheetTitles, Row2Col2s, printConfig);
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        String fileName = ts.getTime() + ".xls"; // 组装附件名称和格式

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        httpHeaders.setContentDispositionFormData("attachment", fileName);

        return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.CREATED);
    }
}
