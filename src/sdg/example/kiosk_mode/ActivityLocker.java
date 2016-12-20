package sdg.example.kiosk_mode;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;

/**
 * This class is responsible for locking an app down so that a user can't exit it without pressing
 * the correct series of buttons:
 * Back, volume down, volume up
 *
 * It sets the app as the home activity, so that the phone should boot up into the app.
 *
 * To use this, the calling activity must connect up the onBackPressed, onKeyDown,
 */
public class ActivityLocker {

    private Activity lockActivity;
    private boolean inKioskMode;
    private DevicePolicyManager dpm;
    private ComponentName deviceAdmin;
    private UnlockPattern unlockState;

    public ActivityLocker(Activity act) throws Exception {
        lockActivity = act;

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
        Common.becomeHomeActivity(lockActivity);
    }
    
    public void onBackPressed() {
        unlockState = UnlockPattern.backPressed;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        unlockGodMode(keyCode);
        return true;
    }

    private void unlockGodMode(int keyCode) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            unlockState = UnlockPattern.backPressed;
        }
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && unlockState == UnlockPattern.backPressed){
            unlockState = UnlockPattern.volumeDownPressed;
        }
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && unlockState == UnlockPattern.volumeDownPressed){
            // Leave mode
            exitLockTask();
        }
        else {
            unlockState = UnlockPattern.reset;
        }
    }

    private void exitLockTask() {
        lockActivity.stopLockTask();
        inKioskMode = false;
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
        volumeDownPressed,
        volumeUpPressed
    }
}
