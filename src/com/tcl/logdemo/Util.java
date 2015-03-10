package com.tcl.logdemo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class Util {

    public static final String TAG = "LogUp/Util";
    
    /**
     * The default buffer size ({@value}) used by
     * {@link #copyStream  copyStream } and {@link #copyReader  copyReader}
     * and by the copyReader/copyStream methods if a zero or negative buffer size is supplied.
     */
    public static final int DEFAULT_COPY_BUFFER_SIZE = 1024;

    public static void logd() {
        String fileName = Thread.currentThread().getStackTrace()[3].getFileName();
        String className = Thread.currentThread().getStackTrace()[3].getClassName();
        String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
        int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();
        String log = "[" + fileName + "]" + ", " + "[" + className + "]" + ", " + 
                "[" + methodName + "]" + ", " + "[" + lineNumber + "]";
        Log.d(TAG, log);
    }

    public static void logd(Object paramObject) {
        String fileName = Thread.currentThread().getStackTrace()[3].getFileName();
        String className = Thread.currentThread().getStackTrace()[3].getClassName();
        String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
        int lineNumber = Thread.currentThread().getStackTrace()[3].getLineNumber();
        String log = "[" + fileName + "]" + ", " + "[" + className + "]" + ", " + 
                "[" + methodName + "]" + ", " + "[" + lineNumber + "]";
        Log.d(TAG, log + ", " + paramObject);
    }
    
    public static String getcurrentTimeMillis() {
        Date mDate = new Date(System.currentTimeMillis());
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String dateTime = dateTimeFormat.format(mDate);
        Log.d(TAG, "current date and time is " + dateTime);
        return dateTime;
    }
    
    public static String getcurrentDate() {
        Date mDate = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(mDate);
        Log.d(TAG, "current date is " + date);
        return date;
    }
    
    public static String getcurrentTime() {
        Date mDate = new Date(System.currentTimeMillis());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String time = timeFormat.format(mDate);
        Log.d(TAG, "current time is " + time);
        return time;
    }

    public static File archive(String srcPath, String destPath) throws IOException {
        return archive(new File(srcPath), new File(destPath));
    }

    // srcFile is file or directory, destFile is file or directory
    public static File archive(File srcFile, File destFile) throws IOException {
        return archive(new File[]{srcFile,}, destFile);
    }
    
    public static File archive(String[] srcPaths, String destPath) throws IOException {
        ArrayList<File> fileList = new ArrayList<File>();
        for (String srcPath : srcPaths) {
            fileList.add(new File(srcPath));
        }
        File[] fileArray = new File[fileList.size()];
        return archive(fileList.toArray(fileArray), new File(destPath));
        //archive(fileList.toArray(new File[]{}), destFile, null);
    }
    
    public static File archive(File[] srcFile, File destFile) throws IOException {
        // init variable
        String destPath = destFile.getAbsolutePath();
        String archiveFilePath = destPath;
        Log.i(TAG, "File.separator is " + File.separator);
        
        // generate *.tar file name
        String fileName = "" + System.currentTimeMillis();
        Log.i(TAG, "srcFile length is " + srcFile.length);
        
        if (srcFile == null || srcFile.length == 0) return null;

        if (srcFile.length == 1) {
            fileName = srcFile[0].getName();
        } else {
            //fileName = getcurrentTimeMillis();
            UUID mUUID = UUID.randomUUID();
            fileName = mUUID.toString();
            Log.d(TAG, "" + mUUID.toString());
            Log.d(TAG, "" + String.valueOf(mUUID.getMostSignificantBits()) + " " + Long.toHexString(mUUID.getMostSignificantBits()));
            Log.d(TAG, "" + String.valueOf(mUUID.getLeastSignificantBits() + " " + Long.toHexString(mUUID.getLeastSignificantBits())));
            Log.d(TAG, "" + UUID.randomUUID().toString());
        }
        
        // if the destFile is directory, then generate *.tar file path
        if (isDirPath(destPath)) {
            String path = destPath.endsWith(File.separator) ? destPath : destPath + File.separator;
            archiveFilePath = path + fileName + LogUp.TAR_EXT;
        }
        Log.d(TAG, "archive file path is " + archiveFilePath);
        
        // create *.tar file
        File archiveFile = fileProber(archiveFilePath);
        
        // start archive srcFile
        TarArchiveOutputStream taos =
                new TarArchiveOutputStream(new FileOutputStream(archiveFile));
        
        for (File file : srcFile) {
            archive(file, taos, "");
        }
        taos.flush();
        taos.close();
        return archiveFile;
    }

    private static void archive(File file, TarArchiveOutputStream taos, String basePath) throws IOException {

        if (file.isDirectory()) {
            TarArchiveEntry entry = new TarArchiveEntry(basePath + file.getName() + File.separator);
            taos.putArchiveEntry(entry);
            taos.closeArchiveEntry();

            File[] files = file.listFiles();
            for (File f : files) {
                archive(f, taos, basePath + file.getName() + File.separator);
            }

        } else {
            TarArchiveEntry entry = new TarArchiveEntry(basePath + file.getName());
            entry.setSize(file.length());
            taos.putArchiveEntry(entry);

            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            IOUtils.copy(bis, taos);

            bis.close();
            taos.closeArchiveEntry();
        }
    }
    
    public static void dearchive(String srcPath) throws IOException {
        File srcFile = new File(srcPath);
        dearchive(srcFile, srcFile.getParentFile());
    }
    
    public static void dearchive(String srcPath, String destPath) throws IOException {
        dearchive(new File(srcPath), new File(destPath));
    }
    
    // srcFile must exists and be a *.tar file, destFile must be a directory
    public static void dearchive(File srcFile, File destFile) throws IOException {
        if (!srcFile.exists() || !srcFile.isFile()
                || !srcFile.getAbsolutePath().endsWith(LogUp.TAR_EXT)) {
            Log.e(TAG, "srcFile is not a archive file!");
            return;
        }
        
        File destDir = new File(destFile.getAbsolutePath() + File.separator);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        TarArchiveInputStream tais =
                new TarArchiveInputStream(new FileInputStream(srcFile));
        dearchive(tais, destDir);
        tais.close();
    }
    
    private static void dearchive(TarArchiveInputStream tais, File destFile) throws IOException {
        TarArchiveEntry entry = null;
        while ((entry = tais.getNextTarEntry()) != null) {
            
            String filePath = destFile.getPath() + File.separator + entry.getName();
            Log.d(TAG, "entry.name = " + entry.getName());
            
            File file = null;
            if (!entry.isDirectory()) {
                file = fileProber(filePath);
                
                if (file != null) {
                    BufferedOutputStream bos =
                            new BufferedOutputStream(new FileOutputStream(file));
                    IOUtils.copy(tais, bos);
                    bos.flush();
                    bos.close();
                }
            }
        }
    }
    
    // Probe file whether exists, Can't apply with directory
    private static File fileProber(String path) {
        File file = new File(path);
        
        if(!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                Log.e(TAG, "create archive file's parent directory failed");
                return null;
            }
        }
        
        if(!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    Log.e(TAG, "create archive file failed");
                    return null;
                }
            } catch (IOException e) {
                Log.e(TAG, "create archive file error");
                e.printStackTrace();
            }
        }
        return file;
    }
    
    // Whether is directory path
    // return true, if directory exists
    // return true, if path end with '/'
    // return true, if the end word without '.'
    private static boolean isDirPath(String path) {
        
        File file = new File(path);
        if(file.exists()) return file.isDirectory();
        
        if(path.endsWith(File.separator)) return true;
        
        String[] str = path.split(File.separator);
        if(str.length > 0) {
            int index = str[str.length-1].indexOf(".");
            if (index == -1) {
                return true;
            }
        }
        return false;
    }
    
    public static File compress(File srcFile) throws IOException, CompressorException {
        if(srcFile == null) return null;
        
        String destPath = srcFile.getAbsolutePath() + LogUp.GZIP_EXT;
        File destFile = new File(destPath);
        
        final OutputStream out = new FileOutputStream(destFile);
        CompressorOutputStream cos =
                new CompressorStreamFactory().
                    createCompressorOutputStream(CompressorStreamFactory.GZIP, out);
        InputStream is = new FileInputStream(srcFile);
        IOUtils.copy(is, cos);
        
        is.close();
        cos.flush();
        cos.close();
        
        return destFile;
    }
    
    public static String getPhoneInfo(Context context) {
        PhoneInfo phoneInfo = PhoneInfo.getInstance(context);
        String imei = phoneInfo.getIMEI();
        
        StringBuilder builder = new StringBuilder();
        builder.append(PhoneInfo.BuildInfo.DEVICE + "\n");
        builder.append(PhoneInfo.BuildInfo.VERSION.INCREMENTAL + "\n");
        builder.append(imei + "\n");
        return builder.toString();
    }
    
    public static void getPhoneInfo(Context context, Element infoElement) {
        PhoneInfo phoneInfo = PhoneInfo.getInstance(context);

        /*Element infoElement = document.addElement("info");
        infoElement.addComment("This is a test for dom4j");

        Element testElement = infoElement.addElement("book");
        testElement.addAttribute("show","yes");
        Element titleElement = testElement.addElement("title");
        titleElement.setText("Dom4j Tutorials");*/
        
        //ro.serialno
        addElement(infoElement, PhoneInfo.SERIAL, PhoneInfo.BuildInfo.SERIAL);
        
        //ro.product.device
        addElement(infoElement, PhoneInfo.PRODUCT, PhoneInfo.BuildInfo.DEVICE);
        //ro.build.version.incremental
        addElement(infoElement, PhoneInfo.VERSION, PhoneInfo.BuildInfo.VERSION.INCREMENTAL);
        addElement(infoElement, PhoneInfo.IMEI, phoneInfo.getIMEI());
        addElement(infoElement, PhoneInfo.DATE, getcurrentDate());
        addElement(infoElement, PhoneInfo.TIME, getcurrentTime());
    }
    
    public static File getPhoneInfo2(Context context, String filename) {
        PhoneInfo phoneInfo = PhoneInfo.getInstance(context);
        String imei = phoneInfo.getIMEI();
        
        Document document = DocumentHelper.createDocument();
        Element infoElement = document.addElement("info");
        
        Element productElement = infoElement.addElement(PhoneInfo.PRODUCT);
        productElement.setText(PhoneInfo.BuildInfo.DEVICE); //ro.product.device
        
        Element versionElement = infoElement.addElement(PhoneInfo.VERSION);
        versionElement.setText(PhoneInfo.BuildInfo.VERSION.INCREMENTAL); //ro.build.version.incremental
        
        Element imeiElement = infoElement.addElement(PhoneInfo.IMEI);
        imeiElement.setText(imei);
        
        return createXMLFile(document, filename);
    }
    
    private static void addElement(Element parentElement, String element, String text) {
        Element newElement = parentElement.addElement(element);
        if (text != null) newElement.setText(text);
    }

    private static File createXMLFile(Document document, String filename) {

        File xmlFile = new File(filename);
        try{
            XMLWriter writer = new XMLWriter(new FileWriter(xmlFile));
            writer.write(document);
            writer.flush();
            writer.close();
            
            return xmlFile;
        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }
    
    public static File getPhoneInfoFile(Context context, String filename, String reporter, String description) {
        Document document = DocumentHelper.createDocument();
        Element infoElement = document.addElement("info");
        
        getPhoneInfo(context, infoElement);
        
        addElement(infoElement, PhoneInfo.REPORTER, reporter);
        addElement(infoElement, PhoneInfo.DESCRIPTION, description);
        
        return createXMLFile(document, filename);
    }
    
    /***
     * Copies the contents of an InputStream to an OutputStream using a
     * copy buffer of a given size and notifies the provided
     * CopyStreamListener of the progress of the copy operation by calling
     * its bytesTransferred(long, int) method after each write to the
     * destination.  If you wish to notify more than one listener you should
     * use a CopyStreamAdapter as the listener and register the additional
     * listeners with the CopyStreamAdapter.
     * <p>
     * The contents of the InputStream are
     * read until the end of the stream is reached, but neither the
     * source nor the destination are closed.  You must do this yourself
     * outside of the method call.  The number of bytes read/written is
     * returned.
     * <p>
     * @param source  The source InputStream.
     * @param dest    The destination OutputStream.
     * @param bufferSize  The number of bytes to buffer during the copy.
     *            A zero or negative value means to use {@link #DEFAULT_COPY_BUFFER_SIZE}.
     * @param streamSize  The number of bytes in the stream being copied.
     *          Should be set to CopyStreamEvent.UNKNOWN_STREAM_SIZE if unknown.
     * @param listener  The CopyStreamListener to notify of progress.  If
     *      this parameter is null, notification is not attempted.
     * @param flush Whether to flush the output stream after every
     *        write.  This is necessary for interactive sessions that rely on
     *        buffered streams.  If you don't flush, the data will stay in
     *        the stream buffer.
     * @exception CopyStreamException  If an error occurs while reading from the
     *            source or writing to the destination.  The CopyStreamException
     *            will contain the number of bytes confirmed to have been
     *            transferred before an
     *            IOException occurred, and it will also contain the IOException
     *            that caused the error.  These values can be retrieved with
     *            the CopyStreamException getTotalBytesTransferred() and
     *            getIOException() methods.
     ***/
    public static final long copyStream(InputStream source, OutputStream dest,
                                        int bufferSize, long streamSize,
                                        FileTransferListener listener,
                                        boolean flush) throws IOException {
        int bytes;
        long total = 0;
        byte[] buffer = new byte[bufferSize >= 0 ? bufferSize : DEFAULT_COPY_BUFFER_SIZE];

        try
        {
            while ((bytes = source.read(buffer)) != -1)
            {
                // Technically, some read(byte[]) methods may return 0 and we cannot
                // accept that as an indication of EOF.

                if (bytes == 0)
                {
                    bytes = source.read();
                    if (bytes < 0) {
                        break;
                    }
                    dest.write(bytes);
                    if(flush) {
                        dest.flush();
                    }
                    ++total;
                    if (listener != null) {
                        listener.bytesTransferred(total, 1);
                    }
                    continue;
                }

                dest.write(buffer, 0, bytes);
                if(flush) {
                    dest.flush();
                }
                total += bytes;
                if (listener != null) {
                    listener.bytesTransferred(total, bytes);
                }
            }
        }
        catch (IOException e)
        {
            throw new IOException("IOException caught while copying.", e);
        }

        return total;
    }
    
    public static void upDone(String filename) {
        String httpUrl = LogUp.http_new_file_addr + filename;
        HttpGet httpGet = new HttpGet(httpUrl);
        
        HttpClient httpClient = new DefaultHttpClient();
        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            Log.d(TAG, "Response length = " + httpResponse.getEntity().getContentLength());
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                Log.d(TAG, "Response Status is OK");
                String strResult = EntityUtils.toString(httpResponse.getEntity());
                JSONObject jsonObject = null;
                jsonObject = new JSONObject(strResult);
                boolean success = jsonObject.getBoolean("success");
                if(success) {
                    Log.d(TAG, "Response JSONObject Success");
                } else {
                    Log.d(TAG, "Response JSONObject Fail");
                }
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
