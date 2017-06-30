package kr.co.dunet.goodall;

import java.util.List;
import kr.co.dunet.app.goodall.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ImageButton;

public class RoomListAdapter extends ArrayAdapter<RoomData> {
	
	private static LayoutInflater inflater = null;
	public List<RoomData> mRoomData;
	
	public RoomListAdapter(Context context, int resource,
			int textViewResourceId, List<RoomData> objects) {
		super(context, resource, textViewResourceId, objects);
		
		inflater = LayoutInflater.from(context);
		
		mRoomData = objects;
		
		// TODO Auto-generated constructor stub
	}
	
	class ViewHolder {
		TextView roomName;
		TextView roomCode;
		TextView roomAdminNickname;
		TextView roomDate;
		TextView noRead;
		ImageButton exitRoom;
	}
	
	public void addItem(String mName, String mCode, String mCode2,
			String mDate, String mPublicYn, String mPassword, String admin,
			Integer noRead, String modDate) {
		RoomData addInfo = null;
		addInfo = new RoomData();
		addInfo.roomName = mName;
		// 룸코드만 있음
		addInfo.roomCode = mCode;
		// 비밀번호까지 섞여있는 코드
		addInfo.roomCode2 = mCode2;
		addInfo.roomDate = mDate.substring(0, 10);
		addInfo.publicYn = mPublicYn;
		addInfo.roomPw = mPassword;
		addInfo.roomAdmin = admin;
		addInfo.roomAdminNickname = ListActivity.Instance().getAdminName(admin);
		addInfo.noRead = noRead;
		addInfo.modDate = modDate == null ? mDate : modDate;
		
		mRoomData.add(addInfo);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View itemLayout = super.getView(position, convertView, parent);
		
		ViewHolder holder = (ViewHolder) itemLayout.getTag();
		
		final int pos = position;
		final Context context = parent.getContext();
		
		if (holder == null) {
			holder = new ViewHolder();
			
			holder.roomName = (TextView) itemLayout
					.findViewById(R.id.room_name);
			holder.roomCode = (TextView) itemLayout
					.findViewById(R.id.room_code);
			holder.roomAdminNickname = (TextView) itemLayout
					.findViewById(R.id.admin_nick_name);
			holder.roomDate = (TextView) itemLayout
					.findViewById(R.id.room_date);
			holder.noRead = (TextView) itemLayout
					.findViewById(R.id.new_message);
			holder.exitRoom = (ImageButton) itemLayout
					.findViewById(R.id.btnExitRoom);
			
			itemLayout.setTag(holder);
		}
		
		if (pos % 2 == 1) {
			itemLayout.setBackgroundResource(R.drawable.background_list_gray);
		} else {
			itemLayout.setBackgroundResource(R.drawable.background_list_white);
		}
		
		RoomData mData = mRoomData.get(pos);

		holder.roomName.setVisibility(View.GONE);
		holder.roomAdminNickname.setVisibility(View.GONE);
		//holder.roomCode.setVisibility(View.GONE);
		
		holder.roomName.setText(mData.roomName);
		holder.roomAdminNickname.setText(mData.roomAdminNickname);
		holder.roomCode.setText("/" + mData.roomCode2);
		holder.roomDate.setText(mData.roomDate);
		
		holder.roomName.setVisibility(View.VISIBLE);
		holder.roomAdminNickname.setVisibility(View.VISIBLE);
		//holder.roomCode.setVisibility(View.VISIBLE);
		
		if (mData.noRead > 0) {
			if (mData.noRead > 99) {
				holder.noRead.setText("99");
			} else {
				holder.noRead.setText(mData.noRead.toString());
			}
			holder.noRead.setVisibility(View.VISIBLE);
		} else {
			holder.noRead.setVisibility(View.GONE);
		}
		
		if (mData.exitView.equals("Y")) {
			holder.exitRoom.setVisibility(View.VISIBLE);
			holder.roomDate.setVisibility(View.GONE);
		} else {
			holder.exitRoom.setVisibility(View.GONE);
			holder.roomDate.setVisibility(View.VISIBLE);
		}
		
		itemLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if (mRoomData.get(pos).exitView.equals("Y")) {
					mRoomData.get(pos).exitView = "N";
				} else {
					// mRoomData.get(pos).exitView = "Y";
					((ListActivity) getContext()).enterRoom(mRoomData.get(pos).roomCode);
				}
				
				RoomListAdapter.this.notifyDataSetChanged();
			}
		});
		
		OnLongClickListener lClick = new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (mRoomData.get(pos).exitView.equals("Y")) {
					mRoomData.get(pos).exitView = "N";
				} else {
					mRoomData.get(pos).exitView = "Y";
				}
				
				RoomListAdapter.this.notifyDataSetChanged();
				return true;
			}
		};
		itemLayout.setOnLongClickListener(lClick);
		
		// 방 나가기
		OnClickListener bClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				System.out.println("==============EXIT ROOM : "
						+ mRoomData.get(pos).roomCode);
				
				v.setVisibility(View.GONE);
				((View) v.getParent()).findViewById(R.id.room_date)
						.setVisibility(View.VISIBLE);
				
				((ListActivity) getContext())
						.exitRoom(mRoomData.get(pos).roomCode);
				
				mRoomData.remove(pos);
			}
		};
		holder.exitRoom.setOnClickListener(bClick);
		
		// convertView.setOnLongClickListener(lClick);
		
		return itemLayout;
	}
}