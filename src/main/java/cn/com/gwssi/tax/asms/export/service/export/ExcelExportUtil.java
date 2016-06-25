package cn.com.gwssi.tax.asms.export.service.export;

import cn.com.gwssi.tax.asms.export.domain.PrintConfig;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.Region;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

//import org.apache.log4j.Logger;

/**
 * ****************************************************************************
 * excel导出工具类,支持动态表头.目前值支持列表式的表格
 *
 * @author(作者)：xuyongyun
 * @author(复审人)：    x 2015-1-21
 * @version(版本)1.0
 * @since 1.0
 * *****************************************************************************
 */

public class ExcelExportUtil {

    //public static Logger log = Logger.getLogger(ExcelExportUtil.class);

    private static short BorderColor;
    private static short Border;
    private static short Bold;
    private static short ALIGN_RIGHT;
    private static short ALIGN_CENTER;
    private static short Encoding = HSSFCell.ENCODING_UTF_16;

    public ExcelExportUtil() {
        //初始化全局样式
        BorderColor = HSSFColor.BLACK.index;
        Border = HSSFCellStyle.BORDER_THIN;
        ALIGN_RIGHT = HSSFCellStyle.ALIGN_RIGHT;
        ALIGN_CENTER = HSSFCellStyle.ALIGN_CENTER;
        Bold = HSSFFont.BOLDWEIGHT_BOLD;
        Encoding = HSSFCell.ENCODING_UTF_16;
    }

