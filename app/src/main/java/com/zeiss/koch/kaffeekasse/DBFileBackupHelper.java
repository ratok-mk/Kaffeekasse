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

    private final SqlDatabaseHelper db;
    public static final String BACKUP_DB_PATH = "//databasebackup//";
    final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private Date lastBackupDate;

    public DBFileBackupHelper(Context context) {
        db = new SqlDatabaseHelper(context);
    }

    public void Backup() {
        String databaseName = db.getDatabaseName();

        Date currentDate = new Date();
        String dateFormatted = dateFormat.format(currentDate);
        try {
            File data = Environment.getDataDirectory();
            File publicData =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            String currentDBPath = "//data//com.zeiss.koch.kaffeekasse//databases//" + databaseName;
            String backupDBFileName = databaseName + "_" + dateFormatted;
            File currentDBFile = new File(data, currentDBPath);
            File backupDBDirectory = new File(publicData, BACKUP_DB_PATH);
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

    public void CheckBackupDate() {
        Date currentDate = new Date();
        String databaseName = db.getDatabaseName();
        try {
            File publicData =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

            String backupDBFilePath = BACKUP_DB_PATH;
            File backupDBFile = new File(publicData, backupDBFilePath);

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
}
