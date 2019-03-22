import org.bson.Document;

class Tra extends Transportation{

    Tra(){
        // 動態定時資料
        urls.add("http://ptx.transportdata.tw/MOTC/v2/Rail/TRA/LiveTrainDelay?$format=JSON&$top=" + FETCH_ALLOW);
        collections.add("tra_rtDelay");
    }

    @Override
    String getIdOf(Document document, String collection) {
        switch (collection) {
            case "tra_rtDelay":
                return document.getString("TrainNo");
            default:
                return "Unexpected";
        }
    }
}
