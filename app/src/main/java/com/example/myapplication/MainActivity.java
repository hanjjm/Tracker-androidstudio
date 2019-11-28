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

import com.kakao.auth.AccessTokenCallback;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.KakaoSDK;
import com.kakao.auth.Session;
import com.kakao.auth.authorization.accesstoken.AccessToken;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.usermgmt.response.model.User;
import com.kakao.usermgmt.response.model.UserAccount;
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
    private int index;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        index = 0;

        if (KakaoSDK.getAdapter() == null) {
            KakaoSDK.init(new GlobalApplication.KakaoSDKAdapter());
        }

       // getKeyHash(this);

        callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);
        Session.getCurrentSession().checkAndImplicitOpen();

        requestMe();

      // onClickLogout(); //이걸 주석 빼면 재로그인 해야함.

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
                    Log.d("UserProfile", userProfile.toString());
                    String userNickname = userProfile.getNickname();
                    String userEmail = userProfile.getEmail();
                    String imagePath = userProfile.getProfileImagePath();

                    Log.d("12321", "1");

                  /*  UserAccount a = new UserAccount();
                    a.getGender()*/
                    // String age = userProfile.get
                    String id = userProfile.getUUID();
                    Intent intent = new Intent(MainActivity.this, GPSActivity.class);
                    intent.putExtra("NickName", userNickname);
                    intent.putExtra("Email", userEmail);
                    intent.putExtra("picture", imagePath);
                    intent.putExtra("id", id);
                    //intent.putExtra("age", age);

                    Log.d("이미지", imagePath);
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

    private void requestMe() {
        List<String> keys = new ArrayList<>();
        keys.add("properties.nickname");
        keys.add("properties.profile_image");
        keys.add("kakao_account.email");
        keys.add("kakao_account.gender");
        keys.add("kakao_account.age_range");
        //keys.add("kakao_account.age_range");
        //keys.add("kakao_account.gender");
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
             //   Log.d("user id : " ,response.getId());

                Log.d("email: " , response.getKakaoAccount().getAgeRange().toString());
                //여기서 보내기
               // Log.d("gender : " , response.getKakaoAccount().getAgeRange().toString());
               // Toast.makeText(MainActivity.this, response.getKakaoAccount().getGender().toString(), Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void onClickLogout() {
        index++;
        UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {
                //redirectSignActivity();
            }
        });
    }

/*    private void handleScopeError(UserAccount account) {
        List<String> neededScopes = new ArrayList<>();
        if (account.needsScopeAgeRange()) {
            neededScopes.add("ageRange");
        }
        if (account.needsScopeGender()) {
            neededScopes.add("gender");
        }
        Session.getCurrentSession().updateScopes(this, neededScopes, new
                AccessTokenCallback() {
                    @Override
                    public void onAccessTokenReceived(AccessToken accessToken) {
                        // 유저에게 성공적으로 동의를 받음. 토큰을 재발급 받게 됨.
                    }

                    @Override
                    public void onAccessTokenFailure(ErrorResult errorResult) {
                        // 동의 얻기 실패
                    }
                });
    }*/


}
