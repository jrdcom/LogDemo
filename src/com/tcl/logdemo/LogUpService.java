package com.tcl.logdemo;

import java.io.IOException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPCmd;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * A service that process each file upload request i.e Intent by opening a
 * socket connection with the FTP server and writing the file
 */
public class LogUpService extends IntentService {

    public static final String TAG = "LogUp/LogUpService";

    public static final String ACTION_FTP_CMD = "com.tcl.logup.FTP_CMD";
    public static final String EXTRAS_FTP_CMD = "cmd";
    public static final String EXTRAS_FILE_PATH = "file_path";

    private LogUpClient client = null;

    public LogUpService(String name) {
        super(name);
    }

    public LogUpService() {
        super("LogUpService");
    }

    /*
     * (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "LogUpService onHandleIntent");

        if (intent.getAction().equals(ACTION_FTP_CMD)) {
            String cmd = intent.getExtras().getString(EXTRAS_FTP_CMD);
            String filepath = intent.getExtras().getString(EXTRAS_FILE_PATH);
            if (FTPCmd.STOR.getCommand().equalsIgnoreCase(cmd)) {
                client = new LogUpClient();
                client.connect(LogUp.server, LogUp.port);
                client.login(LogUp.username, LogUp.password);
                try {
                    boolean setFileType = client.getFtp().setFileType(FTP.BINARY_FILE_TYPE);
                    Log.d(TAG, "setFileType is " + setFileType + ", File Type is BINARY_FILE_TYPE");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String filename = filepath.substring(filepath.lastIndexOf("/")+1);
                boolean status = client.upload(filename, filepath);
                if (status) {
                    Util.upDone(filename);
                    Log.d(TAG, "upload log finish");
                } else {
                    Log.d(TAG, "upload log failed");
                }
            }
            Log.d(TAG, "LogUpService finish");
        }
    }
}