    /**
     * 根据传入参数生成excel文件
     * 如果导出的数据条数大于最大条数，则导出多个excel文档压缩后给客户端下载
     * 否则直接导出excel给客户端下载
     *
     * @param title        导出文档的标题
     * @param recordList   待导出记录列表,每个元素（Array）形式{第1列值,第2列值, ... ,第n列值}
     * @param hjh          合计行,不需要的时候直接传null
     * @param headArray    待导出表头显示名称数组(二维数组),
     *                     数组元素与未合并前的表格一一对应，如：Array11对应表格第二行第二列
     * @param mergeArray   表头如何合并数组(二维数组),每行形式：{合并开始行,合并开始列,合并结束行,合并结束列}
     * @param columnLength 每列在excel中显示的宽度数组，形式:{3500,4500,2500, ... ,6500}
     * @param dir          导出文档的存储目录
     * @param dirTmp       导出文档的临时存储目录
     * @param maxJlsl      单个excel最大导出数据量
     * @return
     * @throws Exception
     * @since 1.0
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public String writeExcel(String title, List<String[]> recordList, String[] hjh, String[][] headArray,
                             int[][] mergeArray, int[] columnLength, short[] alginArray, String dir,
                             String dirTmp, int maxJlsl) throws Exception {

        //记录总数
        int zsl = 0;
        //单个文件最大记录数
        int max = 20000;
        if (recordList != null) {
            zsl = recordList.size();
        }
        if (maxJlsl > 0) {
            max = maxJlsl;
        }

        //导出文档个数
        int excelNum = zsl / max;
        if (zsl % max > 0) {
            excelNum += 1;
        }

        //没有数据时导出空文档
        if (excelNum == 0) {
            excelNum = 1;
        }

        if (excelNum == 1) {//数据量未超出最大数据量

            //生成Wb
            HSSFWorkbook wb = new HSSFWorkbook();
            wb = this.createDynamicWb(wb, title, 1, recordList, hjh, headArray, mergeArray, columnLength, alginArray);
            dir = this.writeFile(wb, dir);

        } else {//数据量超出最大数据量

            //生成的文件列表
            List fileList = new ArrayList();
            for (int i = 1; i <= excelNum; i++) {
                int start = (i - 1) * max;
                int last = i * max;
                if (i == excelNum) {
                    //最后一页
                    last = recordList.size();
                }

                //获取导出数据
                List tmpList = new ArrayList();
                for (int j = start; j < last; j++) {
                    tmpList.add(recordList.get(j));
                }

                //生成Wb
                HSSFWorkbook wb = new HSSFWorkbook();
                wb = this.createDynamicWb(wb, title, 1, tmpList, hjh, headArray, mergeArray, columnLength, alginArray);
                String file = this.writeFile(wb, dirTmp);
                fileList.add(file);
            }
            //压缩生成的excle文件
            dir = this.zipFile(dirTmp, dir, fileList);

        }
        return dir;
    }
//   生成多个sheet页
    public String writeExcel(ArrayList titles, ArrayList titleSheets, ArrayList recordLists, String[] hjh, ArrayList headArrays,
                             ArrayList mergeArrays, ArrayList columnLengths, short[] alginArray, String dir,
                             String dirTmp, int maxJlsl, PrintConfig printConfig) throws Exception {
            //生成Wb
            HSSFWorkbook wb = new HSSFWorkbook();
            wb = this.createDynamicWb(wb, titles, titleSheets,headArrays.size(), recordLists, hjh, headArrays, mergeArrays, columnLengths, alginArray, printConfig);
            dir = this.writeFile(wb, dir);
            return dir;
    }

    /**
     * 创建动态表头工作薄，多个sheet页的情况
     *
     * @param wb           待导出工作薄
     * @param titles        标题
     * @param sheetNum     第几个sheet页，看arraylist的size长度
     * @param recordLists
     * @param hjh          // * @param showNameArray
     * @param mergeArrays
     * @param columnLengths
     * @return
     * @since 1.0
     */
    @SuppressWarnings({"deprecation"})
    private HSSFWorkbook createDynamicWb(HSSFWorkbook wb, ArrayList titles, ArrayList titleSheets, int sheetNum, ArrayList recordLists,
                                         String[] hjh, ArrayList headArrays, ArrayList mergeArrays, ArrayList columnLengths, short[] alginArray, PrintConfig printConfig) {
       //样式
        //普通样式
        HSSFCellStyle style = wb.createCellStyle();
        style.setBorderBottom(Border);
        style.setBottomBorderColor(BorderColor);
        style.setBorderLeft(Border);
        style.setLeftBorderColor(BorderColor);
        style.setBorderRight(Border);
        style.setRightBorderColor(BorderColor);
        style.setBorderTop(Border);
        style.setTopBorderColor(BorderColor);

        //表头样式---主标题
        HSSFCellStyle header_Style = wb.createCellStyle();
        header_Style.setAlignment(ALIGN_CENTER);
        HSSFFont font = wb.createFont();
        font.setBoldweight(Bold);
        font.setFontHeightInPoints((short) 15);
        header_Style.setFont(font);

        //表头样式---子标题
        HSSFCellStyle header_child_Style = wb.createCellStyle();
        header_child_Style.setAlignment(ALIGN_RIGHT);
        HSSFFont fontz = wb.createFont();
        fontz.setFontHeightInPoints((short) 12);
        header_child_Style.setFont(fontz);
//
        //数据区样式
        HSSFCellStyle body_Style = wb.createCellStyle();
        body_Style.setBorderBottom(Border);
        body_Style.setBottomBorderColor(BorderColor);
        body_Style.setBorderLeft(Border);
        body_Style.setLeftBorderColor(BorderColor);
        body_Style.setBorderRight(Border);
        body_Style.setRightBorderColor(BorderColor);
        body_Style.setBorderTop(Border);
        body_Style.setTopBorderColor(BorderColor);
        body_Style.setAlignment(ALIGN_RIGHT);
        body_Style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);

        HSSFCellStyle cs1;
        HSSFCellStyle cs2;
        HSSFCellStyle cs3;
        HSSFCellStyle cs4;


