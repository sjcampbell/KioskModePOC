# KioskModePOC
A proof of concept project, implementing an app in kiosk mode.  

In order for this to really lock an app down so that Android reboots into the app, you need to set a device owner.  

**Warning! Setting a device owner cannot be undone without rooting the device or performing a factory reset.**

`adb shell dpm set-device-owner com.aceage.kioskmode/.AdminReceiver`
