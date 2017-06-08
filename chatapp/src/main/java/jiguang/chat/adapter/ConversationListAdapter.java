package jiguang.chat.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.content.MessageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import jiguang.chat.R;
import jiguang.chat.application.JGApplication;
import jiguang.chat.utils.HandleResponseCode;
import jiguang.chat.utils.SortConvList;
import jiguang.chat.utils.TimeFormat;
import jiguang.chat.utils.ViewHolder;
import jiguang.chat.view.ConversationListView;
import jiguang.chat.view.SwipeLayoutConv;

/**
 * Created by ${chenyn} on 2017/3/30.
 */

public class ConversationListAdapter extends BaseAdapter {

    private List<Conversation> mDatas;
    private Activity mContext;
    private Map<String, String> mDraftMap = new HashMap<String, String>();
    private UIHandler mUIHandler = new UIHandler(this);
    private static final int REFRESH_CONVERSATION_LIST = 0x3003;
    private SparseBooleanArray mArray = new SparseBooleanArray();
    private HashMap<Conversation, Integer> mAtConvMap = new HashMap<Conversation, Integer>();
    private UserInfo mUserInfo;
    private GroupInfo mGroupInfo;
    private ConversationListView mConversationListView;

    public ConversationListAdapter(Activity context, List<Conversation> data, ConversationListView convListView) {
        this.mContext = context;
        this.mDatas = data;
        this.mConversationListView = convListView;
    }

    /**
     * 收到消息后将会话置顶
     *
     * @param conv 要置顶的会话
     */
    public void setToTop(Conversation conv) {
        for (Conversation conversation : mDatas) {
            if (conv.getId().equals(conversation.getId())) {
                mDatas.remove(conversation);
                mDatas.add(0, conv);
                mUIHandler.removeMessages(REFRESH_CONVERSATION_LIST);
                mUIHandler.sendEmptyMessageDelayed(REFRESH_CONVERSATION_LIST, 200);
                return;
            }
        }
        //如果是新的会话
        mDatas.add(0, conv);
        mUIHandler.removeMessages(REFRESH_CONVERSATION_LIST);
        mUIHandler.sendEmptyMessageDelayed(REFRESH_CONVERSATION_LIST, 200);
    }

    @Override
    public int getCount() {
        if (mDatas == null) {
            return 0;
        }
        return mDatas.size();
    }

