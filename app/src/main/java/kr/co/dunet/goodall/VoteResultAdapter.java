package kr.co.dunet.goodall;

import java.util.ArrayList;

import kr.co.dunet.app.goodall.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class VoteResultAdapter extends BaseAdapter {
	private Context mContext = null;
	public ArrayList<VoteResult> mVoteAnswer = new ArrayList<VoteResult>();

	public VoteResultAdapter(Context mContext) {
		super();
		this.mContext = mContext;
	}

	@Override
	public int getCount() {
		return mVoteAnswer.size();
	}

	@Override
	public VoteResult getItem(int position) {
		return mVoteAnswer.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void addItem(String answerCode, String mAnswserContent,
			String mAnswerPercent, String mAnswerCnt, String mAnswerTotal) {
		VoteResult addInfo = null;
		addInfo = new VoteResult();
		// item_id ++;
		addInfo.answerCode = answerCode;
		addInfo.answerContent = mAnswserContent;
		addInfo.answerPercent = mAnswerPercent;
		addInfo.answerPerson = mAnswerCnt;
		addInfo.answerTotal = mAnswerTotal;
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
			convertView = inflater.inflate(R.layout.vote_result_item, null);

			holder.answerContent = (TextView) convertView
					.findViewById(R.id.answer_content);
			holder.answerPercent = (TextView) convertView
					.findViewById(R.id.answer_percent);
			holder.answerPerson = (TextView) convertView
					.findViewById(R.id.answer_person);
			holder.answerTotal = (TextView) convertView
					.findViewById(R.id.answer_total);

			convertView.setTag(holder);
		} else {
			holder = (AnswerViewHolder) convertView.getTag();
		}

		VoteResult mData = mVoteAnswer.get(position);

		holder.answerContent.setText(mData.answerContent);
		holder.answerPerson.setText(mData.answerPerson);
		holder.answerTotal.setText("/" + mData.answerTotal);
		holder.answerPercent.setText(mData.answerPercent + "%");

		return convertView;
	}

	public class AnswerViewHolder {
		public TextView answerContent;
		public TextView answerPercent;
		public TextView answerPerson;
		public TextView answerTotal;
	}
}