        //开始循环写sheet内容
        for (int i = 0; i<headArrays.size();i++){
            HSSFSheet sheet = null;
            HSSFCell cell = null;
            HSSFRow row = null;

            //当前操作行号
            int currRowNum = 0;

            //标题所占行数
            int titleRowNum = 1;
            //动态表头行数
            int headRowNum = 0;
            if (headArrays.get(i)!= null && ((String[][])headArrays.get(i)).length > 0) {
                headRowNum = ((String[][])headArrays.get(i)).length ;
            }

            //动态表头列数
            int headColumnNum = 0;
            if (columnLengths.get(i) != null && ((int[])columnLengths.get(i)).length  > 0) {
                headColumnNum = ((int[])columnLengths.get(i)).length ;
            }
            //写标题
            cs1 = header_Style;
            if(!titles.get(i).equals("")){
                sheet = wb.createSheet((titleSheets.get(i).toString()));//给sheet页命名,title名作为sheet页名
            }else{
                sheet = wb.createSheet(String.valueOf(i));
            }
            sheet.setMargin(HSSFSheet.RightMargin, (double) 0.5);
            HSSFPrintSetup ps = sheet.getPrintSetup();
//            ps.setLandscape(true); // 打印方向，true：横向，false：纵向
//            ps.setPaperSize(HSSFPrintSetup.A3_PAPERSIZE); //纸张
//            ps.setScale((short) 90);
            ps.setLandscape(printConfig.LANDSCAPE); // 打印方向，true：横向，false：纵向
            ps.setPaperSize(printConfig.PAPERSIZE); //纸张
            ps.setScale(printConfig.SCALE);
            for (int j = 0; j < headColumnNum; j++) {
               sheet.setColumnWidth((short) j, (short) ((int[])columnLengths.get(i))[j]);
            }
            //设置大标题
            row = sheet.createRow(0);
            cell = row.createCell(0);
            cell.setCellStyle(cs1);
            if(!titles.get(i).equals("")){
                cell.setCellValue((titles.get(i).toString()));//给sheet页命名,title名作为sheet页名
            }else{
                cell.setCellValue("");
            }
            sheet.addMergedRegion(new Region((short) 0, (short) 0, (short) 0, (short) (((int[])columnLengths.get(i)).length - 1)));
            //添加完标题行号增加
            currRowNum = currRowNum + titleRowNum;
            //写动态表头
            cs2 = body_Style;
            HSSFFont fontc = wb.createFont();
            fontc.setFontHeightInPoints((short) 12);
            fontc.setBoldweight(Bold);
            cs2.setFont(fontc);
            cs2.setAlignment(ALIGN_RIGHT);
            cs2.setFillBackgroundColor((short) 12);
            cs2.setWrapText(true);
            for (int i1 = 0; i1 < headRowNum; i1++) {
                row = sheet.createRow(currRowNum);
//                row.setHeight((short) 400);
                for (int j1 = 0; j1 < headColumnNum; j1++) {
                    cell = row.createCell((short) j1);
                    // cell.setEncoding(Encoding);
                    cell.setCellStyle(cs2);
                    String value = String.valueOf(((String[][])headArrays.get(i))[i1][j1]);
                    cell.setCellValue(value.replaceAll("&","\r\n"));
                }
                //行号每次自增1
                currRowNum++;
            }

            //合并动态表头
            if (mergeArrays.size()>0 && mergeArrays.get(i)!= null &&((int[][])mergeArrays.get(i)).length > 0) {
                for (int i1 = 0; i1 < ((int[][])mergeArrays.get(i)).length; i1++) {
                    //转换合并数组,行数+titleRowNum，列数不变
                    int[][] mera=((int[][])mergeArrays.get(i));
                    sheet.addMergedRegion(new Region( mera[i1][0] + 1, (short) mera[i1][1],
                            mera[i1][2]+ 1, (short) (mera[i1][3])));
                }
            }


            //写列表数据
            if (recordLists != null && ((ArrayList) recordLists.get(i)).size()> 0) {
                cs3 = body_Style;
                HSSFFont font1 = wb.createFont();
                font1.setFontHeightInPoints((short) 11);
                cs3.setFont(font1);
                cs3.setWrapText(true);

                for (int i1 = 0; i1 < ((ArrayList) recordLists.get(i)).size(); i1++) {
                    row = sheet.createRow(currRowNum);
                    String records[] = ((String[])(((ArrayList)recordLists.get(i)).get(i1)));

                    for (int k = 0; k < records.length; k++) {
                        cell = row.createCell(k);
                        //   cell.setEncoding(Encoding);
                        cs3 = body_Style;
                        cs3.setFont(font1);
                        if (alginArray != null && alginArray.length == records.length) {
                            cs3.setAlignment(alginArray[k]);
                        } else {
                            cs3.setAlignment(ALIGN_CENTER);
                        }

                        cell.setCellStyle(cs3);
                        String value = records[k];
                        cell.setCellValue(value.replaceAll("&","\r\n"));
                    }
                    currRowNum++;
                }
            }

        }
        return wb;
    }

    /**
     * 创建动态表头工作薄
     *
     * @param wb           待导出工作薄
     * @param title        标题
     * @param sheetNum     第几个sheet页
     * @param recordList
     * @param hjh          // * @param showNameArray
     * @param mergeArray
     * @param columnLength
     * @return
     * @since 1.0
     */
    @SuppressWarnings({"deprecation"})
    private HSSFWorkbook createDynamicWb(HSSFWorkbook wb, String title, int sheetNum, List<String[]> recordList,
                                         String[] hjh, String[][] headArray, int[][] mergeArray, int[] columnLength, short[] alginArray) {
        HSSFSheet sheet = null;
        HSSFCell cell = null;
        HSSFRow row = null;

        //当前操作行号
        int currRowNum = 0;

        //标题所占行数
        int titleRowNum = 1;

        //动态表头行数
        int headRowNum = 0;
        if (headArray != null && headArray.length > 0) {
            headRowNum = headArray.length;
        }

        //动态表头列数
        int headColumnNum = 0;
        if (columnLength != null && columnLength.length > 0) {
            headColumnNum = columnLength.length;
        }

        //普通样式
        HSSFCellStyle style = wb.createCellStyle();
        style.setBorderBottom(Border);
        style.setBottomBorderColor(BorderColor);
        style.setBorderLeft(Border);
        style.setLeftBorderColor(BorderColor);
        style.setBorderRight(Border);
        style.setRightBorderColor(BorderColor);
        style.setBorderTop(Border);
        style.setTopBorderColor(BorderColor);

        //表头样式---主标题
        HSSFCellStyle header_Style = wb.createCellStyle();
        header_Style.setAlignment(ALIGN_CENTER);
        HSSFFont font = wb.createFont();
        font.setBoldweight(Bold);
        font.setFontHeightInPoints((short) 15);
        header_Style.setFont(font);

        //表头样式---子标题
        HSSFCellStyle header_child_Style = wb.createCellStyle();
        header_child_Style.setAlignment(ALIGN_RIGHT);
        HSSFFont fontz = wb.createFont();
        fontz.setFontHeightInPoints((short) 12);
        header_child_Style.setFont(fontz);
//
        //数据区样式
        HSSFCellStyle body_Style = wb.createCellStyle();
        body_Style.setBorderBottom(Border);
        body_Style.setBottomBorderColor(BorderColor);
        body_Style.setBorderLeft(Border);
        body_Style.setLeftBorderColor(BorderColor);
        body_Style.setBorderRight(Border);
        body_Style.setRightBorderColor(BorderColor);
        body_Style.setBorderTop(Border);
        body_Style.setTopBorderColor(BorderColor);
        body_Style.setAlignment(ALIGN_RIGHT);
        body_Style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);

        HSSFCellStyle cs1;
        HSSFCellStyle cs2;
        HSSFCellStyle cs3;
        HSSFCellStyle cs4;

        //写标题
        cs1 = header_Style;
        // sheet = wb.createSheet(String.valueOf(sheetNum));//给sheet页命名
        sheet = wb.createSheet("基本信息");//给sheet页命名
        sheet.setMargin(HSSFSheet.RightMargin, (double) 0.5);
        HSSFPrintSetup ps = sheet.getPrintSetup();
        ps.setLandscape(false); // 打印方向，true：横向，false：纵向
        ps.setPaperSize(HSSFPrintSetup.A4_PAPERSIZE); //纸张
        ps.setScale((short) 90);
        for (int i = 0; i < headColumnNum; i++) {
            sheet.setColumnWidth((short) i, (short) columnLength[i]);
        }
        //设置大标题
        row = sheet.createRow(0);
        cell = row.createCell(0);
        // cell.setEncoding(Encoding);
        cell.setCellStyle(cs1);
        cell.setCellValue(title);
        // row = sheet.createRow((short) 1);
        // sheet.addMergedRegion(new Region(currRowNum, (short) 0, titleRowNum-1, (short)(columnLength.length-1)));
        sheet.addMergedRegion(new Region((short) 0, (short) 0, (short) 0, (short) (columnLength.length - 1)));


        //添加完标题行号增加
        currRowNum = currRowNum + titleRowNum;

        //写动态表头
        cs2 = body_Style;
        HSSFFont fontc = wb.createFont();
        fontc.setFontHeightInPoints((short) 12);
        fontc.setBoldweight(Bold);
        cs2.setFont(fontc);
        cs2.setAlignment(ALIGN_RIGHT);
        cs2.setFillBackgroundColor((short) 12);
        for (int i = 0; i < headRowNum; i++) {
            row = sheet.createRow((short) currRowNum);
            row.setHeight((short) 400);
            for (int j = 0; j < headColumnNum; j++) {
                cell = row.createCell((short) j);
                // cell.setEncoding(Encoding);
                cell.setCellStyle(cs2);
                cell.setCellValue(String.valueOf(headArray[i][j]));
            }
            //行号每次自增1
            currRowNum++;
        }

        //合并动态表头
        if (mergeArray != null && mergeArray.length > 0) {
            for (int i = 0; i < mergeArray.length; i++) {
                //转换合并数组,行数+titleRowNum，列数不变
                sheet.addMergedRegion(new Region(mergeArray[i][0] + 1, (short) mergeArray[i][1],
                        mergeArray[i][2] + 1, (short) mergeArray[i][3]));
            }
        }

        //写列表数据
        if (recordList != null && recordList.size() > 0) {
            cs3 = body_Style;
            HSSFFont font1 = wb.createFont();
            font1.setFontHeightInPoints((short) 11);
            cs3.setFont(font1);

            for (int i = 0; i < recordList.size(); i++) {
                row = sheet.createRow((short) currRowNum);
                String records[] = recordList.get(i);

                for (int k = 0; k < records.length; k++) {
                    cell = row.createCell((short) k);
                    //   cell.setEncoding(Encoding);
                    cs3 = body_Style;
                    cs3.setFont(font1);
                    if (alginArray != null && alginArray.length == records.length) {
                        cs3.setAlignment(alginArray[k]);
                    } else {
                        cs3.setAlignment(ALIGN_CENTER);
                    }

                    cell.setCellStyle(cs3);
                    cell.setCellValue(records[k]);
                }
                currRowNum++;
            }
        }

