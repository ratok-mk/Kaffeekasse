package com.zeiss.koch.kaffeekasse;

import android.app.backup.FileBackupHelper;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

/**
 * ${FILENAME}
 * Created by koch on 22.10.2016.
 */

public class DBFileBackupHelper {

    private final SqlDatabaseHelper db;
    public static final String BACKUP_DB_PATH = "//databasebackup//";
    final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public DBFileBackupHelper(Context context) {
        db = new SqlDatabaseHelper(context);
    }

    public void Backup() {
        String databaseName = db.getDatabaseName();

        Date currentDate = new Date();
        String dateFormatted = dateFormat.format(currentDate);
        try {
            File data = Environment.getDataDirectory();

            String currentDBPath = "//data//com.zeiss.koch.kaffeekasse//databases//" + databaseName;
            String backupDBFilePath = BACKUP_DB_PATH + databaseName + "_" + dateFormatted;
            File currentDBFile = new File(data, currentDBPath);
            File backupDBDirectory = new File(data, BACKUP_DB_PATH);
            File backupDBFile = new File(data, backupDBFilePath);

            if (!(backupDBDirectory.exists() && backupDBDirectory.isDirectory())) {
                backupDBDirectory.mkdir();
            }

            if (currentDBFile.exists()) {
                FileChannel src = new FileInputStream(currentDBFile).getChannel();
                FileChannel dst = new FileOutputStream(backupDBFile).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }

        } catch (Exception e) {
        }
    }

    public Date LastBackupDate() {
        Date currentDate = new Date();
        String databaseName = db.getDatabaseName();
        try {
            File data = Environment.getDataDirectory();

            String currentDBPath = "//data//com.zeiss.koch.kaffeekasse//databases//" + databaseName;
            String backupDBFilePath = BACKUP_DB_PATH + databaseName + "_*";
            File backupDBFile = new File(data, backupDBFilePath);

            File[] matchingFiles = backupDBFile.listFiles();

            for (int i = 0; i < matchingFiles.length; ++i) {
                File file = matchingFiles[i];
                String filename = file.getAbsolutePath();
                String[] parts = filename.split("_");
                if (parts.length == 2) {
                    try {
                        Date lastBackup = dateFormat.parse(parts[1]);
                        long diff = currentDate.getTime() - lastBackup.getTime();
                        long diffHours = diff / (60 * 60 * 1000);
                        if (diffHours <= 24) {
                            return lastBackup;
                        }
                    } catch (ParseException e) {
                    }
                }
            }
        } catch (Exception e) {
        }

        return null;
    }

    public boolean BackupIsUpToDate()
    {
        return this.LastBackupDate() != null;
    }
}
