package cn.com.gwssi.tax.asms.export.dao.export;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Export  DAO封装类
 */
@Service("exportDAO")
public class ExportDAO{

    @Autowired
    JdbcTemplate jdbcTemplate;

    public List getExportList(String tableName, String subtable) {
        List exportList = null;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT * from SCMS.SCMS_INI_EXPORT  ");
        stringBuilder.append(" where SCMS_INI_EXPORT.subtable= '").append(subtable).append("' ");
        stringBuilder.append(" and SCMS_INI_EXPORT.tablename = '").append(tableName).append("' ");//根据表名找到配置
        stringBuilder.append("  order by  to_number(seq)");
        exportList = jdbcTemplate.queryForList(stringBuilder.toString());
        return exportList;
    }

    //获取查询字段
    public List getSeachList(String tableName, String subtable) {
        List seachList = null;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT * from SCMS.SCMS_INI_EXPORT  ");
        stringBuilder.append(" where SCMS_INI_EXPORT.subtable= '").append(subtable).append("' ");
        stringBuilder.append(" and SCMS_INI_EXPORT.title='99' ");
        stringBuilder.append(" and SCMS_INI_EXPORT.tablename = '").append(tableName).append("' ");//根据表名找到配置
        stringBuilder.append("  order by to_number(seq)");
        seachList = jdbcTemplate.queryForList(stringBuilder.toString());
        return seachList;
    }

    //获取动态标题行的行数
    public Integer getHeadarrayInt(String tableName, String subtable) {
        List seachList = null;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT count(distinct title) sl from SCMS.SCMS_INI_EXPORT  ");
        stringBuilder.append(" where SCMS_INI_EXPORT.subtable= '").append(subtable).append("' ");
        stringBuilder.append(" and to_number(title)>98   ");
        stringBuilder.append(" and SCMS_INI_EXPORT.tablename = '").append(tableName).append("' ");//根据表名找到配置
        seachList = jdbcTemplate.queryForList(stringBuilder.toString());
        HashMap map = (HashMap) seachList.get(0);
        int headarrayint = Integer.parseInt(map.get("SL").toString());
        return headarrayint;
    }

    //获取动态标题行
    public List getHeadarrayList(String tableName, String subtable) {//获取动态标题行所有的list
        List seachList = null;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT * from SCMS.SCMS_INI_EXPORT  ");
        stringBuilder.append(" where SCMS_INI_EXPORT.subtable= '").append(subtable).append("' ");
        stringBuilder.append(" and to_number(title)>99  ");
        stringBuilder.append(" and SCMS_INI_EXPORT.tablename = '").append(tableName).append("' ");//根据表名找到配置
        stringBuilder.append("  order by title, to_number(seq) ");
        seachList = jdbcTemplate.queryForList(stringBuilder.toString());
        return seachList;
    }

    //根据传入sql 查询数据库，得到结果list
    public List getListBySql(String sql) {
        return jdbcTemplate.queryForList(sql);
    }

    //获取动态标题行的需要合并行和列的LIST
    public List getMergeList(String tableName, String subtable) {
        List seachList = null;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT * from SCMS.SCMS_INI_EXPORT  ");
        stringBuilder.append(" where SCMS_INI_EXPORT.subtable= '").append(subtable).append("' ");
        stringBuilder.append("  and (MERGELINE is not null or MERGECOLUMN is not null)   ");
        stringBuilder.append(" and SCMS_INI_EXPORT.tablename = '").append(tableName).append("' ");//根据表名找到配置
        stringBuilder.append("  order by title, to_number(seq) ");
        seachList = jdbcTemplate.queryForList(stringBuilder.toString());
        return seachList;
    }
}
