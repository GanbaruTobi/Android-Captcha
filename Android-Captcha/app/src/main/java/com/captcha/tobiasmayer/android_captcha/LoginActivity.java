package com.captcha.tobiasmayer.android_captcha;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CookieHandler.setDefault(new java.net.CookieManager());
        setContentView(R.layout.activity_login);
        RetrieveCaptchaTask captchaTask = new RetrieveCaptchaTask();
        captchaTask.execute();

        Button buttonSubmit = (Button) findViewById(R.id.buttonSubmit);
        buttonSubmit.setOnClickListener(new SubmitButtonListener());
        Button buttonRefresh = (Button) findViewById(R.id.buttonRefresh);
        buttonRefresh.setOnClickListener(new RefreshButtonListener());
    }

    private class SubmitButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            EditText editTextCaptchaInput = (EditText) findViewById(R.id.editTextCaptchaInput);
            String captchaInput = editTextCaptchaInput.getText().toString().trim();
            if(captchaInput.isEmpty()){
                editTextCaptchaInput.setError(getResources().getString(R.string.editTextCaptchaEmptyError));
            }
            else{
                new RetrieveResponseTask().execute(captchaInput);
            }
        }
    }

    private class RefreshButtonListener implements View.OnClickListener {

        public void onClick(View v) {
            RetrieveCaptchaTask captchaTask = new RetrieveCaptchaTask();
            captchaTask.execute();

        }
    }

    private class RetrieveCaptchaTask extends AsyncTask<Void, Void, Bitmap> {

        private Exception exception;

        @Override
        protected Bitmap doInBackground(Void... params) {

            HttpURLConnection httpURLConnection = null;
            try {
                URL request = new URL(getResources().getString(R.string.CaptchaRequestURL));

                httpURLConnection = (HttpURLConnection) request.openConnection();
                httpURLConnection.setRequestMethod("GET");

                BufferedInputStream bufferedInputStream = new BufferedInputStream(httpURLConnection.getInputStream());

                return BitmapFactory.decodeStream(bufferedInputStream);

            } catch (MalformedURLException e) {
                //must not happen!
                throw new RuntimeException(e);
            } catch (IOException i) {
                exception = i;
                return null;
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap == null) {
                Log.e("LoginActivity","Error on captcha request", exception);
                return;
            }

            ImageView imageViewCaptcha = (ImageView) findViewById(R.id.imageViewCaptcha);
            imageViewCaptcha.setImageBitmap(bitmap);
        }
    }

    private class RetrieveResponseTask extends AsyncTask<String,Void,String> {

        private Exception exception;

        @Override
        protected String doInBackground(String ...params) {
            HttpURLConnection httpURLConnection = null;
            try {
                URL response = new URL(getResources().getString(R.string.CaptchaResponseURL) + params[0]);

                httpURLConnection = (HttpURLConnection) response.openConnection();
                httpURLConnection.setRequestMethod("GET");

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
                StringBuilder stringBuilder = new StringBuilder();

                String inputString;
                while ((inputString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(inputString);
                }

                Log.d("LoginActivity", stringBuilder.toString());
                return stringBuilder.toString();

            } catch (MalformedURLException e) {
                //must not happen!
                throw new RuntimeException(e);
            } catch (IOException i) {
                exception = i;
                return null;
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if(result == null){
                //TODO
                Log.e("LoginActivity","Error on HttpRequest",exception);
                return;
            }

            int code;
            try{
                JSONObject json = new JSONObject(result);
                JSONObject metaObject = json.getJSONObject("meta");
                code = metaObject.getInt("code");

            }catch (JSONException e){
                showErrorDialog("Error","Error on parsing JSON");
                return;
            }
            if(code == 3){

                Intent userInfoIntent = new Intent();
                userInfoIntent.setClass(LoginActivity.this, UserInfoActivity.class);
                userInfoIntent.putExtra("ServerResponse",result);
                startActivity(userInfoIntent);

            }else if(code == 10){
                showErrorDialog("Error","Input didn't match this Captcha");

            }else if(code == 11){
                showErrorDialog("Error","Input didn't match the Captcha");

            }else{
                showErrorDialog("Error","Unknown response");
            }
        }
    }

    private void showErrorDialog(String title, String message){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message).setPositiveButton(android.R.string.ok, null).show();
    }
}
