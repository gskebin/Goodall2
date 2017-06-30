package kr.co.dunet.goodall;

import java.io.File;
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
import android.app.NotificationManager;
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

public class GuestModifyActivity extends Activity {
	private static Toast mToast = null;
	private String msg = null;
	private static ProgressDialog dialog = null;
	private SendHTTPData mSendHTTPData = null;
	private HttpService http = null;
	
	private String myId = null;
	private String nickname = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_user_modify);
		
		findViewById(R.id.chatEmail).setVisibility(View.GONE);
		findViewById(R.id.chatPw).setVisibility(View.GONE);
		findViewById(R.id.chatPwConfirm).setVisibility(View.GONE);
		findViewById(R.id.chatProfile).setVisibility(View.GONE);
		
		// ChatApplication app = (ChatApplication) getApplicationContext();
		
		ChatApplication app = (ChatApplication) getApplicationContext();
		myId = app.getId();
		
		if (dialog == null) {
			dialog = ProgressDialog.show(this, "", "처리중입니다...", false);
			// dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			
			mSendHTTPData = new SendHTTPData();
			mSendHTTPData.execute("get", myId);
			dialog.show();
		}
		
		/*
		 * DBManager mDbManager = DBManager.getInstance(this); String[] columns
		 * = new String[] { "id", "nickname" , "type" }; Cursor c =
		 * mDbManager.selectMember(columns, null, null, null, null, null);
		 * 
		 * if (c != null && c.getCount() > 0) { while (c.moveToNext()) { myId =
		 * c.getString(0); nickname = c.getString(1); break; } c.close(); }
		 * 
		 * EditText username = (EditText) findViewById(R.id.chatName);
		 * username.setText(nickname);
		 */
	}
	
	// 정보 변경하기
	public void btnClick(View v) {
		EditText username = (EditText) findViewById(R.id.chatName);
		
		nickname = username.getText().toString().trim();
		
		if (nickname.equals("")) {
			makeToastMsg("대화명을 입력하세요.");
		} else {
			if (dialog == null) {
				dialog = ProgressDialog.show(this, "", "처리중입니다...", false);
				
				mSendHTTPData = new SendHTTPData();
				mSendHTTPData.execute("update", myId, nickname);
				dialog.show();
			}
			/*
			 * makeToastMsg("수정완료"); //정보 업데이트 DBManager mDbManager = DBManager
			 * .getInstance(getApplicationContext());
			 * 
			 * ContentValues updateRowValue = new ContentValues();
			 * updateRowValue.put("nickname", nickname);
			 * 
			 * int res = mDbManager.updateMember(updateRowValue, "id='" + myId +
			 * "'", null); finish();
			 */
		}
	}
	
	@Override
	public void onBackPressed() {
		finish();
		// overridePendingTransition(0, 0);
	}
	
	public void goBack(View v) {
		finish();
	}
	
	public void goLogout(View v) {
		if (dialog == null) {
			dialog = ProgressDialog.show(this, "", "처리중입니다...", false);
			// dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			
			mSendHTTPData = new SendHTTPData();
			mSendHTTPData.execute("logout", myId);
			dialog.show();
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
			
			List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(
					1);
			
			if (data[0] == "get") {
				http.setUrlString("/member/get_member");
				nameValuePairs.add(new BasicNameValuePair("id", data[1]));
			} else if (data[0] == "update") {
				http.setUrlString("/member/mod_profile_nomember");
				nameValuePairs.add(new BasicNameValuePair("id", data[1]));
				nameValuePairs.add(new BasicNameValuePair("nickname", data[2]));
			} else if (data[0] == "logout") {
				http.setUrlString("/member/logout");
				nameValuePairs.add(new BasicNameValuePair("id", data[1]));
			}
			
			Boolean ret = false;
			http.setNameValuePairs(nameValuePairs);
			
			ret = http.sendData();
			
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
				
				JSONObject status = null;
				String json = http.getJson();
				// Log.d("JOIN", json);
				
				if (json != null) {
					Log.d("HTTP", json);
					try {
						status = new JSONObject(json);
						String state = status.getString("result");
						
						if (state.equals("0000")) {
							String type = status.getString("type");
							
							System.out.println(type);
							
							if (type.equals("get")) {
								EditText username = (EditText) findViewById(R.id.chatName);
								username.setText(status.getString("name"));
							} else if (type.equals("update")) {
								makeToastMsg(status.getString("msg"));
								// 정보 업데이트
								DBManager mDbManager = DBManager
										.getInstance(getApplicationContext());
								
								ContentValues updateRowValue = new ContentValues();
								updateRowValue.put("nickname", nickname);
								
								mDbManager.updateMember(updateRowValue, "id='"
										+ myId + "'", null);
								
								String where = "id='" + myId + "'";
								String[] columns = new String[] { "nickname" };
								Cursor c = mDbManager.selectUser(columns,
										where, null, null, null, null);
								
								if (c != null && c.getCount() > 0) {
									updateRowValue = new ContentValues();
									updateRowValue.put("lastdate", "");
									updateRowValue.put("nickname", nickname);
									mDbManager.updateUser(updateRowValue,
											"id='" + myId + "'", null);
								} else {
									ContentValues addRowValue = new ContentValues();
									addRowValue.put("id", myId);
									addRowValue.put("lastdate", "");
									addRowValue.put("nickname", nickname);
									
									mDbManager.insertUser(addRowValue);
								}
								c.close();
								
								finish();
							} else if (type.equals("logout")) {
								goLogoutSuccess();
							}
						} else {
							makeToastMsg(status.getString("msg"));
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			} else {
				makeToastMsg("통신 실패.\n다시 시도해주세요.");
			}
			
			super.onPostExecute(result);
		}
	}
	
	public void goLogoutSuccess() {
		ChatApplication app = (ChatApplication) getApplicationContext();
		app.setId(null);
		
		// 파일삭제
		File file = getApplicationContext().getFilesDir();
		
		String dir = file.getAbsolutePath() + "/temp";
		String l_dir = file.getAbsolutePath() + "/large";
		String profile_dir = file.getAbsolutePath() + "/profile";
		
		DeleteDir(dir);
		DeleteDir(l_dir);
		DeleteDir(profile_dir);
		
		// DB삭제
		DBManager mDbManager = DBManager.getInstance(getApplicationContext());
		
		mDbManager.deleteMember(null, null);
		mDbManager.deleteChat(null, null);
		mDbManager.deleteRoom(null, null);
		mDbManager.deleteUser(null, null);
		
		IMqttAsyncClient mqtt = app.getMqtt();
		try {
			IMqttToken token = mqtt.disconnect();
			token.waitForCompletion(5000);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		// 등록된 notification 을 제거 한다.
		nm.cancel(333);
		
		Notify.setMessageID(1);
		
		finish();
		
		Intent i = new Intent(this, FromActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(i);
	}
	
	public void DeleteDir(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return;
		}
		File[] childFileList = file.listFiles();
		for (File childFile : childFileList) {
			if (childFile.isDirectory()) {
				DeleteDir(childFile.getAbsolutePath()); // 하위 디렉토리 루프
			} else {
				childFile.delete(); // 하위 파일삭제
			}
		}
		file.delete(); // root 삭제
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
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// android.os.Debug.waitForDebugger();
		super.onConfigurationChanged(newConfig);
		
	}
}
