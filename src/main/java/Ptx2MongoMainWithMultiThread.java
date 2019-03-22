public class Ptx2MongoMainWithMultiThread {

    public static void main(String[] args) throws InterruptedException {

        final int REFRESH_PERIOD = 10*1000; // 每10秒重抓一次全部資料
        final String[] COUNTIES1 = {"Taipei","NewTaipei","YilanCounty","Keelung"}; // 北北宜基
        final String[] COUNTIES2 = {"Taoyuan","Hsinchu","HsinchuCounty","TaiChung"}; // 桃竹中
        final String[] COUNTIES3 = {
                "Tainan","Kaohsiung","ChanghuaCounty","MiaoliCounty",
                "NantouCounty","YunlinCounty","ChiayiCounty","Chiayi","PingtungCounty","HualienCounty",
                "TaitungCounty","KinmenCounty","PenghuCounty","LienchiangCounty"};

        while(true){ // infinite loop
            long startTimeStamp = System.currentTimeMillis();

            // create thread
            InterCityBus interCityBus = new InterCityBus();
            Thread interCityBusThread = new Thread(interCityBus);
            /*Bus bus1 = new Bus(COUNTIES1);
            Thread bus1Thread = new Thread(bus1);
            Bus bus2 = new Bus(COUNTIES2);
            Thread bus2Thread= new Thread(bus2);
            Bus bus3= new Bus(COUNTIES3);
            Thread bus3Thread = new Thread(bus3);
            Tra tra = new Tra();
            Thread traThread = new Thread(tra);*/

            // start and join thread
            interCityBusThread.start();
            /*bus1Thread.start();
            bus2Thread.start();
            bus3Thread.start();
            traThread.start();*/
            interCityBusThread.join();
            /*bus1Thread.join();
            bus2Thread.join();
            bus3Thread.join();
            traThread.join();*/

            System.out.println("---------------------------------------------");
            System.out.println("Total " + interCityBus.threadLength + " documents upserted for interCityBus");
            /*System.out.println("Total " + bus1.threadLength + " documents upserted for bus1");
            System.out.println("Total " + bus2.threadLength + " documents upserted for bus2");
            System.out.println("Total " + bus3.threadLength + " documents upserted for bus3");
            System.out.println("Total " + tra.threadLength + " documents upserted for tra");*/

            System.out.println((System.currentTimeMillis() - startTimeStamp)/1000 + " seconds taken to refresh");
            System.out.println("Waiting for next loop...\n");
            Thread.sleep(REFRESH_PERIOD);
        }
    }
}