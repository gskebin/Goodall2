package kr.co.dunet.goodall;

import java.util.ArrayList;
import java.util.List;
import kr.co.dunet.app.goodall.R;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class GuestLoginActivity extends Activity {
	private static Toast mToast = null;
	private HttpService http = null;
	private String msg = null;
	private static ProgressDialog dialog = null;
	private SendHTTPData mSendHTTPData = null;
	public String nickName;
	public String guestId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_guest_login);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		//android.os.Debug.waitForDebugger();
		super.onConfigurationChanged(newConfig);

	}
	
	public void btnClick(View v) {
		// 로그인
		EditText user_name = (EditText) this.findViewById(R.id.chatName);

		nickName = user_name.getText().toString().trim();

		if (nickName.equals("")) {
			makeToastMsg("대화명을 입력하세요.");
		} else {
			// makeToastMsg("회원가입 진행.");
			hideSoftInputWindow(v);

			if (dialog == null) {
				dialog = ProgressDialog.show(this, "", "처리중입니다...", false);
				// dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

				mSendHTTPData = new SendHTTPData();
				mSendHTTPData.execute(nickName);
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
			// http.setDns("주소");
			// http.setPort("8080");
			http.setUrlString("/member/guest_register");

			List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(
					4);
			nameValuePairs.add(new BasicNameValuePair("nickname", data[0]));

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
							nickName = status.getString("nickname");
							guestId = status.getString("id");
							joinSuccess();
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

	private void joinSuccess() {
		// DB에 로그인 정보 입력
		DBManager mDbManager = DBManager.getInstance(this);

		ContentValues addRowValue = new ContentValues();
		addRowValue.put("id", guestId);
		addRowValue.put("nickname", nickName);
		addRowValue.put("type", "1");

		mDbManager.insertMember(addRowValue);

		ChatApplication app = (ChatApplication) getApplicationContext();
		app.setId(guestId);
		app.setName(nickName);
		app.setFirst(true);
		app.setIsGuest(true);
		
		//서비스 시작
		Intent i = new Intent(getApplicationContext(), MQTTService.class);
		getApplicationContext().startService(i);

		finish();
		
		Intent intent = new Intent(this, ListActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		finish();
		// overridePendingTransition(0, 0);
	}

	public void goBack(View v){
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
