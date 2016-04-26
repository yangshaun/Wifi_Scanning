package com.example.wifi_scanning;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.joda.time.DateTime;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

public class UploadFileToDropbox extends AsyncTask<Void, Void, Boolean> {

    private DropboxAPI<AndroidAuthSession> dropbox;
    private String path;
    private Context context;
    private boolean isloggedin;
    private String id;
    private File tempfile;
    private File oldfile;
    private MainActivity main;
    private Wifi_service service;

    public UploadFileToDropbox(Context context, DropboxAPI<AndroidAuthSession> dropbox, String path, boolean isloggedin, String id, File tempfile, MainActivity main, File oldfile) {
        this.context = context.getApplicationContext();
        this.dropbox = dropbox;
        this.path = path;
        this.isloggedin = isloggedin;
        this.id = id;
        this.tempfile = tempfile;
        this.main = main;
        this.oldfile = oldfile;
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        try {
            main.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    main.update.setEnabled(false);
                }
            });

            // if (oldfile != null) {
            // if (oldfile.exists()) {//舊的檔案存在
            // FileInputStream fileInputStream = new FileInputStream(oldfile);
            // dropbox.putFile(path + id + "_" + new Date() + ".json",
            // fileInputStream, oldfile.length(), null, null);
            // Log.e("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "" +"old   :"+
            // oldfile.listFiles());
            // return true;
            // }
            // }
            if (tempfile != null) {
                if (tempfile.exists()) {// 新的檔案存在
                    FileInputStream fileInputStream = new FileInputStream(tempfile);
                    dropbox.putFile(path + id + "_" + System.currentTimeMillis() + ".json", fileInputStream, tempfile.length(), null, null);
                    return true;
                }
            }

            return false;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DropboxException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Log.e("OOOOOOOOOOOOOOOOO", "" + dropbox.toString());
            main.update.setEnabled(true);
            main.spinner.setEnabled(true);
            main.buttonScan.setEnabled(true);
            main.progress.setVisibility(View.GONE);
            main.buttonScan.setText("開始掃描Wifi");
            Toast.makeText(context, "檔案上傳成功!", Toast.LENGTH_LONG).show();
            tempfile.delete();
            Log.e("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", "" + "已經刪除!!!!!!!");
            Wifi_service.wifitotal = 0;
        } else {
            Toast.makeText(context, "資料上傳錯誤!!!", Toast.LENGTH_SHORT).show();
            main.buttonScan.setText("開始掃描Wifi");
            main.spinner.setEnabled(true);
            main.buttonScan.setEnabled(true);
            main.progress.setVisibility(View.GONE);
            main.update.setEnabled(true);
        }
    }
}