package com.tcl.logdemo;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPCmd;

import com.tcl.logdemo.LogListActivity.ArchiveListener;

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
                ArchiveListener listener = new ArchiveListener();
                boolean status = client.upload(filename, filepath, listener);
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
    
    class ArchiveListener implements FileTransferListener {
        
        BigDecimal totalSizeBigDecimal;
        long totalTransferred;
        private String lastProgress = "";

        public void prepareTransfer(long totalBytes) {
            Log.d(TAG, "FileTransferListener prepareTransfer totalBytes is " + totalBytes);
            totalSizeBigDecimal = new BigDecimal(totalBytes);
            totalTransferred = 0;
        }

        public void bytesTransferred(long totalBytesTransferred,
                int bytesTransferred, long streamSize) {
            
            totalTransferred += bytesTransferred;
            
            BigDecimal bytesTransferredBigDecimal = new BigDecimal(totalTransferred);
            String progress = bytesTransferredBigDecimal.
                    divide(totalSizeBigDecimal, 2, RoundingMode.DOWN).toString();
            
            if (!lastProgress.equalsIgnoreCase(progress)) {
                Log.d(TAG, "transfer progress is " + progress +
                        ", total is " + totalTransferred);
            }
            lastProgress = progress;
        }

        @Override
        public void transferComplete() {
            Log.d(TAG, "FileTransferListener transferComplete");
        }
    }
}
