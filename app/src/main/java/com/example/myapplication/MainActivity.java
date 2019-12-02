package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
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
import com.kakao.network.NetworkTask;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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

       onClickLogout(); //이걸 주석 빼면 재로그인 해야함.

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

                //여기서 보내

                final String url = "http://mr-y.asuscomm.com:3000/users ";

                String age = response.getKakaoAccount().getAgeRange().toString().substring(4, 6);
                Log.d("email : ", response.getKakaoAccount().getEmail());
                Log.d("age: " , age);
                Log.d("gender : ", response.getKakaoAccount().getGender().toString());
                final ContentValues values = new ContentValues();
                //values.put("id", response.getKakaoAccount().getEmail());
                values.put("id", response.getKakaoAccount().getEmail());
                values.put("age", age);
                values.put("gender", response.getKakaoAccount().getGender().toString().substring(0, 1));

                NetworkTask networkTask = new NetworkTask(url, values);
                networkTask.execute();
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


    public class NetworkTask extends AsyncTask<Void, Void, String> {

        private String url;
        private ContentValues values;

        public NetworkTask(String url, ContentValues values) {

            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... params) {

            String result; // 요청 결과를 저장할 변수.
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.request(url, values); // 해당 URL로 부터 결과물을 얻어온다.

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.
            //tv_outPut.setText(s);
        }
    }




    public class RequestHttpURLConnection {

        public String request(String _url, ContentValues _params){

            // HttpURLConnection 참조 변수.
            HttpURLConnection urlConn = null;
            // URL 뒤에 붙여서 보낼 파라미터.
            StringBuffer sbParams = new StringBuffer();

            /**
             * 1. StringBuffer에 파라미터 연결
             * */
            // 보낼 데이터가 없으면 파라미터를 비운다.
            if (_params == null)
                sbParams.append("");
                // 보낼 데이터가 있으면 파라미터를 채운다.
            else {
                // 파라미터가 2개 이상이면 파라미터 연결에 &가 필요하므로 스위칭할 변수 생성.
                boolean isAnd = false;
                // 파라미터 키와 값.
                String key;
                String value;

                for(Map.Entry<String, Object> parameter : _params.valueSet()){
                    key = parameter.getKey();
                    value = parameter.getValue().toString();

                    // 파라미터가 두개 이상일때, 파라미터 사이에 &를 붙인다.
                    if (isAnd)
                        sbParams.append("&");

                    sbParams.append(key).append("=").append(value);

                    // 파라미터가 2개 이상이면 isAnd를 true로 바꾸고 다음 루프부터 &를 붙인다.
                    if (!isAnd)
                        if (_params.size() >= 2)
                            isAnd = true;
                }
            }

            /**
             * 2. HttpURLConnection을 통해 web의 데이터를 가져온다.
             * */
            try{
                URL url = new URL(_url);
                urlConn = (HttpURLConnection) url.openConnection();

                // [2-1]. urlConn 설정.
                urlConn.setRequestMethod("POST"); // URL 요청에 대한 메소드 설정 : POST.
                urlConn.setRequestProperty("Accept-Charset", "UTF-8"); // Accept-Charset 설정.
                urlConn.setRequestProperty("Context_Type", "application/x-www-form-urlencoded;cahrset=UTF-8");

                // [2-2]. parameter 전달 및 데이터 읽어오기.
                String strParams = sbParams.toString(); //sbParams에 정리한 파라미터들을 스트링으로 저장. 예)id=id1&pw=123;
                OutputStream os = urlConn.getOutputStream();
                os.write(strParams.getBytes("UTF-8")); // 출력 스트림에 출력.
                os.flush(); // 출력 스트림을 플러시(비운다)하고 버퍼링 된 모든 출력 바이트를 강제 실행.
                os.close(); // 출력 스트림을 닫고 모든 시스템 자원을 해제.

                // [2-3]. 연결 요청 확인.
                // 실패 시 null을 리턴하고 메서드를 종료.
                if (urlConn.getResponseCode() != HttpURLConnection.HTTP_OK)
                    return null;

                // [2-4]. 읽어온 결과물 리턴.
                // 요청한 URL의 출력물을 BufferedReader로 받는다.
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), "UTF-8"));

                // 출력물의 라인과 그 합에 대한 변수.
                String line;
                String page = "";

                // 라인을 받아와 합친다.
                while ((line = reader.readLine()) != null){
                    page += line;
                }

                return page;

            } catch (MalformedURLException e) { // for URL.
                e.printStackTrace();
            } catch (IOException e) { // for openConnection().
                e.printStackTrace();
            } finally {
                if (urlConn != null)
                    urlConn.disconnect();
            }

            return null;

        }

    }
}
