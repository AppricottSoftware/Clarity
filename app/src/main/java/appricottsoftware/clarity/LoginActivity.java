package appricottsoftware.clarity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
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
import com.facebook.login.LoginManager;
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
import com.google.android.gms.tasks.OnCompleteListener;
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
        Profile fbProfile = Profile.getCurrentProfile();
        if (googleAccount != null && userId != -1) {
            login(getString(R.string.google_login_type));
        }

        else if (fbProfile != null && userId != -1) {
            login(getString(R.string.facebook_login_type));
            fbAccessTokenTracker.startTracking();
        }

        else if (userId != -1) {
            login(getString(R.string.registered_login_type));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fbAccessTokenTracker.stopTracking();
    }

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

    public void registerSocialMediaUser(final String email, String password, final String loginType) {
        ClarityApp.getRestClient().registerRequest(email, password, getApplicationContext(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                try {
                    Log.e(TAG, "register onSuccess : " + statusCode + "\n" + response.toString() );
                    // First time social media user

                    userId = response.getInt("uid");
                    ClarityApp.getSession(getApplicationContext()).setUserID(userId);

                    if (loginType == getString(R.string.facebook_login_type)) {
                        login(getString(R.string.facebook_login_type));
                    }
                    else if (loginType == getString(R.string.google_login_type)) {
                        login(getString(R.string.google_login_type));
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

                try {
                    Log.i(TAG, "Register onFailure -> Status Code: " + statusCode + "\tLogin Type:" + loginType);
                    switch(statusCode) {
                        // Server inactive. Log users out of their social media accounts.
                        case(0):
                            Toast.makeText(getApplicationContext(),
                                    "Server is down. Please try later.",
                                    Toast.LENGTH_LONG).show();

                            if (loginType == getString(R.string.facebook_login_type)) {
                                LoginManager.getInstance().logOut();
                            }
                            else if (loginType == getString(R.string.google_login_type)) {
                                Log.e(TAG, "Google Logout()");
                                googleSignOut();
                            }
                            break;

                        // Bad login credentials
                        case (400):
                            break;

                        // User already in database. Call authenticate endpoint.
                        case (401):
                            authenticate(userEmail, token, loginType);
                            break;

                        default:
                            Log.i(TAG, "Register onFailure. Default Switch. Status Code: " + statusCode);
                            break;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // HTTPS GET function to authenticate user
    public void authenticate(String email, String password, final String loginType) {
        ClarityApp.getRestClient().authenticateUser(email, password, getApplicationContext(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Log.i(TAG, "Authenticate OnSuccess -> Status Code: " + statusCode + "\tLoginType: " + loginType  + "\t" +  response.toString());

                    String uid = response.getString("uid");
                    if (uid == "-1") {
                        Toast unauthToast = Toast.makeText(getApplicationContext(),
                                R.string.no_auth,
                                Toast.LENGTH_SHORT);
                        unauthToast.show();
                    } else {
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
                    e.printStackTrace();
                }
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

                switch (statusCode) {
                    case (0):
                        Toast.makeText(getApplicationContext(),
                                "Server is down. Please try later.",
                                Toast.LENGTH_LONG).show();
                        break;

                    default:
                        Log.i(TAG, "Authenticate onFailure Default Case");
                        break;
                }
            }
        });
    }

    private void facebookLogin() {
        final Profile fbProfile = Profile.getCurrentProfile();

        // Necessary code to stay logged in
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
                                        token = hashPassword(userEmail);
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
                token = hashPassword(userEmail);
                registerSocialMediaUser(userEmail, token, getString(R.string.google_login_type));
            }
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Google Login", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    // Google sign out function
    private void googleSignOut() {
        ClarityApp clarityApp = new ClarityApp();
        GoogleSignInClient mGoogleSignInClient = clarityApp.getGoogleSignInClient();
        if (mGoogleSignInClient != null){
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            revokeAccess();
                        }
                    });
        }
    }

    // Google revoke access to fully delete account info
    private void revokeAccess() {
        final ClarityApp clarityApp = new ClarityApp();
        GoogleSignInClient mGoogleSignInClient = clarityApp.getGoogleSignInClient();
        if (mGoogleSignInClient != null) {
            mGoogleSignInClient.revokeAccess()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            clarityApp.clearGoogleSignInClient();
                        }
                    });
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