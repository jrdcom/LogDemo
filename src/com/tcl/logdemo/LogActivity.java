package com.tcl.logdemo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.net.ftp.FTPCmd;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LogActivity extends Activity {

    public static final String TAG = "LogUp/LogActivity";
    
    public String externalStoragePath = Environment.getExternalStorageDirectory().getPath();

    private EditText serverEditText = null;
    private EditText portEditText = null;
    private EditText usernameEditText = null;
    private EditText passwordEditText = null;
    
    private EditText reporterEditText = null;
    private EditText descEditText = null;
    private Button selectFileButton = null;
    private TextView logTextView = null;
    private Button uploadButton = null;

    private ArrayList<String> loglist = new ArrayList<String>();
    private File srcFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        
        serverEditText = (EditText) findViewById(R.id.log_server);
        portEditText = (EditText) findViewById(R.id.log_port);
        usernameEditText = (EditText) findViewById(R.id.log_username);
        passwordEditText = (EditText) findViewById(R.id.log_password);
        
        reporterEditText = (EditText) findViewById(R.id.log_reporter);
        descEditText = (EditText) findViewById(R.id.log_description);
        logTextView = (TextView) findViewById(R.id.log_path);
        
        findViewById(R.id.log_select_textview).setVisibility(View.GONE);
        selectFileButton = (Button) findViewById(R.id.log_select);
        selectFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //selectFile();
            }
        });
        selectFileButton.setVisibility(View.GONE);
        
        uploadButton = (Button) findViewById(R.id.log_upload);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                new Thread(uploadRunnable).start();
            }
        });

//        String path = externalStoragePath + "/Download/invite.ics";
//        LogUp.server = "192.168.1.105";
//        LogUp.port = 3721;
        //LogUp.server = "192.168.1.101";
        //LogUp.username = "test";
        //LogUp.password = "test";
        LogUp.username = "android";
        LogUp.password = "mobile#3";
        serverEditText.setText(LogUp.server);
        portEditText.setText("21");
        usernameEditText.setText(LogUp.username);
        passwordEditText.setText(LogUp.password);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        new Thread(selectFileRunnable).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        StringBuilder builder = new StringBuilder();
        for (String log : loglist) {
            builder.append(log + "\n");
        }
        logTextView.setText(builder.toString());
    }
    
    Runnable selectFileRunnable = new Runnable() {
        @Override
        public void run() {
            File memInfo = new File("/data/anr/traces.txt");
            srcFile = memInfo;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        if (resultCode == RESULT_OK) {
            Uri contentUri = data != null ? data.getData() : null;
            Log.d(TAG, "data = " + data.toString());
            Log.d(TAG, "contentUri = " + contentUri.toString());
            Log.d(TAG, "contentUri.getPath() = " + contentUri.getPath());
            
            ParcelFileDescriptor logFileDescriptor = null;
            try {
                logFileDescriptor = getContentResolver().openFileDescriptor(contentUri, "r");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //int fd = logFileDescriptor.getFd();
            /*try {
                
                Cursor metadataCursor = null;
                try {
                    metadataCursor = contentResolver.query(
                            contentUri, new String[]{MediaStore.Files.FileColumns.DATA},
                            null, null, null);
                    if (metadataCursor != null) {
                        try {
                            if (metadataCursor.moveToNext()) {
                                String path = metadataCursor.getString(0);
                                logFile = new File(path);
                                Log.d(TAG, logFile.getAbsolutePath());
                            }
                        } finally {
                            metadataCursor.close();
                        }
                    }
                } catch (SQLiteException ex) {
                    ex.printStackTrace();
                }
                
                
                
                
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (file != null) {
                        file.close();
                    }
                } catch (IOException e) {
                }
            }*/
            
            /*if (fd == -1) {
                Toast.makeText(this, "add log file failed", Toast.LENGTH_SHORT).show();
            } else {
                String path = "" + fd;
                loglist.add(path);
            }*/
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    private void uploadLog(File logfile) {
        
        LogUp.server = serverEditText.getText().toString();
        LogUp.port = Integer.valueOf(portEditText.getText().toString());
        LogUp.username = usernameEditText.getText().toString();
        LogUp.password = passwordEditText.getText().toString();
        
        String reporter = reporterEditText.getText().toString();
        String desc = descEditText.getText().toString();
        
        // create phone info file
        String xmlPath = externalStoragePath + "/Download/info.xml";
        File infoFile = Util.getPhoneInfoFile(this, xmlPath, reporter, desc);
        Log.d(TAG, "phone info file path is " + infoFile.getAbsolutePath());
        
        String[] srcPaths = { infoFile.getAbsolutePath(), logfile.getAbsolutePath() };
        String destPath = externalStoragePath + "/Download/";
        
        try {
            File file = Util.archive(srcPaths, destPath);
            Log.d(TAG, "archive finish, file name is " + file.getName());
            file = Util.compress(file);
            Log.d(TAG, "compress finish, file name is " + file.getName());
            
            Intent serviceIntent = new Intent(LogActivity.this, LogUpService.class);
            serviceIntent.setAction(LogUpService.ACTION_FTP_CMD);
            serviceIntent.putExtra(LogUpService.EXTRAS_FTP_CMD, FTPCmd.STOR.getCommand());
            serviceIntent.putExtra(LogUpService.EXTRAS_FILE_PATH, file.getAbsolutePath());
            startService(serviceIntent);
            Log.d(TAG, "Start LogUpService");
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CompressorException e) {
            e.printStackTrace();
        }
    }
    
    private void selectFile() {
        Intent actionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        actionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        actionIntent.setType("*/*");

        Intent fileIntent = new Intent("com.mediatek.filemanager.ADD_FILE");
        fileIntent.addCategory(Intent.CATEGORY_OPENABLE);

        PackageManager pm = getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(fileIntent, PackageManager.MATCH_DEFAULT_ONLY);

        // for test
        Log.d(TAG, "list size = " + list.size());
        for (ResolveInfo r : list) {
            ActivityInfo ai = r.activityInfo;
            Intent intent = new Intent();
            intent.setPackage(ai.packageName);
            intent.setClassName(ai.packageName, ai.name);
            Log.d(TAG, "intent = " + intent.toString());
        }

        if (list.size() != 0) {
            startActivityForResult(fileIntent, 0);
        } else {
            Intent chooser = Intent.createChooser(actionIntent, "Choose file to import");
            startActivityForResult(chooser, 0);
        }
    }
    
    Runnable uploadRunnable = new Runnable() {
        @Override
        public void run() {
            if (srcFile != null) {
                uploadLog(srcFile);
            }
        }
    };

}
