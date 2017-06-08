package jiguang.chat.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.model.UserInfo;
import jiguang.chat.R;
import jiguang.chat.activity.FriendInfoActivity;
import jiguang.chat.controller.FriendInfoController;
import jiguang.chat.utils.HandleResponseCode;

/**
 * Created by ${chenyn} on 2017/3/24.
 */

public class FriendInfoView extends LinearLayout{


    private FriendInfoController mListeners;
    private FriendInfoController mOnChangeListener;
    private ImageView mIv_friendPhoto;
    private TextView mTv_nickName;
    private TextView mTv_signature;
    private TextView mTv_userName;
    private TextView mTv_gender;
    private TextView mTv_birthday;
    private TextView mTv_address;
    private Button mBtn_goToChat;
    private Context mContext;
    private ImageView mSetting;
    private ImageButton mReturnBtn;


    public FriendInfoView(Context context) {
        super(context);
    }

    public FriendInfoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FriendInfoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initModel(FriendInfoActivity context) {
        this.mContext = context;
        mIv_friendPhoto = (ImageView) findViewById(R.id.iv_friendPhoto);
        mTv_nickName = (TextView) findViewById(R.id.tv_nickName);
        mTv_signature = (TextView) findViewById(R.id.tv_signature);
        mTv_userName = (TextView) findViewById(R.id.tv_userName);
        mTv_gender = (TextView) findViewById(R.id.tv_gender);
        mTv_birthday = (TextView) findViewById(R.id.tv_birthday);
        mTv_address = (TextView) findViewById(R.id.tv_address);
        mBtn_goToChat = (Button) findViewById(R.id.btn_goToChat);
        mSetting = (ImageView) findViewById(R.id.jmui_commit_btn);
        mReturnBtn = (ImageButton) findViewById(R.id.return_btn);

    }

    public void setListeners(OnClickListener listeners) {
        mBtn_goToChat.setOnClickListener(listeners);
        mIv_friendPhoto.setOnClickListener(listeners);
        mSetting.setOnClickListener(listeners);
        mReturnBtn.setOnClickListener(listeners);

    }

    public void setOnChangeListener(FriendInfoController onChangeListener) {
        mOnChangeListener = onChangeListener;
    }


    public void initInfo(UserInfo userInfo) {
        if (userInfo != null) {
            if (!TextUtils.isEmpty(userInfo.getAvatar())) {
                userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                    @Override
                    public void gotResult(int status, String desc, Bitmap bitmap) {
                        if (status == 0) {
                            mIv_friendPhoto.setImageBitmap(bitmap);
                        } else {
                            HandleResponseCode.onHandle(mContext, status, false);
                        }
                    }
                });
            }
            mTv_userName.setText(userInfo.getUserName());
            mTv_nickName.setText(userInfo.getNickname());
            if (userInfo.getGender() == UserInfo.Gender.male) {
                mTv_gender.setText(mContext.getString(R.string.man));
            } else if (userInfo.getGender() == UserInfo.Gender.female) {
                mTv_gender.setText(mContext.getString(R.string.woman));
            } else {
                mTv_gender.setText(mContext.getString(R.string.unknown));
            }
            mTv_address.setText(userInfo.getRegion());
            mTv_signature.setText(userInfo.getSignature());

        }

    }
}
