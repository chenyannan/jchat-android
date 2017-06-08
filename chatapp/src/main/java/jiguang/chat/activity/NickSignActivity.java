package jiguang.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import jiguang.chat.R;

/**
 * Created by ${chenyn} on 2017/2/27.
 */

public class NickSignActivity extends BaseActivity {

    private EditText mEd_sign;
    private TextView mTv_count;
    private LinearLayout mLl_nickSign;
    private static final int SIGN_COUNT = 100;
    private static final int NICK_COUNT = 30;

    private static final int GROUP_DESC = 300;
    private static final int GROUP_NAME = 30;
    private Button mJmui_commit_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nick_sign);

        initView();
        Intent intent = getIntent();
        if (intent.getFlags() == PersonalActivity.FLAGS_SIGN) {
            initViewSign("个性签名", SIGN_COUNT);
            initData(SIGN_COUNT);
        } else if (intent.getFlags() == PersonalActivity.FLAGS_NICK) {
            initViewNick("修改昵称", NICK_COUNT);
            String old_nick = intent.getStringExtra("old_nick");
            mEd_sign.setText(old_nick);
            mEd_sign.setSelection(old_nick.length());
            initData(NICK_COUNT);
        } else if (intent.getFlags() == ChatDetailActivity.FLAGS_GROUP_DESC) {
            initViewSign("群描述", GROUP_DESC);
            initData(GROUP_DESC);
        } else if (intent.getFlags() == ChatDetailActivity.FLAGS_GROUP_NAME) {
            initViewNick("群名称", GROUP_NAME);
            initData(GROUP_NAME);
        }
        initListener(intent.getFlags());
    }

    private void initListener(final int flags) {
        mJmui_commit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sign = mEd_sign.getText().toString();
                Intent intent = new Intent();
                if (flags == PersonalActivity.FLAGS_NICK) {//3
                    intent.putExtra(PersonalActivity.NICK_NAME_KEY, sign);
                    setResult(PersonalActivity.NICK_NAME, intent);//4
                } else if (flags == PersonalActivity.FLAGS_SIGN) {//2
                    intent.putExtra(PersonalActivity.SIGN_KEY, sign);
                    setResult(PersonalActivity.SIGN, intent);//1

                } else if (flags == ChatDetailActivity.FLAGS_GROUP_DESC) {//71
                    intent.putExtra(ChatDetailActivity.GROUP_DESC_KEY, sign);
                    setResult(ChatDetailActivity.GROUP_DESC, intent);//70

                } else if (flags == ChatDetailActivity.FLAGS_GROUP_NAME) {//73
                    intent.putExtra(ChatDetailActivity.GROUP_NAME_KEY, sign);
                    setResult(ChatDetailActivity.GROUP_NAME, intent);//72

                }
                //做更新动作
                finish();
            }
        });
    }

    private void initData(final int count) {
        mEd_sign.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int num = count - s.length();
                mTv_count.setText(num + "");
            }
        });
    }

    private void initView() {
        mEd_sign = (EditText) findViewById(R.id.ed_sign);
        mLl_nickSign = (LinearLayout) findViewById(R.id.ll_nickSign);
        mTv_count = (TextView) findViewById(R.id.tv_count);
        mJmui_commit_btn = (Button) findViewById(R.id.jmui_commit_btn);
        
        

    }

    private void initViewSign(String str, int flag) {
        initTitle(true, true, str, "", true, "完成");
        mEd_sign.setFilters(new InputFilter[] {new InputFilter.LengthFilter(SIGN_COUNT)});
        mTv_count.setText(flag + "");
    }

    private void initViewNick(String str, int flag) {
        initTitle(true, true, str, "", true, "完成");
        mEd_sign.setFilters(new InputFilter[] {new InputFilter.LengthFilter(NICK_COUNT)});
        mTv_count.setText(flag + "");

        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        mLl_nickSign.setLayoutParams(params);
    }
}
