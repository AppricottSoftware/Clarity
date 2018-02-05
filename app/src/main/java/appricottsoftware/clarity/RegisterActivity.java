package appricottsoftware.clarity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


import appricottsoftware.clarity.sync.ClarityApp;
import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.bt_register) Button btRegister;
    @BindView(R.id.editText_firstname) EditText firstname;
    @BindView(R.id.editText_lastname) EditText lastname;
    @BindView(R.id.editText_username) EditText username;
    @BindView(R.id.editText_email) EditText email;
    @BindView(R.id.editText_password) EditText password;

    boolean seeSurvey = false;
    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        btRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bt_register:

                String firstnameString = firstname.getText().toString();
                String lastnameString = lastname.getText().toString();
                String usernameString = username.getText().toString();
                String emailString = email.getText().toString();
                String hashedPassword = hashPassword(password.getText().toString());

                // Sanity Check to make sure all instances are populated with actual strings
                if(firstnameString.length() == 0|| lastnameString.length() == 0 || usernameString.length() == 0 ||
                        emailString.length() == 0 || password.getText().toString().length() == 0) {
                    return;
                }

                // create instance of clarityClient
                // pass information to database to store
                if (seeSurvey == true){
                    Intent surveyActivityIntent = new Intent(this, SurveyActivity.class);
                    surveyActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(surveyActivityIntent);
                    finish();
                } else {

                    // After successful save of user info on back end
                    // Switch to home activity


                    Intent homeActivityIntent = new Intent(this, HomeActivity.class);
                    homeActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(homeActivityIntent);
                    finish();
                }

                ClarityApp.getRestClient().registerRequest(usernameString, emailString, hashedPassword, firstnameString, lastnameString, this, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.e(TAG, "onSuccess1 : " + response.toString() );
                        super.onSuccess(statusCode, headers, response);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        Log.e(TAG, "onSuccess2 : " + response.toString());
                        super.onSuccess(statusCode, headers, response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.e(TAG, "onFailue1 : " + errorResponse.toString());
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                        Log.e(TAG, "onFailue2 : " + errorResponse.toString());
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Log.e(TAG, "onFailue3 : " + responseString.toString());
                        super.onFailure(statusCode, headers, responseString, throwable);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        Log.e(TAG, "onSuccess3 : " + responseString.toString());
                        super.onSuccess(statusCode, headers, responseString);
                    }
                });

                break;
            default:
                break;
        }
    }

    public String hashPassword(String originalPassword) {
        String hashedPassword = null;
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = originalPassword.getBytes("UTF-8");
            digest.update(bytes, 0, bytes.length);
            bytes = digest.digest();

            hashedPassword = bytesToHex( bytes );
        }
        catch(NoSuchAlgorithmException | UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return hashedPassword;
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex( byte[] bytes )
    {
        char[] hexChars = new char[ bytes.length * 2 ];
        for( int j = 0; j < bytes.length; j++ )
        {
            int v = bytes[ j ] & 0xFF;
            hexChars[ j * 2 ] = hexArray[ v >>> 4 ];
            hexChars[ j * 2 + 1 ] = hexArray[ v & 0x0F ];
        }
        return new String( hexChars );
    }


} //end RegisterActivity



