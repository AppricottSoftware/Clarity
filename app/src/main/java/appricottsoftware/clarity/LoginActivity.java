package appricottsoftware.clarity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.bt_login) Button btLogin;
    @BindView(R.id.bt_register) Button btRegister;
    @BindView(R.id.et_email) EditText etEmail;
    @BindView(R.id.et_password) EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        btLogin.setOnClickListener(this);
        btRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bt_login:
                String strEmail = etEmail.getText().toString();
                String strPassword = etPassword.getText().toString();

                if (isAuthenticated()) {
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

    public boolean isAuthenticated() {
        return true;
    }

}
