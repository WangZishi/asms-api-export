package cn.com.gwssi.tax.asms.export.service.export;

import cn.com.gwssi.tax.asms.export.dao.export.ExportDAO;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.util.*;

/**
 * 导出服务
 */
@Service("exportService")
public class ExportService
{
    @Autowired
    private ExportDAO           exportDAO;

    public byte[] exportByFilter(String tableName, String subTable, String[] ids)
    {
        ids = ids == null ? new String[0] : ids;
        List<List<String>> idsList = splitList(Arrays.asList(ids));

        List exportList    = exportDAO.getExportList(tableName, subTable);
        List seachList     = exportDAO.getSeachList(tableName, subTable);//除标题行外
        List headarrayList = exportDAO.getHeadarrayList(tableName, subTable);//所有标题行

        List mergeList      = exportDAO.getMergeList(tableName, subTable);//需要合并的标题
        String sql            = "select ";//查询语句
        int    headArrayCount = exportDAO.getHeadarrayInt(tableName, subTable);//动态标题行数
        String headArray[][]  = new String[headArrayCount][seachList.size()];//需要显示的标题行
        String headArray1[][] = new String[1][seachList.size()];//查询的字段，英文，和数据库对应
        String titleExcel;// excel标题
        int    columnLength[] = new int[seachList.size()];//字段显示宽度
        int    mergeArray[][];
        if (mergeList.size() > 0)
        {
            //存在需要合并的行或者列
            mergeArray = new int[mergeList.size()][4];
        } else
        {
            mergeArray = null;
        }

        ///------
        mergeHeader(seachList, headarrayList, mergeList, headArrayCount, headArray, mergeArray);
        //------

        titleExcel = getTitle(exportList);
        sql = getSql(seachList, sql, tableName, headArrayCount, headArray, headArray1, columnLength);
        List<Map> resultList = this.getResult(sql, idsList);
        List<String[]> recordList = getRecordList(headArray1, resultList);//结果集展示数组

        String hjh[] = null;// {"合计","3","5","6","7","8","9","10"};

        //导出excel
        byte[] bytes = getExcelBytes(headArray, titleExcel, columnLength, mergeArray, recordList, hjh);

        return bytes;
    }

    //生成多个sheet页的导出
    public byte[] exportByFilter(String[] tableNames, String subTable, String[] ids)
    {
        ids = ids == null ? new String[0] : ids;
        List<List<String>> idsList = splitList(Arrays.asList(ids));

        //定义传入参数数组
        ArrayList<String[][]> headArrays    = new ArrayList<String[][]>();
        ArrayList<String> titleExcels   = new ArrayList<String>();
        ArrayList<int[]> columnLengths = new ArrayList<int[]>();
        ArrayList<int[][]> mergeArrays   = new ArrayList<int[][]>();
        ArrayList<List<String[]>> recordLists   = new ArrayList<List<String[]>>();

        for (String tableName : tableNames)
        {
            List exportList;
            List seachList;//除标题行外
            List headarrayList;//所有标题行
            List mergeList;//需要合并的标题
            int  headArrayCount;//动态标题行数

            exportList = exportDAO.getExportList(tableName, subTable);
            seachList = exportDAO.getSeachList(tableName, subTable);
            headarrayList = exportDAO.getHeadarrayList(tableName, subTable);
            mergeList = exportDAO.getMergeList(tableName, subTable);
            headArrayCount = exportDAO.getHeadarrayInt(tableName, subTable);

            String sql          = "select ";//查询语句
            String[][] headArray    = new String[headArrayCount][seachList.size()];//需要显示的标题行
            String[][] headArray1   = new String[1][seachList.size()];//查询的字段，英文，和数据库对应
            String titleExcel   = "";//excel标题
            int[]      columnLength = new int[seachList.size()];//字段显示宽度
            int[][]    mergeArray;
            if (mergeList.size() > 0)
            {
                //存在需要合并的行或者列
                mergeArray = new int[mergeList.size()][4];
            } else
            {
                mergeArray = null;
            }
            mergeHeader(seachList, headarrayList, mergeList, headArrayCount, headArray, mergeArray, mergeArrays);
            titleExcels.add(getTitle(exportList));

            sql = getSql(seachList, sql, tableName, headArrayCount, headArray, headArray1, columnLength);
            headArrays.add(headArray);
            columnLengths.add(columnLength);
            List<Map> resultList = this.getResult(sql, idsList);
            recordLists.add(getRecordList(headArray1, resultList));
        }
        String hjh[] = null;// {"合计","3","5","6","7","8","9","10"};


        //导出excel
        byte[] bytes = getExcelBytes(headArrays, titleExcels, columnLengths, mergeArrays, recordLists, hjh);

        return bytes;
    }

