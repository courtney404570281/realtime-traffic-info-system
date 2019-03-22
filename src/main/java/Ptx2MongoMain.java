import java.io.IOException;
import java.security.SignatureException;

public class Ptx2MongoMain {

    public static void main(String[] args) throws InterruptedException, IOException, SignatureException {

        final int REFRESH_PERIOD = 10*1000; // 每10秒重抓一次全部資料

        while(true){ // 無限迴圈
            long startTimeStamp = System.currentTimeMillis();

            InterCityBus interCityBus = new InterCityBus();
            interCityBus.run();
            System.out.println("Total " + interCityBus.threadLength + " documents upserted for interCityBus");

            Bus bus = new Bus(new String[]{"Taipei","NewTaipei","Taoyuan","Taichung","Tainan","Kaohsiung","Keelung","Hsinchu","HsinchuCounty","MiaoliCounty","ChanghuaCounty","NantouCounty","YunlinCounty","ChiayiCounty","Chiayi","PingtungCounty","YilanCounty","HualienCounty","TaitungCounty","KinmenCounty","PenghuCounty","LienchiangCounty"});
            bus.run();
            System.out.println("Total " + bus.threadLength + " documents upserted for bus");

            long endTimeStamp = System.currentTimeMillis();
            System.out.println((endTimeStamp - startTimeStamp)/1000 + " seconds taken to refresh");
            System.out.println("Waiting for next refresh...\n");
            Thread.sleep(REFRESH_PERIOD);
        }
    }
}