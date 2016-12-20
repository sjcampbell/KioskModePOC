package com.aceage.kioskmode;

import android.app.Activity;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.view.KeyEvent;
import android.widget.Toast;

/**
 * This class is responsible for locking an app down so that a user can't exit it without pressing
 * the correct series of buttons:
 * Back, volume down, volume up
 *
 * It sets the app as the home activity, so that the phone should boot up into the app.
 *
 * To use this, the calling activity must connect up the onKeyDown method to the activity's event.
 */
public class ActivityLocker {

    private Activity lockActivity;
    private boolean inKioskMode;
    private DevicePolicyManager dpm;
    private ComponentName deviceAdmin;
    private UnlockPattern unlockState = UnlockPattern.reset;

    /**
     * This constructor puts the app into kiosk mode.
     * The activity must be created already, so this should be called
     * in the activity's onCreate method after setContentView
     * @param mainActivity is the main activity of the app to be locked.
     * @throws Exception if the device cannot enter kiosk mode. The app must be a device admin and a device owner (run adb command)
     */
    public ActivityLocker(Activity mainActivity) throws Exception {
        lockActivity = mainActivity;

        deviceAdmin = new ComponentName(lockActivity, AdminReceiver.class);
        dpm = (DevicePolicyManager) lockActivity.getSystemService(Context.DEVICE_POLICY_SERVICE);

        if (!dpm.isAdminActive(deviceAdmin)) {
            throw new Exception("This app is not a device admin! It must be a device admin in order to enter kiosk mode.");
        }
        if (dpm.isDeviceOwnerApp(lockActivity.getPackageName())) {
            dpm.setLockTaskPackages(deviceAdmin,
                    new String[]{lockActivity.getPackageName()});
        } else {
            throw new Exception("This app is not a device owner! It must be a device owner in order to enter kiosk mode.");
        }

        // Enter Kiosk Mode.
        enterLockTask();
        // Set as the home app.
        Common.becomeHomeActivity(lockActivity, lockActivity.getClass());
    }

    public boolean onKeyDown(int keyCode) {
        toggleGodMode(keyCode);
        return true;
    }

    private void toggleGodMode(int keyCode) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            unlockState = UnlockPattern.backPressed;
        }
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && unlockState == UnlockPattern.backPressed){
            unlockState = UnlockPattern.volumeDownPressed;
        }
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && unlockState == UnlockPattern.volumeDownPressed){
            // Code successful!
            if (inKioskMode) {
                exitLockTask();
            } else {
                try {
                    enterLockTask();
                }
                catch (Exception ex) {
                    Common.showToast(lockActivity, ex.getMessage());
                }
            }
        }
        else {
            unlockState = UnlockPattern.reset;
        }
    }

    private void exitLockTask() {
        lockActivity.stopLockTask();
        restoreLauncher();
        inKioskMode = false;
    }

    private void restoreLauncher() {
        dpm.clearPackagePersistentPreferredActivities(deviceAdmin,
                lockActivity.getPackageName());
        Common.showToast(lockActivity, "Home activity: " + Common.getHomeActivity(lockActivity));
    }

    private void enterLockTask() throws Exception {
        if (dpm.isLockTaskPermitted(lockActivity.getPackageName())) {
            lockActivity.startLockTask();
            inKioskMode = true;
        } else {
            throw new Exception("Kiosk Mode not permitted");
        }
    }

    private enum UnlockPattern {
        reset,
        backPressed,
        volumeDownPressed
        //volumeUpPressed (this state means code was entered successfully)
    }
}