    private void mergeHeader(List seachList, List headarrayList, List mergeList, int headarrayint, String[][] headArray, int[][] mergeArray)
    {
        mergeHeader(seachList, headarrayList, mergeList, headarrayint, headArray, mergeArray, null);
    }

    private void mergeHeader(List seachList, List headarrayList, List mergeList, int headarrayint, String[][] headArray, int[][] mergeArray, ArrayList<int[][]> mergeArrays)
    {
        if (headarrayint > 1)
        {
            //多于一行标题，需要进行多行标题和合并行列的处理
            for (int i = 0; i < mergeList.size(); i++)
            {
                //构造合并数组mergeArray
                HashMap map    = (HashMap) mergeList.get(i);
                int     title  = Integer.parseInt(map.get("TITLE").toString());//所处的标题行的位置，从100开始
                int     zb     = Integer.parseInt(map.get("SEQ").toString());
                int     mergec = 1;
                int     mergel = 1;
                if (map.get("MERGECOLUMN") != null)
                {
                    mergec = Integer.parseInt(map.get("MERGECOLUMN").toString());
                }
                if (map.get("MERGELINE") != null)
                {
                    mergel = Integer.parseInt(map.get("MERGELINE").toString());
                }
                int[] temp = {title - 100, zb, title - 100 + mergel - 1, zb + mergec - 1};
                mergeArray[i] = temp;
            }
            for (int i = 0; i < headarrayint - 1; i++)
            {
                //需要构造的标题行循环
                for (int j = i * seachList.size(); j < (i + 1) * seachList.size(); j++)
                {//在最大列数中循环，即，title=99，的个数，得到一行标题
                    HashMap map = (HashMap) headarrayList.get(j);
                    if (map.get("COLCH") != null)
                    {
                        headArray[i][j - i * seachList.size()] = map.get("COLCH").toString();
                    } else
                    {
                        headArray[i][j - i * seachList.size()] = "";
                    }
                }
            }
            if (mergeArrays != null)
            {
                mergeArrays.add(mergeArray);
            }
        }
    }

    private String getTitle(List exportList)
    {
        String titleExcel = "";
        for (Object anExportList : exportList)
        {
            //全部配置信息
            HashMap map = (HashMap) anExportList;
            if ((map.get("TITLE").toString()).equals("0"))
            {
                //excel大标题
                titleExcel = map.get("COLCH").toString();
            }
        }
        return titleExcel;
    }

    private String getSql(List seachList, String sql, String tableName, int headArrayCount, String[][] headArray, String[][] headArray1, int[] columnLength)
    {
        for (int i = 0; i < seachList.size(); i++)
        {
            // title=99
            HashMap map = (HashMap) seachList.get(i);
            headArray[headArrayCount - 1][i] = map.get("COLCH").toString();// TITLE=99,构造标题行数组headArray
            headArray1[0][i] = map.get("COLEN").toString();
            columnLength[i] = Integer.parseInt(map.get("WIDTH").toString());
            sql += map.get("COLEN").toString() + ',';
        }
        return sql.substring(0, sql.length() - 1) + " from " + tableName + " where 1=1 ";
    }

