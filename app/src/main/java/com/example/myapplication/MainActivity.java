package com.example.myapplication;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.kakao.auth.ISessionCallback;
import com.kakao.auth.KakaoSDK;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static com.kakao.util.helper.Utility.getPackageInfo;

public class MainActivity extends Activity {
    private SessionCallback callback;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (KakaoSDK.getAdapter() == null) {
            KakaoSDK.init(new GlobalApplication.KakaoSDKAdapter());
        }

       // getKeyHash(this);

        callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);
        Session.getCurrentSession().checkAndImplicitOpen();

       // requestMe();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(callback);
    }

    private class SessionCallback implements ISessionCallback {
        @Override
        public void onSessionOpened() {
            //Toast.makeText(MainActivity.this, "성공", Toast.LENGTH_SHORT).show();
            //goHomeActivity();
            //redirectSignActivity();

            UserManagement.getInstance().requestMe(new MeResponseCallback() {
                @Override
                public void onSessionClosed(ErrorResult errorResult) {

                }

                @Override
                public void onNotSignedUp() {

                }

                @Override
                public void onSuccess(UserProfile userProfile) {
                    Logger.d("uUserProfile", userProfile.toString());
                    //Toast.makeText(MainActivity.this, userProfile.toString() , Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    intent.putExtra("userProfileIntent", userProfile.toString());
                    startActivity(intent);
                    finish();
                }
            });





        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            Toast.makeText(MainActivity.this, "실패", Toast.LENGTH_SHORT).show();
            if(exception != null) {
                Logger.e(exception);
            }
           // redirectSignActivity();
        }
    }

    protected void redirectSignActivity() {
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    protected void goHomeActivity() {
        final Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

/*    public static String getKeyHash(final Context context) {
        PackageInfo packageInfo = getPackageInfo(context, PackageManager.GET_SIGNATURES);
        if (packageInfo == null)
            return null;

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());

                Log.w("123123", Base64.encodeToString(md.digest(), Base64.NO_WRAP));
                return Base64.encodeToString(md.digest(), Base64.NO_WRAP);
            } catch (NoSuchAlgorithmException e) {
                Log.w("1111222", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
        return null;
    }*/

   /* private void requestMe() {
        List<String> keys = new ArrayList<>();
        keys.add("properties.nickname");
        keys.add("properties.profile_image");
        keys.add("kakao_account.email");

        UserManagement.getInstance().me(keys, new MeV2ResponseCallback() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                String message = "failed to get user info. msg=" + errorResult;
                Logger.d(message);
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
             //   redirectLoginActivity();
            }

            @Override
            public void onSuccess(MeV2Response response) {
                Logger.d("user id : " + response.getId());
                Logger.d("email: " + response.getKakaoAccount().getEmail());
                Logger.d("gender : " + response.getKakaoAccount().getGender());
                Toast.makeText(MainActivity.this, response.getKakaoAccount().getEmail() , Toast.LENGTH_SHORT).show();
            }

        });
    }*/

}
