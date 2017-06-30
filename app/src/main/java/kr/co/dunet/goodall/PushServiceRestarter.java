package kr.co.dunet.goodall;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class PushServiceRestarter extends BroadcastReceiver {

	public static final String TAG = PushServiceRestarter.class.getName();

	@Override
	public void onReceive(Context context, Intent intent) {
		 Log.i(TAG, "Restart Service");
		if (intent.getAction().equals(MQTTService.ACTION_RESTART_SERVICE)
				|| intent.getAction().equals(
						"android.intent.action.BOOT_COMPLETED")) {
			Intent i = new Intent(context, MQTTService.class);
			context.startService(i);
		}
	}

	public static void registerRestartAlram(Context context) {
		// Log.i(TAG, "registerRestartAlram");
		Intent intent = new Intent(MQTTService.ACTION_RESTART_SERVICE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
				intent, 0);
		long time = SystemClock.elapsedRealtime();
		time += 5 * 1000;
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, time,
				10 * 1000, pendingIntent);
	}

	public static void unregisterRestartAlram(Context context) {
		 Log.i(TAG, "unregisterRestartAlarm");
		Intent intent = new Intent(MQTTService.ACTION_RESTART_SERVICE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
				intent, 0);
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);

	}
}