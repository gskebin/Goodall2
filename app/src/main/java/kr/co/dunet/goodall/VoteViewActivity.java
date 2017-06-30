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
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class VoteViewActivity extends Activity {
	private static Toast mToast = null;
	private ListView listAnswer = null;
	private VoteAnswerAdapter voteAnswerAdapter = null;
	private HttpService http = null;
	private static ProgressDialog dialog = null;
	private SendHTTPData mSendHTTPData = null;
	private String roomCode = "";
	private String id = "";
	public View mHeaderView = null;
	public View mFooterView = null;
	public String vote_code = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_vote_view);
		vote_code = getIntent().getStringExtra("VOTECODE");
		System.out.println("VoteViewActivity::VOTECODE=" + vote_code);
		
		ChatApplication app = (ChatApplication) getApplicationContext();
		
		if(app.getRoomAdmin()) {
			findViewById(R.id.goVoteResult).setVisibility(View.VISIBLE);
		}

		if (dialog == null) {
			dialog = ProgressDialog.show(this, "", "처리중입니다...", false);
			// dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

			mSendHTTPData = new SendHTTPData();
			mSendHTTPData.execute("list", vote_code);
			dialog.show();
		}

		/*
		 * TextView voteName = (TextView) this.findViewById(R.id.vote_name);
		 * TextView voterCnt = (TextView)
		 * this.findViewById(R.id.vote_voter_cnt); TextView totalCnt =
		 * (TextView) this.findViewById(R.id.vote_total_cnt); TextView
		 * registDate = (TextView) this.findViewById(R.id.vote_regist_date);
		 * 
		 * voteName.setText("투표 이름"); voterCnt.setText("5");
		 * totalCnt.setText("16"); registDate.setText("2015.10.19");
		 */

		try {
			listAnswer = (ListView) this.findViewById(R.id.listVoteAnswer);
			mHeaderView = getLayoutInflater().inflate(R.layout.vote_answer_item_header, null);
			listAnswer.addHeaderView(mHeaderView);
			mFooterView = getLayoutInflater().inflate(R.layout.vote_answer_item_footer, null);
			listAnswer.addFooterView(mFooterView);

			voteAnswerAdapter = new VoteAnswerAdapter(this);
			listAnswer.setAdapter(voteAnswerAdapter);
			
			// voteAnswerAdapter.addItem("1번 보기", false, 0);
			// voteAnswerAdapter.addItem("2번 보기", true, 0);
			// voteAnswerAdapter.addItem("3번 보기", false, 0);
			// voteAnswerAdapter.addItem("4번 보기", false, 0);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/*
		listAnswer.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				int cnt = parent.getCount();
        		
        		for(int i=0; i<cnt; i++) {
        			if(position == i) {
        				voteAnswerAdapter.mVoteAnswer.get(i).answerChecked = true;
        			} else {
        				voteAnswerAdapter.mVoteAnswer.get(i).answerChecked = false;
        			}
        		}
        		
        		voteAnswerAdapter.notifyDataSetChanged();
        	}
		});
		*/
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// android.os.Debug.waitForDebugger();
		super.onConfigurationChanged(newConfig);

	}
	
	public void showVote(View v) {
		//makeToastMsg("투표완료");
		Intent intent = new Intent(this, VoteResultActivity.class);
		intent.putExtra("VOTECODE", vote_code);
		startActivity(intent);
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
			
			if(data[0] == "list") {
				http.setUrlString("/vote/vote_answer");
				nameValuePairs.add(new BasicNameValuePair("rid", data[1]));
			} else if(data[0] == "select") {
				http.setUrlString("/vote/vote_answer_select");
				nameValuePairs.add(new BasicNameValuePair("rid", data[1]));
				nameValuePairs.add(new BasicNameValuePair("id", data[2]));
			}

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
							System.out.println("호출");
							if(type.equals("list")) {
								JSONArray list = (JSONArray) status.get("list");
	
								createAnswer(status.getString("title"),
										status.getString("answer_cnt"),
										status.getString("total_cnt"),
										status.getString("reg_date"), list);
							} else if(type.equals("answer")) {
								makeToastMsg(status.getString("msg"));
								finish();
							}
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

				voteAnswerAdapter.addItem(jObject.getString("rid"),
						jObject.getString("title"), false, 0);
				voteAnswerAdapter.notifyDataSetChanged();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void btnAnswerClick(View v) {
		int cnt = voteAnswerAdapter.getCount();

		VoteAnswer ans = null;
		String num = null;
		for (int i = 0; i < cnt; i++) {
			ans = voteAnswerAdapter.getItem(i);
			//System.out.println(ans);
			if(ans.answerChecked == true) {
				num = ans.answerCode;
				//System.out.println(ans.answerCode + "|" + ans.answerContent);
			}
		}
		
		if(num == "" || num == null) {
			makeToastMsg("선택해주세요.");
			return;
		} else {
			ChatApplication app = (ChatApplication) getApplicationContext();
			String myId = app.getId();
			if (dialog == null) {
				dialog = ProgressDialog.show(this, "", "처리중입니다...", false);

				mSendHTTPData = new SendHTTPData();
				mSendHTTPData.execute("select", num, myId);
				dialog.show();
			}
		}
	}
}