    @Override
    public Conversation getItem(int position) {
        if (mDatas == null) {
            return null;
        }
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final Conversation convItem = mDatas.get(position);
        mConversationListView.setUnReadMsg(JMessageClient.getAllUnReadMsgCount());
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_conv_list, null);
        }
        final ImageView headIcon = ViewHolder.get(convertView, R.id.msg_item_head_icon);
        TextView convName = ViewHolder.get(convertView, R.id.conv_item_name);
        TextView content = ViewHolder.get(convertView, R.id.msg_item_content);
        TextView datetime = ViewHolder.get(convertView, R.id.msg_item_date);
        TextView newMsgNumber = ViewHolder.get(convertView, R.id.new_msg_number);
        ImageView groupBlocked = ViewHolder.get(convertView, R.id.iv_groupBlocked);
        ImageView newMsgDisturb = ViewHolder.get(convertView, R.id.new_msg_disturb);

        final SwipeLayoutConv swipeLayout = ViewHolder.get(convertView, R.id.swp_layout);
        final TextView delete = ViewHolder.get(convertView, R.id.tv_delete);
        final LinearLayout msg_item_ll = ViewHolder.get(convertView, R.id.msg_item_ll);

        String draft = mDraftMap.get(convItem.getId());

        //如果会话草稿为空,显示最后一条消息
        if (TextUtils.isEmpty(draft)) {
            Message lastMsg = convItem.getLatestMessage();
            if (lastMsg != null) {
                TimeFormat timeFormat = new TimeFormat(mContext, lastMsg.getCreateTime());
                datetime.setText(timeFormat.getDetailTime());
                String contentStr;
                switch (lastMsg.getContentType()) {
                    case image:
                        contentStr = mContext.getString(R.string.type_picture);
                        break;
                    case voice:
                        contentStr = mContext.getString(R.string.type_voice);
                        break;
                    case location:
                        contentStr = mContext.getString(R.string.type_location);
                        break;
                    case file:
                        String extra = lastMsg.getContent().getStringExtra("video");
                        if (extra != null && extra.equals("mp4")) {
                            contentStr = mContext.getString(R.string.type_smallvideo);
                        } else {
                            contentStr = mContext.getString(R.string.type_file);
                        }
                        break;
                    case video:
                        contentStr = mContext.getString(R.string.type_video);
                        break;
                    case eventNotification:
                        contentStr = mContext.getString(R.string.group_notification);
                        break;
                    case custom:
                        CustomContent customContent = (CustomContent) lastMsg.getContent();
                        Boolean isBlackListHint = customContent.getBooleanValue("blackList");
                        if (isBlackListHint != null && isBlackListHint) {
                            contentStr = mContext.getString(R.string.jmui_server_803008);
                        } else {
                            contentStr = mContext.getString(R.string.type_custom);
                        }
                        break;
                    default:
                        contentStr = ((TextContent) lastMsg.getContent()).getText();
                }

                MessageContent msgContent = lastMsg.getContent();
                Boolean isRead = msgContent.getBooleanExtra("isRead");
                if (lastMsg.isAtMe()) {
                    if (null != isRead && isRead) {
                        mArray.delete(position);
                        mAtConvMap.remove(convItem);
                    } else {
                        mArray.put(position, true);
                    }
                }

                if (mArray.get(position) && JGApplication.isNeedAtMsg) {
                    //有人@我 文字提示
                    contentStr = mContext.getString(R.string.somebody_at_me) + contentStr;
                    SpannableStringBuilder builder = new SpannableStringBuilder(contentStr);
                    builder.setSpan(new ForegroundColorSpan(Color.RED), 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    content.setText(builder);
                } else {
                    content.setText(contentStr);
                }
            } else {
                TimeFormat timeFormat = new TimeFormat(mContext, convItem.getLastMsgDate());
                datetime.setText(timeFormat.getDetailTime());
                content.setText("");
            }
        } else {
            draft = mContext.getString(R.string.draft) + draft;
            SpannableStringBuilder builder = new SpannableStringBuilder(draft);
            builder.setSpan(new ForegroundColorSpan(Color.RED), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            content.setText(builder);
        }

        if (convItem.getType().equals(ConversationType.single)) {
            convName.setText(convItem.getTitle());
            mUserInfo = (UserInfo) convItem.getTargetInfo();
            if (mUserInfo != null && !TextUtils.isEmpty(mUserInfo.getAvatar())) {
                mUserInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                    @Override
                    public void gotResult(int status, String desc, Bitmap bitmap) {
                        if (status == 0) {
                            headIcon.setImageBitmap(bitmap);
                        } else {
                            headIcon.setImageResource(R.drawable.jmui_head_icon);
                            HandleResponseCode.onHandle(mContext, status, false);
                        }
                    }
                });
            } else {
                headIcon.setImageResource(R.drawable.jmui_head_icon);
            }
        } else {
            mGroupInfo = (GroupInfo) convItem.getTargetInfo();
            if (mGroupInfo != null) {
                int blocked = mGroupInfo.isGroupBlocked();
                if (blocked == 1) {
                    groupBlocked.setVisibility(View.VISIBLE);
                } else {
                    groupBlocked.setVisibility(View.GONE);
                }
            }
            headIcon.setImageResource(R.drawable.group);
            convName.setText(convItem.getTitle());
        }

        // TODO 更新Message的数量,
        if (convItem.getUnReadMsgCnt() > 0) {
            if (mUserInfo != null && mUserInfo.getNoDisturb() == 1 || mGroupInfo != null && mGroupInfo.getNoDisturb() == 1) {
                newMsgDisturb.setVisibility(View.VISIBLE);
            } else {
                newMsgNumber.setVisibility(View.VISIBLE);
            }
            if (convItem.getUnReadMsgCnt() < 100) {
                newMsgNumber.setText(String.valueOf(convItem.getUnReadMsgCnt()));
            } else {
                newMsgNumber.setText(mContext.getString(R.string.hundreds_of_unread_msgs));
            }
        } else {
            newMsgDisturb.setVisibility(View.GONE);
            newMsgNumber.setVisibility(View.GONE);
        }

        //侧滑删除会话
        swipeLayout.addSwipeListener(new SwipeLayoutConv.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayoutConv layout) {

            }

            @Override
            public void onOpen(SwipeLayoutConv layout) {
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (convItem.getType() == ConversationType.single) {
                            JMessageClient.deleteSingleConversation(((UserInfo) convItem.getTargetInfo()).getUserName());
                        } else {
                            JMessageClient.deleteGroupConversation(((GroupInfo) convItem.getTargetInfo()).getGroupID());
                        }
                        mDatas.remove(position);
                        notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onStartClose(SwipeLayoutConv layout) {

            }

            @Override
            public void onClose(SwipeLayoutConv layout) {

            }

            @Override
            public void onUpdate(SwipeLayoutConv layout, int leftOffset, int topOffset) {

            }

            @Override
            public void onHandRelease(SwipeLayoutConv layout, float xvel, float yvel) {

            }
        });


        return convertView;
    }


    public void sortConvList() {
        SortConvList sortConvList = new SortConvList();
        Collections.sort(mDatas, sortConvList);
        notifyDataSetChanged();
    }

    public void addNewConversation(Conversation conv) {
        mDatas.add(0, conv);
        notifyDataSetChanged();
    }

    public void addAndSort(Conversation conv) {
        mDatas.add(conv);
        SortConvList sortConvList = new SortConvList();
        Collections.sort(mDatas, sortConvList);
        notifyDataSetChanged();
    }

    public void deleteConversation(Conversation conversation) {
        mDatas.remove(conversation);
        notifyDataSetChanged();
    }

    public void putDraftToMap(Conversation conv, String draft) {
        mDraftMap.put(conv.getId(), draft);
    }

    public void delDraftFromMap(Conversation conv) {
        mArray.delete(mDatas.indexOf(conv));
        mAtConvMap.remove(conv);
        mDraftMap.remove(conv.getId());
        notifyDataSetChanged();
    }

    public String getDraft(String convId) {
        return mDraftMap.get(convId);
    }

    public boolean includeAtMsg(Conversation conv) {
        if (mAtConvMap.size() > 0) {
            Iterator<Map.Entry<Conversation, Integer>> iterator = mAtConvMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Conversation, Integer> entry = iterator.next();
                if (conv == entry.getKey()) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getAtMsgId(Conversation conv) {
        return mAtConvMap.get(conv);
    }

    public void putAtConv(Conversation conv, int msgId) {
        mAtConvMap.put(conv, msgId);
    }

    static class UIHandler extends Handler {

        private final WeakReference<ConversationListAdapter> mAdapter;

        public UIHandler(ConversationListAdapter adapter) {
            mAdapter = new WeakReference<ConversationListAdapter>(adapter);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            ConversationListAdapter adapter = mAdapter.get();
            if (adapter != null) {
                switch (msg.what) {
                    case REFRESH_CONVERSATION_LIST:
                        adapter.notifyDataSetChanged();
                        break;
                }
            }
        }
    }


}
