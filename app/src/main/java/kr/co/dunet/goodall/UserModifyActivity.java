package kr.co.dunet.goodall;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.apache.http.message.BasicNameValuePair;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
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
import java.util.ArrayList;
import java.util.List;

import kr.co.dunet.app.goodall.R;

public class UserModifyActivity extends Activity {
	private static Toast mToast = null;
	private HttpService http = null;
	private String msg = null;
	private static ProgressDialog dialog = null;
	private SendHTTPData mSendHTTPData = null;
	private DBManager mDbManager = null;
	private String myId = null;
	private String nickname = null;
	
	private Uri mImageCaptureUri;
	private static final int PICK_FROM_CAMERA = 0;
	private static final int PICK_FROM_ALBUM = 1;
	
	public String HOST = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_user_modify);
		
		HOST = NetworkConfig.Instance().getWebHost();
		
		ChatApplication app = (ChatApplication) getApplicationContext();
		myId = app.getId();
		
		if (dialog == null) {
			dialog = ProgressDialog.show(this, "", "처리중입니다...", false);
			// dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			
			mSendHTTPData = new SendHTTPData();
			mSendHTTPData.execute("get", myId);
			dialog.show();
		}
	}
	
	// 정보 변경하기
	public void btnClick(View v) {
		EditText username = (EditText) findViewById(R.id.chatName);
		EditText email = (EditText) findViewById(R.id.chatEmail);
		EditText password = (EditText) findViewById(R.id.chatPw);
		EditText password_confirm = (EditText) findViewById(R.id.chatPwConfirm);
		
		nickname = username.getText().toString().trim();
		String e_mail = email.getText().toString().trim();
		String pwd = password.getText().toString().trim();
		String pwd_c = password_confirm.getText().toString().trim();
		
		if (nickname.equals("")) {
			makeToastMsg("대화명을 입력하세요.");
		} else if (e_mail.equals("")) {
			makeToastMsg("이메일을 입력하세요.");
		} else if (!pwd.equals("") && pwd_c.equals("")) {
			makeToastMsg("비밀번호 확인을 입력하세요.");
		} else if (!pwd_c.equals(pwd)) {
			makeToastMsg("비밀번호가 틀립니다.");
		} else {
			if (dialog == null) {
				dialog = ProgressDialog.show(this, "", "처리중입니다...", false);
				
				mSendHTTPData = new SendHTTPData();
				mSendHTTPData.execute("update", myId, nickname, e_mail, pwd);
				dialog.show();
			}
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
	
	public void goLogout(View v) {
		if (dialog == null) {
			dialog = ProgressDialog.show(this, "", "처리중입니다...", false);
			// dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			
			mSendHTTPData = new SendHTTPData();
			mSendHTTPData.execute("logout", myId);
			dialog.show();
		}
	}
	
	public void goLogoutSuccess() {
		ChatApplication app = (ChatApplication) getApplicationContext();
		app.setId(null);
		
		// 파일삭제
		File file = getApplicationContext().getFilesDir();
		
		String dir = file.getAbsolutePath() + "/temp";
		String l_dir = file.getAbsolutePath() + "/large";
		String profile_dir = file.getAbsolutePath() + "/profile";
		
		DeleteDir(dir);
		DeleteDir(l_dir);
		DeleteDir(profile_dir);
		
		// DB삭제
		DBManager mDbManager = DBManager.getInstance(getApplicationContext());
		
		mDbManager.deleteMember(null, null);
		mDbManager.deleteChat(null, null);
		mDbManager.deleteRoom(null, null);
		mDbManager.deleteUser(null, null);
		
		IMqttAsyncClient mqtt = app.getMqtt();
		try {
			IMqttToken token = mqtt.disconnect();
			token.waitForCompletion(5000);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		// 등록된 notification 을 제거 한다.
		nm.cancel(333);
		
		Notify.setMessageID(1);
		
		finish();
		
		Intent i = new Intent(this, FromActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(i);
	}
	
	public void DeleteDir(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return;
		}
		File[] childFileList = file.listFiles();
		for (File childFile : childFileList) {
			if (childFile.isDirectory()) {
				DeleteDir(childFile.getAbsolutePath()); // 하위 디렉토리 루프
			} else {
				childFile.delete(); // 하위 파일삭제
			}
		}
		file.delete(); // root 삭제
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
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// android.os.Debug.waitForDebugger();
		super.onConfigurationChanged(newConfig);
		
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
			
			if (data[0] == "get") {
				http.setUrlString("/member/get_member");
				nameValuePairs.add(new BasicNameValuePair("id", data[1]));
			} else if (data[0] == "update") {
				http.setUrlString("/member/mod_profile");
				nameValuePairs.add(new BasicNameValuePair("id", data[1]));
				nameValuePairs.add(new BasicNameValuePair("nickname", data[2]));
				nameValuePairs.add(new BasicNameValuePair("email", data[3]));
				nameValuePairs.add(new BasicNameValuePair("pwd", data[4]));
			} else if (data[0] == "logout") {
				http.setUrlString("/member/logout");
				nameValuePairs.add(new BasicNameValuePair("id", data[1]));
			}
			
			Boolean ret = false;
			if (data[0].equals("photo")) {
				http.setUrlString("/member/photo");
				
				ret = http.sendProfilePhoto(data);
			} else {
				http.setNameValuePairs(nameValuePairs);
				
				ret = http.sendData();
			}
			
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
						
						if (state.equals("0000")) {
							String type = status.getString("type");
							
							System.out.println(type);
							
							if (type.equals("get")) {
								EditText username = (EditText) findViewById(R.id.chatName);
								username.setText(status.getString("name"));
								EditText email = (EditText) findViewById(R.id.chatEmail);
								email.setText(status.getString("email"));
								File profile_image = new File(getProfile(myId));
								if (profile_image.exists()) {
									Picasso.with(getApplicationContext())
											.load(profile_image)
											.placeholder(
													R.drawable.chat_user_male)
											.noFade()
											.into(((ImageView) findViewById(R.id.chatProfile)));
								} else {
									DownloadProfileThread dThread = new DownloadProfileThread(
											myId + ".png", getProfile(myId));
									dThread.start();
								}
							} else if (type.equals("update")) {
								makeToastMsg(status.getString("msg"));
								// 정보 업데이트
								DBManager mDbManager = DBManager
										.getInstance(getApplicationContext());
								
								ContentValues updateRowValue = new ContentValues();
								updateRowValue.put("nickname", nickname);
								
								mDbManager.updateMember(updateRowValue, "id='"
										+ myId + "'", null);
								
								String where = "id='" + myId + "'";
								String[] columns = new String[] { "nickname" };
								Cursor c = mDbManager.selectUser(columns,
										where, null, null, null, null);
								
								if (c != null && c.getCount() > 0) {
									updateRowValue = new ContentValues();
									updateRowValue.put("lastdate", "");
									updateRowValue.put("nickname", nickname);
									mDbManager.updateUser(updateRowValue,
											"id='" + myId + "'", null);
								} else {
									ContentValues addRowValue = new ContentValues();
									addRowValue.put("id", myId);
									addRowValue.put("lastdate", "");
									addRowValue.put("nickname", nickname);
									
									mDbManager.insertUser(addRowValue);
								}
								c.close();
								
								finish();
							} else if (type.equals("logout")) {
								goLogoutSuccess();
							} else if (type.equals("profile")) {
								// 새로 다운로드
								System.out.println("프로필사진 새로 다운로드");
								DownloadProfileThread dThread = new DownloadProfileThread(
										myId + ".png", getProfile(myId));
								dThread.start();
							}
						} else {
							makeToastMsg(status.getString("msg"));
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			} else {
				makeToastMsg("통신 실패.\n다시 시도해주세요.");
			}
			
			super.onPostExecute(result);
		}
	}
	
	// 다운로드 쓰레드로 돌림..
	class DownloadProfileThread extends Thread {

        String ServerUrl;
		String LocalPath;
		
		DownloadProfileThread(String serverPath, String localPath) {

            ServerUrl = HOST + "/profile/" + serverPath;
			LocalPath = localPath;
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
				conn.disconnect();
				
				// File file = new File(path);
				
				// file.delete();
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
	
	@SuppressLint("HandlerLeak")
	Handler mAfterDown = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			// 파일 다운로드 종료 후 다운받은 파일을 실행시킨다.
			
			Picasso.with(getApplicationContext())
					.load(new File(getProfile(myId)))
					.placeholder(R.drawable.chat_user_male).noFade()
					.into(((ImageView) findViewById(R.id.chatProfile)));
		}
		
	};
	
	public void changeProfile(View v) {
		int id = v.getId();
		switch (id) {
			case R.id.chatProfile:
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
	
	// 선택된 이미지 처리영역
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

				Log.d("path" , new_path);
				
				break;
			
			case PICK_FROM_CAMERA:
				Log.d("Camera", "PICK_FROM_CAMERA");
				Log.d("PATH", mImageCaptureUri.getPath());
				Log.d("PATH2", mImageCaptureUri.toString());
				
				path = mImageCaptureUri.getPath();
				
				new_path = path;
				
				break;
		}
		
		OutputStream out = null;
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		
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
		
		Bitmap resized = BitmapFactory.decodeFile(path, options);
		
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
		
		if (dialog == null) {
			dialog = ProgressDialog.show(this, "", "업로드중입니다.", false);
			
			SendHTTPData mSendHTTPData = new SendHTTPData();
			mSendHTTPData.execute("photo", new_path, myId);
			dialog.show();
		}
	}
	
	/**
	 * 카메라로 촬영한 이미지가 저장될 파일을 만든다.
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
}
