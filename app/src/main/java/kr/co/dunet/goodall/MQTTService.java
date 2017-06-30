package kr.co.dunet.goodall;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

public class MQTTService extends Service {
	private static final String TAG = "MQTTService";
	private Thread thread;
	private ConnectivityManager mConnMan;
	private volatile IMqttAsyncClient mqttClient;
	private String deviceId = null;
	public String HOST = "";
	public static final String ACTION_RESTART_SERVICE = "kr.co.dunet.goodall.restart";
	public String networkName = null;
	private static final long KEEP_ALIVE_INTERVAL = 1000 * 60 *28; // 28분
	private static final String ACTION_KEEPALIVE = "kr.co.dunet.goodall.KEEP_ALIVE";
	
	// public ChatApplication app = (ChatApplication) getApplicationContext();
	
	class MQTTBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "onReceive 켜졋어요");
			changeToWakeMode();
			
			boolean hasConnectivity = false;
			boolean hasChanged = false;
			
			NetworkInfo networkInfo = mConnMan.getActiveNetworkInfo();
//			Log.d(TAG , "네트워크 이름 : " +  networkInfo.getTypeName());
			if (networkInfo != null && networkInfo.isConnected()) {
				Log.i(TAG, networkInfo.getTypeName());
				hasConnectivity = true;
				if (networkName != networkInfo.getTypeName()) {
					hasChanged = true;
					networkName = networkInfo.getTypeName();
				}
			} else {
				hasConnectivity = false;
				networkName = null;
			}
			
			Log.v(TAG,
					"hasConn: " + hasConnectivity + " hasChange: " + hasChanged
							+ " - "
							+ (mqttClient == null || !mqttClient.isConnected()));
			
			if (hasConnectivity && hasChanged
					&& (mqttClient == null || !mqttClient.isConnected())) {
				doConnect();
			} else if (!hasConnectivity && mqttClient != null
					&& mqttClient.isConnected()) {
				Log.d(TAG, "통신끊김");
				/*
				 * IMqttToken token; try { token = mqttClient.disconnect();
				 * token.waitForCompletion(1000); } catch (MqttException e) {
				 * e.printStackTrace(); }
				 */
			}
			closeToWakeMode();
		}
	}
	
	private final BroadcastReceiver screenReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
				System.out.println("화면꺼짐");
				startKeepAlives();
			} else if (intent.getAction().equals(
					"android.intent.action.SCREEN_ON")) {
				System.out.println("화면켜짐");
				stopKeepAlives();
			} else if (intent.getAction().equals(ACTION_KEEPALIVE)) {
				System.out.println("알람 받음");
			}
		}
	};
	
	private void startKeepAlives() {
		if (deviceId == null) {
			System.out.println("로그인 상태가 아님.");
			return;
		}
		
		Intent i = new Intent();
		i.setClass(this, MQTTService.class);
		i.setAction(ACTION_KEEPALIVE);
		PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
		AlarmManager alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis() + KEEP_ALIVE_INTERVAL,
				KEEP_ALIVE_INTERVAL, pi);
	}
	
	private void stopKeepAlives() {
		Intent i = new Intent();
		i.setClass(this, MQTTService.class);
		i.setAction(ACTION_KEEPALIVE);
		PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
		AlarmManager alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmMgr.cancel(pi);
	}
	
	public class MQTTBinder extends Binder {
		public MQTTService getService() {
			return MQTTService.this;
		}
	}
	
	@Override
	public void onCreate() {
		PushServiceRestarter.unregisterRestartAlram(MQTTService.this);

		HOST = NetworkConfig.Instance().getMqttHost();
		
		IntentFilter intentf = new IntentFilter();
		intentf.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(new MQTTBroadcastReceiver(), new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));
		mConnMan = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		intentFilter.addAction(Intent.ACTION_SCREEN_ON);
		
		registerReceiver(screenReceiver, intentFilter);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d(TAG, "onConfigurationChanged()");
		// android.os.Debug.waitForDebugger();
		super.onConfigurationChanged(newConfig);
		
	}
	
	private void setClientID() {
		/*
		 * WifiManager wifiManager = (WifiManager)
		 * getSystemService(Context.WIFI_SERVICE); WifiInfo wInfo =
		 * wifiManager.getConnectionInfo(); deviceId = wInfo.getMacAddress();
		 * if(deviceId == null){ deviceId = MqttAsyncClient.generateClientId();
		 * }
		 */
		Log.d(TAG,"setClientId");
		ChatApplication app = (ChatApplication) getApplicationContext();
		String id = app.getId() == null ? "" : app.getId();
		if (!id.equals("")) {
			deviceId = id;
			return;
		}
		
		DBManager mDbManager = DBManager.getInstance(this);
		String[] columns = new String[] { "id" };
		Cursor c = mDbManager.selectMember(columns, null, null, null, null,
				null);
		
		if (c != null && c.getCount() > 0) {
			while (c.moveToNext()) {
				deviceId = c.getString(0);
				break;
			}
		} else {
			deviceId = null;
		}
		c.close();
		
		app.setId(deviceId);
	}
	
	private void doConnect() {
		Log.d(TAG, "doConnect()");
		if (deviceId == null) {
			return;
		}
		
		IMqttToken token;
		MqttConnectOptions options = new MqttConnectOptions();
		ChatApplication app = (ChatApplication) getApplicationContext();
		if (app.getFirst() == true) {
			options.setCleanSession(true);
			app.setFirst(false);
		} else {
			options.setCleanSession(false);
		}
		options.setKeepAliveInterval(30);
		
		if (mqttClient != null && mqttClient.isConnected()) {
			Log.e(TAG, "이미 연결되어있음");
			return;
		}
		
		try {
			app.setMqtt(new MqttAsyncClient(HOST, deviceId,
					new MemoryPersistence()));
			mqttClient = app.getMqtt();
			token = mqttClient.connect(options);
			token.waitForCompletion(3500);
			mqttClient.setCallback(new MqttEventCallback());
			
			// DB에서 방 정보 받아오기
			DBManager mDbManager = DBManager.getInstance(this);
			String[] columns = new String[] { "room_code" };
			Cursor c = mDbManager.selectRoom(columns, null, null, null, null,
					null);
			
			if (c != null && c.getCount() > 0) {
				while (c.moveToNext()) {
					String room_code = c.getString(0);
					
					token = mqttClient.subscribe(room_code, 1);
					token.waitForCompletion(5000);
				}
			}
			
			c.close();
			
			// token = mqttClient.subscribe("notice", 1);
			// token.waitForCompletion(5000);
			
		} catch (MqttSecurityException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			switch (e.getReasonCode()) {
				case MqttException.REASON_CODE_BROKER_UNAVAILABLE:
				case MqttException.REASON_CODE_CLIENT_TIMEOUT:
				case MqttException.REASON_CODE_CONNECTION_LOST:
				case MqttException.REASON_CODE_SERVER_CONNECT_ERROR:
					Log.v(TAG, "c" + e.getMessage());
					e.printStackTrace();
					break;
				case MqttException.REASON_CODE_FAILED_AUTHENTICATION:
					Intent i = new Intent("RAISEALLARM");
					i.putExtra("ALLARM", e);
					Log.e(TAG, "b" + e.getMessage());
					break;
				default:
					Log.e(TAG, "a" + e.getMessage());
			}
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "onStartCommand()");
		changeToWakeMode();
		
		if (mqttClient == null || !mqttClient.isConnected()) {
			setClientID();
			doConnect();
		}
		closeToWakeMode();
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		changeToWakeMode();
		if (mqttClient != null && mqttClient.isConnected()) {
			IMqttToken token;
			Log.d(TAG, "통신끊김");
			try {
				token = mqttClient.disconnect();
				token.waitForCompletion(1000);
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}
		unregisterReceiver(screenReceiver);
		Log.d(TAG, "레지스터 등록해제" );
		closeToWakeMode();
		super.onDestroy();
	}
	
	private class MqttEventCallback implements MqttCallback {
		
		@Override
		public void connectionLost(Throwable arg0) {
			changeToWakeMode();
			doConnect();
			closeToWakeMode();
		}
		
		@Override
		public void deliveryComplete(IMqttDeliveryToken arg0) {
			
		}
		
		@Override
		@SuppressLint("NewApi")
		public void messageArrived(String topic, final MqttMessage msg)
				throws Exception {
			changeToWakeMode();
			
			Log.i(TAG, "Message arrived from topic" + topic);
			Handler h = new Handler(getMainLooper());
			h.post(new Runnable() {
				@Override
				public void run() {
					// Intent launchA = new Intent(MQTTService.this,
					// MUCMessageActivity.class);
					// launchA.putExtra("message", msg.getPayload());
					// TODO write something that has some sense
					// if(Build.VERSION.SDK_INT >= 11){
					// launchA.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_NO_ANIMATION);
					// }
					/*
					 * else { launchA.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); }
					 * startActivity(launchA);
					 */
					
					JSONObject json_data = null;
					try {
						json_data = new JSONObject(new String(msg.getPayload()));
						String type = json_data.getString("type");
						
						// 일반 메세지
						if (type.equals("msg")) {
							String rid = json_data.getString("rid");
							String id = json_data.getString("id");
							String roomCode = json_data.getString("room_code");
							String roomName = json_data.getString("room_name");
							String nickName = json_data.getString("nickname");
							String message = new String(Base64
									.decodeBase64(json_data.getString("msg")
											.getBytes()));
							
							Log.d("받은메세지", message);
							
							String regDate = json_data.getString("datetime");
							String key = json_data.getString("key");
							
							// DB에 대화내용 기록
							DBManager mDbManager = DBManager
									.getInstance(getApplicationContext());
							
							ContentValues addRowValue = new ContentValues();
							addRowValue.put("rid", rid);
							addRowValue.put("id", id);
							addRowValue.put("room_code", roomCode);
							addRowValue.put("nickname", nickName);
							addRowValue.put("message", message);
							addRowValue.put("reg_date", regDate);
							addRowValue.put("msg_type", type);
							
							mDbManager.insertChat(addRowValue);
							
							ChatApplication app = (ChatApplication) getApplicationContext();
							if (roomCode.equals(app.getChatRoom())) {
								ContentValues updateRowValue = new ContentValues();
								updateRowValue.put("mod_date", regDate);
								
								mDbManager.updateRoom(updateRowValue,
										"room_code='" + roomCode + "'", null);
								
								if (mCallback != null) {
									mCallback.sendData(rid, id, roomCode,
											nickName, message, regDate, type,
											key);
								}
							} else {
								if (!deviceId.equals(id)) {
									Intent intent = new Intent(
											getApplicationContext(),
											MUCMessageActivity.class);
									intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									intent.putExtra("ROOMCODE", roomCode);
									Notify.notifcation(getApplicationContext(),
											message, intent, roomName);
									
									// 정보 업데이트
									String where = "room_code='" + roomCode
											+ "'";
									String[] columns = new String[] { "no_read" };
									Cursor c = mDbManager.selectRoom(columns,
											where, null, null, null, null);
									
									int no_read = 0;
									if (c != null && c.getCount() > 0) {
										while (c.moveToNext()) {
											no_read = c.getInt(0) + 1;
											break;
										}
									}
									
									ContentValues updateRowValue = new ContentValues();
									updateRowValue.put("no_read", no_read);
									updateRowValue.put("mod_date", regDate);
									
									mDbManager.updateRoom(updateRowValue,
											"room_code='" + roomCode + "'",
											null);
									
									Intent bi = new Intent();
									bi.setAction("kr.co.dunet.goodall.new_message");
									bi.putExtra("roomCode", roomCode);
									bi.putExtra("no_read", no_read);
									bi.putExtra("mod_date", regDate);
									sendBroadcast(new Intent(bi));
								}
							}
							// Toast.makeText(getApplicationContext(),
							// "MQTT Message:\n" + message,
							// Toast.LENGTH_SHORT).show();
						}
						// 사진
						else if (type.equals("photo")) {
							String rid = json_data.getString("rid");
							String id = json_data.getString("id");
							String roomCode = json_data.getString("room_code");
							String roomName = json_data.getString("room_name");
							String nickName = json_data.getString("nickname");
							String message = json_data.getString("msg");
							String regDate = json_data.getString("datetime");
							String key = json_data.getString("key");
							
							// DB에 대화내용 기록
							DBManager mDbManager = DBManager
									.getInstance(getApplicationContext());
							
							ContentValues addRowValue = new ContentValues();
							addRowValue.put("rid", rid);
							addRowValue.put("id", id);
							addRowValue.put("room_code", roomCode);
							addRowValue.put("nickname", nickName);
							addRowValue.put("message", message);
							addRowValue.put("reg_date", regDate);
							addRowValue.put("msg_type", type);
							
							mDbManager.insertChat(addRowValue);
							
							ChatApplication app = (ChatApplication) getApplicationContext();
							if (roomCode.equals(app.getChatRoom())) {
								ContentValues updateRowValue = new ContentValues();
								updateRowValue.put("mod_date", regDate);
								
								mDbManager.updateRoom(updateRowValue,
										"room_code='" + roomCode + "'", null);
								
								if (mCallback != null) {
									mCallback.sendFileData(rid, id, roomCode,
											nickName, message, regDate, type,
											key);
								}
							} else {
								if (!deviceId.equals(id)) {
									Intent intent = new Intent(
											getApplicationContext(),
											MUCMessageActivity.class);
									intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									intent.putExtra("ROOMCODE", roomCode);
									Notify.notifcation(getApplicationContext(),
											"사진", intent, roomName);
									
									// 정보 업데이트
									String where = "room_code='" + roomCode
											+ "'";
									String[] columns = new String[] { "no_read" };
									Cursor c = mDbManager.selectRoom(columns,
											where, null, null, null, null);
									
									int no_read = 0;
									if (c != null && c.getCount() > 0) {
										while (c.moveToNext()) {
											no_read = c.getInt(0) + 1;
											break;
										}
									}
									
									ContentValues updateRowValue = new ContentValues();
									updateRowValue.put("no_read", no_read);
									updateRowValue.put("mod_date", regDate);
									
									mDbManager.updateRoom(updateRowValue,
											"room_code='" + roomCode + "'",
											null);
									
									Intent bi = new Intent();
									bi.setAction("kr.co.dunet.goodall.new_message");
									bi.putExtra("roomCode", roomCode);
									bi.putExtra("no_read", no_read);
									bi.putExtra("mod_date", regDate);
									sendBroadcast(new Intent(bi));
								}
							}
						}
						// 투표
						else if (type.equals("vote")) {
							String rid = json_data.getString("rid");
							String id = json_data.getString("id");
							String roomCode = json_data.getString("room_code");
							String roomName = json_data.getString("room_name");
							String nickName = json_data.getString("nickname");
							String message = json_data.getString("msg");
							String regDate = json_data.getString("datetime");
							// String key = json_data.getString("key");
							
							// DB에 대화내용 기록
							DBManager mDbManager = DBManager
									.getInstance(getApplicationContext());
							
							ContentValues addRowValue = new ContentValues();
							addRowValue.put("rid", rid);
							addRowValue.put("id", id);
							addRowValue.put("room_code", roomCode);
							addRowValue.put("nickname", nickName);
							addRowValue.put("message", message);
							addRowValue.put("reg_date", regDate);
							addRowValue.put("msg_type", type);
							
							mDbManager.insertChat(addRowValue);
							
							ChatApplication app = (ChatApplication) getApplicationContext();
							if (roomCode.equals(app.getChatRoom())) {
								ContentValues updateRowValue = new ContentValues();
								updateRowValue.put("mod_date", regDate);
								
								mDbManager.updateRoom(updateRowValue,
										"room_code='" + roomCode + "'", null);
								
								if (mCallback != null) {
									mCallback.sendVoteData(rid, id, roomCode,
											nickName, message, regDate, type);
								}
							} else {
								if (!deviceId.equals(id)) {
									Intent intent = new Intent(
											getApplicationContext(),
											MUCMessageActivity.class);
									intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									intent.putExtra("ROOMCODE", roomCode);
									Notify.notifcation(getApplicationContext(),
											"투표가 등록되었습니다.", intent, roomName);
									
									// 정보 업데이트
									String where = "room_code='" + roomCode
											+ "'";
									String[] columns = new String[] { "no_read" };
									Cursor c = mDbManager.selectRoom(columns,
											where, null, null, null, null);
									
									int no_read = 0;
									if (c != null && c.getCount() > 0) {
										while (c.moveToNext()) {
											no_read = c.getInt(0) + 1;
											break;
										}
									}
									
									ContentValues updateRowValue = new ContentValues();
									updateRowValue.put("no_read", no_read);
									updateRowValue.put("mod_date", regDate);
									
									mDbManager.updateRoom(updateRowValue,
											"room_code='" + roomCode + "'",
											null);
									
									Intent bi = new Intent();
									bi.setAction("kr.co.dunet.goodall.new_message");
									bi.putExtra("roomCode", roomCode);
									bi.putExtra("no_read", no_read);
									bi.putExtra("mod_date", regDate);
									sendBroadcast(new Intent(bi));
								}
							}
						}
						// 추천메세지
						else if (type.equals("recomm")) {
							String id = json_data.getString("id");
							String rid = json_data.getString("rid");
							String like = json_data.getString("like");
							String roomCode = json_data.getString("room_code");
							
							System.out.println("update " + json_data);
							
							DBManager mDbManager = DBManager
									.getInstance(getApplicationContext());
							
							ContentValues updateRowValue = new ContentValues();
							updateRowValue.put("like", Integer.parseInt(like));
							
							int res = mDbManager.updateChat(updateRowValue,
									"rid=" + rid, null);
							
							// Toast.makeText(getApplicationContext(),
							// "MQTT Message:\n" + like,
							// Toast.LENGTH_SHORT).show();
							
							ChatApplication app = (ChatApplication) getApplicationContext();
							if (roomCode.equals(app.getChatRoom())) {
								if (mCallback != null) {
									mCallback.updateLike(rid, like);
								}
							} else {
								if (!deviceId.equals(id)) {
									/*
									 * Intent intent = new Intent(
									 * getApplicationContext(),
									 * MUCMessageActivity.class);
									 * intent.addFlags
									 * (Intent.FLAG_ACTIVITY_CLEAR_TOP);
									 * intent.putExtra("ROOMCODE", roomCode);
									 * Notify
									 * .notifcation(getApplicationContext(),
									 * message, intent, roomName);
									 */
								}
							}
						}
						// 메세지삭제
						else if (type.equals("msgdel")) {
							String id = json_data.getString("id");
							String rid = json_data.getString("rid");
							String roomCode = json_data.getString("room_code");
							
							System.out.println("delete " + json_data);
							
							DBManager mDbManager = DBManager
									.getInstance(getApplicationContext());
							
							int res = mDbManager.deleteChat("rid=" + rid, null);
							
							// Toast.makeText(getApplicationContext(),
							// "MQTT Message:\n" + like,
							// Toast.LENGTH_SHORT).show();
							
							ChatApplication app = (ChatApplication) getApplicationContext();
							if (roomCode.equals(app.getChatRoom())) {
								if (mCallback != null) {
									mCallback.deleteMessage(rid, id);
								}
							} else {
								if (!deviceId.equals(id)) {
									/*
									 * Intent intent = new Intent(
									 * getApplicationContext(),
									 * MUCMessageActivity.class);
									 * intent.addFlags
									 * (Intent.FLAG_ACTIVITY_CLEAR_TOP);
									 * intent.putExtra("ROOMCODE", roomCode);
									 * Notify
									 * .notifcation(getApplicationContext(),
									 * message, intent, roomName);
									 */
								}
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
					// Toast.makeText(getApplicationContext(), "MQTT Message:\n"
					// + new String(msg.getPayload()),
					// Toast.LENGTH_SHORT).show();
					closeToWakeMode();
				}
			});
			
		}
	}
	
	public String getThread() {
		return Long.valueOf(thread.getId()).toString();
	}
	
	private final IBinder mBinder = new MQTTBinder();
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "onBind called");
		return mBinder;
	}
	
	public interface ICallback {
		public void sendData(String... s);
		
		public void sendVoteData(String... s);
		
		public void sendFileData(String... s);
		
		public void updateLike(String... s);
		
		public void deleteMessage(String... s);
	}
	
	private ICallback mCallback;
	
	public void registerCallback(ICallback cb) {
		mCallback = cb;
	}
	
	WifiLock wifiLock = null;
	WakeLock wakeLock = null;
	
	public void changeToWakeMode() {
		Log.d(TAG, "wakelock start");
		if (wifiLock == null) {
			WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
			wifiLock = wifiManager.createWifiLock("wifilock");
			wifiLock.setReferenceCounted(true);
			wifiLock.acquire();
		}
		
		if (wakeLock == null) {
			PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
			wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					"wakelock");
			wakeLock.acquire();
		}
	}
	
	public void closeToWakeMode() {
		Log.d(TAG, "wakelock end");
		if (wifiLock != null) {
			wifiLock.release();
			wifiLock = null;
		}
		
		if (wakeLock != null) {
			wakeLock.release();
			wakeLock = null;
		}
	}
}