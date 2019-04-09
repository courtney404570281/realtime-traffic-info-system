import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SignatureException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

class Ptx {

    static JsonArray getResponseJsonFrom(String ApiUrl) throws IOException, SignatureException {
        final String APPID = "9fa47244010944c8a8a21302042c08c8"; // 基礎加值
        final String APPKEY = "h0I2HuqbohVvSAHLbYUoog7pFVQ"; // 基礎加值
//        String APPID = "475dc2d3707345f181d6410031939d7b"; // 基礎資料
//        String APPKEY = "jqy3ACea4WZBpGrxBp9Nk8uitmI"; // 基礎資料

        // 取得當下的UTC時間，Java8有提供時間格式DateTimeFormatter.RFC_1123_DATE_TIME
        // 但是格式與C#有一點不同，所以只能自行定義
        String xdate = getServerTime();
        String SignDate = "x-date: " + xdate;

        // 取得加密簽章
        String Signature = HMAC_SHA1.Signature(SignDate, APPKEY);
        String sAuth = "hmac username=\"" + APPID +
                "\", algorithm=\"hmac-sha1\", headers=\"x-date\", signature=\"" + Signature + "\"";
//        System.out.println("Signature :" + Signature);
//        System.out.println(sAuth);

        URL url = new URL(ApiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", sAuth);
        connection.setRequestProperty("x-date", xdate);
        connection.setRequestProperty("Accept-Encoding", "gzip");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        InputStream inputStream = connection.getInputStream();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buff)) != -1) {
            baos.write(buff, 0, bytesRead);
        }
        inputStream.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        GZIPInputStream gzis = null;
        String response = "";

        try {
            // 解開gzip
            String line;
            gzis = new GZIPInputStream(bais);
            InputStreamReader reader = new InputStreamReader(gzis);
            BufferedReader in = new BufferedReader(reader);
            // 讀取串流形式回傳資料
            while ((line = in.readLine()) != null) {
                response += (line + "\n");
            }
        } catch (ZipException e) {
            // 非gzip壓縮格式
            response = baos.toString();
        } finally {
            connection.disconnect();
            baos.close();
            bais.close();
            if(gzis != null) gzis.close();
        }

        return new JsonParser().parse(response).getAsJsonArray();
    }

    // 取得當下UTC時間
    private static String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }
}
