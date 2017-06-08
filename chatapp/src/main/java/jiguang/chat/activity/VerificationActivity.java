package jiguang.chat.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import cn.jpush.im.android.api.ContactManager;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import jiguang.chat.R;
import jiguang.chat.database.FriendRecommendEntry;
import jiguang.chat.database.UserEntry;
import jiguang.chat.entity.FriendInvitation;
import jiguang.chat.model.InfoModel;
import jiguang.chat.utils.HandleResponseCode;
import jiguang.chat.utils.SharePreferenceManager;
import jiguang.chat.utils.ToastUtil;

/**
 * Created by ${chenyn} on 2017/3/14.
 */

public class VerificationActivity extends BaseActivity {

    private EditText mEt_reason;
    private UserInfo mMyInfo;
    private String mTargetAppKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        initView();
        initData();
    }

    private void initData() {
        mMyInfo = JMessageClient.getMyInfo();
        mTargetAppKey = mMyInfo.getAppKey();

        mEt_reason.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendAddReason();
                }
                return false;
            }
        });

        mJmui_commit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAddReason();
            }
        });
    }

    private void sendAddReason() {
        final String targetAvatar = InfoModel.getInstance().getAvatarPath();
        String displayName = InfoModel.getInstance().getNickName();
        if (TextUtils.isEmpty(displayName)) {
            displayName = InfoModel.getInstance().getUserName();
        }
        final String userName;
        if (getIntent().getFlags() == 1) {
            userName = getIntent().getStringExtra("detail_add_friend");
        } else {
            userName = InfoModel.getInstance().getUserName();
        }
        final String reason = mEt_reason.getText().toString();
        final String finalDisplayName = displayName;
        ContactManager.sendInvitationRequest(userName, null, reason, new BasicCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage) {
                if (responseCode == 0) {
                    UserEntry userEntry = UserEntry.getUser(mMyInfo.getUserName(), mMyInfo.getAppKey());
                    FriendRecommendEntry entry = FriendRecommendEntry.getEntry(userEntry,
                            userName, mTargetAppKey);
                    if (null == entry) {
                        entry = new FriendRecommendEntry(userName, mTargetAppKey,
                                targetAvatar, finalDisplayName, reason, FriendInvitation.INVITING.getValue(), userEntry, 100);
                    } else {
                        entry.state = FriendInvitation.INVITING.getValue();
                        entry.reason = reason;
                    }
                    entry.save();
                    ToastUtil.shortToast(VerificationActivity.this, "申请成功");
                    finish();
                } else {
                    HandleResponseCode.onHandle(VerificationActivity.this, responseCode, false);
                }
            }
        });
    }

    private void initView() {
        initTitle(true, true, "验证信息", "", true, "发送");
        mEt_reason = (EditText) findViewById(R.id.et_reason);
        String name;
        if (getIntent().getFlags() == 1) {
            name = getIntent().getStringExtra("detail_add_friend_my_nickname");
            if (TextUtils.isEmpty(name)) {
                mEt_reason.setText("我是" + getIntent().getStringExtra("detail_add_friend"));
            }else {
                mEt_reason.setText("我是" + name);
            }
        } else {
            name = InfoModel.getInstance().getNickName();
            if (TextUtils.isEmpty(name)) {
                mEt_reason.setText("我是" + SharePreferenceManager.getRegistrUsername());
            }else {
                mEt_reason.setText("我是" + name);
            }
        }

    }
}
