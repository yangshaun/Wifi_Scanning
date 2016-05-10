package com.example.wifi_scanning;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;

public class Dropbox_initial {
    private static final String TAG = "DBRoulette";
    public static final String ACCESS_KEY = "2l44xvz7c31nha8";
    public static final String ACCESS_SECRET = "yeslinn24oui9ne";
    private final static String FILE_DIR = "/DropboxSample/";

    public static final String DROPBOX_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCESS_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    private static final boolean USE_OAUTH1 = false;
    private static boolean isLoggedIn;

    private static MainActivity main;
    private static Wifi_service service;
    public static DropboxAPI<AndroidAuthSession> dropbox;

    public Dropbox_initial(MainActivity main) {
        this.main = main;
    }

    public void setDropbox_initial(Wifi_service service) {
        this.service = service;
    }

    public void onCreate() {
        loggedIn(false);
        AndroidAuthSession session;
        AppKeyPair pair = new AppKeyPair(ACCESS_KEY, ACCESS_SECRET);
        SharedPreferences prefs = main.getSharedPreferences(DROPBOX_NAME, 0);
        String key = prefs.getString(ACCESS_KEY, null);
        String secret = prefs.getString(ACCESS_SECRET, null);
        if (key != null && secret != null) {
            AccessTokenPair token = new AccessTokenPair(key, secret);
            session = new AndroidAuthSession(pair, AccessType.APP_FOLDER, token);
        } else {
            session = new AndroidAuthSession(pair, AccessType.APP_FOLDER);
        }
        dropbox = new DropboxAPI<AndroidAuthSession>(session);
    }

    protected void onResume() {

        AndroidAuthSession session = dropbox.getSession();
        if (session.authenticationSuccessful()) {
            try {
                main.progress.setVisibility(View.GONE);
              

                session.finishAuthentication();
                TokenPair tokens = session.getAccessTokenPair();
                SharedPreferences prefs = main.getSharedPreferences(DROPBOX_NAME, 0);
                Editor editor = prefs.edit();
                editor.putString(ACCESS_KEY, tokens.key);
                editor.putString(ACCESS_SECRET, tokens.secret);
                editor.commit();
                loggedIn(true);
                Toast.makeText(main, "DropBox 認證成功! 請再按一下上傳資料", Toast.LENGTH_LONG).show();

            } catch (IllegalStateException e) {
                Toast.makeText(main, "DropBox 認證錯誤!!", Toast.LENGTH_LONG).show();

            }
        }
    }

    public static void loggedIn(boolean isLogged) {
        isLoggedIn = isLogged;
    }

    public void onClick(int which) {
        switch (which) {
        case 0:
            if (isLoggedIn) {
                dropbox.getSession().unlink();
                loggedIn(false);
            } else {
                dropbox.getSession().startAuthentication(main);
            }

            break;
        case 1:
        	 Log.e("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", "OOOOOOOOOOOOOOOOOOOOOOOOOO"+" :"+service.tempFile.length());
            UploadFileToDropbox upload = new UploadFileToDropbox(main, dropbox, FILE_DIR, isLoggedIn, main.android_id, service.tempFile, main, service.ondestroyfile);
            upload.execute();
            break;

        default:
            break;
        }
    }

}
