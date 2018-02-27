package appricottsoftware.clarity.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import appricottsoftware.clarity.R;
import appricottsoftware.clarity.RegisterActivity;
import appricottsoftware.clarity.sync.ClarityApp;
import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class SettingFragment extends Fragment {

    private static final String TAG = "SettingFragment";

    @BindView(R.id.sb_setting_length_set) AppCompatSeekBar sbLength;
    @BindView(R.id.tv_setting_progress) TextView tvProgress;
    @BindView(R.id.tv_setting_length_current) TextView tvCurrent;
    @BindView(R.id.Email) EditText email;
    @BindView(R.id.Password) EditText password;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // Initialize view lookups, listener
        // TODO: write a listener for when fragment is done drawing UI
        tvProgress.setVisibility(View.GONE);
        setLengthText();
        setSeekBar();
        getOldEmail();
        setEmailListener();
        setPasswordListener();
    }

    private void getOldEmail() {
        int uid = ClarityApp.getSession(getContext()).getUserID();
        ClarityApp.getRestClient().getEmail(uid, getContext(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    email.setText("");
                    email.setHint(response.getString("email"));
                    email.clearFocus();
                }
                catch (Exception e) {
                    Log.e(TAG, "getOldEmail: ", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(TAG, "getOldEmail Failed");
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    private boolean validateEmail(String emailString) {
        return emailString.length() > 0;
    }

    private void setEmailListener() {
        email.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (validateEmail(email.getText().toString()) && ((actionId == EditorInfo.IME_ACTION_DONE) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN)))) {
                    int uid = ClarityApp.getSession(getContext()).getUserID();
                    ClarityApp.getRestClient().updateEmail(uid, email.getText().toString(), getContext(), new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            getOldEmail();
                            Toast.makeText(getContext(), "Email updated!", Toast.LENGTH_LONG).show();
                            InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(email.getWindowToken(), 0);
                            email.clearFocus();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.e(TAG, "Unable to update email ", throwable);
                            Toast.makeText(getContext(), "Email not updated", Toast.LENGTH_LONG).show();
                            getOldEmail();
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                        }
                    });
                    return true;
                }
                else {
                    Toast.makeText(getContext(), "Invalid email", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        });
    }

    private boolean validatePassword(String password) {
        return password.length() >= 8;
    }

    private void setPasswordListener() {
        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (validatePassword(password.getText().toString()) && ((actionId == EditorInfo.IME_ACTION_DONE) || ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN)))) {
                    int uid = ClarityApp.getSession(getContext()).getUserID();
                    String newPassword = RegisterActivity.hashPassword(password.getText().toString());
                    ClarityApp.getRestClient().updatePassword(uid, newPassword, getContext(), new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Toast.makeText(getContext(), "Password updated!", Toast.LENGTH_LONG).show();
                            InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
                            password.setText("");
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.e(TAG, "Unable to update password ", throwable);
                            Toast.makeText(getContext(), "Password not updated", Toast.LENGTH_LONG).show();
                            getOldEmail();
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                        }
                    });
                    return true;
                }
                else {
                    Toast.makeText(getContext(), "Invalid password", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        });
    }

    private void setSeekBar() {
        // Get the last used max length and set the seekbar to the max length
        sbLength.setOnSeekBarChangeListener(new AppCompatSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (tvProgress.getVisibility() == View.GONE) {
                    tvProgress.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (tvProgress.getVisibility() == View.VISIBLE) {
                    tvProgress.setVisibility(View.GONE);
                }
                Log.e(TAG, "OnStopTrackingTouch " + seekBar.getProgress());
                setLengthText();
                int maxLength = ClarityApp.getSession(getContext()).getMaxLength();
                setMaxLength(maxLength);
            }
        });
    }

    private void setProgress(int progress) {
        Log.e(TAG, "Progress: " + progress);
        // Round the progress to the nearest 10
        progress = ((progress + 5) / 10) * 10;
        // Set the text and progress bar
        sbLength.setProgress(progress);
        tvProgress.setText(getLengthText(progress));
        ClarityApp.getSession(getContext()).setMaxLength(progress);
        int progressLeftX = sbLength.getLeft() + sbLength.getPaddingLeft();
        int progressRightX = sbLength.getRight() - sbLength.getPaddingRight();
        int progressX = (((progressRightX - progressLeftX) * progress) / sbLength.getMax()) + progressLeftX;
        progressX -= tvProgress.getWidth() / 2;
        tvProgress.setX(progressX);
        Log.e(TAG, "setProgress: " + sbLength.getRight() + " " + sbLength.getPaddingRight() + " lX: " + progressLeftX + " rX: " + progressRightX + " X: " + progressX);
    }

    private String getLengthText(int length) {
        if(length <= 0) {
            return "off";
        }
        return Integer.toString(length);
    }

    private void setLengthText() {
        int length = ClarityApp.getSession(getContext()).getMaxLength();
        tvCurrent.setText(getLengthText(length));
        setProgress(length);
        Log.e(TAG, "Length: " + length);
    }

    private void setMaxLength(int maxLength) {
        
    }
}
