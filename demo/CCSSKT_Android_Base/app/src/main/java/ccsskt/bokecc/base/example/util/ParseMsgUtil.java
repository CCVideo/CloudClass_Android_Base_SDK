package ccsskt.bokecc.base.example.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import ccsskt.bokecc.base.example.ValidateActivity;

import static android.content.ContentValues.TAG;
import static ccsskt.bokecc.base.example.Config.mRole;
import static ccsskt.bokecc.base.example.Config.mRoomId;
import static ccsskt.bokecc.base.example.Config.mUserAccount;

/**
 * 作者 ${郭鹏飞}.<br/>
 */

public class ParseMsgUtil {

    private ParseMsgUtil() {}

    public static void parseUrl(final Context context, String url, final ParseCallBack callBack) {
        try {
            if (callBack != null) {
                callBack.onStart();
            }
            if (TextUtils.isEmpty(url)) {
                throw new NullPointerException("课堂链接错误");
            }
            Log.i(TAG, url);
            String arr[] = url.split("\\?|&");
            mRoomId = arr[1].split("=")[1];
            mUserAccount = arr[2].split("=")[1];
            String hosts[] = (arr[0].substring("https://".length(), arr[0].length())).split("/");
            mRole = hosts[hosts.length - 1];
            mRole = mRole.substring(0, mRole.length());
            switch (mRole) {
                case "presenter":
                case "talker":
                    if (callBack != null) {
                        callBack.onSuccess();
                    }
                    ValidateActivity.startSelf(context, mRoomId, mUserAccount, mRole);
                    break;
                default:
                    if (callBack != null) {
                        callBack.onFailure("请使用直播客户端启动");
                    }
            }

        } catch (Exception e) {
            Log.e(TAG, "parseUrl: [ " + e.getMessage() + " ]");
            if (callBack != null) {
                callBack.onFailure("课堂链接错误");
            }
        }
    }

    public interface ParseCallBack{
        void onStart();
        void onSuccess();
        void onFailure(String err);
    }

}
