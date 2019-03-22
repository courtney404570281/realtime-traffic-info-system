import org.bson.Document;

public class Bus extends Transportation{

    public Bus(String[] counties){
        for(String county : counties){
            // 動態定時資料
            urls.add("http://ptx.transportdata.tw/MOTC/v2/Bus/RealTimeByFrequency/City/" + county + "?$format=JSON&$top=" + FETCH_ALLOW);
            collections.add("bus_" + county.toLowerCase() + "_rtFrequency");
            // 動態定點資料
            urls.add("http://ptx.transportdata.tw/MOTC/v2/Bus/RealTimeNearStop/City/" + county + "?$format=JSON&$top=" + FETCH_ALLOW);
            collections.add("bus_" + county.toLowerCase() + "_rtNearStop");
            // 預估到站資料
            urls.add("http://ptx.transportdata.tw/MOTC/v2/Bus/EstimatedTimeOfArrival/City/" + county + "?$filter=PlateNumb%20ne%20'-1'&$format=JSON&$top=" + FETCH_ALLOW);
            collections.add("bus_" + county.toLowerCase() + "_rtEstimated");
        }
    }

    @Override
    String getIdOf(Document document, String collection) {
        if(collection.endsWith("rtFrequency") || collection.endsWith("rtNearStop")) {
            return document.getString("PlateNumb");
        }
        else if(collection.endsWith("rtEstimated")){
            return document.getString("SubRouteID") + "_" +
                    document.getString("StopID") + "_" +
                    document.getString("PlateNumb");
        }
        else{ return "Unexpected"; }
    }
}