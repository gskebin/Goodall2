package kr.co.dunet.goodall;

import java.util.ArrayList;

import kr.co.dunet.app.goodall.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class VoteAnswerAdapter extends BaseAdapter {
	private Context mContext = null;
	public ArrayList<VoteAnswer> mVoteAnswer = new ArrayList<VoteAnswer>();
	private int item_id = 0;
	
	public VoteAnswerAdapter(Context mContext) {
		super();
		this.mContext = mContext;
	}
	
	@Override
	public int getCount() {
		return mVoteAnswer.size();
	}
	
	@Override
	public VoteAnswer getItem(int position) {
		return mVoteAnswer.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void addItem(String answerCode, String mAnswserContent,
			boolean mAnswerChecked, int mAnswerCnt) {
		VoteAnswer addInfo = null;
		addInfo = new VoteAnswer();
		// item_id ++;
		addInfo.answerCode = answerCode;
		addInfo.answerContent = mAnswserContent;
		addInfo.answerChecked = mAnswerChecked;
		addInfo.answerCnt = mAnswerCnt;
		System.out.println("adapter add!");
		mVoteAnswer.add(addInfo);
		dataChange();
	}
	
	public void remove(int position) {
		mVoteAnswer.remove(position);
		dataChange();
	}
	
	public void sort() {
		// Collections.sort(mVoteAnswer, VoteAnswer.ALPHA_COMPARATOR);
		dataChange();
	}
	
	public void dataChange() {
		// mAdapter.notifyDataSetChanged();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		AnswerViewHolder holder;
		final int pos = position;
		
		if (convertView == null) {
			holder = new AnswerViewHolder();
			
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.vote_answer_item, null);
			
			holder.answerContent = (TextView) convertView
					.findViewById(R.id.answer_content);
			
			convertView.setTag(holder);
		} else {
			holder = (AnswerViewHolder) convertView.getTag();
		}
		
		VoteAnswer mData = mVoteAnswer.get(position);
		
		holder.answerContent.setText(mData.answerContent);
		// holder.voterCnt.setText(mData.voterCnt);
		
		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int cnt = getCount();
				
				for (int i = 0; i < cnt; i++) {
					if (i == pos) {
						mVoteAnswer.get(i).answerChecked = true;
					} else {
						mVoteAnswer.get(i).answerChecked = false;
					}
				}
				
				/*
				 * VoteAnswer selectData = mVoteAnswer.get(pos); if
				 * (selectData.answerChecked){
				 * v.setBackgroundResource(R.drawable.vote_answer_unchecked);
				 * selectData.answerChecked = false; }else {
				 * v.setBackgroundResource(R.drawable.vote_answer_checked);
				 * selectData.answerChecked = true; }
				 */
				VoteAnswerAdapter.this.notifyDataSetChanged();
			}
		});
		
		if (mData.answerChecked) {
			convertView.findViewById(R.id.answer_select_btn)
					.setBackgroundResource(R.drawable.vote_answer_selected);
		} else {
			convertView.findViewById(R.id.answer_select_btn)
					.setBackgroundResource(R.drawable.vote_answer_unselected);
		}
		
		return convertView;
	}
	
	public class AnswerViewHolder {
		public TextView answerContent;
	}
}