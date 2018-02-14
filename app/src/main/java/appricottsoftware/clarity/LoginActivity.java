package appricottsoftware.clarity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import appricottsoftware.clarity.models.Session;
import appricottsoftware.clarity.sync.ClarityApp;

import java.io.IOException;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.btn_login) Button btnLogin;
    @BindView(R.id.btn_register) Button btnRegister;
    @BindView(R.id.btn_loginFacebook) LoginButton btnFacebook;
    @BindView(R.id.btn_loginGoogle) SignInButton btnGoogle;
    @BindView(R.id.et_email) EditText etEmail;
    @BindView(R.id.et_password) EditText etPassword;

    private static final String TAG = "LoginActivity";

    private CallbackManager fbCallbackManager;
    private AccessTokenTracker fbAccessTokenTracker;
    private GoogleSignInAccount googleAccount;
    private GoogleSignInClient googleSignInClient;
    private String userEmail;
    private String token;
    private boolean userIsAuthenticated;
    private int userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        btnLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        btnGoogle.setOnClickListener(this);
        btnGoogle.setSize(SignInButton.SIZE_WIDE);

        facebookLogin();
        googleLogin();

        userId = ClarityApp.getSession(this).getUserID();
    }

    @Override
    protected void onStart() {
        super.onStart();

        googleAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (googleAccount != null) {
            login(getString(R.string.google_login_type));
        }

        Profile fbProfile = Profile.getCurrentProfile();
        if (fbProfile != null) {
            login(getString(R.string.facebook_login_type));
            fbAccessTokenTracker.startTracking();
        }

        if (ClarityApp.getSession(this).getUserID() != -1) {
            login(getString(R.string.registered_login_type));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fbAccessTokenTracker.stopTracking();
    }

//    @Override
//    protected void onStop(){
//        super.onStop();
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Facebook login dependency.
        fbCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        // Google login dependency.
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == Integer.parseInt(getString(R.string.google_request_code))) {

            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);

            // Preparing to get google login token
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                final GoogleSignInAccount account = result.getSignInAccount();

                // These lines of code are necessary getting Google login token
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String scope = "oauth2:"+ Scopes.EMAIL+" "+ Scopes.PROFILE;
                            token = GoogleAuthUtil.getToken(getApplicationContext(), account.getAccount(), scope, new Bundle());

                            Log.e(TAG, "Google() email: " + userEmail + "\ttoken: " + token);

                            // Logging in here now that we have access to the token
                            registerSocialMediaUser(userEmail, token, getString(R.string.google_login_type));
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (GoogleAuthException e) {
                            e.printStackTrace();
                        }
                    }
                };
                AsyncTask.execute(runnable);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_login:
                String strPassword = hashPassword(etPassword.getText().toString());
                String strEmail = etEmail.getText().toString();
                authenticate(strEmail, strPassword, getString(R.string.registered_login_type));
                break;
            case R.id.btn_register:
                register();
                break;
            case R.id.btn_loginGoogle:
                googleSignIn();
                break;
            default:
                break;
        }
    }

    public void registerSocialMediaUser(String email, String password, final String loginType) {
        ClarityApp.getRestClient().registerRequest(email, password, this, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.e(TAG, "register onSuccess1 : " + statusCode + "\n" + response.toString() );
                super.onSuccess(statusCode, headers, response);

                try {
                    // User already in database
                    if (response.getInt("userId") == -1) {
                        authenticate(userEmail, token, loginType);
                    }

                    // First time social media user
                    else {
                        userId = response.getInt("userId");
                        ClarityApp.getSession(getApplicationContext()).setUserID(userId);

                        if (loginType == getString(R.string.facebook_login_type)) {
                            login(getString(R.string.facebook_login_type));
                        }
                        else if (loginType == getString(R.string.google_login_type)) {
                            login(getString(R.string.google_login_type));
                        }

                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.e(TAG, "onSuccess2 : " + statusCode + "\n" +  response.toString());
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(TAG, "onFailure1 : " + statusCode + "\n" +  errorResponse.toString());
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.e(TAG, "onFailure2 : " + statusCode + "\n" + errorResponse.toString());
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG, "onFailure3 : " + statusCode + "\n" + responseString.toString());
                super.onFailure(statusCode, headers, responseString, throwable);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.e(TAG, "onSuccess3 : "+ statusCode + "\n" + responseString.toString());
                super.onSuccess(statusCode, headers, responseString);
            }
        });
    }

    // HTTPS GET function to authenticate user
    public void authenticate(String email, String password, final String loginType) {
        final Activity parentActivity = this;
        ClarityApp.getRestClient().authenticateUser(email, password, this, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.e(TAG, "authenticate onSuccess1: " + response.toString());
                try {
                    String strUserId = response.getString("userId");
                    Log.e(TAG, "User ID: " + userId);
                    if (strUserId == "-1") {
                        Log.e(TAG, "Username or password is incorrect");
                        Toast unauthToast = Toast.makeText(getApplicationContext(),
                                            R.string.no_auth,
                                            Toast.LENGTH_SHORT);
                        unauthToast.show();
                    } else {
                        String uid = response.getString("userId");
                        ClarityApp.getSession(getApplicationContext()).setUserID(Integer.parseInt((uid)));

                        if (loginType == getString(R.string.facebook_login_type)) {
                            login(getString(R.string.facebook_login_type));
                        }
                        else if (loginType == getString(R.string.google_login_type)) {
                            login(getString(R.string.google_login_type));
                        }
                        else if (loginType == getString(R.string.registered_login_type)) {
                            login(getString(R.string.registered_login_type));
                        }

                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Exception due to JSON returned as {\"auth\" : \"-1\"} key value pair. Expecting {\"userId\" : uid}");
                    e.printStackTrace();
                }
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.e(TAG, "onSuccess2: " + response.toString());
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.e(TAG, "onSuccess3: " + responseString.toString());
                super.onSuccess(statusCode, headers, responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(TAG, "onFailure1: " + errorResponse.toString());
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.e(TAG, "onFailure2: " + errorResponse.toString());
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG, "onFailure3: " + responseString.toString());
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    private void facebookLogin() {
        final Profile fbProfile = Profile.getCurrentProfile();
        fbAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    Log.d("FB token", "null");
                }
            }
        };

        if (fbProfile == null) {
            fbCallbackManager = CallbackManager.Factory.create();
            btnFacebook.findViewById(R.id.btn_loginFacebook);
            btnFacebook.setReadPermissions(Arrays.asList("public_profile", "email"));
            btnFacebook.registerCallback(fbCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(final LoginResult loginResult) {
                    // App code
                    GraphRequest request = GraphRequest.newMeRequest(
                            loginResult.getAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject object, GraphResponse response) {
                                    FacebookSdk.setIsDebugEnabled(true);
                                    FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

                                    // Extract user info for our backend
                                    try {
                                        userEmail = object.getString("email");
                                        token = loginResult.getAccessToken().toString();
                                        token = token.substring(19, token.length() - 37);

                                        Log.e(TAG, "Facebook() email: " + userEmail + "\ttoken: " + token);
                                        registerSocialMediaUser(userEmail, token,
                                                getString(R.string.facebook_login_type));
                                    }
                                    catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "email");
                    request.setParameters(parameters);
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
    }

    private void googleLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
        ClarityApp clarityApp = new ClarityApp();
        clarityApp.setGoogleSignInClient(googleSignInClient);
    }

    private void googleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, Integer.parseInt(getString(R.string.google_request_code)));
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                userEmail = account.getEmail();
            }
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Google Login", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    // Generic login function that takes the user to HomeActivity
    private void login(String loginType) {
        Intent homeActivityIntent = new Intent(LoginActivity.this, HomeActivity.class);
        homeActivityIntent.putExtra("loginType", loginType);
        startActivity(homeActivityIntent);
        finish();
    }

    // Takes user to RegisterActivity
    private void register() {
        Intent registerActivityIntent = new Intent(this, RegisterActivity.class);
        startActivity(registerActivityIntent);
        finish();
    }

    private String hashPassword(String password) {
        String hash = new RegisterActivity().hashPassword(password);
        return hash;
    }
}
