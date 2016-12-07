package io.jchat.android.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.event.ContactNotifyEvent;
import cn.jpush.im.android.api.model.UserInfo;
import de.greenrobot.event.EventBus;
import io.jchat.android.R;
import io.jchat.android.application.JChatDemoApplication;
import io.jchat.android.chatting.utils.SharePreferenceManager;
import io.jchat.android.controller.ContactsController;
import io.jchat.android.database.FriendEntry;
import io.jchat.android.database.FriendRecommendEntry;
import io.jchat.android.database.UserEntry;
import io.jchat.android.entity.Event;
import io.jchat.android.entity.FriendInvitation;
import io.jchat.android.tools.HanziToPinyin;
import io.jchat.android.view.ContactsView;

public class ContactsFragment extends BaseFragment {
	private View mRootView;
	private ContactsView mContactsView;
	private ContactsController mContactsController;
    private Activity mContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        mContext = this.getActivity();
        EventBus.getDefault().register(this);
		LayoutInflater layoutInflater = getActivity().getLayoutInflater();
		mRootView = layoutInflater.inflate(R.layout.fragment_contacts,
				(ViewGroup) getActivity().findViewById(R.id.main_view), false);
		mContactsView = (ContactsView) mRootView.findViewById(R.id.contacts_view);
		mContactsView.initModule(mRatio);
		mContactsController = new ContactsController(mContactsView, this.getActivity());
		mContactsView.setOnClickListener(mContactsController);
        mContactsView.setListeners(mContactsController);
        mContactsView.setSideBarTouchListener(mContactsController);
        mContactsView.setTextWatcher(mContactsController);
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		ViewGroup p = (ViewGroup) mRootView.getParent();
		if (p != null) {
			p.removeAllViewsInLayout();
		}
		return mRootView;
	}

    @Override
    public void onResume() {
        super.onResume();
        if (SharePreferenceManager.getCachedShowContact()) {
            mContactsView.showContact();
            mContactsController.initContacts();
        } else {
            mContactsView.dismissContact();
        }
    }

    /**
     * 接收到好友相关事件
     * @param event ContactNotifyEvent
     */
	public void onEvent(ContactNotifyEvent event) {
        final UserEntry user = JChatDemoApplication.getUserEntry();
        final String reason = event.getReason();
        final String username = event.getFromUsername();
        final String appKey = event.getfromUserAppKey();
        //收到接受好友请求
        if (event.getType() == ContactNotifyEvent.Type.invite_accepted) {
            //add friend to contact
            JMessageClient.getUserInfo(username, appKey, new GetUserInfoCallback() {
                @Override
                public void gotResult(int status, String desc, final UserInfo userInfo) {
                    if (status == 0) {
                        String name = userInfo.getNickname();
                        if (TextUtils.isEmpty(name)) {
                            name = userInfo.getUserName();
                        }
                        String letter;
                        ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance()
                                .get(name);
                        StringBuilder sb = new StringBuilder();
                        if (tokens != null && tokens.size() > 0) {
                            for (HanziToPinyin.Token token : tokens) {
                                if (token.type == HanziToPinyin.Token.PINYIN) {
                                    sb.append(token.target);
                                } else {
                                    sb.append(token.source);
                                }
                            }
                        }
                        String sortString = sb.toString().substring(0, 1).toUpperCase();
                        if (sortString.matches("[A-Z]")) {
                            letter = sortString.toUpperCase();
                        } else {
                            letter = "#";
                        }
                        FriendEntry friendEntry = FriendEntry.getFriend(user, username, appKey);
                        if (null == friendEntry) {
                            final FriendEntry newFriend = new FriendEntry(username, appKey, userInfo.getAvatar(), name,
                                    letter, user);
                            newFriend.save();
                            mContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mContactsController.refresh(newFriend);
                                }
                            });
                        }
                    }
                }
            });
            FriendRecommendEntry entry = FriendRecommendEntry.getEntry(user, username, appKey);
            Log.d("ContactsFragment", "entry " + entry);
            entry.state = FriendInvitation.ACCEPTED.getValue();
            entry.save();
        //拒绝好友请求
        } else if (event.getType() == ContactNotifyEvent.Type.invite_declined) {
            FriendRecommendEntry entry = FriendRecommendEntry.getEntry(user, username, appKey);
            entry.state = FriendInvitation.REFUSED.getValue();
            entry.save();
        //收到好友邀请事件
        } else if (event.getType() == ContactNotifyEvent.Type.invite_received){
            JMessageClient.getUserInfo(username, appKey, new GetUserInfoCallback() {
                @Override
                public void gotResult(int status, String desc, UserInfo userInfo) {
                    if (status == 0) {
                        String name = userInfo.getNickname();
                        if (TextUtils.isEmpty(name)) {
                            name = userInfo.getUserName();
                        }
                        if (null != userInfo.getAvatar()) {
                            String path = userInfo.getAvatarFile().getPath();
                            FriendRecommendEntry entry = FriendRecommendEntry.getEntry(user, username, appKey);
                            if (null == entry) {
                                entry = new FriendRecommendEntry(username, appKey, path,
                                        name, reason, FriendInvitation.INVITED.getValue(), user);
                                entry.save();
                            }
                        }
                    } else {
                        FriendRecommendEntry entry = FriendRecommendEntry.getEntry(user, username, appKey);
                        if (null == entry) {
                            entry = new FriendRecommendEntry(username, appKey, null,
                                    username, reason, FriendInvitation.INVITED.getValue(), user);
                            entry.save();
                        }
                    }
                }
            });
            SharePreferenceManager.setCachedNewFriendNum(SharePreferenceManager.getCachedNewFriendNum() + 1);
            mContactsView.showNewFriends(SharePreferenceManager.getCachedNewFriendNum());
        }
    }

    public void onEventMainThread(Event.AddFriendEvent event) {
        FriendEntry entry = FriendEntry.getFriend(event.getId());
        mContactsController.refresh(entry);
    }
}