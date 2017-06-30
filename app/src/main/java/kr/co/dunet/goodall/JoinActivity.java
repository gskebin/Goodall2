package kr.co.dunet.goodall;

import java.util.ArrayList;
import java.util.List;
import kr.co.dunet.app.goodall.R;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class JoinActivity extends Activity {
	private static Toast mToast = null;
	private HttpService http = null;
	private String msg = null;
	private static ProgressDialog dialog = null;
	private SendHTTPData mSendHTTPData = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_join);

		/*
		ActionBar mActionBer = getActionBar();
		mActionBer.setDisplayShowHomeEnabled(false);
		mActionBer.setTitle("회원가입");
		mActionBer.setDisplayHomeAsUpEnabled(true);
		*/
		// mActionBer.setBackgroundDrawable(new ColorDrawable(0xFF2C76A3));

		EditText password = (EditText) findViewById(R.id.chatPwd);
		password.setTypeface(Typeface.DEFAULT);

		EditText password_confirm = (EditText) findViewById(R.id.chatPwdConfirm);
		password_confirm.setTypeface(Typeface.DEFAULT);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		//android.os.Debug.waitForDebugger();
		super.onConfigurationChanged(newConfig);

	}
	
	public void btnClick(View v) {
		// 로그인
		EditText user_pwd = (EditText) this.findViewById(R.id.chatPwd);
		EditText user_pwd_confirm = (EditText) this
				.findViewById(R.id.chatPwdConfirm);
		EditText user_name = (EditText) this.findViewById(R.id.chatName);
		EditText user_email = (EditText) this.findViewById(R.id.chatEmail);

		String password = user_pwd.getText().toString().trim();
		String name = user_name.getText().toString().trim();
		String email = user_email.getText().toString().trim();

		if (email.equals("")) {
			makeToastMsg("이메일을 입력하세요.");
		} else if (password.equals("")) {
			makeToastMsg("비밀번호를 입력하세요.");
		} else if (user_pwd_confirm.getText().toString().trim().equals("")) {
			makeToastMsg("비밀번호 확인을 입력하세요.");
		} else if (!user_pwd_confirm.getText().toString().trim()
				.equals(password)) {
			makeToastMsg("비밀번호가 틀립니다.");
		} else if (name.equals("")) {
			makeToastMsg("대화명을 입력하세요.");
		} else {
			// makeToastMsg("회원가입 진행.");
			hideSoftInputWindow(v);

			if (dialog == null) {
				dialog = ProgressDialog.show(this, "", "처리중입니다...", false);
				// dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

				mSendHTTPData = new SendHTTPData();
				mSendHTTPData.execute(email, password, name);
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
			http.setUrlString("/member/register");

			List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(
					4);
			nameValuePairs.add(new BasicNameValuePair("email", data[0]));
			nameValuePairs.add(new BasicNameValuePair("password", data[1]));
			nameValuePairs.add(new BasicNameValuePair("nickname", data[2]));

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

						msg = status.getString("msg");

						if (state.equals("0000")) {
							joinSuccess();
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
		Log.d("JOIN", "SUCCESS");
		Intent intent = new Intent(this, FromActivity.class);
		startActivity(intent);
		// overridePendingTransition(0, 0);
		finish();
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
