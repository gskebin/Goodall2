package kr.co.dunet.goodall;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.message.BasicNameValuePair;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import kr.co.dunet.app.goodall.R;

public class ListActivity extends Activity {
	
	private long mExitModeTime = 0L;
	
	private ListView listRoom = null;
	public RoomListAdapter roomAdapter;
	public ArrayList<RoomData> mData = null;
	public ArrayList<UserInfo> newProfileIds = null;
	
	public Boolean isGuest = false;
	
	private static Toast mToast = null;
	
	private SendHTTPData mSendHTTPData = null;
	private HttpService http = null;
	private String msg = null;
	private static ProgressDialog dialog = null;
	
	private String myId = null;
	
	private static ListActivity mListActivity = null;
	
	public ListActivity() {
		mListActivity = this;
	}
	
	public static ListActivity Instance() {
		return mListActivity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		System.out.println("onCreate()");
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_room_list);
		
		// 서비스가 중단되었을지 모르니 다시 실행시켜준다
		Intent i = new Intent(getApplicationContext(), MQTTService.class);
		getApplicationContext().startService(i);
		
		commonSetting();
	}
	
	@Override
	protected void onResume() {
		System.out.println("onResume()");
		super.onResume();
		
		commonSetting();
	}
	
	public void commonSetting() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("kr.co.dunet.goodall.new_message");
		
		registerReceiver(mReceiver, intentFilter);
		
		ChatApplication app = (ChatApplication) getApplicationContext();
		
		String id = app.getId() == null ? "" : app.getId();
		
		if (id.equals("")) {
			DBManager mDbManager = DBManager.getInstance(this);
			String[] columns = new String[] { "id", "nickname", "type" };
			Cursor c = mDbManager.selectMember(columns, null, null, null, null,
					null);
			
			if (c != null && c.getCount() > 0) {
				Boolean isGuest = false;
				while (c.moveToNext()) {
					id = c.getString(0);
					if (c.getString(2) != null && c.getString(2).equals("1")) {
						isGuest = true;
					}
					break;
				}
				c.close();
				app.setId(id);
				app.setIsGuest(isGuest);
			}
		}
		
		isGuest = app.getIsGuest();
		
		if (isGuest == true) {
			findViewById(R.id.btnRoomMake).setVisibility(View.GONE);
		}
		myId = id;
		
		newProfileIds = new ArrayList<UserInfo>();
		
		try {
			listRoom = (ListView) this.findViewById(R.id.listRoom);
			
			mData = new ArrayList<RoomData>();
			roomAdapter = new RoomListAdapter(this, R.layout.room_item,
					R.id.room_code, mData);
			listRoom.setAdapter(roomAdapter);
			
			DBManager mDbManager = DBManager.getInstance(this);
			String[] columns = new String[] { "room_code", "room_name",
					"reg_date", "admin", "room_password", "no_read", "mod_date" };
			String orderBy = "no_read desc, mod_date desc, _id desc";
			Cursor c = mDbManager.selectRoom(columns, null, null, null, null,
					orderBy);
			
			while (c.moveToNext()) {
				System.out.println("ROOM EXISTS!");
				clearProfileCache(c.getString(3));
				if (myId.equals(c.getString(3)) && c.getString(4) != null
						&& !c.getString(4).equals("")) {
					roomAdapter
							.addItem(
									c.getString(1),
									c.getString(0),
									c.getString(0) + " [비밀번호 : "
											+ c.getString(4) + "]",
									c.getString(2), "Y", c.getString(4),
									c.getString(3), c.getInt(5), c.getString(6));
				} else {
					roomAdapter.addItem(c.getString(1), c.getString(0),
							c.getString(0), c.getString(2), "Y",
							c.getString(4), c.getString(3), c.getInt(5), c.getString(6));
				}
				//System.out.println(c.getString(0));
			}
			
			c.close();
			
			listRoom.setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);
			
			/*
			 * listRoom.setOnItemClickListener(new OnItemClickListener() {
			 * 
			 * @Override public void onItemClick(AdapterView<?> parent, View v,
			 * int position, long id) {
			 * 
			 * System.out.println("방리스트 클릭");
			 * 
			 * if (roomAdapter.mRoomData.get(position).exitView.equals("Y")) {
			 * roomAdapter.mRoomData.get(position).exitView = "N";
			 * //roomAdapter.notifyDataSetChanged(); } else {
			 * roomAdapter.mRoomData.get(position).exitView = "Y";
			 * //roomAdapter.notifyDataSetChanged(); //RoomData mData =
			 * roomAdapter.mRoomData.get(position); //enterRoom(mData.roomCode);
			 * } } });
			 * 
			 * listRoom.setOnItemLongClickListener(new OnItemLongClickListener()
			 * {
			 * 
			 * @Override public boolean onItemLongClick(AdapterView<?> parent,
			 * View v, int position, long id) {
			 * System.out.println("LONG CLICK LISTENER!");
			 * 
			 * roomAdapter.mRoomData.get(position).exitView = "Y";
			 * roomAdapter.notifyDataSetChanged(); return true; } });
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void clearProfileCache(String id) {
		UserInfo tmp = new UserInfo();
		tmp.mId = id;
		
		int id_idx = newProfileIds.indexOf(tmp);
		
		if (id_idx > -1) {
			// 기존에 존재하면
			System.out.println("기존에 추가 : " + id);
			
			return;
		} else {
			System.out.println("새로 추가 : " + id);
		}
		// System.out.println(id_idx);
		
		DBManager mDbManager = DBManager.getInstance(this);
		String where = "id='" + id + "'";
		String[] columns = new String[] { "nickname" };
		Cursor c = mDbManager
				.selectUser(columns, where, null, null, null, null);
		
		if (c != null && c.getCount() > 0) {
			while (c.moveToNext()) {
				tmp.mName = c.getString(0);
				break;
			}
		}
		c.close();
		
		newProfileIds.add(tmp);
	}
	
	public String getAdminName(String id) {
		UserInfo tmp = new UserInfo();
		tmp.mId = id;
		
		int id_idx = newProfileIds.indexOf(tmp);
		
		if (id_idx > -1) {
			// 기존에 존재하면
			return newProfileIds.get(id_idx).mName;
		} else {
			return "";
		}
	}
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("kr.co.dunet.goodall.new_message")) {

				String room_code = intent.getStringExtra("roomCode");
				String mod_date = intent.getStringExtra("mod_date");
				int no_read = intent.getIntExtra("no_read", 0);
				//makeToastMsg(room_code + " : " + no_read);
				
				RoomData tmp = new RoomData();
				tmp.roomCode = room_code;
				int i = mData.indexOf(tmp);
				//System.out.println(i);
				
				if(i > -1) {
					roomAdapter.getItem(i).noRead = no_read;
					roomAdapter.getItem(i).modDate = mod_date;

					//Collections.sort(mData, new DescCompare());
					roomAdapter.sort(new Comparator<RoomData>() {

						@Override
						public int compare(RoomData lhs, RoomData rhs) {
							// TODO Auto-generated method stub
							Collator collator = Collator.getInstance();
							return collator.compare(rhs.modDate, lhs.modDate);
						}
					});
					//roomAdapter.notifyDataSetChanged();
				}
			}
		}
	};
	
	 /**
	  * 내림차순
	  */
	static class DescCompare implements Comparator<RoomData> {

		/**
		 * 내림차순(DESC)
		 */
		@Override
		public int compare(RoomData arg0, RoomData arg1) {
			// TODO Auto-generated method stub
			
			System.out.println(arg0.modDate + " | " + arg1.modDate);
			
			return arg1.modDate.compareTo(arg0.modDate);
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// android.os.Debug.waitForDebugger();
		super.onConfigurationChanged(newConfig);
		
	}
	
	public void enterRoom(String room_code) {
		
		Intent intent = new Intent(this, MUCMessageActivity.class);
		intent.putExtra("ROOMCODE", room_code);
		startActivity(intent);
		
	}
	
	public void btnClickRoomJoin(View v) {
		startActivity(new Intent(this, RoomEnterActivity.class));
	}
	
	public void btnClickRoomMake(View v) {
		startActivity(new Intent(this, RoomCreateActivity.class));
	}
	
	public void btnClickModifyInfo(View v) {
		if (isGuest == true) {
			startActivity(new Intent(this, GuestModifyActivity.class));
		} else {
			startActivity(new Intent(this, UserModifyActivity.class));
		}
	}
	
	public void hideSoftInputWindow(View edit_view) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(edit_view.getWindowToken(), 0);
	}
	
	public void makeToastMsg(Context context, String msg) {
		if (mToast == null) {
			mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
		} else {
			mToast.setText(msg);
		}
		mToast.show();
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(mReceiver);
		
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		if (mExitModeTime != 0
				&& SystemClock.uptimeMillis() - mExitModeTime < 3000) {
			ChatApplication app = (ChatApplication) getApplicationContext();
			app.finishApp(this);
			// finish();
		} else {
			makeToastMsg(this, "뒤로 버튼을 한번 더 누르시면 종료됩니다.");
			
			mExitModeTime = SystemClock.uptimeMillis();
		}
	}
	
	public void exitRoom(String code) {
		ChatApplication app = (ChatApplication) getApplicationContext();
		String id = app.getId();
		if (id == null || id.equals("")) {
			DBManager mDbManager = DBManager.getInstance(this);
			String[] columns = new String[] { "id", "nickname", "type" };
			Cursor c = mDbManager.selectMember(columns, null, null, null, null,
					null);
			
			if (c != null && c.getCount() > 0) {
				while (c.moveToNext()) {
					id = c.getString(0);
					break;
				}
				c.close();
			}
		}
		
		if (dialog == null) {
			dialog = ProgressDialog.show(this, "", "처리중입니다...", false);
			// dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			
			mSendHTTPData = new SendHTTPData();
			mSendHTTPData.execute(code, id);
			dialog.show();
		}
	}
	
	public void exitRoomSuccess(String code) {
		Log.d("ROOM EXIT", "SUCCESS : " + code);
		
		ChatApplication app = (ChatApplication) getApplicationContext();
		IMqttAsyncClient mqtt = app.getMqtt();
		
		IMqttToken token;
		
		try {
			token = mqtt.unsubscribe(code);
			token.waitForCompletion(5000);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// DB 삭제
		DBManager mDbManager = DBManager.getInstance(this);
		mDbManager.deleteRoom("room_code='" + code + "'", null);
		mDbManager.deleteChat("room_code='" + code + "'", null);
		
		File file = getApplicationContext().getFilesDir();
		String dir = file.getAbsolutePath() + "/temp/" + code;
		String dir2 = file.getAbsolutePath() + "/large/" + code;
		
		Runtime runtime = Runtime.getRuntime();
		String cmd = "rm -R " + dir;
		String cmd2 = "rm -R " + dir2;
		try {
			runtime.exec(cmd);
			runtime.exec(cmd2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		roomAdapter.notifyDataSetChanged();
	}
	
	private class SendHTTPData extends AsyncTask<String, Integer, Boolean> {
		
		// AsyncTask 시작전
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		// AsyncTask 시작
		@Override
		protected Boolean doInBackground(String... data) {
			http = HttpService.getInstance();
			http.setUrlString("/room/exit_room");
			
			List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(
					4);
			nameValuePairs.add(new BasicNameValuePair("code", data[0]));
			nameValuePairs.add(new BasicNameValuePair("id", data[1]));
			
			http.setNameValuePairs(nameValuePairs);
			
			Boolean ret = http.sendData();
			
			return ret;
		}
		
		// AsyncTask 중간 처리
		@Override
		protected void onProgressUpdate(Integer... processInfos) {
			super.onProgressUpdate(processInfos);
		}
		
		// AsyncTask 실행 취소
		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
		
		// AsyncTask 완료
		@Override
		protected void onPostExecute(Boolean result) {
			dialog.dismiss();
			dialog = null;
			
			if (result == true) {
				// 성공
				msg = null;
				JSONObject status = null;
				String json = http.getJson();
				Log.d("JOIN", json);
				
				if (json != null) {
					try {
						status = new JSONObject(json);
						String state = status.getString("result");
						
						if (state.equals("0000")) {
							msg = null;
							exitRoomSuccess(status.getString("code"));
						} else {
							msg = status.getString("msg");
						}
						// Log.d("HTTP", state);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					msg = "데이터 수신 실패";
				}
				
				if (msg != null) {
					makeToastMsg(msg);
				}
			} else {
				// 실패
				makeToastMsg("통신 실패");
			}
			
			super.onPostExecute(result);
		}
	}
	
	public void makeToastMsg(String msg) {
		if (mToast == null) {
			mToast = Toast.makeText(getApplicationContext(), msg,
					Toast.LENGTH_SHORT);
		} else {
			mToast.setText(msg);
		}
		mToast.show();
	}
}
