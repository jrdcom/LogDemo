package com.tcl.logdemo;

public interface FileTransferListener {

    public void prepareTransfer(long totalBytes);

    /**
     * It is invoked after a block of bytes to inform the listener of the
     * transfer.
     * @param totalBytesTransferred  The total number of bytes transferred
     *         so far by the copy operation.
     * @param bytesTransferred  The number of bytes copied by the most recent
     *          write.
     * @param streamSize The number of bytes in the stream being copied.
     *        This may be equal to -1 if the size is unknown.
     */
    public void bytesTransferred(long totalBytesTransferred,
                                 int bytesTransferred,
                                 long streamSize);
    
    public void transferComplete();

}
