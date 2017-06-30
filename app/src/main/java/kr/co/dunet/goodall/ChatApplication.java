package kr.co.dunet.goodall;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;

public class ChatApplication extends Application implements
		ActivityLifecycleCallbacks {
	private static ChatApplication myApp;
	
	private String appUserId = null;
	private String appUserName = "";
	private String appChatRoom = "";
	private Boolean appFirst = false;
	private Boolean roomAdmin = false;
	private Boolean isGuest = false;
	private volatile IMqttAsyncClient mqttClient;

	private boolean mIsAppfinishState = false;

	public ChatApplication() {
		myApp = this;
	}
	
	public static ChatApplication Instance() {
		return myApp;
	}
	
	public void finishApp(Activity activity) {
		mIsAppfinishState = true;
		activity.finish();
	}

	@Override
	public void onActivityResumed(Activity activity) {
		if (mIsAppfinishState == true) {
			activity.finish();

			if (activity.isTaskRoot() == true) {
				mIsAppfinishState = false;
			}
		}
	}
	
	public Boolean getIsGuest() {
		return isGuest;
	}

	public void setIsGuest(Boolean isGuest) {
		this.isGuest = isGuest;
	}

	public void setId(String id) {
		appUserId = id;
	}
	
	public String getId() {
		return appUserId;
	}

	public void setFirst(Boolean f) {
		appFirst = f;
	}

	public Boolean getFirst() {
		return appFirst;
	}
	
	public void setRoomAdmin(Boolean f) {
		roomAdmin = f;
	}
	
	public Boolean getRoomAdmin() {
		return roomAdmin;
	}

	public void setChatRoom(String room) {
		appChatRoom = room;
	}

	public String getChatRoom() {
		return appChatRoom;
	}

	public void setName(String name) {
		appUserName = name;
	}

	public String getName() {
		return appUserName;
	}

	public void setMqtt(IMqttAsyncClient mq) {
		mqttClient = mq;
	}

	public IMqttAsyncClient getMqtt() {
		return mqttClient;
	}

	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onActivityDestroyed(Activity activity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onActivityPaused(Activity activity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onActivityStarted(Activity activity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onActivityStopped(Activity activity) {
		// TODO Auto-generated method stub

	}
}
