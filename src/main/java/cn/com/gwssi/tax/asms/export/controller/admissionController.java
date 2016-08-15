package cn.com.gwssi.tax.asms.export.controller;

import cn.com.gwssi.tax.asms.export.domain.PrintConfig;
import cn.com.gwssi.tax.asms.export.service.export.ExportService;
import cn.com.gwssi.tax.asms.export.service.export.UploadFileServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.nashorn.internal.parser.JSONParser;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by TianJ on 2016/6/15.
 */
@RestController
@RequestMapping(value = "/api/v2/excel/admission")
public class admissionController {
    @Autowired
    private ExportService exportService;
    /* 跨域请求*/
//    @RequestMapping(method = RequestMethod.OPTIONS)
//    public ResponseEntity exportAdmissionOptions(HttpServletRequest request) {
//        HttpHeaders httpHeaders = new HttpHeaders();
//        List<HttpMethod> methods = new ArrayList<>();
//        methods.add(HttpMethod.GET);
//        methods.add(HttpMethod.POST);
//        methods.add(HttpMethod.DELETE);
//        methods.add(HttpMethod.PUT);
//        methods.add(HttpMethod.PATCH);
//        httpHeaders.setAccessControlAllowMethods(methods);
//        httpHeaders.setAccessControlAllowOrigin("*");
//        List<String> headers = new ArrayList<>();
//        headers.add("authorization");
//        httpHeaders.setAccessControlAllowHeaders(headers);
//        return new ResponseEntity(httpHeaders, HttpStatus.OK);
//    }

    /**
     * 导出新生录取名单
     * Post
     */
    @RequestMapping(method = RequestMethod.POST)
    public Map<String,String> exportAdmission(@RequestBody String cscIdsStr) throws IOException {
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

        //上传至文件服务器
        String file = UploadFileServer.uploadFile(fileName,bytes);
        Map<String,String> fileMap = new ObjectMapper().readValue(file,Map.class);
        /* 跨域请求*/
//        HttpHeaders httpHeaders = new HttpHeaders();
//        List<HttpMethod> methods = new ArrayList<>();
//        methods.add(HttpMethod.GET);
//        methods.add(HttpMethod.POST);
//        methods.add(HttpMethod.DELETE);
//        methods.add(HttpMethod.PUT);
//        methods.add(HttpMethod.PATCH);
//        httpHeaders.setAccessControlAllowMethods(methods);
//        httpHeaders.setAccessControlAllowOrigin("*");
//        List<String> headers = new ArrayList<>();
//        headers.add("authorization");
//        httpHeaders.setAccessControlAllowHeaders(headers);
//        return new ResponseEntity(fileMap, httpHeaders, HttpStatus.OK);
        return fileMap;
    }

    /**
     * 导出录取结果统计
     * Post
     */
    @RequestMapping(
            value = "/results",
            method = RequestMethod.POST,
            headers = "Content-Type=application/x-www-form-urlencoded")
    public ResponseEntity<byte[]> exportAdmissionResults(@RequestBody String str) throws IOException {
        String namesStr = java.net.URLDecoder.decode(str, "UTF-8");
        namesStr = namesStr.substring(7,namesStr.length()-1);
        String[] temp = namesStr.split(",");
        String[] names = new String[temp.length];
        for(int i=0;i<temp.length;i++){
            names[i] = temp[i].substring(1,temp[i].length()-1);
        }
        byte[] bytes;
        //打印设置
        PrintConfig printConfig = new PrintConfig();
        printConfig.LANDSCAPE = true; // 打印方向，true：横向，false：纵向
        printConfig.PAPERSIZE = HSSFPrintSetup.A4_PAPERSIZE;
        printConfig.SCALE = (short) 90;

        bytes = exportService.exportByFilter("asms.v_exp_admission_result", "0",names,printConfig);
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        String fileName = ts.getTime() + ".xls"; // 组装附件名称和格式

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        httpHeaders.setContentDispositionFormData("attachment", fileName);

        return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.CREATED);

    }
}