    private List<String[]> getRecordList(String[][] headArray1, List<Map> resultList)
    {
        List<String[]> recordList = new ArrayList<String[]>();//结果集展示数组

        for (Map map : resultList)
        {
            String[] result = new String[headArray1[0].length];
            for (int k1 = 0; k1 < headArray1[0].length; k1++)
            {
                String column = headArray1[0][k1].toUpperCase();//map中区分大小写问题
                if (map.get(column) != null)
                {
                    result[k1] = map.get(column).toString();
                } else
                {
                    result[k1] = "";
                }
            }
            recordList.add(result);
        }
        return recordList;
    }

    private byte[] getExcelBytes(String[][] headArray, String titleExcel, int[] columnLength, int[][] mergeArray, List<String[]> recordList, String[] hjh)
    {
        ArrayList<String[][]> headArrays    = new ArrayList<String[][]>();
        ArrayList<String> titleExcels   = new ArrayList<String>();
        ArrayList<int[]> columnLengths = new ArrayList<int[]>();
        ArrayList<int[][]> mergeArrays   = new ArrayList<int[][]>();
        ArrayList<List<String[]>> recordLists   = new ArrayList<List<String[]>>();

        headArrays.add(headArray);
        titleExcels.add(titleExcel);
        columnLengths.add(columnLength);
        mergeArrays.add(mergeArray);
        recordLists.add(recordList);

        return getExcelBytes(headArrays, titleExcels, columnLengths, mergeArrays, recordLists, hjh);
    }

    private byte[] getExcelBytes(ArrayList headArrays, ArrayList titleExcels, ArrayList columnLengths, ArrayList mergeArrays, ArrayList recordLists, String[] hjh)
    {
        ExcelExportUtil es = new ExcelExportUtil();
        short[] excelAlignArray = {HSSFCellStyle.ALIGN_RIGHT, HSSFCellStyle.ALIGN_RIGHT,
                                   HSSFCellStyle.ALIGN_RIGHT, HSSFCellStyle.ALIGN_RIGHT,
                                   HSSFCellStyle.ALIGN_CENTER, HSSFCellStyle.ALIGN_CENTER,
                                   HSSFCellStyle.ALIGN_CENTER, HSSFCellStyle.ALIGN_CENTER};
        String dir    = "./exports/excel/";
        String dirTmp = "./exports/tmp";
        // 一个excel中显示的最多纪录数，后台设置一个相对较大的值，导出多个sheet页时，不因为记录数据过多而生成多个excel文件
        int    maxRecordAmount = 10000;
        byte[] bytes           = new byte[0];
        try
        {
            String filePath = es.writeExcel(titleExcels, recordLists, hjh, headArrays, mergeArrays, columnLengths, excelAlignArray, dir, dirTmp, maxRecordAmount);
            FileInputStream in       = new FileInputStream(filePath);
            int             size     = in.available();
            bytes = new byte[size];
            System.out.println("excelFileSize = " + in.read(bytes) + "bytes.");
            in.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return bytes;
    }

    private <T> List<List<T>> splitList(List<T> idsList)
    {
        List<List<T>> lists      = new ArrayList<List<T>>();
        int           fromIndex  = 0;
        int           toIndex    = idsList.size();
        int           limitIndex = 1000;

        while (fromIndex < toIndex)
        {
            toIndex = Math.min(limitIndex, toIndex);
            lists.add(idsList.subList(fromIndex, toIndex));
            fromIndex = toIndex;
            toIndex = idsList.size();
            limitIndex += 1000;
        }
        return lists;
    }

    private List<Map> getResult(String sql, List<List<String>> idsList)
    {
        List<Map> result = new ArrayList<Map>();
        String tempSql;
        String inString;

        for (List<String> ids : idsList)
        {
            tempSql = sql;
            inString = "";
            for (String id : ids) inString += "'" + id + "',";
            tempSql += " and id in(" + inString.substring(0, inString.length() - 1) + ")";
            result.addAll(exportDAO.getListBySql(tempSql));
        }

        return result;
    }

}
