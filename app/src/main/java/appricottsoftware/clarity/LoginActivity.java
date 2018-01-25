package appricottsoftware.clarity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.LoggingBehavior;
import com.facebook.Profile;
import com.facebook.login.widget.LoginButton;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import appricottsoftware.clarity.sync.ClarityApp;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.bt_login) Button btLogin;
    @BindView(R.id.bt_register) Button btRegister;
    @BindView(R.id.bt_loginFacebook) LoginButton btFacebook;
    @BindView(R.id.et_email) EditText etEmail;
    @BindView(R.id.et_password) EditText etPassword;

    private static final String EMAIL = "email";
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        facebookLoginCheck();

        btLogin.setOnClickListener(this);
        btRegister.setOnClickListener(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bt_login:
                RegisterActivity a = new RegisterActivity();
                String strPassword = a.Hash_Password(etPassword.getText().toString());
                String strEmail = etEmail.getText().toString();

                if (isAuthenticated(strEmail, strPassword)) {
                    Intent homeActivityIntent = new Intent(this, HomeActivity.class);
                    startActivity(homeActivityIntent);
                }

                finish();
                break;
            case R.id.bt_register:
                Intent registerActivityIntent = new Intent(this, RegisterActivity.class);
                startActivity(registerActivityIntent);
                finish();
                break;
            default:
                break;
        }
    }

    public boolean isAuthenticated(String email, String password) {
        ClarityApp.getRestClient().checkLogin(email, password, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                super.onSuccess(statusCode, headers, responseString);
            }
        });
        return true;
    }

    private void facebookLoginCheck() {
        Profile fbProfile = Profile.getCurrentProfile();
        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    Log.d("FB token", "null");
                }
            }
        };

        if (fbProfile == null) {
            callbackManager = CallbackManager.Factory.create();
            btFacebook = findViewById(R.id.bt_loginFacebook);
            btFacebook.setReadPermissions(Arrays.asList(EMAIL));
            btFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    // App code
                    Log.e("Facebook Login", "onSuccess login Facebook");
                    GraphRequest request = GraphRequest.newMeRequest(
                            AccessToken.getCurrentAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject object, GraphResponse response) {
                                    FacebookSdk.setIsDebugEnabled(true);
                                    FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

                                    Log.e("Facebook Login", "AccessToken.getCurrentAccessToken() " + AccessToken.getCurrentAccessToken().toString());
                                    Profile profile = Profile.getCurrentProfile();
                                    Log.e("Facebook Login", "Current profile: " + profile);
                                    if (profile != null) {
                                        Log.e("Facebook Login", String.format("id = %s; name = %s; lastName = %s; uri = %s",
                                                profile.getId(), profile.getFirstName(),
                                                profile.getLastName(), profile.getProfilePictureUri(50, 60)));
//                                        name = String.format("%s %s",profile.getFirstName(),profile.getLastName());
//                                        fbid = profile.getId();
                                    }
                                }
                            });

                    request.executeAsync();
                }

                @Override
                public void onCancel() {
                    // App code
                    Toast.makeText(getApplicationContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(FacebookException exception) {
                    // App code
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                    exception.printStackTrace();
                }
            });
        }
        else {
            Log.d("Facebook Login", "Logged with " + fbProfile.getName());
            // fbid = fbProfile.getId();
            login("2");
        }
        accessTokenTracker.startTracking();
    }

    private void login(String loginType) {
        Intent homeActivityIntent = new Intent(LoginActivity.this, HomeActivity.class);
        homeActivityIntent.putExtra("loginType", loginType);
        startActivity(homeActivityIntent);
        finish();
    }
}
