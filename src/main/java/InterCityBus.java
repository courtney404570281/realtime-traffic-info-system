import org.bson.Document;

class InterCityBus extends Transportation{
    
    InterCityBus(){
        // 動態定時資料
        urls.add("http://ptx.transportdata.tw/MOTC/v2/Bus/RealTimeByFrequency/InterCity?$format=JSON&$top=" + FETCH_ALLOW);
        collections.add("icb_rtFrequency");
        // 動態定點資料
        urls.add("http://ptx.transportdata.tw/MOTC/v2/Bus/RealTimeNearStop/InterCity?$format=JSON&$top=" + FETCH_ALLOW);
        collections.add("icb_rtNearStop");
        // 預估到站資料
//        urls.add("http://ptx.transportdata.tw/MOTC/v2/Bus/EstimatedTimeOfArrival/InterCity?$filter=PlateNumb%20ne%20'-1'&$format=JSON&$top=" + FETCH_ALLOW);
        urls.add("http://ptx.transportdata.tw/MOTC/v2/Bus/EstimatedTimeOfArrival/InterCity?$format=JSON&$top=" + FETCH_ALLOW);
        collections.add("icb_rtEstimated");
    }

    @Override
    String getIdOf(Document document, String collection) {
        switch (collection) {
            case "icb_rtFrequency":
            case "icb_rtNearStop":
                return document.getString("PlateNumb");
            case "icb_rtEstimated":
                return document.getString("SubRouteID") + "_" +
                        document.getString("StopID") + "_" +
                        document.getString("PlateNumb");
            default:
                return "Unexpected";
        }
    }
}
