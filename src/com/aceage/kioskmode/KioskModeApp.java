package com.aceage.kioskmode;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class KioskModeApp extends Activity implements View.OnTouchListener {

    private ActivityLocker locker;

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // Nothing to add here.
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        try {
            locker = new ActivityLocker(this);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        catch(Exception ex) {
            Common.showToast(this, ex.getMessage());
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return locker.onKeyDown(keyCode);
    }
}