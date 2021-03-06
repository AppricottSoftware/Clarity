package appricottsoftware.clarity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


import appricottsoftware.clarity.models.Session;
import appricottsoftware.clarity.sync.ClarityApp;
import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.bt_register) Button btRegister;
    @BindView(R.id.editText_email) EditText email;
    @BindView(R.id.editText_password) EditText password;

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

                String emailString = email.getText().toString();
                String hashedPassword = hashPassword(password.getText().toString());

                // Sanity Check to make sure all instances are populated with actual strings
                if(!isValidEmail(emailString)){
                    Toast.makeText(v.getContext(), "Email Invalid", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(password.getText().toString().length() == 0 || password.getText().toString().length() < 6) {
                    Toast.makeText(v.getContext(), "Password Invalid", Toast.LENGTH_SHORT).show();
                    return;
                }

                ClarityApp.getRestClient().registerRequest(emailString, hashedPassword, getApplicationContext(), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);

                        // Setting userID for the session from returned JSON object
                        try {
                            ClarityApp.getSession(getApplicationContext()).setUserID(response.getInt("uid"));

                            // After successful save of user info on back end
                            // Switch to home activity
                            Intent homeActivityIntent = new Intent(getApplicationContext(), HomeActivity.class);
                            homeActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            homeActivityIntent.putExtra("loginType", getString(R.string.registered_login_type));
                            startActivity(homeActivityIntent);
                            finish();
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        try {
                            switch(statusCode) {
                                case(0):
                                    Toast.makeText(getApplicationContext(),
                                            "Server is down. Please try later.",
                                            Toast.LENGTH_LONG).show();
                                    break;
                                default:
                                    Log.i(TAG, "Register onFailure. Default Switch. Status Code: " + statusCode);
                                    break;
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                    }

                });
                break;

            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent loginActivityIntent = new Intent(this, LoginActivity.class);
        startActivity(loginActivityIntent);
        finish();
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }


    public static String hashPassword(String originalPassword) {
        String hashedPassword = null;
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
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