package com.aceage.kioskmode;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.view.KeyEvent;

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

    private Activity mLockActivity;
    private boolean mInKioskMode;
    private DevicePolicyManager mDpm;
    private ComponentName mDeviceAdmin;
    private UnlockPattern mUnlockState = UnlockPattern.reset;

    /**
     * This constructor puts the app into kiosk mode.
     * The activity must be created already, so this should be called
     * in the activity's onCreate method after setContentView
     * @param mainActivity is the main activity of the app to be locked.
     * @throws IllegalStateException if the device cannot enter kiosk mode. The app must be a device admin and a device owner (run adb command)
     */
    public ActivityLocker(Activity mainActivity) throws IllegalStateException {
        mLockActivity = mainActivity;

        mDeviceAdmin = new ComponentName(mLockActivity, AdminReceiver.class);
        mDpm = (DevicePolicyManager) mLockActivity.getSystemService(Context.DEVICE_POLICY_SERVICE);

        if (!mDpm.isAdminActive(mDeviceAdmin)) {
            throw new IllegalStateException("This app is not a device admin! It must be a device admin in order to enter kiosk mode.");
        }
        if (mDpm.isDeviceOwnerApp(mLockActivity.getPackageName())) {
            mDpm.setLockTaskPackages(mDeviceAdmin,
                    new String[]{mLockActivity.getPackageName()});
        } else {
            throw new IllegalStateException("This app is not a device owner! It must be a device owner in order to enter kiosk mode.");
        }

        // Enter Kiosk Mode.
        enterLockTask();
        // Set as the home app.
        Common.becomeHomeActivity(mLockActivity, mLockActivity.getClass());
    }

    public boolean onKeyDown(int keyCode) {
        toggleGodMode(keyCode);
        return true;
    }

    private void toggleGodMode(int keyCode) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            mUnlockState = UnlockPattern.backPressed;
        }
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && mUnlockState == UnlockPattern.backPressed){
            mUnlockState = UnlockPattern.volumeDownPressed;
        }
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && mUnlockState == UnlockPattern.volumeDownPressed) {
            // Code successful!
            if (mInKioskMode) {
                exitLockTask();
            } else {
                try {
                    enterLockTask();
                } catch (IllegalStateException ex) {
                    Common.showToast(mLockActivity, ex.getMessage());
                }
            }
        }
        else {
            mUnlockState = UnlockPattern.reset;
        }
    }

    private void exitLockTask() {
        mLockActivity.stopLockTask();
        restoreLauncher();
        mInKioskMode = false;
    }

    private void restoreLauncher() {
        mDpm.clearPackagePersistentPreferredActivities(mDeviceAdmin,
                mLockActivity.getPackageName());
        Common.showToast(mLockActivity, "Home activity: " + Common.getHomeActivity(mLockActivity));
    }

    private void enterLockTask() throws IllegalStateException {
        if (mDpm.isLockTaskPermitted(mLockActivity.getPackageName())) {
            mLockActivity.startLockTask();
            mInKioskMode = true;
            Common.becomeHomeActivity(mLockActivity, mLockActivity.getClass());
        } else {
            throw new IllegalStateException("Kiosk mode not permitted");
        }
    }

    private enum UnlockPattern {
        reset,
        backPressed,
        volumeDownPressed
        //volumeUpPressed (this state means code was entered successfully)
    }
}
