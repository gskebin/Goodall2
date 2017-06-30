package kr.co.dunet.goodall;

import java.util.ArrayList;

import kr.co.dunet.app.goodall.R;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class VoteListAdapter extends BaseAdapter {
	private Context mContext = null;
	public ArrayList<VoteData> mVoteData = new ArrayList<VoteData>();

	public VoteListAdapter(Context mContext) {
		super();
		this.mContext = mContext;
	}

	@Override
	public int getCount() {
		return mVoteData.size();
	}

	@Override
	public Object getItem(int position) {
		return mVoteData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void addItem(String mVoteCode, String mVoteName, String mVoterCnt,
			String mTotalCnt, String mRegistDate, String mVoteYn) {
		VoteData addInfo = null;
		addInfo = new VoteData();
		addInfo.voteCode = mVoteCode;
		addInfo.voteName = mVoteName;
		addInfo.voterCnt = mVoterCnt;
		addInfo.totalCnt = mTotalCnt;
		addInfo.registDate = mRegistDate;
		addInfo.voteYn = mVoteYn;

		mVoteData.add(addInfo);
	}

	public void remove(int position) {
		mVoteData.remove(position);
		dataChange();
	}

	public void sort() {
		// Collections.sort(mVoteData, VoteData.ALPHA_COMPARATOR);
		dataChange();
	}

	public void dataChange() {
		// mAdapter.notifyDataSetChanged();
	}

	public void orderByMsg() {
		System.out.println("ORDER BY MESSAGE CNT");
	}

	public void orderByLike() {
		System.out.println("ORDER BY LIKE CNT");
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		VoteViewHolder holder;
		final int pos = position;

		if (convertView == null) {
			holder = new VoteViewHolder();

			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.vote_item, null);

			holder.voteName = (TextView) convertView
					.findViewById(R.id.vote_name);
			holder.voterCnt = (TextView) convertView
					.findViewById(R.id.vote_voter_cnt);
			holder.totalCnt = (TextView) convertView
					.findViewById(R.id.vote_total_cnt);
			holder.registDate = (TextView) convertView
					.findViewById(R.id.vote_regist_date);
			holder.counter = (LinearLayout) convertView
					.findViewById(R.id.vote_voter_counter);

			convertView.setTag(holder);

			/*
			if (pos % 2 == 0) {
				convertView
						.setBackgroundResource(R.drawable.background_list_white);
			} else {
				convertView
						.setBackgroundResource(R.drawable.background_list_gray);
			}
			*/
		} else {
			holder = (VoteViewHolder) convertView.getTag();
		}

		if (pos % 2 == 0) {
			convertView
			.setBackgroundResource(R.drawable.background_list_white);
		} else {
			convertView
			.setBackgroundResource(R.drawable.background_list_gray);
		}
		
		VoteData mData = mVoteData.get(position);

		holder.voteName.setText(mData.voteName);
		holder.voterCnt.setText(mData.voterCnt);
		holder.totalCnt.setText(mData.totalCnt);
		holder.registDate.setText(mData.registDate);
		
		if(mData.totalCnt == "") {
			holder.counter.setVisibility(View.GONE);
			holder.registDate.setVisibility(View.GONE);
			holder.voteName.setGravity(Gravity.CENTER_HORIZONTAL);
		} else {
			holder.counter.setVisibility(View.VISIBLE);
			holder.registDate.setVisibility(View.VISIBLE);
			holder.voteName.setGravity(Gravity.LEFT);
		}

		return convertView;
	}

	public class VoteViewHolder {
		public TextView voteName;
		public TextView voterCnt;
		public TextView totalCnt;
		public TextView registDate;
		public LinearLayout counter;
	}
}