package appricottsoftware.clarity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private String email;
    private String token;
    private boolean userIsAuthenticated;


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

    @Override
    protected void onStop(){
        super.onStop();

        // Ignore this code at the bottom for now. It'll be replaced soon.
//        SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);
//        SharedPreferences.Editor editor = settings.edit();
//        editor.putBoolean("userIsAuthenticated", userIsAuthenticated);
//        editor.apply();

        // TODO save user id from backend use it to save the session.
//        ClarityApp.getSession(getApplicationContext()).setUserID(1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Facebook login dependency.
        fbCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        // Google login dependency.
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == Integer.parseInt(getString(R.string.google_request_code))) {

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
                            Log.e(TAG, "accessToken:\t"+token); //accessToken:ya29.Gl...

                            // Logging in here now that we have access to the token
                            login(getString(R.string.google_login_type));
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (GoogleAuthException e) {
                            e.printStackTrace();
                        }
                    }
                };
                AsyncTask.execute(runnable);
            }

            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_login:
                String strPassword = new RegisterActivity().hashPassword(etPassword.getText().toString());
                String strEmail = etEmail.getText().toString();
                authenticate(strEmail, strPassword);
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
                Log.e(TAG, "onSuccess1 : " + response.toString() );
                super.onSuccess(statusCode, headers, response);

                if (loginType == getString(R.string.registered_login_type)) {
                    login(getString(R.string.registered_login_type));
                }
                else if (loginType == getString(R.string.facebook_login_type)) {
                    login(getString(R.string.facebook_login_type));
                }
                else if (loginType == getString(R.string.google_login_type)) {
                    login(getString(R.string.google_login_type));
                }
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
    }

    // HTTPS GET function to authenticate user. Currently not working.
    public void authenticate(String email, String password) {
        final Activity parentActivity = this;
        ClarityApp.getRestClient().authenticateUser(email, password, this, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    if (response.getString("auth").equals("failure")) {
                        Toast unauthToast = Toast.makeText(getApplicationContext(),
                                            R.string.no_auth,
                                            Toast.LENGTH_SHORT);
                        unauthToast.show();
                    } else {
                        // TODO eventually need to get info from JSON object to save user ID
                        ClarityApp.getSession(getApplicationContext()).setUserID(1);

                        login("1");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
                                        email = object.getString("email");
                                        Log.e(TAG, "Email: " + email + "\tPassword: " + loginResult.getAccessToken().toString());
                                        String str = loginResult.getAccessToken().toString();
                                        str = str.substring(19, str.length() - 37);
                                        Log.e(TAG, "String length: " + str.length() + " " + str);
                                        registerSocialMediaUser(email, str,
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

                    //login(getString(R.string.facebook_login_type));
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
                email = account.getEmail();
                Log.e(TAG, "been here done that\t\temail: " + email + "\tPassword: " + token);
            }

            // Signed in successfully, show authenticated UI.
//            login(getString(R.string.google_login_type));
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
}
