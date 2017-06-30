package kr.co.dunet.goodall;

import java.util.ArrayList;

import kr.co.dunet.app.goodall.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class VoteCreateAdapter extends BaseAdapter {
	private Context mContext = null;
	public ArrayList<VoteCreateData> mVoteCreate = new ArrayList<VoteCreateData>();
	private int item_id = 0;
	LayoutInflater mLayoutInflater = null;

	public VoteCreateAdapter(Context context) {
		super();
		mContext = context;
		//mVoteCreate = data;
		mLayoutInflater = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		return mVoteCreate.size();
	}

	@Override
	public Object getItem(int position) {
		return mVoteCreate.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void addItem(String mAnswserContent) {
		VoteCreateData addInfo = null;
		addInfo = new VoteCreateData();
		item_id++;
		addInfo.answerCode = "item" + item_id;
		addInfo.answerContent = mAnswserContent;
		mVoteCreate.add(addInfo);
	}

	public void remove(int position) {
		mVoteCreate.remove(position);
		dataChange();
	}

	public void sort() {
		// Collections.sort(mVoteCreate, VoteCreateData.ALPHA_COMPARATOR);
		dataChange();
	}

	public void dataChange() {
		// mAdapter.notifyDataSetChanged();
	}

	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int pos = position;
		/*
		AnswerViewHolder holder;

		if (convertView == null) {
			holder = new AnswerViewHolder();
	
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.vote_create_answer_item,
					null);
	
			holder.answerContent = (TextView) convertView
					.findViewById(R.id.answer_create_content);
	
			convertView.setTag(holder);
		} else {
			holder = (AnswerViewHolder) convertView.getTag();
		}
		*/
		View itemLayout = mLayoutInflater.inflate(R.layout.vote_create_answer_item,
				null);
		
		EditText answerContent = (EditText) itemLayout
				.findViewById(R.id.answer_create_content);
		
		answerContent.setText(mVoteCreate.get(position).answerContent);

		/*
		VoteCreateData mData = mVoteCreate.get(position);

		holder.answerContent.setText(mData.answerContent);
		*/

		// 아이템 삭제
		OnClickListener bClick = new OnClickListener() {
			@Override
			public void onClick(View v) {

				if(getCount() > 2) {
					mVoteCreate.remove(pos);
					VoteCreateAdapter.this.notifyDataSetChanged();
				} else {
				}
			}
		};
		
		ImageButton removeItem = (ImageButton) itemLayout
				.findViewById(R.id.answer_remove_btn);
		removeItem.setOnClickListener(bClick);
		
		/*
		holder.removeItem = (ImageButton) convertView
				.findViewById(R.id.answer_remove_btn);
		holder.removeItem.setOnClickListener(bClick);

		return convertView;
		 */
		
		return itemLayout;
	}

	public class AnswerViewHolder {
		public TextView answerContent;
		public ImageButton removeItem;
	}
}