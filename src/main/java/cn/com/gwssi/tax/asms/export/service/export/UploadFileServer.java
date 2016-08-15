package cn.com.gwssi.tax.asms.export.service.export;

import org.apache.poi.util.ArrayUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by TianJ on 2016/8/12.
 */
public class UploadFileServer {

    public static byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static String uploadFile(String fileName,byte[] bytes) {
        String returnContent = "";
        String BOUNDARY = "011000010111000001101001"; //boundary就是request头和上传文件内容的分隔符
        try {
            URL url = new URL("http://192.168.99.100:8080/api/v3/file/upload?fileName=" + fileName);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            con.connect();
            DataOutputStream out = new DataOutputStream(con.getOutputStream());

            StringBuffer strBuf = new StringBuffer();
            strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
            strBuf.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n");
            String contentType = "application/vnd.ms-excel";
            strBuf.append("Content-Type:" + contentType + "\r\n\r\n");

            byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("utf-8");

            byte[] data = concat(concat(strBuf.toString().getBytes("utf-8"), bytes), endData);
            out.write(data);

            out.flush();
            out.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));// 设置编码,否则中文乱码
            String content;
            while ((content = reader.readLine()) != null) {
                returnContent += content;
            }
            reader.close();
            con.disconnect();
        }catch (Exception e){
            e.printStackTrace();
        }
        return returnContent;
    }
}
