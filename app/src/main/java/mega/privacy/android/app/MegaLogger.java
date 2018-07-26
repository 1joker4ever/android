package mega.privacy.android.app;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedDeque;

import mega.privacy.android.app.utils.Util;

public abstract class MegaLogger {
    protected static ConcurrentLinkedDeque<String> logQueue = new ConcurrentLinkedDeque<>();
    protected static String separator = "&&";
    protected SimpleDateFormat simpleDateFormat;
    protected File logFile;
    protected String dir, fileName;

    public MegaLogger(String fileName, boolean fileLogger) {
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        logFile = null;
        dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Util.logDIR + "/";
        this.fileName = fileName;
        startAsyncLogger();
    }

    protected String createMessage(String message) {
        String currentDateAndTime = simpleDateFormat.format(new Date());
        message = "(" + currentDateAndTime + ") - " + message;
        return message;
    }

    protected static void startAsyncLogger() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    String log = logQueue.pollFirst();
                    if (log != null) {
                        String[] combined = log.split(separator);
                        Log.d(combined[0], combined[1]);
                    } else {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        thread.start();
    }

    protected boolean isReadyToWriteToFile(boolean enabled){
        if (enabled) {
            if(logFile == null || !logFile.exists()){
                File dirFile = new File(dir);
                if (!dirFile.exists()) {
                    dirFile.mkdirs();
                }

                logFile = new File(dirFile, fileName);
                if (!logFile.exists()) {
                    try {
                        logFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return true;
        }
        return false;
    }

    protected void writeToFile(String appendContents) {
        try {
            if (logFile != null && logFile.canWrite()) {
                logFile.createNewFile(); // ok if returns false, overwrite
                Writer out = new BufferedWriter(new FileWriter(logFile, true), 256);
                out.write(appendContents);
                out.close();
            }
        } catch (IOException e) {
               Log.e("Mega Logger", "Error appending string data to file " + e.getMessage(), e);
        }
    }

    protected abstract void logToFile();
}
