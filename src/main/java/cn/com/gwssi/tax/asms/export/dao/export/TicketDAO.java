package cn.com.gwssi.tax.asms.export.dao.export;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import jxl.CellType;
import jxl.DateCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.apache.commons.fileupload.FileItem;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by TianJ on 2016/7/19.
 */
@Service("TicketDAO")
public class TicketDAO {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public int updateTicketState(String[] ids) {
        String inString = "";
        for (String id : ids) inString += "'" + id + "',";
        String sql = "update SCMS.SCMS_AIRTICKET set STATE = 'AT0005' where ID in(" + inString.substring(0, inString.length() - 1) + ") and state='AT0002'";
        return jdbcTemplate.update(sql);
    }

    public int updateTicket(String id, String ticketLine, String airNo, Double priceSave, String time, String remark) {
        String sql = "update SCMS.SCMS_AIRTICKET t " +
                " set t.TICKETNO = '" + airNo + "',t.AIRLINE = '" + ticketLine + "',t.state = 'AT0003', t.remark = '" + remark + "'" +
                " ,t.PRICE = '" + priceSave + "',t.FLIGHTDATE =to_date('" + time + "','yyyy-MM-dd') where t.id='" + id + "'";

        return jdbcTemplate.update(sql);
    }

    private boolean idIsExist(String id) {
        String sql = "SELECT ID num FROM SCMS.SCMS_AIRTICKET WHERE ID = '" + id + "'";
        List list = jdbcTemplate.queryForList(sql);
        return list.size() > 0 ? true : false;
    }

    private String decodeNull(Object o) {
        if (o == null) {
            return "";
        }
        return o.toString().trim();
    }

    public List<String> checkAndImport(InputStream inputStream) throws IOException {
        List<String> errors = new ArrayList<>();
        if (!inputStream.markSupported()) {
            inputStream = new PushbackInputStream(inputStream, 8);
        }
        if (!POIFSFileSystem.hasPOIFSHeader(inputStream)) {
            errors.add("errors");
            errors.add("请检验Excel版本，需为excle 97-2003 工作簿（*.xls）！");
            return errors;
        }
        Workbook wb = null;
        try {
            wb = Workbook.getWorkbook(inputStream);
        } catch (BiffException e) {
            e.printStackTrace();
        }
        // 获得该工作区的第一个sheet

        Sheet sheet = wb.getSheet(0);
        int maxRows = sheet.getRows();
        int maxColumns = sheet.getColumns();
        if (maxRows <= 2 || maxColumns > 16) {
            errors.add("errors");
            errors.add("导入的数据文件标题不正确或者列数大于16列！");
            return errors;
        }
        String id = ""; // 机票编号
        String flightDate = ""; // 乘机日期
        String airline = ""; // 航线
        String price = ""; // 机票价格
        String ticketNo = ""; // 机票号码
        String remark = "";//备注

        for (int m = 2; m < sheet.getRows(); m++) {
            id = String.valueOf(decodeNull(sheet.getCell(0, m).getContents()));
            flightDate = String.valueOf(decodeNull(sheet.getCell(11, m).getContents()));
            airline = String.valueOf(decodeNull(sheet.getCell(12, m).getContents()));
            price = String.valueOf(decodeNull(sheet.getCell(13, m).getContents()));
            ticketNo = String.valueOf(decodeNull(sheet.getCell(14, m).getContents()));
            remark = String.valueOf(decodeNull(sheet.getCell(15, m).getContents()));
            int row = m + 1;

            if ("".equals(id)) {
                errors.add("第" + row + "行:机票编号不能为空！");
            } else if (!idIsExist(id)) {
                errors.add("第" + row + "行:机票编号不存在！");
            }
            if ("".equals(flightDate)) {
                errors.add("第" + row + "行:乘机日期不能为空！");
            } else if (sheet.getCell(11, m).getType() != CellType.DATE) {
                errors.add("第" + row + "行:乘机日期格式非法（正确格式: YYYY/MM/DD）！");
            }
            if ("".equals(airline)) {
                errors.add("第" + row + "行:航线不能为空！");
            } else if (airline.length() > 30) {
                errors.add("第" + row + "行:航线字段超长！");
            }
            if ("".equals(price)) {
                errors.add("第" + row + "行:机票价格不能为空！");
            } else {
                try {
                    Double price_double = Double.parseDouble(price);
                    if (price_double < 0) {
                        errors.add("第" + row + "行:机票价格不能为负数！");
                    }
                } catch (NumberFormatException e) {
                    errors.add("第" + row + "行:机票价格必须为数字！");
                }

            }
            if ("".equals(ticketNo)) {
                errors.add("第" + row + "行:机票号码不能为空！");
            } else if (ticketNo.length() > 100) {
                errors.add("第" + row + "行:机票号码字段超长！");
            }
            if (!"".equals(remark)) {
                if (remark.length() > 300) {
                    errors.add("第" + row + "行:备注字段超长！");
                }
            }
        }
        if (errors.size() > 0) {
            errors.add(0,"errors");
            return errors;
        }
        Double price_double;
        List<String> idsList = new ArrayList<>();
        idsList.add("ids");
        for (int m = 2; m < sheet.getRows(); m++) {
            // 机票编号
            id = String.valueOf(decodeNull(sheet.getCell(0, m).getContents()));
            // 乘机日期
            DateCell dc = (DateCell) sheet.getCell(11, m);
            Date date = dc.getDate();
            SimpleDateFormat ds = new SimpleDateFormat("yyyy-MM-dd");
            flightDate = ds.format(date);
            // 航线
            airline = String.valueOf(decodeNull(sheet.getCell(12, m).getContents()));
            // 机票价格
            price_double = new Double(decodeNull(sheet.getCell(13, m).getContents()));
            // 机票号码
            ticketNo = String.valueOf(decodeNull(sheet.getCell(14, m).getContents()));
            // 备注
            remark = String.valueOf(decodeNull(sheet.getCell(15, m).getContents()));
            updateTicket(id, airline, ticketNo, price_double, flightDate, remark);
            idsList.add(id);
        }
        return idsList;
    }
}
