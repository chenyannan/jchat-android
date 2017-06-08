package jiguang.chat.activity.fragment;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.model.UserInfo;
import jiguang.chat.R;
import jiguang.chat.activity.LoginActivity;
import jiguang.chat.controller.MeController;
import jiguang.chat.utils.HandleResponseCode;
import jiguang.chat.utils.SharePreferenceManager;
import jiguang.chat.utils.ToastUtil;
import jiguang.chat.view.MeView;

/**
 * Created by ${chenyn} on 2017/2/20.
 */

public class MeFragment extends BaseFragment {
    private View mRootView;
    public MeView mMeView;
    private MeController mMeController;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getActivity();
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        mRootView = layoutInflater.inflate(R.layout.fragment_me,
                (ViewGroup) getActivity().findViewById(R.id.main_view), false);
        mMeView = (MeView) mRootView.findViewById(R.id.me_view);
        mMeView.initModule(mDensity, mWidth);
        mMeController = new MeController(this, mWidth);
        mMeView.setListener(mMeController);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup p = (ViewGroup) mRootView.getParent();
        if (p != null) {
            p.removeAllViewsInLayout();
        }
        return mRootView;
    }

    @Override
    public void onResume() {
        String avatarPath = SharePreferenceManager.getCachedAvatarPath();
        UserInfo myInfo = JMessageClient.getMyInfo();
        mMeView.showNickName(myInfo);
        if (TextUtils.isEmpty(avatarPath)) {
            if (myInfo != null) {
                if (!TextUtils.isEmpty(myInfo.getAvatar())) {
                    myInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                        @Override
                        public void gotResult(int responseCode, String responseMessage, Bitmap avatarBitmap) {
                            if (responseCode == 0) {
                                mMeView.showPhoto(avatarBitmap);
                                mMeController.setBitmap(avatarBitmap);
                            } else {
                                HandleResponseCode.onHandle(mContext, responseCode, false);
                            }
                        }
                    });
                }
            }
        } else {
            mMeView.showPhoto(BitmapFactory.decodeFile(avatarPath));
        }
        super.onResume();
    }

    public void cancelNotification() {
        NotificationManager manager = (NotificationManager) this.getActivity().getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
    }

    //退出登录
    public void Logout() {
        final Intent intent = new Intent();
        UserInfo info = JMessageClient.getMyInfo();
        if (null != info) {
            SharePreferenceManager.setCachedUsername(info.getUserName());
            JMessageClient.logout();
            intent.setClass(mContext, LoginActivity.class);
            startActivity(intent);
        } else {
            ToastUtil.shortToast(mContext, "退出失败");
        }
    }

}
