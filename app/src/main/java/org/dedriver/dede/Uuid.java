package org.dedriver.dede;

import android.content.Context;
import android.content.SharedPreferences;

import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import timber.log.Timber;

public class Uuid {
    private static final String UUID_FILE_NAME = "uuid.txt";
    private final Context context;
    private String uuid = null;

    public Uuid(Context context) {
        this.context = context;
    }

    public String getUuid() {
        if (!hasUuid()) {
            setUuid();
        }
        return this.uuid;
    }

    private boolean hasUuid() {
        return this.uuid != null;
    }

    private String readAppId() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("org.dedriver.dede.prefs", Context.MODE_PRIVATE);
        Timber.d("appID: %s", sharedPreferences.getString("appID", ""));
        return sharedPreferences.getString("appID", "");
    }

    private String readUuid() {
        FileInputStream fileInputStream;
        String result = null;
        try {
            fileInputStream = this.context.openFileInput(UUID_FILE_NAME);
            if (fileInputStream == null) {
                Timber.e("readUuid: uuid file access failed");
                return null;
            }
            StringBuilder stringBuilder = new StringBuilder();
            int i;
            while ((i = fileInputStream.read()) != -1) {
                stringBuilder.append((char) i);
            }
            fileInputStream.close();
            String[] details = stringBuilder.toString().split("\n");
            if (details.length < 1) {
                Timber.e("readUuid: uuid file read failed");
                return null;
            }
            result = details[0];
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void writeUuid(String id) {
        FileOutputStream fileOutputStream;

        try {
            fileOutputStream = this.context.openFileOutput(UUID_FILE_NAME, Context.MODE_PRIVATE);
            if (fileOutputStream == null) {
                Timber.e("writeUuid: uuid file access failed");
                return;
            }
            fileOutputStream.write(id.getBytes());
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setUuid() {
        this.uuid = readAppId();
//        String result;
//
//        /*read uuid from file system*/
//        result = readAppId();
////        result = readUuid();
//        /*return uuid, if present in file system*/
//        if (result != null) {
//            this.uuid = result;
//        } else {
//            /*create, write to file system and return uuid, if not present in file system*/
//            this.uuid = UUID.randomUUID().toString();
//            writeUuid(this.uuid);
//        }
    }

    @Override
    public @NotNull String toString() {
        return this.uuid;
    }
}
