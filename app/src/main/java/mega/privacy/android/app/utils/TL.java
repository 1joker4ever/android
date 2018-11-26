package mega.privacy.android.app.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Logger for testing.
 */
public class TL {

    private final static boolean OUTPUT = true;

//    private static final String LOG_FILE = Environment.getExternalStorageDirectory() + File.separator +
//            "MEGA"+ File.separator +
//            "MEGA Logs"+ File.separator +
//            "test_log.txt";
    private static final String LOG_PATH = Environment.getExternalStorageDirectory() + File.separator +
            "MEGA"+ File.separator +
            "MEGA Logs";
    private static final String FILE_NAME = "test_log.txt";

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd HH:mm:ss.SSS");

    public static void log(Object context,Object any) {
        String msg = (any == null) ? "NULL" : any.toString();
        String dateStr = DATE_FORMAT.format(new Date());
        if (context != null) {
            if (context instanceof String) {
                msg = "[" + dateStr + "] " + context + "--->" + msg;
            } else {
                msg = "[" + dateStr + "] " + context.getClass().getSimpleName() + "--->" + msg;
            }
        }
        if (OUTPUT) {
            File log = new File(LOG_PATH);
            try {
                if (!log.exists()) {
                    log.mkdir();
                }
                
                File logFile = new File(LOG_PATH + File.separator + FILE_NAME);
                if (!logFile.exists()){
                    logFile.createNewFile();
                }
                FileWriter writer = new FileWriter(LOG_PATH + File.separator + FILE_NAME,true);
                writer.write(msg + "\n");
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.e("@#@",msg);
    }

    public static void main(String[] args) {
        System.out.println(new Date(1540165899000L));
    }
}
