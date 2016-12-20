/*
 * Kiosk Mode (aka Screen Pinning, aka Task Locking) demo for Android 5+
 *
 * Copyright 2015, SDG Systems, LLC
 */

package sdg.example.kiosk_mode;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class KioskModeDemo extends Activity implements View.OnTouchListener {
    private final static String TAG = "KioskModeDemo";
    private Button button;
    private boolean inKioskMode = false;
    private DevicePolicyManager dpm;
    private ComponentName deviceAdmin;

    private UnlockPattern unlock = UnlockPattern.reset;

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // Nothing to add here.
        return false;
    }

    private enum UnlockPattern {
        reset,
        backPressed,
        volumeDownPressed,
        volumeUpPressed
    }

    private void showToast(String text) {
        if (BuildConfig.DEBUG) {
            //Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }
    }

    private void setKioskMode(boolean on) {
        try {
            if (on) {
                enterLockTask();
            } else {
                exitLockTask();
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e);
        }
    }

    private void exitLockTask() {
        stopLockTask();
        inKioskMode = false;
        button.setText("Enter Kiosk Mode");
    }

    private void enterLockTask() {
        if (dpm.isLockTaskPermitted(this.getPackageName())) {
            startLockTask();
            inKioskMode = true;
            button.setText("Exit Kiosk Mode");
        } else {
            showToast("Kiosk Mode not permitted");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        button = (Button) findViewById(R.id.button1);
        deviceAdmin = new ComponentName(this, AdminReceiver.class);
        dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        if (!dpm.isAdminActive(deviceAdmin)) {
            showToast("This app is not a device admin!");
        }
        if (dpm.isDeviceOwnerApp(getPackageName())) {
            dpm.setLockTaskPackages(deviceAdmin,
                    new String[] { getPackageName() });
        } else {
            showToast("This app is not the device owner!");
        }

        // Enter Kiosk Mode.
        enterLockTask();
        // Set as the home app.
        Common.becomeHomeActivity(this);
        // Keep the screen on.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        unlock = UnlockPattern.backPressed;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Unlock the task by pressing:
        // Back, Volume Down, Volume Up
        unlockGodMode(keyCode);
        return true;
    }

    public void toggleKioskMode(View view) {
        setKioskMode(!inKioskMode);
    }

    public void restoreLauncher(View view) {
        dpm.clearPackagePersistentPreferredActivities(deviceAdmin,
                this.getPackageName());
        showToast("Home activity: " + Common.getHomeActivity(this));
    }

    public void setLauncher(View view) {
        Common.becomeHomeActivity(this);
    }

    private void unlockGodMode(int keyCode) {
        if(keyCode==KeyEvent.KEYCODE_BACK) {
            unlock = UnlockPattern.backPressed;
        }
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && unlock == UnlockPattern.backPressed){
            unlock = UnlockPattern.volumeDownPressed;
        }
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && unlock == UnlockPattern.volumeDownPressed){
            // Leave mode
            exitLockTask();
        }
        else {
            unlock = UnlockPattern.reset;
        }
    }
}