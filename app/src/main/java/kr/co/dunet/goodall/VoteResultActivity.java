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
import android.widget.TextView;
import android.widget.Toast;

public class VoteResultActivity extends Activity {
	private static Toast mToast = null;
	private ListView listAnswer = null;
	private VoteResultAdapter voteResultAdapter = null;
	private HttpService http = null;
	private static ProgressDialog dialog = null;
	private SendHTTPData mSendHTTPData = null;
	public View mHeaderView = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_vote_result);
		String vote_code = getIntent().getStringExtra("VOTECODE");
		System.out.println("VoteViewActivity::VOTECODE=" + vote_code);

		if (dialog == null) {
			dialog = ProgressDialog.show(this, "", "처리중입니다...", false);
			// dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

			mSendHTTPData = new SendHTTPData();
			mSendHTTPData.execute(vote_code);
			dialog.show();
		}

		try {
			listAnswer = (ListView) this.findViewById(R.id.listVoteAnswer);
			
			mHeaderView = getLayoutInflater().inflate(R.layout.vote_answer_item_header, null);
			listAnswer.addHeaderView(mHeaderView);

			voteResultAdapter = new VoteResultAdapter(this);
			listAnswer.setAdapter(voteResultAdapter);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// android.os.Debug.waitForDebugger();
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

			List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(
					1);

			http.setUrlString("/vote/vote_result");
			nameValuePairs.add(new BasicNameValuePair("rid", data[0]));

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
				// Log.d("JOIN", json);

				if (json != null) {
					Log.d("HTTP", json);
					try {
						status = new JSONObject(json);
						String state = status.getString("result");
						String type = status.getString("type");

						if (state.equals("0000")) {
							JSONArray list = (JSONArray) status.get("list");

							createAnswer(status.getString("title"),
									status.getString("answer_cnt"),
									status.getString("total_cnt"),
									status.getString("reg_date"), list);
						} else {
							makeToastMsg(status.getString("msg"));
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}

			super.onPostExecute(result);
		}
	}

	public void createAnswer(String title, String answer_cnt, String total_cnt,
			String reg_date, JSONArray list) {

		TextView voteName = (TextView) mHeaderView.findViewById(R.id.vote_name);
		TextView voterCnt = (TextView) mHeaderView.findViewById(R.id.vote_voter_cnt);
		TextView totalCnt = (TextView) mHeaderView.findViewById(R.id.vote_total_cnt);
		TextView registDate = (TextView) mHeaderView
				.findViewById(R.id.vote_regist_date);

		voteName.setText(title);
		voterCnt.setText(answer_cnt);
		totalCnt.setText(total_cnt);
		registDate.setText(reg_date);

		System.out.println("리스트수 " + list.length());
		for (int i = 0; i < list.length(); i++) {
			try {
				JSONObject jObject = list.getJSONObject(i);

				voteResultAdapter.addItem(jObject.getString("rid"),
						jObject.getString("title"), jObject.getString("c_avg"),
						jObject.getString("cnt"), answer_cnt);
				voteResultAdapter.notifyDataSetChanged();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
