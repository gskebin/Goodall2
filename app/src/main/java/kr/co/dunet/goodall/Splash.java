package kr.co.dunet.goodall;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class Splash extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		try {
			Thread.sleep(2000);
			
			// Intent intent = new Intent();
			// intent.setAction("kr.co.dunet.goodall.restart");
			// sendBroadcast(intent);
			
			ChatApplication app = (ChatApplication) getApplicationContext();
			String id = app.getId() == null ? "" : app.getId();
			Log.i("SPLASH", id);
			
			String roomCode = getIntent().getStringExtra("ROOMCODE");
			if (roomCode != null) {
				Intent intent = new Intent(this, MUCMessageActivity.class);
				intent.putExtra("ROOMCODE", roomCode);
				startActivity(intent);
			} else if (id.equals("")) {
				Intent svc = new Intent(this, MQTTService.class);
				// Intent svc = new Intent(this, MqttService.class);
				startService(svc);
				
				DBManager mDbManager = DBManager.getInstance(this);
				String[] columns = new String[] { "id", "nickname", "type" };
				Cursor c = mDbManager.selectMember(columns, null, null, null,
						null, null);
				
				if (c != null && c.getCount() > 0) {
					Boolean isGuest = false;
					while (c.moveToNext()) {
						id = c.getString(0);
						if (c.getString(2) != null
								&& c.getString(2).equals("1")) {
							isGuest = true;
						}
						break;
					}
					c.close();
					app.setId(id);
					app.setIsGuest(isGuest);
					startActivity(new Intent(this, ListActivity.class));
				} else {
					c.close();
					startActivity(new Intent(this, FromActivity.class));
				}
			} else {
				Intent i = new Intent(this, ListActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(i);
			}
			// } catch (InterruptedException e) {
			// e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getBaseContext(),
					"에러 : " + e.getMessage() + "(개발자에게 문의하세요)",
					Toast.LENGTH_SHORT).show();
		} finally {
			finish();
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		android.os.Debug.waitForDebugger();
		super.onConfigurationChanged(newConfig);
		
	}
}
