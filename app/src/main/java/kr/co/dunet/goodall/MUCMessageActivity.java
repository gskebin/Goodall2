package kr.co.dunet.goodall;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kr.co.dunet.app.goodall.R;

public class MUCMessageActivity extends Activity {
	
	public ArrayList<Chats> mData = null;
	public ArrayList<String> mNData = null;
	public ArrayList<UserInfo> newProfileIds = null;
	
	private EditText textMessage;
	private ListView listview;
	private View fileView;
	private Button addBtn;
	private ArrayAdapterEx mAdapter;
	public Thread thread = null;
	
	private String roomName = "";
	private String roomCode = "";
	private String roomAdmin = "";
	private String myId = "";
	private String myName = "";
	private String isGuest;
	
	private SendHTTPData mSendHTTPData = null;
	private HttpService http = null;
	private DBManager mDbManager = null;
	
	private String lastRid = null;
	private Boolean isEnd = false;
	public Boolean lastitemVisibleFlag = false;
	public Boolean isMessageProcess = true;
	Bitmap bm = null;
	Bitmap resized = null;
	
	private Uri mImageCaptureUri;
	private static final int PICK_FROM_CAMERA = 0;
	private static final int PICK_FROM_ALBUM = 1;
	
	public String searchText = "";
	public String searchIdx = "";
	public Integer search_pos = null;
	
	private static Toast mToast = null;
	
	private static MUCMessageActivity mMUCMessageActivity = null;
	
	private static Dialog alertDialog = null;
	
	public String HOST = "";
	
	public MUCMessageActivity() {
		mMUCMessageActivity = this;
	}
	
	public static MUCMessageActivity Instance() {
		return mMUCMessageActivity;
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		commonStart();
	}
	
	public void sendRecomm(String rid) {
		mAdapter.notifyDataSetChanged();
		mSendHTTPData = new SendHTTPData();
		mSendHTTPData.execute("recomm", myId, myName, rid, roomCode, roomName);
	}
	
	public void sendDelete(String rid, int pos) {
		mNData.remove(pos);
		mAdapter.notifyDataSetChanged();
		mSendHTTPData = new SendHTTPData();
		mSendHTTPData.execute("msgdel", myId, myName, rid, roomCode, roomName);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}
	
