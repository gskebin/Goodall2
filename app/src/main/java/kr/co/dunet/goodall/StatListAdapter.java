package kr.co.dunet.goodall;

import java.util.ArrayList;

import kr.co.dunet.app.goodall.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class StatListAdapter extends BaseAdapter {
	private Context mContext = null;
	public ArrayList<StatData> mStatData = new ArrayList<StatData>();

	public StatListAdapter(Context mContext) {
		super();
		this.mContext = mContext;
	}

	@Override
	public int getCount() {
		return mStatData.size();
	}

	@Override
	public Object getItem(int position) {
		return mStatData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

    public void addItem(String mUserName, String mMsgCnt, String mMsgPer, String mLikeCnt, String mLikePer){
        StatData addInfo = null;
        addInfo = new StatData();
        addInfo.userName = mUserName;
        addInfo.msgCnt = mMsgCnt;
        addInfo.msgPer = mMsgPer;
        addInfo.likeCnt = mLikeCnt;
        addInfo.likePer = mLikePer;
        
        mStatData.add(addInfo);
    }
    
    public void remove(int position){
    	mStatData.remove(position);
        dataChange();
    }
    
    public void sort(){
        //Collections.sort(mStatData, StatData.ALPHA_COMPARATOR);
        dataChange();
    }
    
    public void dataChange(){
        //mAdapter.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	StatViewHolder holder;
		final int pos = position;
		
        if (convertView == null) {
            holder = new StatViewHolder();
            
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.stat_item, null);
            
            holder.userName = (TextView) convertView.findViewById(R.id.stat_user_name);
            holder.msgCnt = (TextView) convertView.findViewById(R.id.stat_msg_cnt);
            holder.msgPer = (TextView) convertView.findViewById(R.id.stat_msg_per);
            holder.likeCnt = (TextView) convertView.findViewById(R.id.stat_like_cnt);
            holder.likePer = (TextView) convertView.findViewById(R.id.stat_like_per);
            
            convertView.setTag(holder);

            /*
    		if (pos%2 == 1){
    			convertView.setBackgroundResource(R.drawable.background_list_gray);
    		} else {
    			convertView.setBackgroundResource(R.drawable.background_list_white);
    		}
    		*/
        }else{
            holder = (StatViewHolder) convertView.getTag();
        }
        
        if (pos%2 == 0){
        	convertView.setBackgroundResource(R.drawable.background_list_white);
        } else {
        	convertView.setBackgroundResource(R.drawable.background_list_gray);
        }
        
        StatData mData = mStatData.get(position);

        holder.userName.setText(mData.userName);
        holder.msgCnt.setText(mData.msgCnt);
        holder.msgPer.setText(mData.msgPer);
        holder.likeCnt.setText(mData.likeCnt);
        holder.likePer.setText(mData.likePer);
        
        return convertView;
    }

    public class StatViewHolder {
        public TextView userName;
        public TextView msgCnt;
        public TextView msgPer;
        public TextView likeCnt;
        public TextView likePer;
    }
}