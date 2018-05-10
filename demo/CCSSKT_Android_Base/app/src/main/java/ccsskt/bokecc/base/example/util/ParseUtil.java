package ccsskt.bokecc.base.example.util;

import android.text.TextUtils;

import com.bokecc.sskt.base.exception.ApiException;

import org.json.JSONObject;

/**
 * 作者 ${郭鹏飞}.<br/>
 */
 public class ParseUtil {

    private ParseUtil() {
        throw new UnsupportedOperationException();
    }

    public static JSONObject getJsonObj(String data) throws Exception {
        JSONObject object = new JSONObject(data);
        String result = object.getString("result");
        switch (result) {
            case OK:
                return object;
            case FAILED:
                String err = object.optString("errorMsg");
                if (TextUtils.isEmpty(err)) {
                    err = object.optString("string");
                }
                if (TextUtils.isEmpty(err)) {
                    err = object.optString("reason");
                }
                throw new ApiException(TextUtils.isEmpty(err) ? data : err);
            default:
                throw new ApiException("unknown error");
        }
    }

    private static final String OK = "OK";
    private static final String FAILED = "FAIL";

}
