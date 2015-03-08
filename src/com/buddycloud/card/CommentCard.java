package com.buddycloud.card;

import java.text.ParseException;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.buddycloud.FullScreenImageActivity;
import com.buddycloud.MainActivity;
import com.buddycloud.R;
import com.buddycloud.customviews.MeasuredMediaView;
import com.buddycloud.customviews.MeasuredMediaView.MeasureListener;
import com.buddycloud.model.MediaModel;
import com.buddycloud.model.PostsModel;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.AvatarUtils;
import com.buddycloud.utils.ImageHelper;
import com.buddycloud.utils.TextUtils;
import com.buddycloud.utils.TimeUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

public class CommentCard extends AbstractCard {

	private static final String MEDIA_URL_SUFIX = "?maxwidth=600";
	private static final String MEDIA_URL_SUFIX_FULL = "?maxwidth=1024";
	
	private Spanned anchoredContent;
	private MainActivity activity;
	private JSONObject comment;
	private String channelJid;
	private String role;

	public CommentCard(String channelJid, JSONObject comment,
			MainActivity activity, String role) {
		this.channelJid = channelJid;
		this.comment = comment;
		this.activity = activity;
		this.role = role;
		this.anchoredContent = TextUtils.anchor(comment.optString("content"));
	}

	@Override
	public JSONObject getPost() {
		return comment;
	}

