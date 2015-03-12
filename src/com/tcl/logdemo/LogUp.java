package com.tcl.logdemo;

import java.io.File;

import android.os.Environment;

public class LogUp {

    public static String server = "192.168.1.100";
    public static int port = -1;
    public static String username = "anonymous";
    public static String password = null;
    public static int LOGIN_TIMES = 3;
    
    public static String http_new_file_addr = "http://" + server + ":8080/springMvc/fileOperate/newfile.aspx?file_name=";
    
    public static final String TAR_EXT = ".tar";
    public static final String GZIP_EXT = ".gz";
    
    public String externalStoragePath = Environment.getExternalStorageDirectory().getPath();
    
    public static String getString() {
        StringBuilder builder = new StringBuilder();
        builder.append("server:" + server);
        builder.append("port:" + port);
        builder.append("username:" + username);
        builder.append("password:" + password);
        return "LogUp [ status = " + builder.toString() + "]";
    }
    
    public static File getHomeDirectory() {
        String path = Environment.getExternalStorageDirectory().getPath() + "/LogUp";
        File home = new File(path);
        if (!home.exists()) {
            home.mkdirs();
        }
        return home;
    }
    
    public static String getHomePath() {
        File home = getHomeDirectory();
        return home.getPath();
    }
}
