package com.buddycloud.card;

import java.text.ParseException;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.buddycloud.FullScreenImageActivity;
import com.buddycloud.MainActivity;
import com.buddycloud.R;
import com.buddycloud.customviews.MeasuredMediaView;
import com.buddycloud.customviews.MeasuredMediaView.MeasureListener;
import com.buddycloud.customviews.TypefacedEditText;
import com.buddycloud.fragments.ChannelStreamFragment;
import com.buddycloud.model.MediaModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.PostsModel;
import com.buddycloud.model.SubscribedChannelsModel;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.AvatarUtils;
import com.buddycloud.utils.ImageHelper;
import com.buddycloud.utils.InputUtils;
import com.buddycloud.utils.TextUtils;
import com.buddycloud.utils.TimeUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

public class PostCard extends AbstractCard {
	
	private static final String MEDIA_URL_SUFIX = "?maxwidth=600";
	private static final String MEDIA_URL_SUFIX_FULL = "?maxwidth=1024";
	
	private JSONObject post;
	private String channelJid;
	private CardListAdapter repliesAdapter = new CardListAdapter();
	private Spanned anchoredContent;
	private MainActivity activity;
	private String role;
	private ChannelStreamFragment fragment;

	public PostCard(String channelJid, JSONObject post, 
			ChannelStreamFragment fragment, String role) {
		this.channelJid = channelJid;
		this.fragment = fragment;
		this.activity = (MainActivity) fragment.getActivity();
		this.role = role;
		setPost(post);
	}

	public JSONObject getPost() {
		return post;
	}
	
	public void setPost(JSONObject post) {
		this.post = post;
		this.anchoredContent = TextUtils.anchor(post.optString("content"));
		fillReplyAdapter(this.activity);
	}

	private void fillReplyAdapter(Context context) {
		repliesAdapter.clear();
		JSONArray comments = post.optJSONArray("replies");
		for (int i = 0; comments != null && i < comments.length(); i++) {
			JSONObject comment = comments.optJSONObject(i);
			repliesAdapter.addCard(toReplyCard(comment, context));
		}
		repliesAdapter.sort();
		repliesAdapter.notifyDataSetChanged();
	}

	@Override
	public View getContentView(int position, View convertView, ViewGroup viewGroup) {
		
		final String postAuthor = post.optString("author");
		String published = post.optString("published");
		String mediaStr = post.optString("media");
		final String postId = post.optString("id");
		
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
			convertView = inflater.inflate(R.layout.post_entry, viewGroup, false);
			holder = fillHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (CardViewHolder) convertView.getTag();
		}
		
		final Context context = viewGroup.getContext();
		String avatarURL = AvatarUtils.avatarURL(viewGroup.getContext(), postAuthor);	
		ImageView avatarView = holder.getView(R.id.bcProfilePic);
		DisplayImageOptions dio = new DisplayImageOptions.Builder()
				.cloneFrom(ImageHelper.defaultImageOptions())
				.showImageOnFail(R.drawable.avatar_icon)
				.showImageOnLoading(R.drawable.avatar_icon)
				.preProcessor(ImageHelper.createRoundProcessor(16, false, -1))
				.build();
		
