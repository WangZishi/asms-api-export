package cn.com.gwssi.tax.asms.export.domain;

import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.util.ShortField;

/**
 * Created by TianJ on 2016/6/12.
 */
public class PrintConfig {
    public Boolean LANDSCAPE = true;  // 打印方向，true：横向，false：纵向
    public Short PAPERSIZE = HSSFPrintSetup.A4_PAPERSIZE;
    public Short SCALE = (short) 90;
}
