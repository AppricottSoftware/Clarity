package appricottsoftware.clarity;

import android.app.Activity;
import android.content.Intent;
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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import appricottsoftware.clarity.sync.ClarityApp;

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

    private static final String EMAIL = "email";
    private CallbackManager fbCallbackManager;
    private AccessTokenTracker fbAccessTokenTracker;
    private GoogleSignInAccount googleAccount;
    private GoogleSignInClient googleSignInClient;
    private boolean userIsAuthenticated = false;


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

        // TODO keep persistent login for users who register with e-mail password. Waiting on database.
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

    // HTTPS GET function to authenticate user. Currently not working.
    public void authenticate(String email, String password) {
        final Activity parentActivity = this;
        ClarityApp.getRestClient(this).authenticateUser(email, password, this, new JsonHttpResponseHandler() {
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
                        login("1");
//                        Intent homeActivityIntent = new Intent(parentActivity, HomeActivity.class);
//                        homeActivityIntent.putExtra("loginType", )
//                        startActivity(homeActivityIntent);
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
        Profile fbProfile = Profile.getCurrentProfile();
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
            btnFacebook = findViewById(R.id.btn_loginFacebook);
            btnFacebook.setReadPermissions(Arrays.asList(EMAIL));
            btnFacebook.registerCallback(fbCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    // App code
                    GraphRequest request = GraphRequest.newMeRequest(
                            AccessToken.getCurrentAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject object, GraphResponse response) {
                                    FacebookSdk.setIsDebugEnabled(true);
                                    FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

                                    // Can use Profile to extract user info here
                                }
                            });

                    request.executeAsync();
                    login(getString(R.string.facebook_login_type));
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

            // Signed in successfully, show authenticated UI.
            login(getString(R.string.google_login_type));
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Google Login", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    // Generic login function that takes the user to HomeActivity
    // "1" is e-mail password, "2" is facebook, "3" is google
    private void login(String loginType) {
        Intent homeActivityIntent = new Intent(LoginActivity.this, HomeActivity.class);
        homeActivityIntent.putExtra("loginType", loginType);
        startActivity(homeActivityIntent);
        finish();
    }

    private void register() {
        Intent registerActivityIntent = new Intent(this, RegisterActivity.class);
        startActivity(registerActivityIntent);
        finish();
    }
}
