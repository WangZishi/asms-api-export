package cn.com.gwssi.tax.asms.export.service.export;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.HashMap;

/**
 * Created by TianJ on 2016/7/26.
 */
//java的poi技术读取Excel
@Service("print201Service")
public class Print201Service {
    public byte[] print201(HashMap application) {
        byte[] bytes = null;
        try {
            InputStream is = new FileInputStream("./src/main/resources/201Template.xls");
            HSSFWorkbook workbook = new HSSFWorkbook(is);
            is.close();
            HSSFSheet sheet = workbook.getSheetAt(0);
            HSSFRow row3 = sheet.getRow(3);
            HSSFCell school = row3.getCell(18);
            school.setCellValue(String.valueOf(application.get("school")));
            sheet.getRow(5).getCell(2).setCellValue(String.valueOf(application.get("fullname")));
            sheet.getRow(5).getCell(10).setCellValue(String.valueOf(application.get("lastname")));
            sheet.getRow(5).getCell(21).setCellValue(String.valueOf(application.get("firstname")));

            sheet.getRow(7).getCell(1).setCellValue(String.valueOf(application.get("nationality")));
            sheet.getRow(7).getCell(10).setCellValue(String.valueOf(application.get("passportCode")));
            sheet.getRow(7).getCell(21).setCellValue(String.valueOf(application.get("gender")));
            sheet.getRow(7).getCell(26).setCellValue(String.valueOf(application.get("maritalStatus")));

            sheet.getRow(8).getCell(4).setCellValue(String.valueOf(application.get("birthday_year")));
            sheet.getRow(8).getCell(10).setCellValue(String.valueOf(application.get("birthday_month")));
            sheet.getRow(8).getCell(15).setCellValue(String.valueOf(application.get("birthday_day")));

            sheet.getRow(9).getCell(21).setCellValue(String.valueOf(application.get("birth")));

            sheet.getRow(10).getCell(11).setCellValue(String.valueOf(application.get("permanentAddress")));

            sheet.getRow(11).getCell(11).setCellValue(String.valueOf(application.get("permanentTel")));

            sheet.getRow(14).getCell(1).setCellValue(String.valueOf(application.get("certificate1")));
            sheet.getRow(14).getCell(21).setCellValue(String.valueOf(application.get("workEngaged")));

            sheet.getRow(15).getCell(18).setCellValue(String.valueOf(application.get("emploer")));

            sheet.getRow(16).getCell(24).setCellValue(String.valueOf(application.get("studyFrom_year")));
            sheet.getRow(16).getCell(26).setCellValue(String.valueOf(application.get("studyFrom_month")));
            sheet.getRow(16).getCell(31).setCellValue(String.valueOf(application.get("studyTo_year")));
            sheet.getRow(16).getCell(34).setCellValue(String.valueOf(application.get("studyTo_month")));

            sheet.getRow(17).getCell(1).setCellValue(String.valueOf(application.get("major")));

            sheet.getRow(18).getCell(24).setCellValue(String.valueOf(application.get("elementaryFrom_year")));
            sheet.getRow(18).getCell(26).setCellValue(String.valueOf(application.get("elementaryFrom_month")));
            sheet.getRow(18).getCell(31).setCellValue(String.valueOf(application.get("elementaryTo_year")));
            sheet.getRow(18).getCell(34).setCellValue(String.valueOf(application.get("elementaryTo_month")));

            sheet.getRow(19).getCell(1).setCellValue(String.valueOf(application.get("elementaryUniversity")));

            sheet.getRow(21).getCell(1).setCellValue(String.valueOf(application.get("studentKind")));
            sheet.getRow(21).getCell(11).setCellValue(String.valueOf(application.get("cscId")));
            sheet.getRow(21).getCell(21).setCellValue(String.valueOf(application.get("registerDeadline_year")));
            sheet.getRow(21).getCell(25).setCellValue(String.valueOf(application.get("registerDeadline_month")));
            sheet.getRow(21).getCell(30).setCellValue(String.valueOf(application.get("registerDeadline_day")));

            sheet.getRow(23).getCell(23).setCellValue(String.valueOf(application.get("scholarship")));

            sheet.getRow(24).getCell(16).setCellValue(String.valueOf(application.get("tuition")));
            sheet.getRow(24).getCell(23).setCellValue(String.valueOf(application.get("lodging")));
            sheet.getRow(24).getCell(34).setCellValue(String.valueOf(application.get("medical")));

            sheet.getRow(25).getCell(24).setCellValue(String.valueOf(application.get("material")));

            sheet.getRow(26).getCell(12).setCellValue(String.valueOf(application.get("other")));
            String dir = "./exports/excel/";
            ExcelExportUtil util = new ExcelExportUtil();
            String filePath = util.writeFile(workbook, dir);

            FileInputStream in = new FileInputStream(filePath);
            int size = in.available();
            bytes = new byte[size];
            System.out.println("excelFileSize = " + in.read(bytes) + "bytes.");
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }
}