		ImageLoader.getInstance().displayImage(avatarURL, avatarView, dio);
		avatarView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!channelJid.equals(postAuthor)) {
					activity.showChannelFragment(postAuthor);
				}
			}
		});
		
		final TextView postAuthorView = holder.getView(R.id.bcPostAuthor);
		if (!TextUtils.isEmpty(postAuthor)) {
			postAuthorView.setText(TextUtils.capitalize(postAuthor.trim()));
		}
		
		final View postContainer = holder.getView(R.id.postContainer);
		final TextView contentTextView = holder.getView(R.id.bcPostContent);
		final TextView contentTextViewAlt = holder.getView(R.id.bcPostPending);
		final MeasuredMediaView mediaView = holder.getView(R.id.bcImageContent);
		mediaView.setImageBitmap(null);
		drawNoMediaLayout(contentTextView, contentTextViewAlt, mediaView);
		
		if (mediaArray != null) {
			drawMediaLayout(mediaArray, context, contentTextView,
				contentTextViewAlt, mediaView);
		}
		
		FrameLayout postContentWrapper = holder.getView(R.id.postContentWrapper);
		postContentWrapper.forceLayout();
		
		View contextArrowDown = holder.getView(R.id.bcArrowDown);
		contextArrowDown.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PostContextUtils.showPostContextActions(context, channelJid, 
						postId, role, PostContextUtils.TOPIC_POST);
			}
		});
		
		TextView publishedView = holder.getView(R.id.bcPostDate);
		TextView publishedPendingView = holder.getView(R.id.bcPostPending);
		boolean pending = PostsModel.isPending(post);
		if (!pending) {
			try {
				long publishedTime = TimeUtils.fromISOToDate(published).getTime();
				publishedView.setText(
						DateUtils.getRelativeTimeSpanString(publishedTime, 
								new Date().getTime(), DateUtils.MINUTE_IN_MILLIS));
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
		
		// REPLIES SECTION
		LinearLayout replyListViewSection = holder.getView(R.id.replyListViewSection);
		if (repliesAdapter.getCount() > 0) {
			replyListViewSection.setVisibility(View.VISIBLE);
			ReplySectionUtils.configure(replyListViewSection, repliesAdapter);
		}
		else {
			replyListViewSection.setVisibility(View.GONE);
		}
		
		// COMMENT BOX SECTION
		View commentBoxFrameView = holder.getView(R.id.commentBoxFrameView);
		if (!SubscribedChannelsModel.canPost(role)) {
			commentBoxFrameView.setVisibility(View.GONE);
		} else {
			commentBoxFrameView.setVisibility(View.VISIBLE);
			ImageView replyAuthorPicView = holder.getView(R.id.bcReplyAuthorPic);
			String replyAuthorURL = AvatarUtils.avatarURL(viewGroup.getContext(), 
					Preferences.getPreference(context, Preferences.MY_CHANNEL_JID));

			ImageLoader.getInstance().displayImage(replyAuthorURL, replyAuthorPicView, dio);

			final TypefacedEditText replyTxt = holder.getView(R.id.postNewCommentTxt);
			replyTxt.setOnEditorActionListener(new OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					
					if (actionId == EditorInfo.IME_ACTION_SEND || 
							actionId == EditorInfo.IME_ACTION_DONE) {
						
						sendReply(context, replyTxt);
					}
					return false;
				}
			});
			replyTxt.setOnKeyPreImeListener(new TypefacedEditText.KeyImeChange() {
				
				@Override
				public boolean onKeyPreIme(int keyCode, KeyEvent event) {
					if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
						new Handler().postDelayed(new Runnable() {
							
							@Override
							public void run() {
								fragment.showAddPostTopicBtn();
							}
						}, 1000);
					}
					
					return false;
				}
			});
			replyTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus) {
						fragment.hideAddPostTopicBtn();
					}
				}
			});
		}

		// adjust shadow
		adjustShadow(context, postContainer, replyListViewSection);
		return convertView;
	}

	
	private void adjustShadow(final Context context,
			View postContainer,
			View replyListViewSection) {
		
		if (getRepliesAdapter() != null && getRepliesAdapter().getCount() > 0) {
			setBackground(context, postContainer, R.drawable.post_item_list_shadow);
			if(!SubscribedChannelsModel.canPost(role)) {
				setBackground(context, replyListViewSection, R.drawable.comment_item_shadow);
			} else {
				setBackground(context, replyListViewSection, R.drawable.comment_item_list_shadow);
			}
		}
		else if(!SubscribedChannelsModel.canPost(role)) {
			setBackground(context, postContainer, R.drawable.post_item_shadow); 
		}
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void setBackground(final Context context, View view, int resId) {
		if (view == null) return ;
		
		Drawable drawable = context.getResources().getDrawable(resId);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			view.setBackgroundDrawable(drawable);
		} else {
			 view.setBackground(drawable);
		}
	}
	
	public CardListAdapter getRepliesAdapter() {
		return repliesAdapter;
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
				
			}
			
			@Override
			public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
			}
			
			@Override
			public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
				contentTextViewAlt.setVisibility(View.VISIBLE);
				contentTextView.setVisibility(View.INVISIBLE);
				contentTextViewAlt.setText(anchoredContent);
				contentTextViewAlt.setMovementMethod(LinkMovementMethod.getInstance());
				
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

	private void sendReply(final Context context, final EditText replyTxt) {

		JSONObject replyPost = createReply(replyTxt);
		replyTxt.setText("");
		
		PostsModel.getInstance().save(context, replyPost, new ModelCallback<JSONObject>() {
			@Override
			public void success(JSONObject response) {
				Toast.makeText(context, context.getString(R.string.message_reply_created), 
						Toast.LENGTH_LONG).show();
				InputUtils.hideKeyboard(fragment.getActivity());
				fragment.fillRemotely(null, null);
			}
			
			@Override
			public void error(Throwable throwable) {
			}
		}, channelJid);
	}

	private JSONObject createReply(EditText postContent) {
		JSONObject reply = new JSONObject();
		try {
			reply.putOpt("content", postContent.getText().toString());
			reply.putOpt("replyTo", this.post.optString("id"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return reply;
	}
	
	private CommentCard toReplyCard(JSONObject comment, Context context) {
		CommentCard commentCard = new CommentCard(channelJid, comment, 
				activity, role);
		return commentCard;
	}
	
	private static CardViewHolder fillHolder(View view) {
		return CardViewHolder.create(view, 
				R.id.postContentWrapper, R.id.postContainer,
				R.id.postTitleWrapper, R.id.bcProfilePic, 
				R.id.bcPostAuthor, 
				R.id.bcPostDate, R.id.bcTopRightView, 
				R.id.bcArrowDown, R.id.bcPostPending,
				R.id.bcImageContent, R.id.bcPostContent, 
				R.id.replyListViewSection,
				R.id.commentBoxFrameView, R.id.bcReplyAuthorPic,
				R.id.postNewCommentTxt);
	}

	@Override
	public int compareTo(Card anotherCard) {
		try {
			Date otherUpdated = TimeUtils.threadUpdated(anotherCard.getPost());
			Date thisUpdated = TimeUtils.threadUpdated(this.getPost());
			return otherUpdated.compareTo(thisUpdated);
		} catch (ParseException e) {
			return 0;
		}
	}

	public void addPendingCard(JSONObject pendingItem) {
		repliesAdapter.addCard(toReplyCard(pendingItem, activity));
		repliesAdapter.sort();
	}
}
