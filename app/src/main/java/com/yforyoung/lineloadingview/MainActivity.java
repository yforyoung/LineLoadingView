package com.yforyoung.lineloadingview;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private LineLoadingView lineLoadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lineLoadingView = findViewById(R.id.music_loading);
        findViewById(R.id.bt_control).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lineLoadingView.isPlaying()) {
                    lineLoadingView.pauseAnim();
                } else {
                    lineLoadingView.startAnim();
                }
            }
        });

        findViewById(R.id.bt_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lineLoadingView.stopAnim();
            }
        });


    }
}
