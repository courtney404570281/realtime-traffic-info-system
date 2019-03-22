import org.apache.commons.cli.*;

public class Ptx2MongoFixedAndDaily {

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("f", "fixed", false, "update fixed data");
        options.addOption("d", "daily", false, "update daily data");
        CommandLine cmd;
        try {
            cmd = new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
            new HelpFormatter().printHelp("Ptx2MongoFixedAndDaily.jar", options);
            return;
        }

        FixedAndDaily fixedAndDaily = new FixedAndDaily(cmd);
        Thread thread = new Thread(fixedAndDaily);
        thread.start();
    }
}