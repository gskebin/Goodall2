package kr.co.dunet.goodall;

import java.util.ArrayList;
import java.util.List;

import kr.co.dunet.app.goodall.R;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.Toast;

public class UserListActivity extends Activity {
	public ArrayList<UserData> mData = null;
	public ArrayList<UserInfo> newProfileIds = null;
	private static Toast mToast = null;
	private ListView listUser = null;
	public UserListAdapter userListAdapter = null;
	private HttpService http = null;
	private static ProgressDialog dialog = null;
	private SendHTTPData mSendHTTPData = null;
	private String roomCode = "";
	
	private static UserListActivity mUserListActivity = null;
	
	public UserListActivity() {
		mUserListActivity = this;
	}
	
	public static UserListActivity Instance() {
		return mUserListActivity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_user_list);
		
		roomCode = getIntent().getStringExtra("ROOMCODE");
		newProfileIds = new ArrayList<UserInfo>();
		
		try {
			listUser = (ListView) this.findViewById(R.id.listUser);
			
			mData = new ArrayList<UserData>();
			userListAdapter = new UserListAdapter(this,
					R.layout.user_list_item, R.id.name, mData);
			
			listUser.setAdapter(userListAdapter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (dialog == null) {
			dialog = ProgressDialog.show(this, "", "처리중입니다...", false);
			
			mSendHTTPData = new SendHTTPData();
			mSendHTTPData.execute(roomCode);
			dialog.show();
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
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
	
	/**
	 * 데이터 통신용 클래스 시작
	 */
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
			http.setUrlString("/room/room_user_list");
			
			List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(
					1);
			nameValuePairs.add(new BasicNameValuePair("code", data[0]));
			
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
				
				JSONObject status = null;
				String json = http.getJson();
				
				if (json != null) {
					Log.d("HTTP", json);
					try {
						status = new JSONObject(json);
						String state = status.getString("result");
						
						if (state.equals("0000")) {
							JSONArray list = (JSONArray) status.get("list");
							
							if (list.length() > 0) {
								for (int i = 0; i < list.length(); i++) {
									JSONObject jObject = list.getJSONObject(i);
									
									UserData addInfo = null;
									
									addInfo = new UserData();
									addInfo.userId = jObject.getString("id");
									addInfo.userName = jObject
											.getString("nickname");
									
									MUCMessageActivity.Instance()
											.clearProfileCache(addInfo.userId,
													addInfo.userName);
									
									userListAdapter.add(addInfo);
								}
							}
							userListAdapter.notifyDataSetChanged();
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			} else {
				// 실패
				makeToastMsg("통신 실패.\n다시 시도해주세요.");
			}
			
			super.onPostExecute(result);
		}
	}
}
