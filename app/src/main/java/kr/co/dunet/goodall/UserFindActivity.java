package kr.co.dunet.goodall;

import java.util.ArrayList;
import java.util.List;

import kr.co.dunet.app.goodall.R;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class UserFindActivity extends Activity {
	private static Toast mToast = null;
	private HttpService http = null;
	private String msg = null;
	private static ProgressDialog dialog = null;
	private SendHTTPData mSendHTTPData = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_user_find);

		/*
		ActionBar mActionBer = getActionBar();
		mActionBer.setDisplayShowHomeEnabled(false);
		mActionBer.setTitle("계정찾기");
		mActionBer.setDisplayHomeAsUpEnabled(true);
		*/
	}

	public void btnClick(View v) {
		hideSoftInputWindow(v);

		EditText emailInput = (EditText) findViewById(R.id.chatEmail);
		String email = emailInput.getText().toString().trim();

		if (email == null || email.equals("")) {
			makeToastMsg("이메일을 입력하세요");
			return;
		}

		System.out.println("EMAIL : " + email);

		if (dialog == null) {
			dialog = ProgressDialog.show(this, "", "처리중입니다...", false);
			// dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

			mSendHTTPData = new SendHTTPData();
			mSendHTTPData.execute(email);
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
			http.setUrlString("/member/find_id_password");

			ChatApplication app = (ChatApplication) getApplicationContext();

			List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(
					4);
			nameValuePairs.add(new BasicNameValuePair("email", data[0]));

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
							//TextView matchId = (TextView) findViewById(R.id.chatMatchingId);
							//matchId.setText("입력하신 이메일로\n아이디와 임시비밀번호가 전송되었습니다.");
							//makeToastMsgLong("입력하신 이메일로\n아이디와 임시비밀번호가 전송되었습니다.");
							//finish();
							findSuccess();
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
	
	public void findSuccess() {
		LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
		View dialoglayout = inflater.inflate(R.layout.alert_msg, null);
		
		TextView alert_title = (TextView) dialoglayout
				.findViewById(R.id.alert_title);
		TextView alert_msg = (TextView) dialoglayout
				.findViewById(R.id.alert_msg);
		TextView alert_submit = (TextView) dialoglayout
				.findViewById(R.id.alert_submit);
		alert_title.setText("계정찾기");
		
		String alert_msg_text = "입력하신 이메일로\n아이디와 임시비밀번호가 전송되었습니다.";
		alert_msg.setText(alert_msg_text);
		
		final Dialog alertDialog = new Dialog(this, R.style.CustomDialog);
		
		alert_submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.dismiss();
				finish();
			}
		});
		
		alertDialog.setContentView(dialoglayout);
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.show();
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
		mToast = null;
	}
	
	public void makeToastMsgLong(String msg) {
		if (mToast == null) {
			mToast = Toast.makeText(getApplicationContext(), msg,
					Toast.LENGTH_LONG);
		} else {
			mToast.setText(msg);
		}
		mToast.show();
		mToast = null;
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