	@Override
	public View getContentView(int position, View convertView,
			final ViewGroup viewGroup) {

		final String replyAuthor = comment.optString("author");
		String published = comment.optString("published");
		String mediaStr = comment.optString("media");
		final String replyId = comment.optString("id");
		
		JSONArray mediaArray = null;
		if (mediaStr != null && mediaStr.length() > 0) {
			try {
				mediaArray = new JSONArray(mediaStr);
			} catch (JSONException e) {}
		}
		
		boolean reuse = convertView != null && convertView.getTag() != null;
		CardViewHolder holder = null;

		if (!reuse) {
			LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
			convertView = inflater.inflate(R.layout.comment_entry, viewGroup, false);
			holder = fillHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (CardViewHolder) convertView.getTag();
		}

		final Context context = viewGroup.getContext();
		String avatarURL = AvatarUtils.avatarURL(viewGroup.getContext(), replyAuthor);
		DisplayImageOptions dio = new DisplayImageOptions.Builder()
				.cloneFrom(ImageHelper.defaultImageOptions())
				.showImageOnFail(R.drawable.ic_avatar)
				.showImageOnLoading(R.drawable.ic_avatar)
				.preProcessor(ImageHelper.createRoundProcessor(16, false, -1))
				.build();

		ImageView avatarView = holder.getView(R.id.bcProfilePic);
		ImageLoader.getInstance().displayImage(avatarURL, avatarView, dio);
		avatarView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!channelJid.equals(replyAuthor)) {
					activity.showChannelFragment(replyAuthor);
				}
			}
		});

		TextView replyAuthorView = holder.getView(R.id.bcPostAuthor);
		if (!TextUtils.isEmpty(replyAuthor)) {
			replyAuthorView.setText(TextUtils.capitalize(replyAuthor.trim()));
		}
		
		final TextView contentTextView = holder.getView(R.id.bcCommentContent);
		final TextView contentTextViewAlt = holder.getView(R.id.bcCommentPending);
		final MeasuredMediaView mediaView = holder.getView(R.id.bcImageContent);
		mediaView.setImageBitmap(null);
		drawNoMediaLayout(contentTextView, contentTextViewAlt, mediaView);
		
		if (mediaArray != null) {
			drawMediaLayout(mediaArray, context, contentTextView,
				contentTextViewAlt, mediaView);
		}

		View contextArrowDown = holder.getView(R.id.bcArrowDown);
		contextArrowDown.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PostContextUtils.showPostContextActions(viewGroup.getContext(),
						channelJid, replyId, role, PostContextUtils.COMMENT_POST);
			}
		});

		TextView publishedView = holder.getView(R.id.bcPostDate);
		TextView publishedPendingView = holder.getView(R.id.bcCommentPending);
		boolean pending = PostsModel.isPending(comment);
		if (!pending) {
			try {
				long publishedTime = TimeUtils.fromISOToDate(published).getTime();
				publishedView.setText(DateUtils.getRelativeTimeSpanString(
						publishedTime, new Date().getTime(),
						DateUtils.MINUTE_IN_MILLIS));
				publishedPendingView.setVisibility(View.GONE);
				contextArrowDown.setVisibility(View.VISIBLE);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else {
			publishedView.setText(Html.fromHtml("&#x231A;"));
			publishedPendingView.setVisibility(View.VISIBLE);
			contextArrowDown.setVisibility(View.GONE);
		}

		return convertView;
	}

	private static CardViewHolder fillHolder(View view) {
		return CardViewHolder.create(view, R.id.bcProfilePic,
				R.id.postContainer, R.id.commentTitleWrapper,
				R.id.bcPostAuthor, R.id.bcPostDate, R.id.bcTopRightView,
				R.id.bcArrowDown, R.id.bcCommentPending, R.id.bcImageContent,
				R.id.bcCommentContent);
	}

	private void drawNoMediaLayout(TextView contentTextView,
			TextView contentTextViewAlt, final MeasuredMediaView mediaView) {
		mediaView.setVisibility(View.GONE);
		contentTextViewAlt.setVisibility(View.GONE);
		contentTextView.setVisibility(View.VISIBLE);
		contentTextView.setText(anchoredContent);
		contentTextView.setMovementMethod(LinkMovementMethod.getInstance());
	}
	
	private void drawMediaLayout(final JSONArray mediaArray, final Context context,
			final TextView contentTextView, final TextView contentTextViewAlt,
			final MeasuredMediaView mediaView) {
		contentTextView.setText(TextUtils.anchor(getMediaURL(mediaArray, context)));
		mediaView.setMeasureListener(new MeasureListener() {
			@Override
			public void measure(int widthMeasureSpec, int heightMeasureSpec) {
				drawMediaLayout(mediaArray, context, contentTextView,
						contentTextViewAlt, mediaView, widthMeasureSpec);
			}
		});
		mediaView.setVisibility(View.VISIBLE);
	}

	private void drawMediaLayout(final JSONArray mediaArray,
			final Context context, final TextView contentTextView,
			final TextView contentTextViewAlt,
			final MeasuredMediaView mediaView, int widthMeasureSpec) {
		
		final String imageURLLo = getMediaLoURL(mediaArray, context);
		DisplayImageOptions dio = new DisplayImageOptions.Builder()
				.cloneFrom(ImageHelper.defaultImageOptions())
				.preProcessor(ImageHelper.createRoundProcessor(8, true, widthMeasureSpec))
				.build();
		
		ImageLoader.getInstance().displayImage(imageURLLo, mediaView, dio, new ImageLoadingListener() {
			
			@Override
			public void onLoadingStarted(String arg0, View arg1) {
				contentTextViewAlt.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
			}
			
			@Override
			public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
				contentTextViewAlt.setVisibility(View.GONE);
				contentTextView.setVisibility(View.VISIBLE);
				contentTextView.setText(anchoredContent);
				contentTextView.setMovementMethod(LinkMovementMethod.getInstance());
				
				final String imageURLHi = getMediaHiURL(mediaArray, context);
				
				mediaView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent();
						intent.setClass(context, FullScreenImageActivity.class);
						intent.putExtra(FullScreenImageActivity.IMAGE_URL, imageURLLo);
						intent.putExtra(FullScreenImageActivity.IMAGE_URL_HIGH_RES, imageURLHi);
						activity.startActivityForResult(intent, FullScreenImageActivity.REQUEST_CODE);
					}
				});
			}
			
			@Override
			public void onLoadingCancelled(String arg0, View arg1) {
				// TODO Auto-generated method stub
			}
		});
	}
	
	private String getMediaLoURL(JSONArray mediaArray, final Context context) {
		String userMediaURL = getMediaURL(mediaArray, context);
		return userMediaURL + MEDIA_URL_SUFIX;
	}
	
	private String getMediaHiURL(JSONArray mediaArray, final Context context) {
		String userMediaURL = getMediaURL(mediaArray, context);
		return userMediaURL + MEDIA_URL_SUFIX_FULL;
	}
	
	private String getMediaURL(JSONArray mediaArray, final Context context) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		JSONObject mediaJson = mediaArray.optJSONObject(0);
		String userMediaURL = apiAddress + "/" + mediaJson.optString("channel") + 
				MediaModel.ENDPOINT + "/" + mediaJson.optString("id");
		return userMediaURL;
	}
	@Override
	public void setPost(JSONObject post) {
		this.comment = post;
	}

	@Override
	public int compareTo(Card anotherCard) {
		try {
			Date otherUpdated = TimeUtils.updated(anotherCard.getPost());
			Date thisUpdated = TimeUtils.updated(this.getPost());
			return thisUpdated.compareTo(otherUpdated);
		} catch (ParseException e) {
			return 0;
		}
	}
}
