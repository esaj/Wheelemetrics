package com.github.esaj.wheelemetrics.data.record;

import com.github.esaj.wheelemetrics.data.LoggableData;

import java.io.File;
import java.io.IOException;

/**
 * Simple service for logging data into a file
 */
public interface TelemetryFileService
{
    /**
     * Opens a new log file with given name. If previous file was still in use, it will be finalized first.
     * @param fileName  Name of the new file
     */
    void openNewFile(String fileName);

    /**
     * Returns current file (if any)
     * @return  Current file
     */
    File getCurrentFile();

    /**
     * Finalizes the file by writing out remaining entries from queue, flushing the stream and closing.
     * @param finalFile Final file to write the data into, if different than current file, the current file will
     *                  be renamed to match given file (for file dialog-purposes)
     */
    void finalizeFile(File finalFile);

    /**
     * Adds a log entry to file
     * @param logEntry      Log entry to write
     * @param binary        If true, writes binary data, otherwise writes the given String as-is
     */
    void addLogEntry(LoggableData logEntry, boolean binary);

    /**
     * Adds a log entry to file
     * @param data  Log entry to add
     */
    void addLogEntry(LoggableData data);

    /**
     * Checks if external storage is available for read and write
     * @return True, if external storage is available
     */
    boolean isExternalStorageWritable();

    /**
     *  Checks if external storage is available to at least read
     *  @return True, if external storage is at least readable
     */
    boolean isExternalStorageReadable();

    /**
     * Gets a File-instance for given filename in default storage
     * @param fileName      Name of the file
     * @return  File-instance
     * @throws IOException
     */
    File getStorageFile(String fileName) throws IOException;
}
