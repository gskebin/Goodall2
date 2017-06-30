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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class VoteListActivity extends Activity {
	private static Toast mToast = null;
	private ListView listVote = null;
	private VoteListAdapter voteListAdapter = null;
	private HttpService http = null;
	private static ProgressDialog dialog = null;
	private SendHTTPData mSendHTTPData = null;
	private String roomCode = "";
	private String id = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_vote_list);
		
		/*
		ActionBar mActionBer = getActionBar();
		mActionBer.setDisplayShowHomeEnabled(false);
		mActionBer.setTitle(" 투표");
		mActionBer.setDisplayHomeAsUpEnabled(true);
		*/
		
		roomCode = getIntent().getStringExtra("ROOMCODE");
		
		ChatApplication app = (ChatApplication) getApplicationContext();
		id = app.getId() == null ? "" : app.getId();
		Boolean roomAdmin = app.getRoomAdmin();
		
		if(roomAdmin != true) {
			findViewById(R.id.btnAddVote).setVisibility(View.GONE);
			findViewById(R.id.btnTemp).setVisibility(View.VISIBLE);
		}
		
		if (dialog == null) {
			dialog = ProgressDialog.show(this, "", "처리중입니다...", false);
			// dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

			mSendHTTPData = new SendHTTPData();
			mSendHTTPData.execute(roomCode, id);
			dialog.show();
		}
		
		try{
			listVote = (ListView) this.findViewById(R.id.listVote);

			voteListAdapter = new VoteListAdapter(this);
			
			listVote.setAdapter(voteListAdapter);
			listVote.setActivated(true);
			
			/*
			voteListAdapter.addItem("12345", "건대앱 투표 테스트", "10", "15", "2015.05.10","Y");
			voteListAdapter.addItem("12346", "건대앱 투표 테스트2", "5", "15", "2015.05.10","Y");
			voteListAdapter.addItem("12347", "건대앱 투표 테스트3", "2", "15", "2015.05.10","Y");
			voteListAdapter.addItem("12345", "건대앱 투표 테스트", "10", "15", "2015.05.10","Y");
			voteListAdapter.addItem("12346", "건대앱 투표 테스트2", "5", "15", "2015.05.10","Y");
			voteListAdapter.addItem("12347", "건대앱 투표 테스트3", "2", "15", "2015.05.10","Y");
			voteListAdapter.addItem("12345", "건대앱 투표 테스트", "10", "15", "2015.05.10","Y");
			voteListAdapter.addItem("12346", "건대앱 투표 테스트2", "5", "15", "2015.05.10","Y");
			voteListAdapter.addItem("12347", "건대앱 투표 테스트3", "2", "15", "2015.05.10","Y");
			voteListAdapter.addItem("12345", "건대앱 투표 테스트", "10", "15", "2015.05.10","Y");
			voteListAdapter.addItem("12346", "건대앱 투표 테스트2", "5", "15", "2015.05.10","Y");
			voteListAdapter.addItem("12347", "건대앱 투표 테스트3", "2", "15", "2015.05.10","Y");
			voteListAdapter.addItem("12345", "건대앱 투표 테스트", "10", "15", "2015.05.10","Y");
			voteListAdapter.addItem("12346", "건대앱 투표 테스트2", "5", "15", "2015.05.10","N");
			voteListAdapter.addItem("12347", "건대앱 투표 테스트3", "2", "15", "2015.05.10","N");
			voteListAdapter.addItem("12345", "건대앱 투표 테스트", "10", "15", "2015.05.10","N");
			voteListAdapter.addItem("12346", "건대앱 투표 테스트2", "5", "15", "2015.05.10","N");
			voteListAdapter.addItem("12347", "건대앱 투표 테스트3", "2", "15", "2015.05.10","N");
			*/
			
			listVote.setOnItemClickListener(new OnItemClickListener() {
	            @Override
	            public void onItemClick(AdapterView<?> parent, View v, int position, long id){
	                VoteData mData = voteListAdapter.mVoteData.get(position);
	                if(mData.voteYn == "Y") {
	                	showVote(mData.voteCode);
	                } else {
	                	enterVote(mData.voteCode);
	                }
	            }
	        });
			
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		//android.os.Debug.waitForDebugger();
		super.onConfigurationChanged(newConfig);

	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		System.out.println("onResume");
		if (dialog == null) {
			dialog = ProgressDialog.show(this, "", "처리중입니다...", false);
			// dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

			mSendHTTPData = new SendHTTPData();
			mSendHTTPData.execute(roomCode, id);
			dialog.show();
		}
		try{
			listVote = (ListView) this.findViewById(R.id.listVote);

			voteListAdapter = new VoteListAdapter(this);
			
			listVote.setAdapter(voteListAdapter);
			listVote.setActivated(true);
			
			listVote.setOnItemClickListener(new OnItemClickListener() {
	            @Override
	            public void onItemClick(AdapterView<?> parent, View v, int position, long id){
	                VoteData mData = voteListAdapter.mVoteData.get(position);
	                //System.out.println(mData.voteYn);
	                if(mData.voteYn.equals("Y")) {
	                	showVote(mData.voteCode);
	                } else if(mData.voteYn.equals("N")) {
	                	enterVote(mData.voteCode);
	                }
	            }
	        });
			
		} catch (Exception e){
			e.printStackTrace();
		}
		
		super.onResume();
	}

	/**
	 * 투표하기
	 * @param vote_code
	 */
	public void enterVote(String vote_code){
		Intent intent = new Intent(this, VoteViewActivity.class);
		intent.putExtra("VOTECODE", vote_code);
		startActivity(intent);
	}
	
	/**
	 * 투표 결과보기
	 * 현재 투표결과보는 페이지가 없음
	 * @param vote_code
	 */
	public void showVote(String vote_code) {
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
	
	public void addVote(View v){
		Intent intent = new Intent(this, VoteCreateActivity.class);
		startActivity(intent);
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
			http.setUrlString("/vote/vote_list");

			List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(
					1);
			nameValuePairs.add(new BasicNameValuePair("code", data[0]));
			nameValuePairs.add(new BasicNameValuePair("id", data[1]));

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
				//Log.d("JOIN", json);

				if (json != null) {
					Log.d("HTTP", json);
					try {
						status = new JSONObject(json);
						String state = status.getString("result");
						
						if (state.equals("0000")) {
							JSONArray list = (JSONArray) status.get("list");
							
							System.out.println("리스트수 " + list.length());
							if(list.length() > 0) {
								for (int i =0; i<list.length(); i++) {
									JSONObject jObject = list.getJSONObject(i);
									
									voteListAdapter.addItem(
											jObject.getString("rid"),
											jObject.getString("title"),
											jObject.getString("vote_voter_cnt"),
											jObject.getString("vote_total_cnt"),
											jObject.getString("regdate"),
											jObject.getString("yn")
											);
								}
							} else {
								voteListAdapter.addItem(
										"",
										"등록된 투표가 없습니다",
										"",
										"",
										"",
										""
										);
							}
							voteListAdapter.notifyDataSetChanged();
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}

			super.onPostExecute(result);
		}
	}
}