//        //写合计行
//        if(hjh!=null&&hjh.length>0){
//        	cs = body_Style;
//            HSSFFont font1 = wb.createFont();
//            font1.setFontHeightInPoints((short)11);
//            cs.setFont(font1);
//
//    		row = sheet.createRow((short) currRowNum);
//            for(int k=0;k<hjh.length ;k++){
//            	cell = row.createCell((short) k);
//               // cell.setEncoding(Encoding);
//                cs = body_Style;
//                cs.setFont(font1);
//                if(alginArray!=null&&alginArray.length==hjh.length){
//                	cs.setAlignment(alginArray[k]);
//                }else{
//                	cs.setAlignment(ALIGN_CENTER);
//                }
//                cell.setCellStyle(cs);
//                cell.setCellValue(hjh[k]);
//            }
//        }

        return wb;
    }

    /**
     * 压缩文件，并把压缩后的文件写入目标目录，同时删除原目录下的文件
     *
     * @param dirTmp   待压缩文件存放目录
     * @param dir      压缩后文件的存放目录
     * @param fileList 待压缩文件列表
     * @return
     * @since 1.0
     */
    @SuppressWarnings("rawtypes")
    private String zipFile(String dirTmp, String dir, List fileList) {
        dir = this.getZipFile(dirTmp, dir, fileList);
        for (int i = 0; i < fileList.size(); i++) {
            File file = new File((String) fileList.get(i));
            if (file.exists()) {
                if (file.isFile()) {
                    file.delete();
                }
            }
        }
        return dir;
    }

    /**
     * 把生成的excel wb写入文件
     *
     * @param wb  excel工作表
     * @param dir 待写入目录
     * @throws InterruptedException
     * @throws IOException
     * @since 1.0
     */
    private String writeFile(HSSFWorkbook wb, String dir) throws InterruptedException, IOException {
        FileOutputStream fileOut = null;
        try {
            //延迟30ms防止文件重名
            Thread.sleep(30L);
        } catch (InterruptedException e1) {
            throw e1;
        }
        Date date = new Date();
        // 格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        // 格式化日期(产生文件名)
        try {
            if (dir != null && !"".equals(dir)) {
                dir += "/" + sdf.format(date) + ".xls";
                this.makeDir(dir);
                fileOut = new FileOutputStream(dir);
                wb.write(fileOut);

            } else {
                //log.info("ExcelExportUtil-->writeFile-->导出文件路径：" + dir + "不存在!");
            }
        } catch (Exception ex) {
            //log.error("ExcelExportUtil-->writeFile-->导出excel文件出错!", ex);
            ex.printStackTrace();
        } finally {
            if (fileOut != null) {
                fileOut.close();
            }
        }
        return dir;
    }

    private void makeDir(String path) {
        try {
            String str = path;
            File f;
            String s[] = str.split("/");
            String dir = "";
            dir = s[0];
            for (int i = 1; i < s.length; i++) {
                if (s[i].contains(".")) {
                    return;
                } else {
                    dir = dir + "/" + s[i];
                    f = new File(dir);
                    if (!f.exists()) {
                        f.mkdir();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 压缩文件，并把压缩后的文件写入目标目录，同时删除原目录下的文件
     *
     * @param dirTmp   待压缩文件存放目录
     * @param dir      压缩后文件的存放目录
     * @param fileList 待压缩文件列表
     * @return
     * @since 1.0
     */
    @SuppressWarnings("rawtypes")
    public String getZipFile(String dirTmp, String dir, List fileList) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        FileOutputStream out = null;
        ZipOutputStream zipOut = null;
        FileInputStream in = null;
        try {
            if (dir != null && !"".equals(dir)) {
                dir += "/" + sdf.format(date) + ".zip";
                this.makeDir(dir);
                out = new FileOutputStream(dir);
                zipOut = new ZipOutputStream(out);
                for (int i = 0; i < fileList.size(); i++) {
                    File f = new File((String) fileList.get(i));
                    if (!f.exists()) {
                        //log.error("压缩文件失败");
                        break;
                    }
                    in = new FileInputStream((String) fileList.get(i));
                    String fileName = ((String) fileList.get(i)).substring(((String) fileList.get(i)).lastIndexOf('/') + 1, ((String) fileList.get(i)).length());
                    ZipEntry entry = new ZipEntry(fileName);
                    zipOut.putNextEntry(entry);
                    int nNumber = 0;
                    byte[] buffer = new byte[1024];
                    while ((nNumber = in.read(buffer)) != -1) {
                        zipOut.write(buffer, 0, nNumber);
                    }

                }
                System.gc();
            } else {
                //log.info("ExcelExportUtil-->getZipFile-->导出文件路径：" + dir + "不存在!");
            }
        } catch (Exception e) {
            //log.error("ExcelExportUtil-->getZipFile-->压缩文件失败");
            e.printStackTrace();
        } finally {
            try {
                if (zipOut != null) {
                    zipOut.close();
                }
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                //log.error("ExcelExportUtil-->getZipFile-->压缩文件结束后关闭流失败");
                e.printStackTrace();
            }
        }

        return dir;
    }

}
