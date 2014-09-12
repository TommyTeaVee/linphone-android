package org.linphone.ui;
/*
BubbleChat.java
Copyright (C) 2012  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;

import org.linphone.LinphoneUtils;
import org.linphone.R;
import org.linphone.core.LinphoneChatMessage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

/**
 * @author Sylvain Berfini
 */
@SuppressLint("SimpleDateFormat")
public class BubbleChat {
	private static final HashMap<String, Integer> emoticons = new HashMap<String, Integer>();
	static {
	    emoticons.put(":)", R.drawable.emo_im_happy);
	    emoticons.put(":-)", R.drawable.emo_im_happy);
	    emoticons.put(":(", R.drawable.emo_im_sad);
	    emoticons.put(":-(", R.drawable.emo_im_sad);
	    emoticons.put(":-P", R.drawable.emo_im_tongue_sticking_out);
	    emoticons.put(":P", R.drawable.emo_im_tongue_sticking_out);
	    emoticons.put(";-)", R.drawable.emo_im_winking);
	    emoticons.put(";)", R.drawable.emo_im_winking);
	    emoticons.put(":-D", R.drawable.emo_im_laughing);
	    emoticons.put(":D", R.drawable.emo_im_laughing);
	    emoticons.put("8-)", R.drawable.emo_im_cool);
	    emoticons.put("8)", R.drawable.emo_im_cool);
	    emoticons.put("O:)", R.drawable.emo_im_angel);
	    emoticons.put("O:-)", R.drawable.emo_im_angel);
	    emoticons.put(":-*", R.drawable.emo_im_kissing);
	    emoticons.put(":*", R.drawable.emo_im_kissing);
	    emoticons.put(":-/", R.drawable.emo_im_undecided);
	    emoticons.put(":/ ", R.drawable.emo_im_undecided); // The space after is needed to avoid bad display of links
	    emoticons.put(":-\\", R.drawable.emo_im_undecided);
	    emoticons.put(":\\", R.drawable.emo_im_undecided);
	    emoticons.put(":-O", R.drawable.emo_im_surprised);
	    emoticons.put(":O", R.drawable.emo_im_surprised);
	    emoticons.put(":-@", R.drawable.emo_im_yelling);
	    emoticons.put(":@", R.drawable.emo_im_yelling);
	    emoticons.put("O.o", R.drawable.emo_im_wtf);
	    emoticons.put("o.O", R.drawable.emo_im_wtf);
	    emoticons.put(":'(", R.drawable.emo_im_crying);
	    emoticons.put("$.$", R.drawable.emo_im_money_mouth);
	}
	
	private RelativeLayout view;
	private ImageView statusView;
	private Button download;
	private LinphoneChatMessage message;
	private Context context;
	private BubbleChatActionListener listener;
	private LinearLayout layout;
	private ImageView imageView;
	
