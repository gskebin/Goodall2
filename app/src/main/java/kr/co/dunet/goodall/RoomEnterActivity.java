package kr.co.dunet.goodall;

import java.util.ArrayList;
import java.util.List;
import kr.co.dunet.app.goodall.R;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class RoomEnterActivity extends Activity {
	
	private static Toast mToast = null;
	private HttpService http = null;
	private String msg = null;
	private static ProgressDialog dialog = null;
	private SendHTTPData mSendHTTPData = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_room_enter);
		
		/*
		 * ActionBar mActionBer = getActionBar();
		 * mActionBer.setDisplayShowHomeEnabled(false);
		 * mActionBer.setTitle(" 방 입장");
		 * mActionBer.setDisplayHomeAsUpEnabled(true);
		 */
	}
	
	public void btnClickEnter(View v) {
		EditText room_code = (EditText) this.findViewById(R.id.roomCode);
		EditText room_password = (EditText) this
				.findViewById(R.id.roomPassword);
		String roomCode = room_code.getText().toString();
		String roomPassword = room_password.getText().toString();
		
		if (roomCode.equals("")) {
			makeToastMsg("방코드를 입력하세요.");
		} else {
			hideSoftInputWindow(v);
			
			if (dialog == null) {
				dialog = ProgressDialog.show(this, "", "처리중입니다...", false);
				// dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				
				mSendHTTPData = new SendHTTPData();
				mSendHTTPData.execute(roomCode, roomPassword);
				dialog.show();
			}
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// android.os.Debug.waitForDebugger();
		super.onConfigurationChanged(newConfig);
		
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
			http.setUrlString("/room/enter_room");
			
			ChatApplication app = (ChatApplication) getApplicationContext();
			
			List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(
					4);
			nameValuePairs.add(new BasicNameValuePair("code", data[0]));
			nameValuePairs.add(new BasicNameValuePair("password", data[1]));
			nameValuePairs.add(new BasicNameValuePair("id", app.getId()));
			nameValuePairs
					.add(new BasicNameValuePair("nickname", app.getName()));
			
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
							enterSuccess(status.getString("code"),
									status.getString("name"),
									status.getString("date"),
									status.getString("mod_date"),
									status.getString("admin"),
									status.getString("password"),
									status.getString("admin_name"));
						} else if (state.equals("0002")) {
							// 방 비밀번호 입력칸 생성
							msg = status.getString("msg");
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
	
	private void enterSuccess(String code, String name, String date,
			String mod_date, String admin, String password,
			String admin_nickname) {
		Log.d("ROOM ENTER", "SUCCESS");
		
		ChatApplication app = (ChatApplication) getApplicationContext();
		IMqttAsyncClient mqtt = app.getMqtt();
		
		IMqttToken token;
		
		try {
			token = mqtt.subscribe(code, 1);
			token.waitForCompletion(5000);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// 방 DB 입력
		DBManager mDbManager = DBManager.getInstance(this);
		
		String where = "room_code='" + code + "'";
		String[] select = new String[] { "room_code" };
		
		Cursor c = mDbManager.selectRoom(select, where, null, null, null, null);
		if (c != null && c.getCount() > 0) {
		} else {
			ContentValues addRowValue = new ContentValues();
			addRowValue.put("room_code", code);
			addRowValue.put("room_name", name);
			addRowValue.put("reg_date", date);
			addRowValue.put("mod_date", mod_date);
			addRowValue.put("admin", admin);
			addRowValue.put("room_password", password);
			
			mDbManager.insertRoom(addRowValue);
		}
		c.close();
		
		// 방 생성자의 정보 체크
		mDbManager = DBManager.getInstance(this);
		where = "id='" + admin + "'";
		String[] columns = new String[] { "nickname" };
		c = mDbManager.selectUser(columns, where, null, null, null, null);
		
		if (c != null && c.getCount() > 0) {
		} else {
			ContentValues addRowValue = new ContentValues();
			addRowValue.put("id", admin);
			addRowValue.put("lastdate", "");
			addRowValue.put("nickname", admin_nickname);
			
			mDbManager.insertUser(addRowValue);
		}
		c.close();
		
		Intent intent = new Intent(this, MUCMessageActivity.class);
		intent.putExtra("ROOMCODE", code);
		startActivity(intent);
		// overridePendingTransition(0, 0);
		finish();
	}
	
	@Override
	public void onBackPressed() {
		finish();
		// overridePendingTransition(0, 0);
	}
	
	public void goBack(View v) {
		finish();
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
	
	public void hideSoftInputWindow(View edit_view) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(edit_view.getWindowToken(), 0);
	}
}