package ccsskt.bokecc.base.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.bokecc.sskt.base.net.EasyCall;
import com.bokecc.sskt.base.net.EasyCallback;
import com.bokecc.sskt.base.net.EasyOKHttp;
import com.bokecc.sskt.base.net.EasyOptions;
import com.bokecc.sskt.base.net.EasyResponse;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import ccsskt.bokecc.base.example.base.TitleActivity;
import ccsskt.bokecc.base.example.base.TitleOptions;
import ccsskt.bokecc.base.example.util.ParseUtil;

public class ValidateActivity extends TitleActivity<ValidateActivity.ValidateViewHolder> {

    private static final String KEY_ROOM_ID = "room_id";
    private static final String KEY_USER_ACCOUNT = "user_account";
    private static final String KEY_USER_ROLE = "user_role";

    private static Intent newIntent(Context context, String roomId, String userAccount, String mRole) {
        Intent intent = new Intent(context, ValidateActivity.class);
        intent.putExtra(KEY_ROOM_ID, roomId);
        intent.putExtra(KEY_USER_ACCOUNT, userAccount);
        intent.putExtra(KEY_USER_ROLE, mRole);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    public static void startSelf(Context context, String roomId, String userAccount, String mRole) {
        context.startActivity(newIntent(context, roomId, userAccount, mRole));
    }

    public static final String TAG = ValidateActivity.class.getSimpleName();

    private String mRoomId;
    private String mUserAccount;
    private String mRole;

    private InputMethodManager imm;

    private EasyOKHttp mEasyOKHttp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_validate;
    }

    @Override
    protected ValidateViewHolder getViewHolder(View contentView) {
        return new ValidateViewHolder(contentView);
    }

    @Override
    protected void onBindViewHolder(ValidateViewHolder holder) {

        final TitleOptions.Builder builder = new TitleOptions.Builder();
        TitleOptions options = builder.leftStatus(TitleOptions.VISIBLE).
                leftResId(R.drawable.title_back).
                rightStatus(TitleOptions.GONE).
                titleStatus(TitleOptions.VISIBLE).
                title("验证").
                onTitleClickListener(new TitleOptions.OnLeftClickListener() {
                    @Override
                    public void onLeft() {
                        finish();
                    }
                }).
                build();
        setTitleOptions(options);


        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        mRoomId = getIntent().getStringExtra(KEY_ROOM_ID);
        mUserAccount = getIntent().getStringExtra(KEY_USER_ACCOUNT);
        mRole = getIntent().getStringExtra(KEY_USER_ROLE);

        showSoftboard();
        mEasyOKHttp = new EasyOKHttp.Builder().baseUrl("https://ccapi.csslcloud.net/").build();
    }

    private Runnable mShowSoftboardRunnable = new Runnable() {
        @Override
        public void run() {
            imm.showSoftInput(mViewHolder.mNickname, 0);
        }
    };

    public void showSoftboard() {
        mHandler.postDelayed(mShowSoftboardRunnable, 150);
    }

    final class ValidateViewHolder extends TitleActivity.ViewHolder {

        @BindView(R.id.id_validate_nickname)
        EditText mNickname;
        @BindView(R.id.id_validate_passwd)
        EditText mPassword;

        ValidateViewHolder(View view) {
            super(view);
        }

        private String getEditTextValue(EditText editText) {
            return editText.getText().toString().trim();
        }

        @OnClick(R.id.id_validate_room)
        void goRoom() {
            String nickname = getEditTextValue(mNickname);
            String password = getEditTextValue(mPassword);
            Map<String, Object> params = new HashMap<>();
            params.put("roomid", mRoomId);
            params.put("userid", mUserAccount);
            params.put("role", mRole.equals("talker") ? "1" : "0");
            params.put("name", nickname);
            params.put("password", password);
            params.put("client", "1");
            EasyOptions options = new EasyOptions.OKHttpOptionsBuilder().path("api/room/auth").params(params).build();
            showProgress();
            mEasyOKHttp.createCall("api/room/auth", options).enqueue(new EasyCallback() {
                @Override
                public void onResponse(EasyCall call, EasyResponse response) {
                    try {
                        dismissProgress();
                        JSONObject object = ParseUtil.getJsonObj(response.string());
                        String sessionid = object.getJSONObject("data").getString("sessionid");
                        MainActivity.startSelf(ValidateActivity.this, sessionid, mRoomId, mUserAccount);
                    } catch (Exception e) {
                        showToast(e.getMessage());
                    }
                }

                @Override
                public void onFailure(EasyCall call, Throwable t) {
                    dismissProgress();
                    showToast(t.getMessage());
                }
            });
        }

    }

}
