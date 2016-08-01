package cn.com.gwssi.tax.asms.export.controller;

import cn.com.gwssi.tax.asms.export.dao.export.TicketDAO;
import cn.com.gwssi.tax.asms.export.domain.PrintConfig;
import cn.com.gwssi.tax.asms.export.service.export.ExportService;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by TianJ on 2016/7/19.
 */
@RestController
@RequestMapping(value = "/api/v2/excel/ticket")
public class ticketController {
    @Autowired
    private ExportService exportService;
    @Autowired
    private TicketDAO ticketDao;
    /**
     * 导出机票
     * Post
     */
    @RequestMapping(
            value = "/export",
            method = RequestMethod.POST,
            headers = "Content-Type=application/x-www-form-urlencoded")
    public ResponseEntity<byte[]> exportAdmission(@RequestBody String str) throws IOException {
        String idsStr = java.net.URLDecoder.decode(str, "UTF-8");
        idsStr = idsStr.substring(5,idsStr.length()-1);
        String[] temp = idsStr.split(",");
        String[] ids = new String[temp.length];
        for(int i=0;i<temp.length;i++){
            ids[i] = temp[i].substring(1,temp[i].length()-1);
        }
        String tableName = "V_EXP_ASMS_TICKET"; //视图名称
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

        int count = ticketDao.updateTicketState(ids);

        return new ResponseEntity<byte[]>(bytes, httpHeaders, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/import",
            method = RequestMethod.OPTIONS
    )
    public ResponseEntity importTicketsOptions(HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        List<HttpMethod> methods = new ArrayList<>();
        methods.add(HttpMethod.GET);
        methods.add(HttpMethod.POST);
        methods.add(HttpMethod.DELETE);
        methods.add(HttpMethod.PUT);
        methods.add(HttpMethod.PATCH);
        httpHeaders.setAccessControlAllowMethods(methods);
        httpHeaders.setAccessControlAllowOrigin("*");
        List<String> headers = new ArrayList<>();
        headers.add("authorization");
        httpHeaders.setAccessControlAllowHeaders(headers);
        return new ResponseEntity(httpHeaders, HttpStatus.OK);
    }

    /**
     * 导入机票信息
     * POST
     */
    @RequestMapping(value = "/import",
            method = RequestMethod.POST
    )
    public ResponseEntity importTickets(HttpServletRequest request) {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        System.out.println("isMultipart = " + isMultipart);
        List<String> contents = new ArrayList<>();
        HttpHeaders httpHeaders = new HttpHeaders();
        List<HttpMethod> methods = new ArrayList<>();
        methods.add(HttpMethod.GET);
        methods.add(HttpMethod.POST);
        methods.add(HttpMethod.DELETE);
        methods.add(HttpMethod.PUT);
        methods.add(HttpMethod.PATCH);
        httpHeaders.setAccessControlAllowMethods(methods);
        httpHeaders.setAccessControlAllowOrigin("*");
        List<String> headers = new ArrayList<>();
        headers.add("authorization");
        httpHeaders.setAccessControlAllowHeaders(headers);
        if (isMultipart) {
            try {
                InputStream file = ((StandardMultipartHttpServletRequest) request).getMultiFileMap().get("file").get(0).getInputStream();
                contents = ticketDao.checkAndImport(file);
                return new ResponseEntity<>(contents,httpHeaders,HttpStatus.OK);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ResponseEntity<>(contents, HttpStatus.BAD_REQUEST);
    }


}