	public void commonStart() {
		HOST = NetworkConfig.Instance().getWebHost();
		
		mNData = new ArrayList<String>();
		newProfileIds = new ArrayList<UserInfo>();
		
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		// 등록된 notification 을 제거 한다.
		nm.cancel(333);
		
		Notify.setMessageID(1);
		
		Intent serviceIntent = new Intent(this, MQTTService.class);
		bindService(serviceIntent, connection, BIND_AUTO_CREATE);
		
		roomCode = getIntent().getStringExtra("ROOMCODE");
		
		ChatApplication app = (ChatApplication) getApplicationContext();
		app.setChatRoom(roomCode);
		
		mDbManager = DBManager.getInstance(this);
		
		ContentValues updateRowValue = new ContentValues();
		updateRowValue.put("no_read", 0);
		
		mDbManager.updateRoom(updateRowValue, "room_code='" + roomCode + "'",
				null);
		
		String where = "room_code='" + roomCode + "'";
		String[] columns = new String[] { "room_code", "room_name", "admin",
				"room_password" };
		Cursor c = mDbManager
				.selectRoom(columns, where, null, null, null, null);
		
		String roomPassword = "";
		
		if (c != null && c.getCount() > 0) {
			while (c.moveToNext()) {
				roomName = c.getString(1);
				roomAdmin = c.getString(2);
				roomPassword = c.getString(3);
				break;
			}
		}
		// System.out.println(roomName + " | " + roomAdmin + " | " +
		// roomPassword + " | " + c.getCount());
		c.close();
		
		TextView topTitle = (TextView) this.findViewById(R.id.topTitle);
		TextView topCode = (TextView) this.findViewById(R.id.topCode);
		TextView adminNickname = (TextView) this
				.findViewById(R.id.adminNickname);
		topTitle.setText(roomName);
		
		/*
		 * ActionBar mActionBer = getActionBar();
		 * mActionBer.setDisplayShowHomeEnabled(false);
		 * mActionBer.setTitle(roomName + "["+roomCode+"]");
		 * mActionBer.setDisplayHomeAsUpEnabled(true);
		 */
		// mActionBer.setBackgroundDrawable(new ColorDrawable(0xFF2C76A3));
		
		columns = new String[] { "id", "nickname", "type" };
		c = mDbManager.selectMember(columns, null, null, null, null, null);
		
		if (c != null && c.getCount() > 0) {
			while (c.moveToNext()) {
				myId = c.getString(0);
				myName = c.getString(1);
				isGuest = c.getString(2);
				break;
			}
			
			if (isGuest != null && isGuest.equals("1")) {
				app.setIsGuest(true);
			}
		}
		c.close();
		
		if (roomAdmin.equals(myId)) {
			System.out.println("room admin : " + roomAdmin + " | " + myId);
			
			clearProfileCache(myId, myName);
			app.setRoomAdmin(true);
			if (roomPassword != null && !roomPassword.equals("")) {
				topCode.setText("/" + roomCode + " [비밀번호 : " + roomPassword
						+ "]");
			} else {
				topCode.setText("/" + roomCode);
			}
			adminNickname.setText(myName);
			
			findViewById(R.id.btnStats).setVisibility(View.VISIBLE);
		} else {
			System.out
					.println("room admin guest : " + roomAdmin + " | " + myId);
			
			String adminName = "";
			mDbManager = DBManager.getInstance(this);
			where = "id='" + roomAdmin + "'";
			columns = new String[] { "nickname" };
			c = mDbManager.selectUser(columns, where, null, null, null, null);
			
			if (c != null && c.getCount() > 0) {
				while (c.moveToNext()) {
					adminName = c.getString(0);
					break;
				}
			}
			c.close();
			
			clearProfileCache(roomAdmin, adminName);
			app.setRoomAdmin(false);
			topCode.setText("/" + roomCode);
			adminNickname.setText(adminName);
		}
		
		textMessage = (EditText) this.findViewById(R.id.chatET);
		listview = (ListView) this.findViewById(R.id.listMessages);
		fileView = (View) this.findViewById(R.id.addFileSection);
		addBtn = (Button) this.findViewById(R.id.plusBtn);
		
		textMessage.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				fileView.setVisibility(View.GONE);
				addBtn.setBackgroundResource(R.drawable.btn_chat_plus);
			}
		});
		
		textMessage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				fileView.setVisibility(View.GONE);
				addBtn.setBackgroundResource(R.drawable.btn_chat_plus);
			}
		});
		
		mData = new ArrayList<Chats>();
		mAdapter = new ArrayAdapterEx(this, R.layout.listitem,
				R.id.send_message_text, mData);
		
		listview.setAdapter(mAdapter);
		
		// Set a listener to send a chat text message
		Button send = (Button) this.findViewById(R.id.sendBtn);
		send.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String text = textMessage.getText().toString().trim();
				if (text == null || text.equals(""))
					return;
				
				Chats chat = new Chats();
				chat.mName = myName;
				chat.mMessage = text;
				chat.mDate = "";
				chat.isMine = "Y";
				chat.mType = "msg";
				chat.mProfile = getProfile(myId);
				clearProfileCache(myId, myName);
				mAdapter.add(chat);
				String uniqueValue = java.util.UUID.randomUUID().toString();
				mNData.add(uniqueValue);
				
				mSendHTTPData = new SendHTTPData();
				mSendHTTPData.execute("msg", myId, myName, text, roomCode,
						roomName, uniqueValue);
				
				textMessage.setText("");
				
				lastitemVisibleFlag = true;

				listview.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
			}
		});
		
		listview.setOnScrollListener(new AbsListView.OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
						&& lastitemVisibleFlag) {
					// TODO 화면이 바닦에 닿을때 처리
					// Log.d("MUCList", "스크롤 바닥");
					//lastitemVisibleFlag = true;
					listview.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
				} else if (lastitemVisibleFlag == true) {
					// Log.d("MUCList", "스크롤 고정해제");
					// listview.setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);
					lastitemVisibleFlag = false;
				}
				// Log.d("MUCList", "스크롤상태변화");
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				
				if (firstVisibleItem + visibleItemCount == totalItemCount
						&& totalItemCount != 0) {
					if (lastitemVisibleFlag == false) {
						lastitemVisibleFlag = true;
						
						Log.d("MUCList", "최하단 스크롤 고정");
						// 스크롤 고정
						listview.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
					}
				} else {
					// Log.d("MUCList", "스크롤변화");
					if (totalItemCount - visibleItemCount > firstVisibleItem
							&& totalItemCount != 0 && lastitemVisibleFlag == true) {
						
						lastitemVisibleFlag = false;
						Log.d("MUCList", "하단 스크롤 해제 : " + totalItemCount);
						// 스크롤 고정해제
						listview.setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);
					}
				}
				// System.out.println((totalItemCount - visibleItemCount) +
				// " | "
				// + firstVisibleItem);
				
				/*
				 * Log.d("MUCList", "firstVisibleItem : " +
				 * Integer.toString(firstVisibleItem));
				 */
				// if(firstVisibleItem == 3 && view.getChildAt(3) != null &&
				// view.getChildAt(3).getTop() == 0) {
				// }
				if (firstVisibleItem <= 2 && isMessageProcess == false) {
					// Log.d("MUCList", "최상단");
					if (isEnd != true) {
						Log.d("MUCList", "리스트 추가 불러오기");
						
						// listview.setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);
						
						addMessage(firstVisibleItem);
					}
				}
			}
		});
		
		addMessage(0);
		listview.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
	}
	
	public void clearProfileCache(String id, String nickname) {
		UserInfo tmp = new UserInfo();
		tmp.mId = id;
		
		int id_idx = newProfileIds.indexOf(tmp);
		
		if (id_idx > -1) {
			// 기존에 존재하면
			// System.out.println("기존에 추가 : " + id);
			
			return;
		} else {
			// System.out.println("새로 추가 : " + id);
		}
		// System.out.println(id_idx);
		
		mDbManager = DBManager.getInstance(this);
		String where = "id='" + id + "'";
		String[] columns = new String[] { "lastdate", "nickname" };
		Cursor c = mDbManager
				.selectUser(columns, where, null, null, null, null);
		
		String last_date = "";
		
		if (c != null && c.getCount() > 0) {
			while (c.moveToNext()) {
				last_date = c.getString(0);
				tmp.mName = c.getString(1);
				break;
			}
		} else {
			// 사용자 데이터가 없으면 USER_TABLE에 추가한다.
			ContentValues addRowValue = new ContentValues();
			addRowValue.put("id", id);
			addRowValue.put("lastdate", "");
			addRowValue.put("nickname", nickname);
			
			mDbManager.insertUser(addRowValue);
			
			tmp.mName = nickname;
		}
		c.close();
		
		newProfileIds.add(tmp);
		
		System.out.println("마지막 갱신일" + last_date);
		
		// 프로필 사진이 변경된 이력이 있는지 체크하는 영역
		System.out.println("프로필 체크 " + id);
		SendProfileHTTPData mSendProfileHTTPData = new SendProfileHTTPData();
		mSendProfileHTTPData.execute("profile", id, last_date);
	}
	
	public String getChatName(String id) {
		UserInfo tmp = new UserInfo();
		tmp.mId = id;
		
		int id_idx = newProfileIds.indexOf(tmp);
		
		if (id_idx > -1) {
			// 기존에 존재하면
			return newProfileIds.get(id_idx).mName;
		} else {
			return "";
		}
	}
	
	public void changeProfile(String id, String last_date, String nickname,
			String photo) {
		// 캐시삭제
		String path = getProfile(id);
		
		// 마지막 정보갱신일 업데이트
		mDbManager = DBManager.getInstance(this);
		ContentValues updateRowValue = new ContentValues();
		updateRowValue.put("lastdate", last_date);
		updateRowValue.put("nickname", nickname);
		
		UserInfo tmp = new UserInfo();
		tmp.mId = id;
		
		int id_idx = newProfileIds.indexOf(tmp);
		
		if (id_idx > -1) {
			// 기존에 존재하면
			newProfileIds.get(id_idx).mName = nickname;
		}
		
		mDbManager.updateUser(updateRowValue, "id='" + id + "'", null);
		
		try {
			mAdapter.notifyDataSetChanged();
		} catch (Exception e) {
		}
		
		if (photo.equals("Y")) {
			// 새로 다운로드
			DownloadProfileThread dThread = new DownloadProfileThread(id, path,
					last_date);
			dThread.start();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// ActionBar 메뉴 클릭에 대한 이벤트 처리
		int id = item.getItemId();
		switch (id) {
			case android.R.id.home:
				ChatApplication app = (ChatApplication) getApplicationContext();
				app.setChatRoom("");
				
				startActivity(new Intent(this, ListActivity.class));
				
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public String makeTime(String date_time) {
		String msg_time = "";
		
		SimpleDateFormat f = null;
		
		try {
			f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
					java.util.Locale.getDefault());
			Date m_datetime = f.parse(date_time);
			
			f = new SimpleDateFormat("aa h:mm", java.util.Locale.getDefault());
			msg_time = f.format(m_datetime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return msg_time;
	}
	
	public void showMessage(String... c) {
		Chats chat = new Chats();
		
		chat.mName = c[0];
		chat.mType = c[5];
		
		if (c[5].equals("msg")) {
			chat.mMessage = c[1];
		} else if (c[5].equals("photo")) {
			chat.mMessage = c[1];
			String[] result = c[1].split(",");
			System.out.println("file_name " + result[0]);
			// for(String s : result) System.out.println("file_name " + s);
			String filename = result[1];
			
			getPhoto(filename, c[4]);
		} else if (c[5].equals("vote")) {
			chat.mMessage = "투표가 등록되었습니다.\n[" + c[1] + "]";
		}
		
		// chat.mDate = c[2].substring(11);
		chat.mDate = makeTime(c[2]);
		
		if (c[3].equals(myId)) {
			chat.isMine = "Y";
			
			// 캐시삭제
			clearProfileCache(c[3], c[0]);
		} else {
			chat.isMine = "N";
			
			// 캐시삭제
			clearProfileCache(c[3], c[0]);
		}
		
		chat.mProfile = getProfile(c[3]);
		chat.mRid = c[4];
		chat.mId = c[3];
		mNData.add(c[4]);
		mAdapter.add(chat);
		
		mAdapter.notifyDataSetChanged();
	}
	
	public void addShowMessage(String... c) {
		Chats chat = new Chats();
		
		chat.mName = c[0];
		// chat.mDate = c[2].substring(11);
		chat.mDate = makeTime(c[2]);
		chat.mType = c[6];
		
		chat.mMessage = c[1];
		if (c[6].equals("msg")) {
			chat.mMessage = c[1];
		} else if (c[6].equals("vote")) {
			chat.mMessage = "투표가 등록되었습니다.\n[" + c[1] + "]";
		}
		
		if (c[3].equals(myId)) {
			chat.isMine = "Y";
			
			// 캐시삭제
			clearProfileCache(c[3], c[0]);
		} else {
			chat.isMine = "N";
			
			// 캐시삭제
			clearProfileCache(c[3], c[0]);
		}
		chat.mProfile = getProfile(c[3]);
		chat.mRid = c[4];
		chat.mId = c[3];
		chat.mLike = c[5];
		mNData.add(0, c[4]);
		mAdapter.insert(chat, 0);
		
		chat = null;
		
		if (c[6].equals("photo")) {
			String[] result = c[1].split(",");
			System.out.println("file_name " + result[0]);
			// for(String s : result) System.out.println("file_name " + s);
			String filename = result[1];
			getPhoto(filename, c[4]);
		}
	}
	
	public void getPhoto(String filename, String idx) {
		File file = null;
		
		file = getApplicationContext().getFilesDir();
		
		String dir = file.getAbsolutePath() + "/temp/" + roomCode;
		String path = file.getAbsolutePath() + "/temp/" + roomCode + "/"
				+ filename;
		System.out.println("위치 : " + path);
		
		file = new File(dir);
		if (!file.exists()) {
			// 디렉토리가 존재하지 않으면 디렉토리 생성
			file.mkdirs();
		}
		
		file = new File(path);
		
		if (!file.exists()) {
			// 파일이 존재하지 않으면...
			DownloadThread dThread = new DownloadThread(filename, path, idx);
			dThread.start();
		} else {
			// 파일이 존재하면...
			System.out.println("파일존재 " + path);
			
			int rid = mNData.indexOf(idx);
			
			mAdapter.getItem(rid).mPhotoPath = path;
			
			mAdapter.notifyDataSetChanged();
			
			// System.out.println("파일존재 " + ret_img.toString());
		}
	}
	
	public String getProfile(String filename) {
		File file = null;
		
		file = getApplicationContext().getFilesDir();
		
		String dir = file.getAbsolutePath() + "/profile/";
		String path = file.getAbsolutePath() + "/profile/" + filename + ".png";
		System.out.println("위치 : " + path);
		
		file = new File(dir);
		if (!file.exists()) {
			// 디렉토리가 존재하지 않으면 디렉토리 생성
			file.mkdirs();
		}
		
		return path;
	}
	
	// 다운로드 쓰레드로 돌림..
	class DownloadProfileThread extends Thread {
		String ServerUrl;
		String LocalPath;
		String LocalLargePath;
		String LastDate;
		
		DownloadProfileThread(String serverPath, String localPath,
				String last_date) {
			ServerUrl = HOST + "/profile/" + serverPath + ".png";
			File file = getApplicationContext().getFilesDir();
			LocalLargePath = file.getAbsolutePath() + "/profile/large"
					+ serverPath + ".jpg";
			LocalPath = localPath;
			LastDate = last_date;
		}
		
		@Override
		public void run() {
			URL imgurl;
			int Read;
			try {
				imgurl = new URL(ServerUrl);
				HttpURLConnection conn = (HttpURLConnection) imgurl
						.openConnection();
				int len = conn.getContentLength();
				File file = null;
				if (len > 0) {
					byte[] tmpByte = new byte[len];
					InputStream is = conn.getInputStream();
					file = new File(LocalPath);
					FileOutputStream fos = new FileOutputStream(file);
					for (;;) {
						Read = is.read(tmpByte);
						if (Read <= 0) {
							break;
						}
						fos.write(tmpByte, 0, Read);
					}
					is.close();
					fos.close();
				}
				conn.disconnect();
				
				// 큰사이즈 이미지 삭제
				File large_file = new File(LocalLargePath);
				large_file.delete();
				
				Picasso.with(getApplicationContext()).invalidate(file);
				
				mAfterDown.sendEmptyMessage(0);
			} catch (MalformedURLException e) {
				Log.e("ERROR1", e.getMessage());
			} catch (IOException e) {
				Log.e("ERROR2", e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	// 다운로드 쓰레드로 돌림..
	class DownloadThread extends Thread {
		String ServerUrl;
		String LocalPath;
		String rid;
		
		DownloadThread(String serverPath, String localPath, String idx) {
			ServerUrl = HOST + "/uploads/" + roomCode + "/" + serverPath;
			LocalPath = localPath;
			rid = idx;
		}
		
		@Override
		public void run() {
			URL imgurl;
			int Read;
			try {
				imgurl = new URL(ServerUrl);
				HttpURLConnection conn = (HttpURLConnection) imgurl
						.openConnection();
				int len = conn.getContentLength();
				if (len > 0) {
					byte[] tmpByte = new byte[len];
					InputStream is = conn.getInputStream();
					File file = new File(LocalPath);
					FileOutputStream fos = new FileOutputStream(file);
					for (;;) {
						Read = is.read(tmpByte);
						if (Read <= 0) {
							break;
						}
						fos.write(tmpByte, 0, Read);
					}
					is.close();
					fos.close();
				}
				conn.disconnect();
				
				int idx = mNData.indexOf(rid);
				System.out.println("MESSAGE POSITION " + idx);
				System.out.println("photo Info " + LocalPath);
				
				// bm = BitmapFactory.decodeFile(LocalPath);
				// mAdapter.getItem(idx).mPhoto = bm;
				mAdapter.getItem(idx).mPhotoPath = LocalPath;
				
				mAfterDown.sendEmptyMessage(0);
			} catch (MalformedURLException e) {
				Log.e("ERROR1", e.getMessage());
			} catch (IOException e) {
				Log.e("ERROR2", e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	@SuppressLint("HandlerLeak")
	Handler mAfterDown = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			// 파일 다운로드 종료 후 다운받은 파일을 실행시킨다.
			mAdapter.notifyDataSetChanged();
			
			try {
				UserListActivity.Instance().userListAdapter
						.notifyDataSetChanged();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		
	};
	
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
			
			String send_url = "";
			List<BasicNameValuePair> nameValuePairs = null;
			
			if (data[0].equals("msg")) {
				send_url = "/chat/send_msg";
				nameValuePairs = setMessage(data);
			} else if (data[0].equals("recomm")) {
				send_url = "/chat/send_like";
				nameValuePairs = setRecomm(data);
			} else if (data[0].equals("msgdel")) {
				send_url = "/chat/del_msg";
				nameValuePairs = setRecomm(data);
			} else if (data[0].equals("profile")) {
				send_url = "/member/check_profile";
				nameValuePairs = setProfile(data);
			}
			
			http.setUrlString(send_url);
			
			Boolean ret = false;
			if (data[0].equals("photo")) {
				send_url = "/chat/photo";
				http.setUrlString(send_url);
				
				ret = http.sendFileData(data);
			} else {
				http.setNameValuePairs(nameValuePairs);
				
				ret = http.sendData();
			}
			
			return ret;
		}
		
		private List<BasicNameValuePair> setRecomm(String... data) {
			List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(
					5);
			nameValuePairs.add(new BasicNameValuePair("id", data[1]));
			nameValuePairs.add(new BasicNameValuePair("name", data[2]));
			nameValuePairs.add(new BasicNameValuePair("rid", data[3]));
			nameValuePairs.add(new BasicNameValuePair("code", data[4]));
			nameValuePairs.add(new BasicNameValuePair("roomname", data[5]));
			
			return nameValuePairs;
		}
		
		private List<BasicNameValuePair> setProfile(String... data) {
			List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(
					2);
			nameValuePairs.add(new BasicNameValuePair("id", data[1]));
			nameValuePairs.add(new BasicNameValuePair("last_date", data[2]));
			
			return nameValuePairs;
		}
		
		private List<BasicNameValuePair> setMessage(String... data) {
			List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(
					5);
			nameValuePairs.add(new BasicNameValuePair("id", data[1]));
			nameValuePairs.add(new BasicNameValuePair("name", data[2]));
			nameValuePairs.add(new BasicNameValuePair("msg", data[3]));
			nameValuePairs.add(new BasicNameValuePair("code", data[4]));
			nameValuePairs.add(new BasicNameValuePair("roomname", data[5]));
			nameValuePairs.add(new BasicNameValuePair("key", data[6]));
			
			return nameValuePairs;
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
			if (result == true) {
				// 성공
				
				JSONObject status = null;
				String json = http.getJson();
				// Log.d("JOIN", json);
				
				if (json != null) {
					Log.d("수신 성공 : ", json);
					try {
						status = new JSONObject(json);
						String state = status.getString("result");
						
						if (state.equals("0000")) {
							// msg = "로그인 성공";
						} else {
							// msg = status.getString("msg");
							if (state.equals("9999")) {
								makeToastMsg(status.getString("msg"));
							}
						}
						// Log.d("HTTP", state);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					// msg = "데이터 수신 실패";
				}
			} else {
				// 실패
				makeToastMsg("통신 실패.\n다시 시도해주세요.");
			}
			
			super.onPostExecute(result);
		}
	}
	
	private class SendProfileHTTPData extends
			AsyncTask<String, Integer, String> {
		
		// AsyncTask 시작전
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		// AsyncTask 시작
		@Override
		protected String doInBackground(String... data) {
			http = HttpService.getInstance();
			
			String send_url = "";
			List<BasicNameValuePair> nameValuePairs = null;
			
			if (data[0].equals("profile")) {
				send_url = "/member/check_profile";
				nameValuePairs = setProfile(data);
			}
			
			http.setUrlString(send_url);
			
			http.setNameValuePairs(nameValuePairs);
			
			String ret = http.sendProfileData();
			
			System.out.println("프로필 체크 시작");
			
			return ret;
		}
		
		private List<BasicNameValuePair> setProfile(String... data) {
			List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(
					2);
			nameValuePairs.add(new BasicNameValuePair("id", data[1]));
			nameValuePairs.add(new BasicNameValuePair("last_date", data[2]));
			
			return nameValuePairs;
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
		protected void onPostExecute(String json) {
			if (json != null) {
				Log.d("프로필 데이터 수신 성공 : ", json);
				JSONObject status = null;
				try {
					status = new JSONObject(json);
					String state = status.getString("result");
					
					if (state.equals("0000")) {
						// msg = "로그인 성공";
						try {
							String type = status.getString("type") == null ? ""
									: status.getString("type");
							String change = status.getString("change") == null ? ""
									: status.getString("change");
							
							System.out.println(type);
							System.out.println(change);
							if (type != null && type.equals("profile")
									&& change.equals("Y")) {
								System.out.println("정보갱신");
								String id = status.getString("id");
								String nickname = status.getString("nickname");
								String photo = status.getString("photo");
								String last_date = status
										.getString("last_date");
								System.out.println(id + " | " + last_date);
								
								if (roomAdmin.equals(id)) {
									((TextView) findViewById(R.id.adminNickname))
											.setText(nickname);
								}
								
								changeProfile(id, last_date, nickname, photo);
							} else if (type != null && type.equals("profile")
									&& change.equals("N")) {
								System.out.println("정보갱신 안함");
								String id = status.getString("id");
								String last_date = status
										.getString("last_date");
								System.out.println(id + " | " + last_date);
							}
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}
					} else {
						// msg = status.getString("msg");
					}
					// Log.d("HTTP", state);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			super.onPostExecute(json);
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unbindService(connection);
		super.onDestroy();
	}
	
	public void addFile(View v) {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		
		if (fileView.getVisibility() == View.VISIBLE) {
			fileView.setVisibility(View.GONE);
			addBtn.setBackgroundResource(R.drawable.btn_chat_plus);
		} else {
			fileView.setVisibility(View.VISIBLE);
			addBtn.setBackgroundResource(R.drawable.btn_close);
		}
	}
	
	public void addMessage(int pos) {
		isMessageProcess = true;
		String[] columns = new String[] { "_id", "rid", "id", "room_code",
				"nickname", "like", "message", "reg_date", "msg_type" };
		
		String where = "";
		if (lastRid == null) {
			where = "room_code='" + roomCode + "'";
		} else {
			where = "room_code='" + roomCode + "' and rid<'" + lastRid + "'";
		}
		String orderBy = "reg_date desc, rid desc";
		Cursor c = mDbManager.selectChat(columns, where, null, null, null,
				orderBy, "80");
		
		int appPosition = c.getCount() + pos;
		
		//System.out.println(c.getCount());
		
		if (c == null || c.getCount() < 80) {
			isEnd = true;
		}
		
		if (c != null && c.getCount() > 0) {
			while (c.moveToNext()) {
				addShowMessage(c.getString(4), c.getString(6), c.getString(7),
						c.getString(2), c.getString(1),
						String.valueOf(c.getInt(5)), c.getString(8));
			}
			c.moveToLast();
			lastRid = c.getString(1);
			// Toast.makeText(this, lastRid, Toast.LENGTH_LONG).show();
			
			mAdapter.notifyDataSetChanged();
		}
		c.close();
		
		isMessageProcess = false;
		listview.setSelection(appPosition);
	}
	
	private void scrollMyListViewToBottom() {
		listview.post(new Runnable() {
			@Override
			public void run() {
				// Select the last row so it will scroll into view...
				listview.setSelection(mAdapter.getCount() - 1);
			}
		});
	}
	
	public void hideSoftInputWindow(View edit_view) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(edit_view.getWindowToken(), 0);
		
		fileView.setVisibility(View.GONE);
		addBtn.setBackgroundResource(R.drawable.btn_chat_plus);
	}
	
	@Override
	public void onBackPressed() {
		View searchSection = (View) this.findViewById(R.id.searchSection);
		if (searchSection.getVisibility() == View.VISIBLE) {
			searchSection.setVisibility(View.GONE);
			EditText searchWord = (EditText) this.findViewById(R.id.searchWord);
			searchWord.setText("");
			View headSection = (View) this.findViewById(R.id.headSection);
			headSection.setVisibility(View.VISIBLE);
			View sendMessageSection = (View) this
					.findViewById(R.id.sendMessageSection);
			sendMessageSection.setVisibility(View.VISIBLE);
		} else if (fileView.getVisibility() == View.VISIBLE) {
			fileView.setVisibility(View.GONE);
			addBtn.setBackgroundResource(R.drawable.btn_chat_plus);
		} else {
			ChatApplication app = (ChatApplication) getApplicationContext();
			app.setChatRoom("");
			
			startActivity(new Intent(this, ListActivity.class));
			finish();
		}
		
		// overridePendingTransition(0, 0);
	}
	
	public void goBack(View v) {
		ChatApplication app = (ChatApplication) getApplicationContext();
		app.setChatRoom("");
		
		startActivity(new Intent(this, ListActivity.class));
		finish();
	}

	public MQTTService mService;
	
	public ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = ((MQTTService.MQTTBinder) service).getService();
			mService.registerCallback(mCallback);
		}
		
		public void onServiceDisconnected(ComponentName className) {
			mService = null;
		}
		
		private MQTTService.ICallback mCallback = new MQTTService.ICallback() {
			public void sendData(String... s) {
				/* 서비스에서 데이터를 받아 메소드 호출 또는 핸들러로 전달 */
				// name, message, date, id, rid
				// rid, id, roomCode, nickName, message, regDate, type, key
				Log.i("MSG", s[1] + " | " + myId);
				if (!s[1].equals(myId)) {
					Log.i("MSG", "메세지 받음");
					showMessage(s[3], s[4], s[5], s[1], s[0], s[6]);
				} else {
					Log.i("MSG", "SEND 메세지 받음");
					
					int idx = mNData.indexOf(s[7]);
					System.out.println("MESSAGE POSITION " + idx);
					
					if (idx > -1) {
						// mAdapter.getItem(idx).mMessage = s[4];
						// mAdapter.getItem(idx).mDate = s[5].substring(11);
						mAdapter.getItem(idx).mDate = makeTime(s[5]);
						mAdapter.getItem(idx).mRid = s[0];
						
						mNData.set(idx, s[0]);
						
						mAdapter.notifyDataSetChanged();
					}
				}
			}
			
			public void sendVoteData(String... s) {
				/* 서비스에서 데이터를 받아 메소드 호출 또는 핸들러로 전달 */
				// name, message, date, id, rid
				// rid, id, roomCode, nickName, message, regDate, type
				Log.i("MSG", "투표메세지 받음");
				showMessage(s[3], s[4], s[5], s[1], s[0], s[6]);
			}
			
			public void sendFileData(String... s) {
				// name, message, date, id, rid
				// rid, id, roomCode, nickName, message, regDate, type, key
				Log.i("MSG", s[1] + " | " + myId);
				if (!s[1].equals(myId)) {
					Log.i("MSG", "PHOTO 메세지 받음");
					showMessage(s[3], s[4], s[5], s[1], s[0], s[6]);
				} else {
					Log.i("MSG", "SEND PHOTO 메세지 받음");
					
					int idx = mNData.indexOf(s[7]);
					System.out.println("MESSAGE POSITION " + idx);
					
					mAdapter.getItem(idx).mMessage = s[4];
					// mAdapter.getItem(idx).mDate = s[5].substring(11);
					mAdapter.getItem(idx).mDate = makeTime(s[5]);
					mAdapter.getItem(idx).mRid = s[0];
					
					mNData.set(idx, s[0]);
					
					mAdapter.notifyDataSetChanged();
					
					String filename = s[4].split(",")[1];
					getPhoto(filename, s[0]);
				}
			}
			
			public void updateLike(String... s) {
				int idx = mNData.indexOf(s[0]);
				System.out.println("LIKE POSITION " + s[0] + " | " + idx);
				if (idx > -1) {
					mAdapter.getItem(idx).mLike = s[1];
					
					mAdapter.notifyDataSetChanged();
				}
			}
			
			public void deleteMessage(String... s) {
				int idx = mNData.indexOf(s[0]);
				System.out.println("DELETE POSITION " + s[0] + " | " + idx);
				if (idx > -1) {
					mAdapter.m_List.remove(idx);
					
					mAdapter.notifyDataSetChanged();
				}
			}
		};
	};
	
	public void btnClickEnter(View v) {
		fileView.setVisibility(View.GONE);
		addBtn.setBackgroundResource(R.drawable.btn_chat_plus);
		
		int id = v.getId();
		switch (id) {
			case R.id.btnAlbum:
				// 사진
				Intent intent = new Intent(Intent.ACTION_PICK);
				intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
				startActivityForResult(intent, PICK_FROM_ALBUM);
				
				// startActivity(new Intent(getApplicationContext(),
				// AlbumListActivity.class));
				break;
			case R.id.btnCamera:
				// 카메라
				Intent cintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				// Crop된 이미지를 저장할 파일의 경로를 생성
				mImageCaptureUri = createSaveFile();
				cintent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
						mImageCaptureUri);
				startActivityForResult(cintent, PICK_FROM_CAMERA);
				
				break;
			case R.id.btnStats:
				// 통계
				Intent sintent = new Intent(this, StatisticsActivity.class);
				sintent.putExtra("ROOMCODE", roomCode);
				startActivity(sintent);
				
				break;
			case R.id.btnVote:
				// 투표
				Intent vintent = new Intent(this, VoteListActivity.class);
				vintent.putExtra("ROOMCODE", roomCode);
				startActivity(vintent);
				
				break;
		}
	}
	
	public static String getRealPathFromUri(Context context, Uri contentUri) {
		Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(contentUri, proj, null,
					null, null);
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode != RESULT_OK) {
			return;
		}
		
		String path = null;
		String new_path = null;
		
		switch (requestCode) {
			case PICK_FROM_ALBUM:
				Log.d("Album", "PICK_FROM_ALBUM");
				mImageCaptureUri = data.getData();
				// File original_file = getImageFile(mImageCaptureUri);
				Log.d("PATH", mImageCaptureUri.toString());
				
				/*
				 * Cursor c = getContentResolver().query(mImageCaptureUri, null,
				 * null, null, null); c.moveToNext(); path = c.getString(c
				 * .getColumnIndex(MediaStore.MediaColumns.DATA)); c.close();
				 */
				path = getRealPathFromUri(this, mImageCaptureUri);
				
				new_path = createSaveFile().getPath();
				
				break;
			
			case PICK_FROM_CAMERA:
				Log.d("Camera", "PICK_FROM_CAMERA");
				Log.d("PATH", mImageCaptureUri.getPath());
				Log.d("PATH2", mImageCaptureUri.toString());
				
				path = mImageCaptureUri.getPath();
				
				new_path = path;
				
				break;
		}
		
		// options = new BitmapFactory.Options();
		// options.inSampleSize = 4;
		
		// bm = BitmapFactory.decodeFile(path, options);
		
		// ImageProcessThread dThread = new ImageProcessThread(path, new_path);
		// dThread.start();
		
		String uniqueValue = null;
		Chats chat = new Chats();
		int h;
		int w;
		
		OutputStream out = null;
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		// 리턴으로 비트맵을 돌려받진 않지만 정보를 얻어올수있다. true
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		// h = options.outHeight;
		// w = options.outWidth;
		
		float widthScale = options.outWidth / 1024;
		float heightScale = options.outHeight / 1024;
		float scale = widthScale > heightScale ? widthScale : heightScale;
		if (scale >= 8) {
			options.inSampleSize = 8;
		} else if (scale >= 6) {
			options.inSampleSize = 6;
		} else if (scale >= 4) {
			options.inSampleSize = 4;
		} else if (scale >= 2) {
			options.inSampleSize = 2;
		} else {
			options.inSampleSize = 1;
		}
		
		options.inJustDecodeBounds = false;
		
		resized = BitmapFactory.decodeFile(path, options);
		
		int degree = GetExifOrientation(path);
		
		resized = GetRotatedBitmap(resized, degree);
		
		try {
			out = new FileOutputStream(new File(new_path));
			if (resized.compress(CompressFormat.JPEG, 80, out)) {
				out.flush();
				out.close();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		chat.mName = myName;
		chat.mDate = "";
		chat.isMine = "Y";
		chat.mType = "photo";
		chat.mProfile = getProfile(myId);
		
		// chat.mPhoto = resized;
		chat.mPhotoPath = new_path;
		// Log.d("Image", chat.mPhoto.toString());
		
		clearProfileCache(myId, myName);
		mAdapter.add(chat);
		uniqueValue = java.util.UUID.randomUUID().toString();

		mNData.add(uniqueValue);
		
		textMessage.setText("");
		
		lastitemVisibleFlag = true;
		
		listview.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		
		SendHTTPData mSendHTTPData = new SendHTTPData();
		mSendHTTPData.execute("photo", new_path, myId, myName, roomCode,
				roomName, uniqueValue);
	}
	
	/**
	 * Crop된 이미지가 저장될 파일을 만든다.
	 * 
	 * @return Uri
	 */
	private Uri createSaveFile() {
		Uri uri;
		String url = "tmp_" + String.valueOf(System.currentTimeMillis())
				+ ".jpg";
		uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
				url));
		return uri;
	}
	
	/**
	 * 선택된 uri의 사진 Path를 가져온다. uri 가 null 경우 마지막에 저장된 사진을 가져온다.
	 * 
	 * @param uri
	 * @return
	 */
	private File getImageFile(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		if (uri == null) {
			uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		}
		
		Cursor mCursor = getContentResolver().query(uri, projection, null,
				null, MediaStore.Images.Media.DATE_MODIFIED + " desc");
		if (mCursor == null || mCursor.getCount() < 1) {
			return null; // no cursor or no record
		}
		int column_index = mCursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		mCursor.moveToFirst();
		
		String path = mCursor.getString(column_index);
		
		if (mCursor != null) {
			mCursor.close();
			mCursor = null;
		}
		
		return new File(path);
	}
	
	public void showPhoto(String filename) {
		System.out.println(filename);
		Intent intent = new Intent(this, PhotoActivity.class);
		intent.putExtra("filename", filename);
		intent.putExtra("ROOMCODE", roomCode);
		startActivity(intent);
	}
	
	public void showProfile(String id) {
		Intent intent = new Intent(this, ProfileActivity.class);
		intent.putExtra("id", id);
		startActivity(intent);
	}
	
	public void goVote() {
		Intent vintent = new Intent(this, VoteListActivity.class);
		vintent.putExtra("ROOMCODE", roomCode);
		startActivity(vintent);
	}
	
	// 이미지 방향 구하는 함수
	public synchronized static int GetExifOrientation(String filepath) {
		int degree = 0;
		ExifInterface exif = null;
		
		try {
			exif = new ExifInterface(filepath);
		} catch (IOException e) {
			Log.e("image orientation", "cannot read exif");
			e.printStackTrace();
		}
		
		if (exif != null) {
			int orientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION, -1);
			
			if (orientation != -1) {
				// We only recognize a subset of orientation tag values.
				switch (orientation) {
					case ExifInterface.ORIENTATION_ROTATE_90:
						degree = 90;
						break;
					
					case ExifInterface.ORIENTATION_ROTATE_180:
						degree = 180;
						break;
					
					case ExifInterface.ORIENTATION_ROTATE_270:
						degree = 270;
						break;
				}
				
			}
		}
		
		return degree;
	}
	
	// 이미지 회전하는 함수
	public synchronized static Bitmap GetRotatedBitmap(Bitmap bitmap,
			int degrees) {
		if (degrees != 0 && bitmap != null) {
			Matrix m = new Matrix();
			m.setRotate(degrees, (float) bitmap.getWidth() / 2,
					(float) bitmap.getHeight() / 2);
			try {
				Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0,
						bitmap.getWidth(), bitmap.getHeight(), m, true);
				if (bitmap != b2) {
					bitmap.recycle();
					bitmap = b2;
				}
			} catch (OutOfMemoryError ex) {
				// We have no memory to rotate. Return the original bitmap.
			}
		}
		
		return bitmap;
	}
	
	public void goChatUserOpen(View v) {
		// 현재 대화방 사용자 리스트 띄우기
		Intent vintent = new Intent(this, UserListActivity.class);
		vintent.putExtra("ROOMCODE", roomCode);
		startActivity(vintent);
	}
	
	/*
	 * 검색영역
	 */
	public void goSearchOpen(View v) {
		View searchSection = (View) this.findViewById(R.id.searchSection);
		searchSection.setVisibility(View.VISIBLE);
		View headSection = (View) this.findViewById(R.id.headSection);
		headSection.setVisibility(View.GONE);
		View sendMessageSection = (View) this
				.findViewById(R.id.sendMessageSection);
		sendMessageSection.setVisibility(View.GONE);
		fileView.setVisibility(View.GONE);
		addBtn.setBackgroundResource(R.drawable.btn_chat_plus);
		
		EditText searchWord = (EditText) this.findViewById(R.id.searchWord);
		
		searchWord.requestFocus();
		
		// 키보드 띄우기
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
				InputMethodManager.HIDE_IMPLICIT_ONLY);
		
		searchWord
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						// TODO Auto-generated method stub
						if (actionId == EditorInfo.IME_ACTION_SEARCH) {
							searchText = v.getText().toString().trim();
							
							if (searchText != null && !searchText.equals("")) {
								System.out.println(searchText);
								
								hideSoftInputWindow(v);
								searchIdx = "";
								requestSearch(0);
								return true;
							}
							
						}
						return false;
					}
				});
	}
	
	public void goSearchPrev(View v) {
		if (searchText != null && !searchText.equals("")
				&& !searchIdx.equals("")) {
			hideSoftInputWindow(v);
			requestSearch(0);
		}
	}
	
	public void goSearchNext(View v) {
		if (searchText != null && !searchText.equals("")
				&& !searchIdx.equals("")) {
			hideSoftInputWindow(v);
			requestSearch(1);
		}
	}
	
	/**
	 * 검색하기
	 * 
	 * @param way
	 *            방향 0: 위, 1: 아래
	 */
	public void requestSearch(Integer way) {
		
		ProgressDialog dialog = ProgressDialog.show(this, "", "검색중입니다.", false);
		dialog.show();
		
		String where = "";
		isMessageProcess = true;
		lastitemVisibleFlag = true;
		listview.setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);
		search_pos = null;
		
		String orderBy = "";
		if (way.equals(0)) {
			// 위로 검색
			orderBy = "reg_date desc, rid desc";
			if (searchIdx.equals("")) {
				where = "room_code='" + roomCode + "' and message like '%"
						+ searchText + "%' and msg_type='msg'";
			} else {
				where = "room_code='" + roomCode + "' and message like '%"
						+ searchText + "%' and msg_type='msg' and rid<'"
						+ searchIdx + "'";
			}
		} else {
			// 아래로 검색
			orderBy = "reg_date asc, rid asc";
			where = "room_code='" + roomCode + "' and message like '%"
					+ searchText + "%' and msg_type='msg' and rid>'"
					+ searchIdx + "'";
		}
		
		String[] columns = new String[] { "_id", "rid", "id", "room_code",
				"nickname", "like", "message", "reg_date", "msg_type" };
		Cursor c = mDbManager.selectChat(columns, where, null, null, null,
				orderBy, "1");
		
		dialog.dismiss();
		
		if (c != null && c.getCount() > 0) {
			c.moveToLast();
			searchIdx = c.getString(1);
			
			search_pos = mNData.indexOf(searchIdx);
			
			if (search_pos != null && search_pos > -1) {
				System.out.println("검색위치이동");
				int p = search_pos - 1 > -1 ? search_pos - 1 : search_pos;
				mAdapter.notifyDataSetChanged();
				listview.setSelection(p);
			} else {
				// 현재 대화 목록에 해당 메세지가 없을경우
				// 추가로 메세지를 받아온다.
				searchAddMessage(searchIdx);
			}
		} else {
			// 검색결과가 없을때
			LayoutInflater inflater = null;
			inflater = LayoutInflater.from(getApplicationContext());
			View dialoglayout = inflater.inflate(R.layout.alert_msg, null);
			
			TextView alert_title = (TextView) dialoglayout
					.findViewById(R.id.alert_title);
			TextView alert_msg = (TextView) dialoglayout
					.findViewById(R.id.alert_msg);
			TextView alert_submit = (TextView) dialoglayout
					.findViewById(R.id.alert_submit);
			alert_title.setText("검색 결과");
			
			String alert_msg_text = "검색결과가 없습니다.";
			alert_msg.setText(alert_msg_text);
			
			alert_submit.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					alertDialog.dismiss();
				}
			});
			
			alertDialog = new Dialog(this, R.style.CustomDialog);
			alertDialog.setContentView(dialoglayout);
			alertDialog.setCanceledOnTouchOutside(false);
			alertDialog.show();
			// makeToastMsg("검색결과가 없습니다.");
		}
		c.close();
	}
	
	private void searchAddMessage(String idx) {
		addMessage(0);
		mAdapter.notifyDataSetChanged();
		
		isMessageProcess = true;
		lastitemVisibleFlag = true;
		listview.setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);
		
		search_pos = mNData.indexOf(idx);
		
		if (search_pos != null && search_pos > -1) {
			System.out.println("검색위치이동");
			int p = search_pos - 1 > -1 ? search_pos - 1 : search_pos;
			listview.setSelection(p);
		} else {
			// 현재 대화 목록에 해당 메세지가 없을경우
			// 추가로 메세지를 받아온다.
			searchAddMessage(idx);
		}
	}
	
	public String getSearchText() {
		return searchText;
	}
	
	public void goSearchClose(View v) {
		isMessageProcess = false;
		lastitemVisibleFlag = true;
		listview.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		searchText = "";
		searchIdx = "";
		hideSoftInputWindow(v);
		View searchSection = (View) this.findViewById(R.id.searchSection);
		searchSection.setVisibility(View.GONE);
		EditText searchWord = (EditText) this.findViewById(R.id.searchWord);
		searchWord.setText("");
		View headSection = (View) this.findViewById(R.id.headSection);
		headSection.setVisibility(View.VISIBLE);
		View sendMessageSection = (View) this
				.findViewById(R.id.sendMessageSection);
		sendMessageSection.setVisibility(View.VISIBLE);
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
}
