package kr.co.dunet.goodall;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import kr.co.dunet.app.goodall.R;

public class FromActivity extends Activity {

	private long mExitModeTime = 0L;
	public static String USERNAME = null;
	public static String PASSWORD = null;
	private static Toast mToast = null;
	private static ProgressDialog dialog = null;
	private HttpService http = null;
	private String msg = null;
	private SendHTTPData mSendHTTPData = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_from);

		EditText password = (EditText) findViewById(R.id.chatPwd);
		password.setTypeface(Typeface.DEFAULT);

		//Intent intent = new Intent();
		//intent.setAction("kr.co.dunet.goodall.restart");
		//sendBroadcast(intent);
	}
	
	//계정 찾기
	public void findIdClick(View v){
		startActivity(new Intent(this, UserFindActivity.class));
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		android.os.Debug.waitForDebugger();
		super.onConfigurationChanged(newConfig);

	}

	/*
	 * 회원가입
	 */
	public void joinBtnClick(View v) {
		startActivity(new Intent(this, JoinActivity.class));
		//overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	public void btnClick(View v) {
		EditText user_id = (EditText) this.findViewById(R.id.chatFrom);
		EditText user_pwd = (EditText) this.findViewById(R.id.chatPwd);

		USERNAME = user_id.getText().toString().trim();
		PASSWORD = user_pwd.getText().toString().trim();

		if (USERNAME.equals("")) {
			makeToastMsg("아이디(이메일)를 입력하세요");
		} else if (PASSWORD.equals("")) {
			makeToastMsg("비밀번호를 입력하세요");
		} else {
			hideSoftInputWindow(v);

			if (dialog == null) {
				dialog = ProgressDialog.show(this, "", "로그인중입니다...", false);
				// dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

				mSendHTTPData = new SendHTTPData();
				mSendHTTPData.execute(USERNAME, PASSWORD);
				dialog.show();
			}
		}
	}
	
	public void btnGuestClick(View v) {
		try {
			//게스트 로그인
			//makeToastMsg("서비스 준비중입니다.");
			
			//임시아아디발급
			startActivity(new Intent(this, GuestLoginActivity.class));
			
		} catch(Exception e) {
			makeToastMsg("Guest 버튼 에러 : " + e.getMessage() + "(개발자에게 문의하세요)");
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
			// http.setDns("주소");
			// http.setPort("8080");
			http.setUrlString("/member/login");

			List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(
					2);
			nameValuePairs.add(new BasicNameValuePair("id", data[0]));
			nameValuePairs.add(new BasicNameValuePair("pwd", data[1]));

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
							// msg = "로그인 성공";
							msg = null;
							String nickName = status.getString("nickname");
							String id = status.getString("id");
							loginSuccess(id, nickName);
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

	public void loginSuccess(String id, String nickName) {
		// DB에 로그인 정보 입력
		DBManager mDbManager = DBManager.getInstance(this);

		ContentValues addRowValue = new ContentValues();
		addRowValue.put("id", id);
		addRowValue.put("nickname", nickName);

		mDbManager.insertMember(addRowValue);

		ChatApplication app = (ChatApplication) getApplicationContext();
		app.setId(id);
		app.setName(nickName);
		app.setFirst(true);
		app.setIsGuest(false);
		
		//서비스 시작
		Intent i = new Intent(getApplicationContext(), MQTTService.class);
		getApplicationContext().startService(i);
		
		finish();
		
		Intent intent = new Intent(this, ListActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void hideSoftInputWindow(View edit_view) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(edit_view.getWindowToken(), 0);
	}

	@Override
	public void onBackPressed() {
		if (mExitModeTime != 0
				&& SystemClock.uptimeMillis() - mExitModeTime < 3000) {
			finish();
		} else {
			makeToastMsg("뒤로 버튼을 한번 더 누르시면 종료됩니다.");

			mExitModeTime = SystemClock.uptimeMillis();
		}
	}

}
