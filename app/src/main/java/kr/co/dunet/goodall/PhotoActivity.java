package kr.co.dunet.goodall;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import kr.co.dunet.app.goodall.R;
import uk.co.senab.photoview.PhotoViewAttacher;

public class PhotoActivity extends Activity {
	private static ProgressDialog dialog = null;
	private String roomCode = "";
	private ImageView img_v = null;
	PhotoViewAttacher mAttacher;
	private String imagePath = null;
	private Bitmap bm = null;
	private String filename = "";
	private static Dialog alertDialog = null;
	private static Toast mToast = null;
	
	public String HOST = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_photo);
		
		HOST = NetworkConfig.Instance().getWebHost();
		
		View v = getLayoutInflater().inflate(R.layout.photo_save_btn, null);
		
		ActionBar mActionBar = getActionBar();
		mActionBar.setCustomView(v);
		mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

		/*
		mActionBar.setTitle("닫기");
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(true);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(true);
		mActionBar.setDisplayUseLogoEnabled(false);
		*/
		
		filename = getIntent().getStringExtra("filename");
		roomCode = getIntent().getStringExtra("ROOMCODE");
		System.out.println("사진 뷰 시작 " + filename);
		
		img_v = (ImageView) this.findViewById(R.id.photo_section);
		
		mAttacher = new PhotoViewAttacher(img_v);
		
		/*
		 * IntentFilter intentFilter = new IntentFilter(
		 * Intent.ACTION_MEDIA_SCANNER_STARTED);
		 * intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		 * intentFilter.addDataScheme("file"); registerReceiver(mReceiver,
		 * intentFilter);
		 */
		
		if (dialog == null) {
			dialog = ProgressDialog.show(this, "", "처리중입니다...", false);
			dialog.show();
			
			getPhoto(filename);
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// android.os.Debug.waitForDebugger();
		super.onConfigurationChanged(newConfig);
		
	}
	
	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) { // Inflate the
	 * menu items for use in the action bar MenuInflater inflater =
	 * getMenuInflater(); inflater.inflate(R.menu.photo_item, menu); return
	 * super.onCreateOptionsMenu(menu); }
	 */
	
	public void onPhotoSaveClick(View v) {
		File file = getApplicationContext().getFilesDir();
		String org_path = file.getAbsolutePath() + "/large/" + roomCode + "/"
				+ filename;
		
		String save_path = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/Pictures/Goodall";
		
		String file_name = "save_" + String.valueOf(System.currentTimeMillis())
				+ ".jpg";
		
		file = new File(save_path);
		if (!file.exists()) {
			// 디렉토리가 존재하지 않으면 디렉토리 생성
			file.mkdirs();
		}
		
		boolean result = copyFile(new File(org_path), save_path + "/"
				+ file_name);
		
		if (result) {
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
						Uri.parse("file://"
								+ Environment.getExternalStorageDirectory()
								+ "/Pictures/Goodall/" + file_name)));
			} else {
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
						Uri.parse("file://"
								+ Environment.getExternalStorageDirectory()
								+ "/Pictures/Goodall")));
			}
			
			makeToastMsg("저장이 완료되었습니다.");
		} else {
			makeToastMsg("저장 실패하였습니다.");
		}
	}
	
	public void activityClose(View v) {
		finish();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		/*
		 * case R.id.photo_save: File file =
		 * getApplicationContext().getFilesDir(); String org_path =
		 * file.getAbsolutePath() + "/large/" + roomCode + "/" + filename;
		 * 
		 * String save_path = Environment.getExternalStorageDirectory()
		 * .getAbsolutePath() + "/Pictures/Goodall";
		 * 
		 * String file_name = "save_" +
		 * String.valueOf(System.currentTimeMillis()) + ".jpg";
		 * 
		 * file = new File(save_path); if (!file.exists()) { // 디렉토리가 존재하지 않으면
		 * 디렉토리 생성 file.mkdirs(); }
		 * 
		 * boolean result = copyFile(new File(org_path), save_path + "/" +
		 * file_name);
		 * 
		 * if (result) { if (Build.VERSION.SDK_INT >
		 * Build.VERSION_CODES.JELLY_BEAN) { sendBroadcast(new Intent(
		 * Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" +
		 * Environment .getExternalStorageDirectory() + "/Pictures/Goodall/" +
		 * file_name))); } else { sendBroadcast(new
		 * Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment
		 * .getExternalStorageDirectory() + "/Pictures/Goodall"))); }
		 * 
		 * makeToastMsg("저장이 완료되었습니다."); } else { makeToastMsg("저장 실패하였습니다."); }
		 * 
		 * return true;
		 */
			case android.R.id.home:
				// NavUtils.navigateUpFromSameTask(this);
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
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
	
	/*
	 * private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	 * 
	 * @Override public void onReceive(Context context, Intent intent) { if
	 * (intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) { //
	 * mTitle.setText("Media Scanner started scanning " + //
	 * intent.getData().getPath()); } else if (intent.getAction().equals(
	 * Intent.ACTION_MEDIA_SCANNER_FINISHED)) { //
	 * mTitle.setText("Media Scanner finished scanning " + //
	 * intent.getData().getPath()); } } };
	 */
	
	/**
	 * 파일 복사
	 * 
	 * @param file
	 * @param save_file
	 * @return
	 */
	private boolean copyFile(File file, String save_file) {
		boolean result;
		if (file != null && file.exists()) {
			try {
				FileInputStream fis = new FileInputStream(file);
				FileOutputStream newfos = new FileOutputStream(save_file);
				int readcount = 0;
				byte[] buffer = new byte[1024];
				while ((readcount = fis.read(buffer, 0, 1024)) != -1) {
					newfos.write(buffer, 0, readcount);
				}
				newfos.close();
				fis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			result = true;
		} else {
			result = false;
		}
		
		return result;
	}
	
	public void getPhoto(String filename) {
		File file = getApplicationContext().getFilesDir();
		
		String dir = file.getAbsolutePath() + "/large/" + roomCode;
		String path = file.getAbsolutePath() + "/large/" + roomCode + "/"
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
			System.out.println("파일다운 " + path);
			DownloadThread dThread = new DownloadThread(filename, path);
			dThread.start();
		} else {
			// 파일이 존재하면...
			System.out.println("파일존재 " + path);
			try {
				bm = BitmapFactory.decodeFile(path);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				bm = BitmapFactory.decodeFile(path);
			}
			img_v.setImageBitmap(bm);
			
			// Glide.with(this).load(path).into(img_v);
			// Picasso.with(getApplicationContext()).load(new
			// File(path)).into(img_v);
			mAttacher.update();
			dialog.dismiss();
			dialog = null;

		}
	}
	
	// 다운로드 쓰레드로 돌림..
	class DownloadThread extends Thread {
		String ServerUrl;
		String LocalPath;
		String rid;
		
		DownloadThread(String serverPath, String localPath) {
			ServerUrl = HOST + "/uploads/" + roomCode + "/" + serverPath;
			LocalPath = localPath;
			imagePath = LocalPath;
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
				} else {
					imagePath = null;
				}
				conn.disconnect();
				
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
			
			if (imagePath == null) {
				makeToastMsg("파일전송이 실패하였습니다.");
			} else {
				try {
					bm = BitmapFactory.decodeFile(imagePath);
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
					bm = BitmapFactory.decodeFile(imagePath);
				}
				img_v.setImageBitmap(bm);
			}
			
			// Glide.with(getApplicationContext()).load(imagePath).into(img_v);
			// Picasso.with(getApplicationContext()).load(new
			// File(imagePath)).into(img_v);
			
			mAttacher.update();
			dialog.dismiss();
			dialog = null;
		}
	};
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		System.out.println("PHOTO 종료");
		if (bm != null) {
			bm.recycle();
			bm = null;
		}
		super.onDestroy();
	}
}