	public BubbleChat(Context context, ViewGroup parent, LinphoneChatMessage message, BubbleChatActionListener listener) {
		this.message = message;
		this.context = context;
		this.listener = listener;
		view = new RelativeLayout(context);
		
		LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    	
    	if (!message.isOutgoing()) {
    		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
    		view.setBackgroundResource(R.drawable.chat_bubble_incoming);
    	}
    	else {
    		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    		view.setBackgroundResource(R.drawable.chat_bubble_outgoing);
    	}

    	layoutParams.setMargins(10, 0, 10, 0);
    	
    	view.setId(message.getStorageId());
    	view.setLayoutParams(layoutParams);
    	
    	
    	Spanned text = null;
    	if (message.getText() != null) {
	    	if (context.getResources().getBoolean(R.bool.emoticons_in_messages)) {
	    		text = getSmiledText(context, getTextWithHttpLinks(message.getText()));
	    	} else {
	    		text = getTextWithHttpLinks(message.getText());
	    	}
    	}
    	
    	if (context.getResources().getBoolean(R.bool.display_time_aside)) {
	    	if (!message.isOutgoing()) {
	    		layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.chat_bubble_alt_incoming, parent);
	    	} else {
	    		layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.chat_bubble_alt_outgoing, parent);
	    	}
    	} else {
    		if (!message.isOutgoing()) {
	    		layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.chat_bubble_incoming, parent);
	    	} else {
	    		layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.chat_bubble_outgoing, parent);
	    	}
    	}
    	
    	TextView msgView = (TextView) layout.findViewById(R.id.message);
    	if (text != null && msgView != null) {
	    	msgView.setText(text);
	    	msgView.setMovementMethod(LinkMovementMethod.getInstance());
    	} else if (msgView != null) {
    		msgView.setVisibility(View.GONE);
    	}
    	
    	imageView = (ImageView) layout.findViewById(R.id.image);
    	download = (Button) layout.findViewById(R.id.download);
    	
    	if (download != null && message.getFileTransferInformation() != null) {
	    	final String imageName = message.getFileTransferInformation().getName();
	    	if (imageName != null && !LinphoneUtils.isExistingFile(context, imageName) && !message.isOutgoing()) {
	    		download.setVisibility(View.VISIBLE);
	    		download.setText(context.getString(R.string.download_image));
	    		download.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (BubbleChat.this.listener != null) {
							setDownloadInProgressLayout();
							BubbleChat.this.listener.onDownloadButtonClick();
						}
					}
				});
    		} else {
    			displayImageIfPossible(imageName);
    		}
    	}
    	
    	TextView timeView = (TextView) layout.findViewById(R.id.time);
    	timeView.setText(timestampToHumanDate(context, message.getTime()));
    	
    	statusView = (ImageView) layout.findViewById(R.id.status);
    	if (statusView != null) {
    		if (message.getStatus() == LinphoneChatMessage.State.Delivered) {
    			statusView.setImageResource(R.drawable.chat_message_delivered);
    		} else if (message.getStatus() == LinphoneChatMessage.State.NotDelivered) {
    			statusView.setImageResource(R.drawable.chat_message_not_delivered);
    		} else {
    			statusView.setImageResource(R.drawable.chat_message_inprogress);
    		}
    	}
    	
    	view.addView(layout);
	}
	
	public void setDownloadInProgressLayout() {
		download.setEnabled(false);
		download.setText(BubbleChat.this.context.getString(R.string.downloading_image));
	}
	
	private void displayImageIfPossible(final String imageName) {
		Bitmap bm = LinphoneUtils.readBitmapFromFile(context, imageName);
		if (bm != null) {
			imageView.setImageBitmap(bm);
			imageView.setVisibility(View.VISIBLE);
			download.setVisibility(View.GONE);
			
			if (imageView != null) {
	    		imageView.setOnClickListener(new OnClickListener() {
	    			@Override
	    			public void onClick(View v) {
	    				Intent intent = new Intent(Intent.ACTION_VIEW);
	    				intent.setDataAndType(Uri.parse("file://" + LinphoneUtils.getBitmapPathFromFile(context, imageName)), "image/*");
	    				BubbleChat.this.context.startActivity(intent);
	    			}
	    		});
	    	}
		}
	}
	
	private String timestampToHumanDate(Context context, long timestamp) {
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(timestamp);
			
			SimpleDateFormat dateFormat;
			if (isToday(cal)) {
				dateFormat = new SimpleDateFormat(context.getResources().getString(R.string.today_date_format));
			} else {
				dateFormat = new SimpleDateFormat(context.getResources().getString(R.string.messages_date_format));
			}
			
			return dateFormat.format(cal.getTime());
		} catch (NumberFormatException nfe) {
			return String.valueOf(timestamp);
		}
	}
	
	private boolean isToday(Calendar cal) {
        return isSameDay(cal, Calendar.getInstance());
    }
	
	private boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            return false;
        }
        
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }
	
	public static Spannable getSmiledText(Context context, Spanned spanned) {
		SpannableStringBuilder builder = new SpannableStringBuilder(spanned);
		String text = spanned.toString();

		for (Entry<String, Integer> entry : emoticons.entrySet()) {
			String key = entry.getKey();
			int indexOf = text.indexOf(key);
			while (indexOf >= 0) {
				int end = indexOf + key.length();
				builder.setSpan(new ImageSpan(context, entry.getValue()), indexOf, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				indexOf = text.indexOf(key, end);
			}
		}
		
		return builder;
	}
	
	public static Spanned getTextWithHttpLinks(String text) {
		if (text.contains("http://")) {
			int indexHttp = text.indexOf("http://");
			int indexFinHttp = text.indexOf(" ", indexHttp) == -1 ? text.length() : text.indexOf(" ", indexHttp);
			String link = text.substring(indexHttp, indexFinHttp);
			String linkWithoutScheme = link.replace("http://", "");
			text = text.replaceFirst(link, "<a href=\"" + link + "\">" + linkWithoutScheme + "</a>");
		}
		if (text.contains("https://")) {
			int indexHttp = text.indexOf("https://");
			int indexFinHttp = text.indexOf(" ", indexHttp) == -1 ? text.length() : text.indexOf(" ", indexHttp);
			String link = text.substring(indexHttp, indexFinHttp);
			String linkWithoutScheme = link.replace("https://", "");
			text = text.replaceFirst(link, "<a href=\"" + link + "\">" + linkWithoutScheme + "</a>");
		}
		
		return Html.fromHtml(text);
	}
	
	public View getView() {
		return view;
	}
	
	public LinphoneChatMessage getNativeMessageObject() {
		return message;
	}
	
	public interface BubbleChatActionListener {
		void onDownloadButtonClick();
	}
}
