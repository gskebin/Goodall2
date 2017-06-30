package kr.co.dunet.goodall;

import java.util.ArrayList;
import java.util.List;
import kr.co.dunet.app.goodall.R;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;

public class RoomCreateActivity extends Activity {
	private static Toast mToast = null;
	private HttpService http = null;
	private String msg = null;
	private static ProgressDialog dialog = null;
	private SendHTTPData mSendHTTPData = null;
	
	public static String myId = null;
	public static String myName = null;
	public static String roomName = null;
	public static String roomPw = "";
	private static LayoutInflater inflater = null;
	private static Dialog alertDialog = null;
	
	private boolean passwordProtected = false;
	
	private String roomCode = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_room_create);
		
		/*
		 * ActionBar mActionBer = getActionBar();
		 * mActionBer.setDisplayShowHomeEnabled(false);
		 * mActionBer.setTitle(" 방 생성");
		 * mActionBer.setDisplayHomeAsUpEnabled(true);
		 */
		
		DBManager mDbManager = DBManager.getInstance(this);
		String[] columns = new String[] { "id", "nickname" };
		Cursor c = mDbManager.selectMember(columns, null, null, null, null,
				null);
		
		while (c.moveToNext()) {
			myId = c.getString(0);
			myName = c.getString(1);
		}
		c.close();
		
		Button btnPublicY = (Button) this.findViewById(R.id.btnPublicY);
		Button btnPublicN = (Button) this.findViewById(R.id.btnPublicN);
		
		if (!passwordProtected) {
			// 공개
			btnPublicY.setBackgroundResource(R.drawable.btn_radio_on);
			btnPublicN.setBackgroundResource(R.drawable.btn_radio_off);
			LinearLayout pwLayout = (LinearLayout) this
					.findViewById(R.id.pwLayout);
			pwLayout.setVisibility(View.GONE);
			// pwLayout.setAlpha(0);
		} else {
			// 비공개
			btnPublicY.setBackgroundResource(R.drawable.btn_radio_off);
			btnPublicN.setBackgroundResource(R.drawable.btn_radio_on);
			LinearLayout pwLayout = (LinearLayout) this
					.findViewById(R.id.pwLayout);
			pwLayout.setVisibility(View.VISIBLE);
			// pwLayout.setAlpha(1);
		}
		
		inflater = LayoutInflater.from(getApplicationContext());
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// android.os.Debug.waitForDebugger();
		super.onConfigurationChanged(newConfig);
		
	}
	
	public void btnClickComplete(View v) {
		EditText room_name = (EditText) this.findViewById(R.id.roomName);
		EditText room_pw1 = (EditText) this.findViewById(R.id.roomPW1);
		EditText room_pw2 = (EditText) this.findViewById(R.id.roomPW2);
		
		roomName = room_name.getText().toString();
		String roomPw1 = room_pw1.getText().toString();
		String roomPw2 = room_pw2.getText().toString();
		
		if (roomName.equals("")) {
			makeToastMsg("방제목을 입력하세요.");
		} else if (passwordProtected
				&& (roomPw1.equals("") || roomPw2.equals(""))) {
			makeToastMsg("비밀번호를 입력하세요.");
		} else if (passwordProtected && (roomPw1.compareTo(roomPw2) != 0)) {
			makeToastMsg("비밀번호가 일치하지 않습니다.");
		} else {
			// 패스워드
			if (passwordProtected) {
				roomPw = roomPw1;
			} else {
				roomPw = "";
			}
			
			hideSoftInputWindow(v);
			
			System.out.println("myId=======================" + myId);
			System.out.println("roomPw=======================" + roomPw);
			System.out.println("roomName=======================" + roomName);
			
			if (dialog == null) {
				dialog = ProgressDialog.show(this, "", "처리중입니다...", false);
				// dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				
				mSendHTTPData = new SendHTTPData();
				mSendHTTPData.execute(myId, roomPw, roomName, myName);
				dialog.show();
			}
			
		}
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
			http.setUrlString("/room/create");
			
			List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(
					4);
			nameValuePairs.add(new BasicNameValuePair("id", data[0]));
			nameValuePairs.add(new BasicNameValuePair("password", data[1]));
			nameValuePairs.add(new BasicNameValuePair("name", data[2]));
			nameValuePairs.add(new BasicNameValuePair("nickname", data[3]));
			
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
							String room_code = status.getString("code");
							String room_date = status.getString("date");
							String room_password = status.getString("password");
							msg = null;
							createSuccess(room_code, room_date, room_password);
						} else {
							msg = status.getString("msg");
						}
						
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
	
	private void createSuccess(String room_code, String room_date,
			String room_password) {
		
		ChatApplication app = (ChatApplication) getApplicationContext();
		IMqttAsyncClient mqtt = app.getMqtt();
		
		IMqttToken token;
		
		try {
			token = mqtt.subscribe(room_code, 1);
			token.waitForCompletion(5000);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		DBManager mDbManager = DBManager.getInstance(this);
		
		ContentValues addRowValue = new ContentValues();
		addRowValue.put("room_code", room_code);
		addRowValue.put("room_name", roomName);
		addRowValue.put("admin", myId);
		addRowValue.put("reg_date", room_date);
		addRowValue.put("mod_date", room_date);
		addRowValue.put("room_password", room_password);
		
		mDbManager.insertRoom(addRowValue);
		System.out.println("ROOM SET!!!!!!!!!!!!!!!!!");
		
		View dialoglayout = inflater.inflate(R.layout.alert_msg, null);
		
		TextView alert_title = (TextView) dialoglayout
				.findViewById(R.id.alert_title);
		TextView alert_msg = (TextView) dialoglayout
				.findViewById(R.id.alert_msg);
		TextView alert_submit = (TextView) dialoglayout
				.findViewById(R.id.alert_submit);
		alert_title.setText("방 정보");
		
		String alert_msg_text = "";
		if (roomPw.equals("")) {
			alert_msg_text = "방 입장 코드 : " + room_code;
		} else {
			alert_msg_text = "방 입장 코드 : " + room_code + "\n[비밀번호 : "
					+ room_password + "]";
		}
		alert_msg.setText(alert_msg_text);
		
		roomCode = room_code;
		
		alert_submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.dismiss();
				goNext(roomCode);
			}
		});
		
		alertDialog = new Dialog(this, R.style.CustomDialog);
		alertDialog.setContentView(dialoglayout);
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.show();
	}
	
	public void goNext(String room_code) {
		Intent intent = new Intent(this, MUCMessageActivity.class);
		intent.putExtra("ROOMCODE", room_code);
		startActivity(intent);
		
		finish();
	}
	
	public void btnClickPublicY(View v) {
		
		Button btnPublicY = (Button) this.findViewById(R.id.btnPublicY);
		Button btnPublicN = (Button) this.findViewById(R.id.btnPublicN);
		// 토글 : 비공개 -> 공개
		EditText room_pw1 = (EditText) this.findViewById(R.id.roomPW1);
		EditText room_pw2 = (EditText) this.findViewById(R.id.roomPW2);
		room_pw1.setText("");
		room_pw2.setText("");
		btnPublicY.setBackgroundResource(R.drawable.btn_radio_on);
		btnPublicN.setBackgroundResource(R.drawable.btn_radio_off);
		LinearLayout pwLayout = (LinearLayout) this.findViewById(R.id.pwLayout);
		pwLayout.setVisibility(View.GONE);
		// pwLayout.setAlpha(0);
		passwordProtected = false;
	}
	
	public void btnClickPublicN(View v) {
		
		Button btnPublicY = (Button) this.findViewById(R.id.btnPublicY);
		Button btnPublicN = (Button) this.findViewById(R.id.btnPublicN);
		// 토글 : 공개 -> 비공개
		btnPublicY.setBackgroundResource(R.drawable.btn_radio_off);
		btnPublicN.setBackgroundResource(R.drawable.btn_radio_on);
		LinearLayout pwLayout = (LinearLayout) this.findViewById(R.id.pwLayout);
		pwLayout.setVisibility(View.VISIBLE);
		// pwLayout.setAlpha(1);
		passwordProtected = true;
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
	
	public void goBack(View v) {
		finish();
	}
	
}