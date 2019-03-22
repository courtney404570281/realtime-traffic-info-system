import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SignatureException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

import com.google.gson.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

public class PtxProducer {

    public static void main(String[] args) {

//        String APIUrl = "http://ptx.transportdata.tw/MOTC/v2/Bus/RealTimeByFrequency/InterCity?" +
//                "$top=1&$format=JSON";
        String APIUrl = "http://ptx.transportdata.tw/MOTC/v2/Bus/RealTimeByFrequency/InterCity?" +
                "$top=30&$format=JSON&$filter=PlateNumb%20eq%20'020-FH'";
        String bootstrapServer = "192.168.1.237:9092";

        HttpURLConnection connection = createConnection(APIUrl);
        try {
            // 將InputStream轉換為Byte Array
            InputStream inputStream = connection.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buff)) != -1) {
                baos.write(buff, 0, bytesRead);
            }
            inputStream.close();

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

            String response = "";
            GZIPInputStream gzis = null;
            try {
                // 解開GZIP
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
                baos.close();
                bais.close();
                if(gzis != null) gzis.close();
            }

            // Parse response
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonArray ja = new JsonParser().parse(response).getAsJsonArray();

            // create a kafka producer
            KafkaProducer<String, String> producer = createKafkaProducer(bootstrapServer);

            for (JsonElement je : ja) {
//                System.out.println(gson.toJson(je));
                String plateNumb = je.getAsJsonObject().get("PlateNumb").getAsString();
                JsonObject busPosition = je.getAsJsonObject().get("BusPosition").getAsJsonObject();
                Float lat = busPosition.get("PositionLat").getAsFloat();
                Float lon = busPosition.get("PositionLon").getAsFloat();
                String msg = plateNumb + " at " + lat + "N " + lon + "E";
                System.out.println(msg);
                producer.send(new ProducerRecord<>("interCityBus", plateNumb, je.toString()),
                        new Callback() {
                    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                        if (e != null) {
                            e.printStackTrace();
                            System.out.println("Something bad happened");
                        }
                    }
                });
            }

            producer.flush();
            producer.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    // 取得當下UTC時間
    private static String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

    private static HttpURLConnection createConnection(String APIUrl){
        HttpURLConnection connection = null;

        String APPID = "9fa47244010944c8a8a21302042c08c8"; // 基礎加值
        String APPKey = "h0I2HuqbohVvSAHLbYUoog7pFVQ"; // 基礎加值
//        String APPID = "475dc2d3707345f181d6410031939d7b"; // 基礎資料
//        String APPKey = "jqy3ACea4WZBpGrxBp9Nk8uitmI"; // 基礎資料

        // 取得當下的UTC時間，Java8有提供時間格式DateTimeFormatter.RFC_1123_DATE_TIME
        // 但是格式與C#有一點不同，所以只能自行定義
        String xdate = getServerTime();
        String SignDate = "x-date: " + xdate;

        String Signature = "";
        try {
            // 取得加密簽章
            Signature = HMAC_SHA1.Signature(SignDate, APPKey);
        } catch (SignatureException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        System.out.println("Signature :" + Signature);
        String sAuth = "hmac username=\"" + APPID +
                "\", algorithm=\"hmac-sha1\", headers=\"x-date\", signature=\"" + Signature + "\"";
        System.out.println(sAuth);

        try {
            URL url = new URL(APIUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", sAuth);
            connection.setRequestProperty("x-date", xdate);
            connection.setRequestProperty("Accept-Encoding", "gzip");
            connection.setDoInput(true);
            connection.setDoOutput(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return connection;
    }

    private static KafkaProducer<String, String> createKafkaProducer(String bootstrapServer){
        // create Producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // create safe producer
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
        properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");

        // high throughout producer
        properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");
        properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32*1024));

        // create the producer
        return new KafkaProducer<>(properties);
    }
}