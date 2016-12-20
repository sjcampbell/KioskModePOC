package com.aceage.kioskmode;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AdminReceiver extends DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) {
        showToast(context, "[Device Admin enabled]");
        Class c = context.getClass();
        Common.becomeHomeActivity(context, c);
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return "Warning: Device Admin is going to be disabled.";
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        showToast(context, "[Device Admin disabled]");
    }

    @Override
    public void onLockTaskModeEntering(Context context, Intent intent,
            String pkg) {
        showToast(context, "[Kiosk Mode enabled]");
    }

    @Override
    public void onLockTaskModeExiting(Context context, Intent intent) {
        showToast(context, "[Kiosk Mode disabled]");
    }

    private void showToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }
}
