package com.ttarfall.statusbar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.ttarfall.statusbar.base.BaseActivity;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private Toolbar toolbar;
    private Button btnStusNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        btnStusNav = (Button)findViewById(R.id.btn_status_navigation);
        btnStusNav.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_status_navigation:
                startActivity(new Intent(this, StatusNavActivity.class));
                break;
            default:
                break;
        }
    }
}
