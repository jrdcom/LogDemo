package com.tcl.logdemo;

public interface FileTransferListener {

    /**
     * It is invoked after a block of bytes to inform the listener of the
     * transfer.
     * @param totalBytesTransferred  The total number of bytes transferred
     *         so far by the copy operation.
     * @param bytesTransferred  The number of bytes copied by the most recent
     *          write.
     */
    public void bytesTransferred(long totalBytesTransferred,
                                 int bytesTransferred);

}
