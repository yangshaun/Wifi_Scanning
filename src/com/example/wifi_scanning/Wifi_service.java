package com.example.wifi_scanning;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
public class Wifi_service extends Service implements LocationListener {
	public MyTimerTask task;
	static MainActivity main;
	public Timer timer;
	static int period;
	String[] Wifiary;
	public static int wifitotal = 0;
	static LocationManager status;
	public static WifiManager wifi;
	public static File tempFile;
	public boolean firsttimetoscan;
	List<ScanResult> Wifi_results;
	Location location;
	Double longitude;
	Double latitude;
	public JSONArray data = new JSONArray();
	public static int datalength;
	JSONObject wifidata;
	static public LocationManager lms;
	public FileWriter fr;
	String bestProvider = LocationManager.GPS_PROVIDER;
	// ////////////////////////////////////////
	/*
	 * 
	 * 試著改成bind service看看 不壞掉就用service 桌面上有備份
	 * 
	 * 
	 * *
	 */
	// //////////////////////////////////////////////////////////

	boolean isGPSopened;
	boolean AbleToLocate;
	private ArrayAdapter adapter;
	private boolean getService = true;
	private boolean isrequestlocationupdate;
	private boolean isremoveupdate;
	private BroadcastReceiver receiver;
	public static File numberFile;
	public FileWriter fr_num;
	private static SharedPreferences wifi_num;
	public static File ondestroyfile;
	public static int wifinum;
	// ////////////////////////////////////////////////////////
	public Wifi_service() {
	}
	public Wifi_service(MainActivity main) {
		Wifi_service.main = main;
	}
	public class MyTimerTask extends TimerTask {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			wifi.setWifiEnabled(true);
			getGPS();
			Log.e("PPPPPPPPPPPPPPPPPPPPPPP", "schedule was calles!!!!!!!!!!!!");
			getWiFi();
		}

	}
	public void getWiFi() {
		firsttimetoscan = true;
		wifi.startScan();
	}
	void FetchData() {

		Wifiary = new String[Wifi_results.size()];
		for (int i = 0; i < Wifi_results.size(); i++) {
			Wifiary[i] = "Name:" + Wifi_results.get(i).SSID + "\n"
					+ "Frequency:" + Wifi_results.get(i).frequency + "  dbm"
					+ "\n" + "BSSID: " + Wifi_results.get(i).BSSID + "\n"
					+ "Timestamp: "
					+ ConvertTImeStamp(Wifi_results.get(i).timestamp / 1000L)
					+ "\n" + "Level: " + Wifi_results.get(i).level + "\n"
					+ "Gps : (" + String.format("%.3f", longitude) + ","
					+ String.format("%.3f", latitude) + ")";

		}
		for (int i = 0; i < Wifi_results.size(); i++) {
			wifidata = new JSONObject();
			try {
				wifidata.put("Name", Wifi_results.get(i).SSID);
				wifidata.put("Frequency", Wifi_results.get(i).frequency);
				wifidata.put("BSSID", Wifi_results.get(i).BSSID);
				wifidata.put("Level", Wifi_results.get(i).level);
				wifidata.put("Gps", "(" + String.format("%.3f", longitude)
						+ "," + String.format("%.3f", latitude) + ")");
				data.put(wifidata);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			fr.append(data.toString(2));
			fr.flush();
			final SharedPreferences.Editor editor = wifi_num.edit();
			editor.putInt("wifi_num", wifinum += data.length());
			Set<String> mySet = new HashSet<String>();
			Collections.addAll(mySet, Wifiary);
			editor.putStringSet("wifi_data", mySet);
			editor.commit();

			Log.e("DATADATDA",
					"Free spac :" + tempFile.getFreeSpace() + "    \n"
							+ tempFile.length() + "    "
							+ wifi_num.getInt("wifi_num", 0) + "data:\n"
							+ data.toString());
			wifitotal += data.length();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		datalength = data.length();
		// Log.e("DATADATDA",tempFile.length()+"    "+data.length()+"data:\n"+data.toString());
		if (isRunning(getBaseContext()) && Wifiary != null) {
			setAdapter();
		}
		data = new JSONArray();
		firsttimetoscan = false;
	}
	private String ConvertTImeStamp(Long time) { // �n��UNIX time�N�O1970�}�l
		Date date = new Date(time); // *1000 is to convert seconds to
									// milliseconds
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
		String formattedDate = sdf.format(date);
		return formattedDate;
	}
	void getGPS() {

		if (status.isProviderEnabled(LocationManager.GPS_PROVIDER)
				|| status.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			isGPSopened = true;
			locationServiceInitial();
			if (location != null) {
				AbleToLocate = true;
				longitude = location.getLongitude(); // 取得經度
				latitude = location.getLatitude(); // 取得緯度
			} else {
				AbleToLocate = false;
			}
		} else {
			getService = true;
			isGPSopened = false;
			if (main != null) {
				main.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getBaseContext(), "嘿你至少要開個GPS或是網路!!!",
								Toast.LENGTH_LONG).show();
					}
				});
			}
			Intent tempintent = new Intent(
					Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			tempintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			final int notifyID = 1; // 通知的識別號碼
			final NotificationManager notificationManager =
					(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE); // 取得系統的通知服務
			final Notification notification = new Notification.Builder(
					getApplicationContext())
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("Wifi_scanning").setContentText("嘿幫我開一下GPS吧!!")
					.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
					.build(); // 建立通知
			notificationManager.notify(notifyID, notification);
			startActivity(tempintent);
		
		}

	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

	}
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		AbleToLocate = false;
	}
	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}
	private void locationServiceInitial() {
		// 取得系統定位服務
		Criteria criteria = new Criteria(); // 資訊提供者選取標準
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		criteria.setCostAllowed(true);
		bestProvider = lms.getBestProvider(criteria, true); // 選擇精準度最高的提供者
		location = lms.getLastKnownLocation(bestProvider);

	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.e("time!!!!!!!!!!!!!!!!!!", "time :" + period);
		timer.scheduleAtFixedRate(task, 1500, period); // //////////
														// ////////////////////
		return START_STICKY;
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		SharedPreferences settings = getSharedPreferences("period", 0);
		wifi_num = getSharedPreferences("wifi_num", 2);
		Wifi_service.period = settings.getInt("period", period);
		Wifi_service.wifinum = wifi_num.getInt("wifi_num", wifinum);
		task = new MyTimerTask();
		lms = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		status = (LocationManager) (this
				.getSystemService(Context.LOCATION_SERVICE));
		wifi = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		wifi.setWifiEnabled(true);
		timer = new Timer();
		registerReceiver(receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				// TODO Auto-generated method stub
				Log.e("QWQWQWQWQWQWQW",
						"wifi manager on receive was called!!!!!!!!!!!!!");
				Wifi_results = wifi.getScanResults();
				if (firsttimetoscan == true)
					FetchData();
			}
		}, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

		final File tempDir = getCacheDir();

		try {

			tempFile = new File(tempDir.toString() + "/file.txt");
			if (tempFile.exists()) {
				Log.e("QWQWQWQWQWQWQW", "File existed!!!!");
				fr = new FileWriter(tempFile, true);

			} else {
				Log.e("QWQWQWQWQWQWQW", "File Created!!!" + "    " + tempDir);
				tempFile = new File(tempDir, "file.txt");
				fr = new FileWriter(tempFile);

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void onDestroy() {
		// final File tempDir = getCacheDir();
		// try {
		// ondestroyfile=File.createTempFile("oldfile", ".txt", tempDir);
		// copy(tempFile,ondestroyfile);
		//
		//
		// } catch (IOException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }

		Log.e("QWQWQWQWQWQWQW", "Service_OnDestroy");
		timer.cancel();
		try {
			fr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	// //////// ////////// ////////// ////////// ////////// //////////
	// ////////// ////////// ////////// ////////// //////////

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		Log.e("QWQWQWQWQWQWQW", "On_Service_onUnbind");
		return super.onUnbind(intent);
	}
	public boolean isGPSopened() {
		return isGPSopened;
	}
	public boolean AbleToLocate() {
		return AbleToLocate;
	}

	public String[] getWifiary() {
		return Wifiary;
	}

	public void setAdapter() {
		Wifiary = getWifiary();
		this.adapter = new ArrayAdapter(main,
				android.R.layout.simple_list_item_1, Wifiary);
		main.lv.setAdapter(adapter);
		main.progress.setVisibility(View.GONE);
		main.update.setEnabled(true);
		main.update.setText("上傳資料至DropBox");
	}

	public boolean getService() {
		return getService;

	};
	// public void copy(File src, File dst) throws IOException {
	// InputStream in = new FileInputStream(src);
	// OutputStream out = new FileOutputStream(dst);
	// byte[] buf = new byte[1024];
	// int len;
	// while ((len = in.read(buf)) > 0) {
	// out.write(buf, 0, len);
	// }
	// in.close();
	// out.close();
	// }

	public boolean isRunning(Context ctx) {
		ActivityManager activityManager = (ActivityManager) ctx
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasks = activityManager
				.getRunningTasks(Integer.MAX_VALUE);
		for (RunningTaskInfo task : tasks) {
			if (ctx.getPackageName().equalsIgnoreCase(
					task.baseActivity.getPackageName()))
				return true;
		}

		return false;
	}

}