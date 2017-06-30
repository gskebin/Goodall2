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

public class StatisticsActivity extends Activity {
	private static Toast mToast = null;
	private ListView listStat = null;
	private StatListAdapter statAdapter = null;
	private HttpService http = null;
	private static ProgressDialog dialog = null;
	private SendHTTPData mSendHTTPData = null;
	private String roomCode = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_stat);

		/*
		ActionBar mActionBer = getActionBar();
		mActionBer.setDisplayShowHomeEnabled(false);
		mActionBer.setTitle(" 통계");
		mActionBer.setDisplayHomeAsUpEnabled(true);
		*/
		
		roomCode = getIntent().getStringExtra("ROOMCODE");
		
		if (dialog == null) {
			dialog = ProgressDialog.show(this, "", "처리중입니다...", false);
			// dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

			mSendHTTPData = new SendHTTPData();
			mSendHTTPData.execute(roomCode);
			dialog.show();
		}

		try {
			listStat = (ListView) this.findViewById(R.id.listStat);
			
			statAdapter = new StatListAdapter(this);
			listStat.setAdapter(statAdapter);
			
			/*
			statAdapter.addItem("a", "1", "5", "9", "3");
			statAdapter.addItem("b", "2", "6", "0", "4");
			statAdapter.addItem("c", "3", "7", "1", "5");
			statAdapter.addItem("d", "4", "8", "2", "6");
			statAdapter.addItem("b", "2", "6", "0", "4");
			statAdapter.addItem("c", "3", "7", "1", "5");
			statAdapter.addItem("d", "4", "8", "2", "6");
			statAdapter.addItem("b", "2", "6", "0", "4");
			statAdapter.addItem("c", "3", "7", "1", "5");
			statAdapter.addItem("d", "4", "8", "2", "6");
			statAdapter.addItem("b", "2", "6", "0", "4");
			statAdapter.addItem("c", "3", "7", "1", "5");
			statAdapter.addItem("d", "4", "8", "2", "6");
			statAdapter.addItem("b", "2", "6", "0", "4");
			statAdapter.addItem("c", "3", "7", "1", "5");
			statAdapter.addItem("d", "4", "8", "2", "6");
			statAdapter.addItem("b", "2", "6", "0", "4");
			statAdapter.addItem("c", "3", "7", "1", "5");
			statAdapter.addItem("d", "4", "8", "2", "6");
			statAdapter.addItem("b", "2", "6", "0", "4");
			statAdapter.addItem("c", "3", "7", "1", "5");
			statAdapter.addItem("d", "4", "8", "2", "6");
			statAdapter.addItem("b", "2", "6", "0", "4");
			statAdapter.addItem("c", "3", "7", "1", "5");
			statAdapter.addItem("d", "4", "8", "2", "6");
			*/
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		//android.os.Debug.waitForDebugger();
		super.onConfigurationChanged(newConfig);

	}
	
	/*
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) { 

	    switch (item.getItemId()) { 
	       case android.R.id.home: 
	       //onBackPressed(); 
	       return true; 
	    } 

	    return super.onOptionsItemSelected(item); 
	 }
	 */ 


	@Override
	public void onBackPressed() {
		finish();
		// overridePendingTransition(0, 0);
	}
	
	public void goBack(View v){
		finish();
	}

    public void orderByMsg(View v){
    	System.out.println("ORDER BY MESSAGE CNT");
    	try{
			statAdapter = new StatListAdapter(this);
			listStat.setAdapter(statAdapter);
			
			/*
			statAdapter.addItem("d", "11", "5", "9", "3");
			statAdapter.addItem("c", "2", "6", "0", "4");
			statAdapter.addItem("e", "3", "7", "1", "5");
			statAdapter.addItem("f", "4", "8", "2", "6");
			*/
    	}catch(Exception e){
			e.printStackTrace();
    	}
    }
    
    public void orderByLike(View v){
    	System.out.println("ORDER BY LIKE CNT");
    	try{
			statAdapter = new StatListAdapter(this);
			listStat.setAdapter(statAdapter);
			
			/*
			statAdapter.addItem("asdf", "22", "55", "9", "3");
			statAdapter.addItem("bvcd", "44", "6", "0", "4");
			statAdapter.addItem("e", "3", "7", "1", "5");
			statAdapter.addItem("f", "4", "8", "2", "6");
			*/
    	}catch(Exception e){
			e.printStackTrace();
    	}
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
			http.setUrlString("/chat/stat");

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
				//Log.d("JOIN", json);

				if (json != null) {
					Log.d("HTTP", json);
					try {
						status = new JSONObject(json);
						String state = status.getString("result");
						
						if (state.equals("0000")) {
							// msg = "로그인 성공";
							JSONArray msg = (JSONArray) status.get("msg");
							
							
							System.out.println("메세지수 " + msg.length());
							for (int i =0 ; i<msg.length();i++) {
								JSONObject jObject = msg.getJSONObject(i);
								
								statAdapter.addItem(
										jObject.getString("nickname"),
										jObject.getString("message"),
										jObject.getString("msg_per"),
										jObject.getString("like"),
										jObject.getString("like_per"));
								
								statAdapter.notifyDataSetChanged();
							}

						} else {
							//msg = status.getString("msg");
						}
						// Log.d("HTTP", state);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					//msg = "데이터 수신 실패";
				}
			} else {
				// 실패
			}

			super.onPostExecute(result);
		}
	}
}
