package com.tcl.logdemo;

public class LogUp {

    public static String server = "192.168.1.100";
    public static int port = -1;
    public static String username = "anonymous";
    public static String password = null;
    public static int LOGIN_TIMES = 3;
    
    public static String http_new_file_addr = "http://" + server + ":8080/springMvc/fileOperate/newfile.aspx?file_name=";
    
    public static final String TAR_EXT = ".tar";
    public static final String GZIP_EXT = ".gz";
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("server:" + server);
        builder.append("port:" + port);
        builder.append("username:" + username);
        builder.append("password:" + password);
        return "LogUp [ status = " + builder.toString() + "]";
    }
    
    
}
