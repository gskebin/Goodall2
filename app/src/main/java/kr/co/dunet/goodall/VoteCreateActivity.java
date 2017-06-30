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
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class VoteCreateActivity extends Activity {
	private static Toast mToast = null;
	private HttpService http = null;
	private String msg = null;
	private static ProgressDialog dialog = null;
	private SendHTTPData mSendHTTPData = null;
	public View listContent = null;
	public LinearLayout list = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_vote_create);
		
		try {
			list = (LinearLayout) this.findViewById(R.id.createAnswerList);
			
			listContent = getLayoutInflater().inflate(R.layout.vote_create_answer_item, null);
			list.addView(listContent);
			
			listContent = getLayoutInflater().inflate(R.layout.vote_create_answer_item, null);
			list.addView(listContent);
		} catch (Exception e) {
			e.printStackTrace();
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
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// android.os.Debug.waitForDebugger();
		super.onConfigurationChanged(newConfig);
		
	}
	
	public void addVoteDone(View v) {
		System.out.println("addVote");
		
		ChatApplication app = (ChatApplication) getApplicationContext();
		String roomCode = app.getChatRoom();
		String myId = app.getId();
		
		EditText title = (EditText) this.findViewById(R.id.subjectEdit);
		
		List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(
				10);
		nameValuePairs.add(new BasicNameValuePair("id", myId));
		nameValuePairs.add(new BasicNameValuePair("room_code", roomCode));
		nameValuePairs.add(new BasicNameValuePair("title", title.getText()
				.toString()));
		
		int item_cnt = list.getChildCount();
		for (int i = 0; i < item_cnt; i++) {
			EditText v_data = (EditText) list.getChildAt(i).findViewById(
					R.id.answer_create_content);
			
			System.out.println(v_data.getText().toString());
			
			nameValuePairs.add(new BasicNameValuePair("v_data[]", v_data
					.getText().toString()));
		}
		
		hideSoftInputWindow(v);
		
		if (dialog == null) {
			dialog = ProgressDialog.show(this, "", "처리중입니다...", false);
			// dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			
			mSendHTTPData = new SendHTTPData();
			mSendHTTPData.execute(nameValuePairs);
			dialog.show();
		}
	}
	
	private class SendHTTPData extends AsyncTask<Object, Integer, Boolean> {
		
		// AsyncTask 시작전
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		// AsyncTask 시작
		@Override
		protected Boolean doInBackground(Object... params) {
			http = HttpService.getInstance();
			http.setUrlString("/vote/register");
			
			@SuppressWarnings("unchecked")
			List<BasicNameValuePair> nameValuePairs = (List<BasicNameValuePair>) params[0];
			
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
				
				// System.out.println(json);
				
				if (json != null) {
					try {
						status = new JSONObject(json);
						String state = status.getString("result");
						
						msg = status.getString("msg");
						
						if (state.equals("0000")) {
							// 성공
							finish();
						}
						// Log.d("HTTP", state);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					finish();
					//msg = "데이터 수신 실패";
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
	
	public void addAnswer(View v) {
		if (list.getChildCount() > 4) {
			makeToastMsg("선택문항은 5개까지 가능합니다.");
			return;
		}
		try {
			listContent = getLayoutInflater().inflate(R.layout.vote_create_answer_item, null);
			list.addView(listContent);
			listContent.requestFocus();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void removeList(View v) {
		if (list.getChildCount() < 3) {
			makeToastMsg("선택문항은 최소 2개입니다.");
			return;
		}
		list.removeView((ViewGroup)v.getParent());
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
