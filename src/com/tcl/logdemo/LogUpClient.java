package com.tcl.logdemo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import android.util.Log;

public class LogUpClient {

    public static final String TAG = "LogUp/LogUpClient";

    private FTPClient ftp = null;

    public LogUpClient() {
        this.ftp = new FTPClient();
    }

    public FTPClient getFtp() {
        return ftp;
    }

    public boolean connect(String server, int port) {
        boolean status = true;

        try {
            int reply;
            if (port > 0) {
                ftp.connect(server, port);
            } else {
                ftp.connect(server);
            }
            Log.d(TAG, "Connected to " + server + " on " + (port>0 ? port : ftp.getDefaultPort()));

            // After connection attempt, you should check the reply code to verify success.
            reply = ftp.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                Log.e(TAG, "FTP server refused connection.");
                status = false;
            }

        } catch (IOException e) {

            status = false;
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                }
                catch (IOException f) {
                    // do nothing
                }
            }
            Log.e(TAG, "Could not connect to server.");
            e.printStackTrace();
        }

        return status;
    }

    public boolean login(String username, String password) {
        boolean status = false;
        if (ftp == null || !ftp.isConnected()) {
            status = connect(LogUp.server, LogUp.port);
            if (!status) return status;
        }

        try {
            for (int i = 0; i < LogUp.LOGIN_TIMES; ++i) {
                if (status = ftp.login(username, password)) {
                    break;
                } else {
                    ftp.logout();
                    continue;
                }
            }

            if (status) {
                int reply = ftp.getReplyCode();
                Log.d(TAG, "Login successful. Reply Code is " + reply);
            } else {
                Log.e(TAG, "Could not login to server.");
                return status;
            }

        } catch (IOException e) {
            status = false;
            e.printStackTrace();
        }

        return status;
    }

    public boolean listFiles() {
        boolean status = false;
        if (ftp == null) {
            return false;
        }

        try {
            for (FTPFile f : ftp.listFiles()) {
                //Log.d(TAG, f.getRawListing());
                Log.d(TAG, f.toFormattedString());
            }
            Log.d(TAG, "ftp.listFiles().length = " + ftp.listFiles().length);

            ftp.noop(); // check that control connection is working OK
            ftp.logout();

        } catch (IOException e) {
            status = false;
            e.printStackTrace();

        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException f){
                    // do nothing
                }
            }
        }

        if (status) {
            Log.i(TAG, "Server Error.");
        } else {
            Log.i(TAG, "Server Ok.");
        }

        return status;
    }

    public boolean upload(String filename, String filepath) {
        boolean status = false;
        if (ftp == null) {
            return false;
        }

        try {
            InputStream input = new FileInputStream(filepath);
            status = ftp.storeFile(filename, input);
            input.close();

            ftp.noop(); // check that control connection is working OK
            ftp.logout();

        } catch (IOException e) {
            status = false;
            e.printStackTrace();

        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException f){
                    // do nothing
                }
            }
        }

        if (status) {
            Log.i(TAG, "upload Ok.");
        } else {
            Log.i(TAG, "upload Error.");
        }

        return status;
    }
    
    public boolean upload(String filename, String filepath, FileTransferListener ftl) {
        boolean status = false;
        if (ftp == null) {
            return false;
        }

        try {
            InputStream input = new FileInputStream(filepath);
            OutputStream output = ftp.storeFileStream(filename);
            Util.copyStream(input, output, -1, -1, ftl, false);
            input.close();

            ftp.noop(); // check that control connection is working OK
            ftp.logout();

        } catch (IOException e) {
            status = false;
            e.printStackTrace();

        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException f){
                    // do nothing
                }
            }
        }

        if (status) {
            Log.i(TAG, "upload Ok.");
        } else {
            Log.i(TAG, "upload Error.");
        }

        return status;
    }

    public void prepare() {
        connect(LogUp.server, LogUp.port);
        login(LogUp.username, LogUp.password);
    }

    public String getFTPStatus() {
        int code = -1;
        try {
            code =  ftp.stat();
            Log.d(TAG, "FTP STAT is " + code);

            if (FTPReply.isPositiveCompletion(code)) {
                String reply = ftp.getReplyString();
                Log.i(TAG, "FTP Reply String is " + reply);
                return reply;
            }
        } catch (IOException e) {
            Log.e(TAG, "FTP STAT Error");
            e.printStackTrace();
        }

        return null;
    }
}
