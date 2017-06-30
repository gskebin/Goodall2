package kr.co.dunet.goodall;

import java.io.File;
import java.util.List;
import kr.co.dunet.app.goodall.R;
import com.squareup.picasso.Picasso;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.Spannable;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ArrayAdapterEx extends ArrayAdapter<Chats> {
	
	private static LayoutInflater inflater = null;
	private static String copyText = null;
	// private static AlertDialog dialog = null;
	private static Dialog dialog = null;
	public List<Chats> m_List = null;
	public Boolean roomAdmin = false;
	
	public ArrayAdapterEx(Context context, int resource,
			int textViewResourceId, List<Chats> objects) {
		super(context, resource, textViewResourceId, objects);
		
		inflater = LayoutInflater.from(context);
		
		m_List = objects;
		
		ChatApplication app = (ChatApplication) context.getApplicationContext();
		roomAdmin = app.getRoomAdmin();
		
		// TODO Auto-generated constructor stub
	}
	
	class ViewHolder {
		View send_area;
		View recevie_area;
		
		TextView send_message;
		TextView send_vote;
		ImageView send_image;
		TextView send_date;
		TextView send_name;
		TextView send_like;
		ImageView send_profile;
		
		TextView recevie_message;
		TextView recevie_vote;
		ImageView recevie_image;
		TextView recevie_date;
		TextView recevie_name;
		TextView recevie_like;
		ImageView recevie_profile;
	}
	
	/**
	 * 특정 단어 텍스트 폰트컬러, 백그라운드컬러, 폰트사이즈 변경
	 * 
	 * @param view
	 * @param fulltext
	 * @param subtext
	 * @param fontColor
	 * @param backgroundColor
	 * @param fontSize
	 */
	public static void setTextViewColorPartial(TextView view, String fulltext,
			String subtext, Integer fontColor, Integer backgroundColor,
			Integer fontSize) {
		view.setText(fulltext, TextView.BufferType.SPANNABLE);
		Spannable str = (Spannable) view.getText();
		int i = fulltext.indexOf(subtext);
		if (i > -1) {
			if (fontColor != null) {
				str.setSpan(new ForegroundColorSpan(fontColor), i,
						i + subtext.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			if (backgroundColor != null) {
				str.setSpan(new BackgroundColorSpan(backgroundColor), i, i
						+ subtext.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			if (fontSize != null) {
				str.setSpan(new AbsoluteSizeSpan(fontSize), i,
						i + subtext.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
	}
	
	public void makeSearchText(TextView view, String fulltext,
			ViewGroup parent, Integer pos) {
		View searchSection = (View) ((ViewGroup) parent.getParent())
				.findViewById(R.id.searchSection);
		if (searchSection.getVisibility() == View.GONE) {
			return;
		}
		// EditText searchWord = (EditText)
		// ((ViewGroup)parent.getParent()).findViewById(R.id.searchWord);
		
		// String subtext = searchWord.getText().toString().trim();
		String subtext = MUCMessageActivity.Instance().getSearchText();
		Integer selected_pos = MUCMessageActivity.Instance().search_pos;
		if (subtext == null || subtext.equals("")) {
			return;
		}
		
		if (selected_pos == pos) {
			setTextViewColorPartial(view, fulltext, subtext, 0xff3284b6,
					0xffffff00, null);
		} else {
			setTextViewColorPartial(view, fulltext, subtext, 0xffffffff,
					0xff3284b6, null);
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View itemLayout = super.getView(position, convertView, parent);
		
		final int pos = position;
		final Context context = parent.getContext();
		
		ViewHolder viewHolder = (ViewHolder) itemLayout.getTag();
		
		OnClickListener photoClick = new OnClickListener() {
			
			public void onClick(View v) {
				// System.out.println("photo Click2!!!" + pos);
				// System.out.println("photo Click!!!" +
				// m_List.get(pos).mMessage.split(",")[1]);
				
				((MUCMessageActivity) getContext())
						.showPhoto(m_List.get(pos).mMessage.split(",")[0]);
			}
		};
		
		OnClickListener profileClick = new OnClickListener() {
			
			public void onClick(View v) {
				((MUCMessageActivity) getContext())
						.showProfile(m_List.get(pos).mId);
			}
		};
		
		OnClickListener voteClick = new OnClickListener() {
			
			public void onClick(View v) {
				((MUCMessageActivity) getContext()).goVote();
			}
		};
		
		OnLongClickListener lClick = new OnLongClickListener() {
			
			@SuppressLint("ViewHolder")
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				
				// System.out.println("TEST!!!!" + test.getText());
				View dialoglayout = inflater.inflate(R.layout.toast_msg, null);
				
				TextView btn_copy = (TextView) dialoglayout
						.findViewById(R.id.chat_toast_copy);
				TextView btn_recomm = (TextView) dialoglayout
						.findViewById(R.id.chat_toast_recomm);
				TextView btn_del = (TextView) dialoglayout
						.findViewById(R.id.chat_toast_delete);
				
				if (roomAdmin != true) {
					btn_del.setVisibility(View.GONE);
				}
				if (!m_List.get(pos).mType.equals("msg")) {
					btn_copy.setVisibility(View.GONE);
				} else {
					btn_copy.setVisibility(View.VISIBLE);
					
					TextView test = (TextView) v;
					
					copyText = test.getText().toString();
				}
				
				// 복사하기
				btn_copy.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// System.out.println("COPY TEST!!!!");
						setClipBoardLink(getContext(), copyText);
					}
				});
				
				// 추천하기
				btn_recomm.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO 추천
						
						System.out.println("POSITON RID "
								+ m_List.get(pos).mRid);
						/*
						m_List.get(pos).mLike = String.valueOf((Integer
								.parseInt(m_List.get(pos).mLike) + 1));
						*/
						((MUCMessageActivity) getContext()).sendRecomm(m_List
								.get(pos).mRid);
						// dialog.hide();
						dialog.dismiss();
					}
				});
				
				// 삭제하기
				btn_del.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO 삭제
						
						System.out.println("DELETE RID " + m_List.get(pos).mRid);
						((MUCMessageActivity) getContext()).sendDelete(
								m_List.get(pos).mRid, pos);
						m_List.remove(pos);
						
						// dialog.hide();
						dialog.dismiss();
					}
				});
				
				/*
				 * AlertDialog.Builder builder = new AlertDialog.Builder(
				 * inflater.getContext(), R.style.CustomDialog);
				 * builder.setView(dialoglayout); //
				 * builder.setCancelable(false); dialog = builder.create();
				 * 
				 * dialog.setCanceledOnTouchOutside(true);
				 * 
				 * dialog.show();
				 */
				dialog = new Dialog(inflater.getContext(), R.style.CustomDialog);
				dialog.setContentView(dialoglayout);
				dialog.setCanceledOnTouchOutside(true);
				dialog.show();
				
				return true;
			}
		};
		
		if (viewHolder == null) {
			viewHolder = new ViewHolder();
			
			viewHolder.send_message = (TextView) itemLayout
					.findViewById(R.id.send_message_text);
			viewHolder.send_vote = (TextView) itemLayout
					.findViewById(R.id.send_vote_text);
			viewHolder.send_image = (ImageView) itemLayout
					.findViewById(R.id.send_message_image);
			viewHolder.send_date = (TextView) itemLayout
					.findViewById(R.id.send_date);
			viewHolder.send_name = (TextView) itemLayout
					.findViewById(R.id.send_name);
			viewHolder.send_like = (TextView) itemLayout
					.findViewById(R.id.send_like);
			viewHolder.send_profile = (ImageView) itemLayout
					.findViewById(R.id.send_profile);
			
			viewHolder.recevie_message = (TextView) itemLayout
					.findViewById(R.id.recevie_message_text);
			viewHolder.recevie_vote = (TextView) itemLayout
					.findViewById(R.id.recevie_vote_text);
			viewHolder.recevie_image = (ImageView) itemLayout
					.findViewById(R.id.recevie_message_image);
			viewHolder.recevie_date = (TextView) itemLayout
					.findViewById(R.id.recevie_date);
			viewHolder.recevie_name = (TextView) itemLayout
					.findViewById(R.id.recevie_name);
			viewHolder.recevie_like = (TextView) itemLayout
					.findViewById(R.id.recevie_like);
			viewHolder.recevie_profile = (ImageView) itemLayout
					.findViewById(R.id.recevie_profile);
			
			viewHolder.recevie_area = (View) itemLayout
					.findViewById(R.id.recevie_area);
			viewHolder.send_area = (View) itemLayout
					.findViewById(R.id.send_area);
			
			itemLayout.setTag(viewHolder);
		}
		
		// int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
		// 210, context.getResources().getDisplayMetrics());
		File profileImg = new File(getItem(pos).mProfile);
		String chatName = MUCMessageActivity.Instance().getChatName(getItem(pos).mId);
		if(chatName.equals("")) {
			chatName = getItem(pos).mName;
		}
		if (getItem(pos).isMine == "Y") {
			viewHolder.send_message.setText(getItem(pos).mMessage);
			
			// Linkify.addLinks(viewHolder.send_message, Linkify.WEB_URLS);
			viewHolder.send_date.setText(getItem(pos).mDate);
			//viewHolder.send_name.setText(getItem(pos).mName);
			viewHolder.send_name.setText(chatName);
			String like = Integer.parseInt(getItem(pos).mLike) > 0 ? "♥"
					+ getItem(pos).mLike : "";
			viewHolder.send_like.setText(like);
			// System.out.println("like test " + getItem(pos).mLike);
			
			// 프로필 이미지
			Picasso.with(context).load(profileImg)
					.placeholder(R.drawable.chat_user_male).noFade()
					.into(viewHolder.send_profile);
			
			viewHolder.send_image.setVisibility(View.GONE);
			viewHolder.send_message.setVisibility(View.GONE);
			viewHolder.send_vote.setVisibility(View.GONE);
			
			if (getItem(pos).mType.equals("msg")) {
				makeSearchText(viewHolder.send_message,
						getItem(pos).mMessage, parent, pos);
				
				//viewHolder.send_image.setVisibility(View.GONE);
				viewHolder.send_message.setVisibility(View.VISIBLE);
				//viewHolder.send_vote.setVisibility(View.GONE);
				
				viewHolder.send_message.setOnLongClickListener(lClick);
			} else if (getItem(pos).mType.equals("vote")) {
				viewHolder.send_vote.setText(getItem(pos).mMessage);
				
				//viewHolder.send_image.setVisibility(View.GONE);
				//viewHolder.send_message.setVisibility(View.GONE);
				viewHolder.send_vote.setVisibility(View.VISIBLE);
				
				viewHolder.send_vote.setOnClickListener(voteClick);
				viewHolder.send_vote.setOnLongClickListener(lClick);
			} else {
				viewHolder.send_image.setVisibility(View.VISIBLE);
				// viewHolder.send_image.setImageBitmap(getItem(pos).mPhoto);
				// Glide.with(getContext()).load(getItem(pos).mPhotoPath).placeholder(R.drawable.splash_new).into(viewHolder.send_image);
				// Picasso.with(context).load(getItem(pos).mPhotoPath).placeholder(R.drawable.splash_new).fit().into(viewHolder.send_image);
				Picasso.with(context)
						.load(new File(getItem(pos).mPhotoPath))
						.placeholder(R.drawable.splash_new).noFade()
						.into(viewHolder.send_image);
				
				// viewHolder.send_image.setLayoutParams(new
				// LinearLayout.LayoutParams(px,
				// LinearLayout.LayoutParams.WRAP_CONTENT));
				// Log.d("Image Photo", getItem(pos).mPhoto.toString());
				//viewHolder.send_message.setVisibility(View.GONE);
				//viewHolder.send_vote.setVisibility(View.GONE);
				
				viewHolder.send_image.setOnClickListener(photoClick);
				viewHolder.send_image.setOnLongClickListener(lClick);
			}
			
			if (profileImg.exists()) {
				viewHolder.send_profile.setOnClickListener(profileClick);
			} else {
				viewHolder.send_profile.setOnClickListener(null);
			}
			
			viewHolder.send_area.setVisibility(View.VISIBLE);
			viewHolder.recevie_area.setVisibility(View.GONE);
		} else {
			viewHolder.recevie_message.setText(getItem(pos).mMessage);
			
			// Linkify.addLinks(viewHolder.recevie_message, Linkify.WEB_URLS);
			viewHolder.recevie_date.setText(getItem(pos).mDate);
			//viewHolder.recevie_name.setText(getItem(pos).mName);
			viewHolder.recevie_name.setText(chatName);
			
			String like = Integer.parseInt(getItem(pos).mLike) > 0 ? "♥"
					+ getItem(pos).mLike : "";
			viewHolder.recevie_like.setText(like);
			
			// 프로필 이미지
			
			Picasso.with(context).load(profileImg)
					.placeholder(R.drawable.chat_user_male).noFade()
					.into(viewHolder.recevie_profile);
			
			viewHolder.recevie_image.setVisibility(View.GONE);
			viewHolder.recevie_message.setVisibility(View.GONE);
			viewHolder.recevie_vote.setVisibility(View.GONE);
			
			if (getItem(pos).mType.equals("msg")) {
				makeSearchText(viewHolder.recevie_message,
						getItem(pos).mMessage, parent, pos);
				
				//viewHolder.recevie_image.setVisibility(View.GONE);
				viewHolder.recevie_message.setVisibility(View.VISIBLE);
				//viewHolder.recevie_vote.setVisibility(View.GONE);
				
				viewHolder.recevie_message.setOnLongClickListener(lClick);
			} else if (getItem(pos).mType.equals("vote")) {
				viewHolder.recevie_vote.setText(getItem(pos).mMessage);
				
				//viewHolder.recevie_image.setVisibility(View.GONE);
				//viewHolder.recevie_message.setVisibility(View.GONE);
				viewHolder.recevie_vote.setVisibility(View.VISIBLE);
				
				viewHolder.recevie_vote.setOnClickListener(voteClick);
				viewHolder.recevie_vote.setOnLongClickListener(lClick);
			} else {
				viewHolder.recevie_image.setVisibility(View.VISIBLE);
				// viewHolder.recevie_image.setImageBitmap(getItem(pos).mPhoto);
				// Glide.with(getContext()).load(getItem(pos).mPhotoPath).placeholder(R.drawable.splash_new).into(viewHolder.recevie_image);
				// Picasso.with(context).load(getItem(pos).mPhotoPath).placeholder(R.drawable.splash_new).into(viewHolder.recevie_image);
				Picasso.with(context)
						.load(new File(getItem(pos).mPhotoPath))
						.placeholder(R.drawable.splash_new).noFade()
						.into(viewHolder.recevie_image);
				
				//viewHolder.recevie_message.setVisibility(View.GONE);
				//viewHolder.recevie_vote.setVisibility(View.GONE);
				
				viewHolder.recevie_image.setOnClickListener(photoClick);
				viewHolder.recevie_image.setOnLongClickListener(lClick);
			}
			
			if (profileImg.exists()) {
				viewHolder.recevie_profile.setOnClickListener(profileClick);
			} else {
				viewHolder.recevie_profile.setOnClickListener(null);
			}
			
			viewHolder.send_area.setVisibility(View.GONE);
			viewHolder.recevie_area.setVisibility(View.VISIBLE);
		}
		
		return itemLayout;
	}
	
	/**
	 * 클립보드에 복사 기능
	 * 
	 * @param context
	 * @param link
	 */
	public static void setClipBoardLink(Context context, String link) {
		ClipboardManager clipboardManager = (ClipboardManager) context
				.getSystemService(context.CLIPBOARD_SERVICE);
		ClipData clipData = ClipData.newPlainText("label", link);
		clipboardManager.setPrimaryClip(clipData);
		
		Toast.makeText(context,
				context.getString(R.string.toast_text_clipboard_adress),
				Toast.LENGTH_SHORT).show();
		
		// dialog.hide();
		dialog.dismiss();
	}
}