package com.tcl.logdemo;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.net.ftp.FTPCmd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class LogListActivity extends Activity {

    public static final String TAG = "LogUp/LogListActivity";

    private Button logReadyButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_list);

        logReadyButton = (Button) findViewById(R.id.button_log_ready);
        logReadyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                new Thread(uploadRunnable).start();
            }
        });
    }

    Runnable uploadRunnable = new Runnable() {
        @Override
        public void run() {
            long size = 0;
            String[] paths = getResources().getStringArray(R.array.log_file_path);
            ArrayList<String> srcPaths = new ArrayList<String>();

            long time = System.currentTimeMillis();
            for (String path : paths) {
                long folderSize = Util.getFolderSize(path);
                Log.d(TAG, "path size = " + folderSize);
                if (folderSize > 0) {
                    size = size + folderSize;
                    srcPaths.add(path);
                }
            }

            Log.d(TAG, "elapsed time is " + (System.currentTimeMillis() - time));
            Log.d(TAG, "paths size = " + size);
            Log.d(TAG, "paths size = " + Util.getFormatSize(size));
            
            String destPath = LogUp.getHomePath();
            
            ArchiveListener listener = new ArchiveListener();
            
            File file = null;
            try {
                time = System.currentTimeMillis();
                file = Util.archive(srcPaths.toArray(new String[]{}), destPath, listener);
                Log.d(TAG, "archive finish, file name is " + file.getName() + ", size is " + file.length());
                Log.d(TAG, "listener totalTransferred is " + listener.totalTransferred);
                Log.d(TAG, "elapsed time is " + (System.currentTimeMillis() - time));
                
                time = System.currentTimeMillis();
                file = Util.compress(file, listener);
                Log.d(TAG, "compress finish, file name is " + file.getName() + ", size is " + file.length());
                Log.d(TAG, "elapsed time is " + (System.currentTimeMillis() - time));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CompressorException e) {
                e.printStackTrace();
            }
            
            LogUp.server = "192.168.1.100";
            LogUp.username = "android";
            LogUp.password = "mobile#3";
            
            Intent serviceIntent = new Intent(LogListActivity.this, LogUpService.class);
            serviceIntent.setAction(LogUpService.ACTION_FTP_CMD);
            serviceIntent.putExtra(LogUpService.EXTRAS_FTP_CMD, FTPCmd.STOR.getCommand());
            serviceIntent.putExtra(LogUpService.EXTRAS_FILE_PATH, file.getAbsolutePath());
            startService(serviceIntent);
            Log.d(TAG, "Start LogUpService");
            
        }
    };
    
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
