package kr.co.dunet.goodall;

import java.io.File;
import java.util.List;
import kr.co.dunet.app.goodall.R;
import com.squareup.picasso.Picasso;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UserListAdapter extends ArrayAdapter<UserData> {
	
	public List<UserData> mUserData;
	
	public UserListAdapter(Context context, int resource,
			int textViewResourceId, List<UserData> objects) {
		super(context, resource, textViewResourceId, objects);
		
		mUserData = objects;
	}
	
	class ViewHolder {
		TextView name;
		ImageView profile;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View itemLayout = super.getView(position, convertView, parent);
		
		final int pos = position;
		final Context context = parent.getContext();
		
		ViewHolder holder = (ViewHolder) itemLayout.getTag();
		
		OnClickListener profileClick = new OnClickListener() {
			
			public void onClick(View v) {
				MUCMessageActivity.Instance().showProfile(
						mUserData.get(pos).userId);
			}
		};
		
		if (holder == null) {
			holder = new ViewHolder();
			
			holder.name = (TextView) itemLayout.findViewById(R.id.name);
			holder.profile = (ImageView) itemLayout.findViewById(R.id.profile);
			
			itemLayout.setTag(holder);
		}
		
		if (pos % 2 == 1) {
			itemLayout.setBackgroundResource(R.drawable.background_list_gray);
		} else {
			itemLayout.setBackgroundResource(R.drawable.background_list_white);
		}
		
		UserData mData = mUserData.get(pos);
		
		holder.name.setText(mData.userName);
		
		File profileImg = new File(MUCMessageActivity.Instance().getProfile(
				mData.userId));
		
		Picasso.with(context).load(profileImg)
				.placeholder(R.drawable.chat_user_male).noFade()
				.into(holder.profile);
		
		if (profileImg.exists()) {
			holder.profile.setOnClickListener(profileClick);
		} else {
			holder.profile.setOnClickListener(null);
		}
		
		return itemLayout;
	}
}