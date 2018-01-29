package appricottsoftware.clarity;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SurveyActivity extends AppCompatActivity {

    @BindView(R.id.toggleButton_cat1) ToggleButton btCat1;
    @BindView(R.id.toggleButton_cat2) ToggleButton btCat2;
    @BindView(R.id.toggleButton_cat3) ToggleButton btCat3;
    @BindView(R.id.toggleButton_cat4) ToggleButton btCat4;
    @BindView(R.id.toggleButton_cat5) ToggleButton btCat5;
    @BindView(R.id.toggleButton_cat6) ToggleButton btCat6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        ButterKnife.bind(this);

        btCat1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {btCat1.setBackground(getResources().getDrawable(R.drawable.ic_nav_drawer_like_green));}
                else {btCat1.setBackground(getResources().getDrawable(R.drawable.ic_nav_drawer_like_white));}
            }
        });
        btCat2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {btCat2.setBackground(getResources().getDrawable(R.drawable.ic_nav_drawer_like_green));}
                else {btCat2.setBackground(getResources().getDrawable(R.drawable.ic_nav_drawer_like_white));}
            }
        });
        btCat3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {btCat3.setBackground(getResources().getDrawable(R.drawable.ic_nav_drawer_like_green));}
                else {btCat3.setBackground(getResources().getDrawable(R.drawable.ic_nav_drawer_like_white));}
            }
        });
        btCat4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {btCat4.setBackground(getResources().getDrawable(R.drawable.ic_nav_drawer_like_green));}
                else {btCat4.setBackground(getResources().getDrawable(R.drawable.ic_nav_drawer_like_white));}
            }
        });
        btCat5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {btCat5.setBackground(getResources().getDrawable(R.drawable.ic_nav_drawer_like_green));}
                else {btCat5.setBackground(getResources().getDrawable(R.drawable.ic_nav_drawer_like_white));}
            }
        });
        btCat6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {btCat6.setBackground(getResources().getDrawable(R.drawable.ic_nav_drawer_like_green));}
                else {btCat6.setBackground(getResources().getDrawable(R.drawable.ic_nav_drawer_like_white));}
            }
        });

    }
}





/*

TV & Film                   68
Sports & Recreation         77
Technology                  127
History                     125
News & Politics             99
Religion & Spirituality     69

*/