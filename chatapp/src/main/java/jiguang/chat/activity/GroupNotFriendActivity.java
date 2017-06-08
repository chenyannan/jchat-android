package jiguang.chat.activity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.eventbus.EventBus;
import jiguang.chat.R;
import jiguang.chat.application.JGApplication;
import jiguang.chat.entity.Event;
import jiguang.chat.entity.EventType;

/**
 * Created by ${chenyn} on 2017/5/10.
 */

public class GroupNotFriendActivity extends BaseActivity implements View.OnClickListener {

    private ImageView mIv_friendPhoto;
    private TextView mTv_nickName;
    private TextView mTv_sign;
    private TextView mTv_userName;
    private TextView mTv_gender;
    private TextView mTv_birthday;
    private TextView mTv_address;
    private Button mBtn_add_friend;
    private Button mBtn_send_message;
    private String mUserName;
    private UserInfo mUserInfo;
    private String mMyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_not_friend);

        initView();
        initData();
    }

    private void initData() {
        mUserName = getIntent().getStringExtra(JGApplication.TARGET_ID);
        JMessageClient.getUserInfo(mUserName, new GetUserInfoCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, UserInfo info) {
                if (responseCode == 0) {
                    mUserInfo = info;
                    String avatar = info.getAvatar();
                    if (!TextUtils.isEmpty(avatar)) {
                        mIv_friendPhoto.setImageBitmap(BitmapFactory.decodeFile(avatar));
                    }
                    mTv_nickName.setText(info.getNickname());
                    mTv_sign.setText(info.getSignature());
                    mTv_userName.setText(info.getUserName());
                    if (info.getGender() == UserInfo.Gender.male) {
                        mTv_gender.setText("男");
                    } else if (info.getGender() == UserInfo.Gender.female) {
                        mTv_gender.setText("女");
                    } else {
                        mTv_gender.setText("未知");
                    }
                    mTv_birthday.setText(getBirthday(info));
                    mTv_address.setText(info.getAddress());
                }
            }
        });

        UserInfo myInfo = JMessageClient.getMyInfo();
        mMyName = myInfo.getNickname();
        if (TextUtils.isEmpty(mMyName)) {
            mMyName = myInfo.getUserName();
        }
    }

    private void initView() {
        initTitle(true, true, "详细资料", "", false, "");
        mIv_friendPhoto = (ImageView) findViewById(R.id.iv_friendPhoto);
        mTv_nickName = (TextView) findViewById(R.id.tv_nickName);
        mTv_sign = (TextView) findViewById(R.id.tv_sign);
        mTv_userName = (TextView) findViewById(R.id.tv_userName);
        mTv_gender = (TextView) findViewById(R.id.tv_gender);
        mTv_birthday = (TextView) findViewById(R.id.tv_birthday);
        mTv_address = (TextView) findViewById(R.id.tv_address);
        mBtn_add_friend = (Button) findViewById(R.id.btn_add_friend);
        mBtn_send_message = (Button) findViewById(R.id.btn_send_message);

        mBtn_add_friend.setOnClickListener(this);
        mBtn_send_message.setOnClickListener(this);
    }

    public String getBirthday(UserInfo info) {
        long birthday = info.getBirthday();
        Date date = new Date(birthday);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        return dateFormat.format(date);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.btn_add_friend:
                intent.setClass(GroupNotFriendActivity.this, VerificationActivity.class);
                intent.putExtra("detail_add_friend", mUserName);
                intent.putExtra("detail_add_friend_my_nickname", mMyName);
                intent.setFlags(1);
                startActivity(intent);
                break;
            case R.id.btn_send_message:
                intent.setClass(GroupNotFriendActivity.this, ChatActivity.class);
                //创建会话
                intent.putExtra(JGApplication.TARGET_ID, mUserInfo.getUserName());
                intent.putExtra(JGApplication.TARGET_APP_KEY, mUserInfo.getAppKey());
                String notename = mUserInfo.getNotename();
                if (TextUtils.isEmpty(notename)) {
                    notename = mUserInfo.getNickname();
                    if (TextUtils.isEmpty(notename)) {
                        notename = mUserInfo.getUserName();
                    }
                }
                intent.putExtra(JGApplication.CONV_TITLE, notename);
                Conversation conv = JMessageClient.getSingleConversation(mUserInfo.getUserName(), mUserInfo.getAppKey());
                //如果会话为空，使用EventBus通知会话列表添加新会话
                if (conv == null) {
                    conv = Conversation.createSingleConversation(mUserInfo.getUserName(), mUserInfo.getAppKey());
                    EventBus.getDefault().post(new Event.Builder()
                            .setType(EventType.createConversation)
                            .setConversation(conv)
                            .build());
                }
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
