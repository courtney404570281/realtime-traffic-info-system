import org.apache.commons.cli.CommandLine;
import org.bson.Document;

class FixedAndDaily extends Transportation {

    FixedAndDaily(CommandLine cmd) {
        if(cmd.hasOption("f")){
            // 台鐵車站資料
            urls.add("http://ptx.transportdata.tw/MOTC/v2/Rail/TRA/Station?$format=JSON&$top=" + FETCH_ALLOW);
            collections.add("tra_station");
            // 台鐵票價資料
            /*urls.add("http://ptx.transportdata.tw/MOTC/v2/Rail/TRA/ODFare?$format=JSON&$top=" + FETCH_ALLOW);
            collections.add("tra_odfare");*/
            // 公路客運路線與站牌
            urls.add("http://ptx.transportdata.tw/MOTC/v2/Bus/StopOfRoute/InterCity?$format=JSON&$top=" + FETCH_ALLOW);
            collections.add("icb_stopOfRoute");
            // 公路客運路線資料
            urls.add("http://ptx.transportdata.tw/MOTC/v2/Bus/Route/InterCity?$format=JSON&$top=" + FETCH_ALLOW);
            collections.add("icb_route");
            // 公路客運路線資料
            urls.add("http://ptx.transportdata.tw/MOTC/v2/Bus/Stop/InterCity?$format=JSON&$top=" + FETCH_ALLOW);
            collections.add("icb_stop");
        }
        /* TODO
        if(cmd.hasOption("d")){

        }*/
    }

    @Override
    String getIdOf(Document document, String collection) {
        switch (collection) {
            case "tra_station":
                return document.getString("StationID");
            case "tra_odfare":
                return document.getString("OriginStationID") + "_" +
                        document.getString("DestinationStationID");
            case "icb_stopOfRoute":
                return document.getString("SubRouteID");
            case "icb_route":
                return document.getString("RouteID");
            case "icb_stop":
                return document.getString("StopID");
            default:
                return "Unexpected";
        }
    }
}
