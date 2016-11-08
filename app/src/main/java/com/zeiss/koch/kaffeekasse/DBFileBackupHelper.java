package com.zeiss.koch.kaffeekasse;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ${FILENAME}
 * Created by koch on 22.10.2016.
 */

public class DBFileBackupHelper {

    final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private final SqlDatabaseHelper db;
    private static final String BACKUP_DB_PATH = "//databasebackup//";
    private static final String DB_PATH = "//data//com.zeiss.koch.kaffeekasse//databases//";
    private Date lastBackupDate;

    public DBFileBackupHelper(Context context) {
        db = new SqlDatabaseHelper(context);
    }

    public static File BackupDirectory()
    {
        File publicData =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File backupDBDirectory = new File(publicData, BACKUP_DB_PATH);
        return backupDBDirectory;
    }

    public void Backup() {
        Date currentDate = new Date();
        String dateFormatted = dateFormat.format(currentDate);
        try {
            File data = Environment.getDataDirectory();
            String databaseName = db.getDatabaseName();
            String currentDBPath = DB_PATH + databaseName;
            String backupDBFileName = databaseName + "_" + dateFormatted + ".db";
            File currentDBFile = new File(data, currentDBPath);
            File backupDBDirectory = BackupDirectory();
            File backupDBFile = new File(backupDBDirectory, backupDBFileName);

            if (!(backupDBDirectory.exists() && backupDBDirectory.isDirectory())) {
                backupDBDirectory.mkdirs();
            }

            if (currentDBFile.exists()) {
                FileChannel src = new FileInputStream(currentDBFile).getChannel();
                FileChannel dst = new FileOutputStream(backupDBFile).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Restore(File restoreFile) {
        try {
            File backupDBFile = restoreFile;

            File data = Environment.getDataDirectory();
            String databaseName = db.getDatabaseName();
            String currentDBPath = DB_PATH + databaseName;
            File currentDBFile = new File(data, currentDBPath);

            if (backupDBFile.exists()) {
                FileChannel src = new FileInputStream(backupDBFile).getChannel();
                FileChannel dst = new FileOutputStream(currentDBFile).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void CheckBackupDate() {
        Date currentDate = new Date();
        try {
            File backupDBFile = BackupDirectory();
            File[] matchingFiles = backupDBFile.listFiles();

            for (int i = 0; i < matchingFiles.length; ++i) {
                File file = matchingFiles[i];
                String filename = file.getAbsolutePath();
                filename = removeExtension(filename);
                String[] parts = filename.split("_");
                if (parts.length == 2) {
                    try {
                        Date lastBackup = dateFormat.parse(parts[1]);
                        long diff = currentDate.getTime() - lastBackup.getTime();
                        long diffHours = diff / (60 * 60 * 1000);
                        if (diffHours <= 24) {
                            this.lastBackupDate = lastBackup;
                        }
                    } catch (ParseException e) {
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public boolean BackupIsUpToDate()
    {
        CheckBackupDate();
        return this.lastBackupDate != null;
    }

    public String LastBackupDate()
    {
        if (this.lastBackupDate != null) {
            return dateFormat.format(this.lastBackupDate);
        }

        return "";
    }

    private static String removeExtension(String s) {

        String separator = System.getProperty("file.separator");
        String filename = s;

//        // Remove the path upto the filename.
//        int lastSeparatorIndex = s.lastIndexOf(separator);
//        if (lastSeparatorIndex == -1) {
//            filename = s;
//        } else {
//            filename = s.substring(lastSeparatorIndex + 1);
//        }

        // Remove the extension.
        int extensionIndex = filename.lastIndexOf(".");
        if (extensionIndex == -1)
            return filename;

        return filename.substring(0, extensionIndex);
    }
}
