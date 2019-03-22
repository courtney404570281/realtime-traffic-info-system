import com.google.gson.JsonArray;

import java.io.*;
import java.security.SignatureException;

public class PtxTest {

    public static void main(String[] args) throws IOException, SignatureException {

        final String[] counties = {"Taipei","NewTaipei","Taoyuan","Taichung","Tainan","Kaohsiung","Keelung","Hsinchu","HsinchuCounty","MiaoliCounty","ChanghuaCounty","NantouCounty","YunlinCounty","ChiayiCounty","Chiayi","PingtungCounty","YilanCounty","HualienCounty","TaitungCounty","KinmenCounty","PenghuCounty","LienchiangCounty"};

        for(String county : counties){
            long startTimeStamp = System.currentTimeMillis();
            String apiUrl = "http://ptx.transportdata.tw/MOTC/v2/Bus/EstimatedTimeOfArrival/City/"+county+"?$filter=PlateNumb%20ne%20'-1'&$format=JSON";
            JsonArray ja = Ptx.getResponseJsonFrom(apiUrl);
            long endTimeStamp = System.currentTimeMillis();
            System.out.printf("%-16s%-7d%-4d\n", county, ja.size(), (endTimeStamp-startTimeStamp));

        }

        long startTimeStamp = System.currentTimeMillis();
        String apiUrl = "http://ptx.transportdata.tw/MOTC/v2/Bus/EstimatedTimeOfArrival/interCity?$filter=PlateNumb%20ne%20'-1'&$format=JSON";
        JsonArray ja = Ptx.getResponseJsonFrom(apiUrl);
        long endTimeStamp = System.currentTimeMillis();
        System.out.printf("%-16s%-7d%-4d\n", "interCity", ja.size(), (endTimeStamp-startTimeStamp));


//        Type RailStationListType = new TypeToken<ArrayList<RailStation>>(){}.getType();
//        Gson gsonReceiver = new Gson();
//        List<RailStation> obj = gsonReceiver.fromJson(response, RailStationListType);
//        System.out.println(response);
//
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        JsonElement je = new JsonParser().parse(response);
//        String prettyJsonString = gson.toJson(je);
//        System.out.println(prettyJsonString);

    }


}