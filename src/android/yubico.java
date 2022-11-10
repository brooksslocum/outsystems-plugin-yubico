package com.outsystems;

import android.app.Activity;
import android.content.Intent;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import com.yubico.yubikit.android.YubiKitManager;
import com.yubico.yubikit.android.ui.OtpActivity;
import com.yubico.yubikit.core.application.CommandException;
import com.yubico.yubikit.management.DeviceInfo;
import com.yubico.yubikit.management.ManagementSession;
import com.yubico.yubikit.android.transport.nfc.NfcConfiguration;
import com.yubico.yubikit.android.transport.nfc.NfcNotAvailable;
import com.yubico.yubikit.core.util.NdefUtils;
import com.yubico.yubikit.android.transport.nfc.NfcYubiKeyDevice;
import java.io.IOException;

public class yubico extends CordovaPlugin {

    CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;

        if (action.equals("getOTP")) {
            this.getOTP(callbackContext);
            return true;
        }
        if (action.equals("startNFCDiscovery")) {
            this.startNFCDiscovery(callbackContext);
            return true;
        }
        if (action.equals("stopNFCDiscovery")) {
            this.stopNFCDiscovery(callbackContext);
            return true;
        }
        return false;
    }

    private void startNFCDiscovery(CallbackContext callbackContext) {
        YubiKitManager yubiKitManager = new YubiKitManager(cordova.getContext());

        try {
            yubiKitManager.startNfcDiscovery(new NfcConfiguration(), cordova.getActivity(), device -> {

                // A YubiKey was brought within NFC range
                ManagementSession.create(device, result -> {
                    try {
                        
                        String credential = "abc";
                        callbackContext.success(credential);

                    } catch (IOException | CommandException e) {
                        callbackContext.error("Error #001: Could not read YubiKey Serial Number.");
                    } catch (Exception e) {
                        callbackContext.error("Error #001: Could not read YubiKey Serial Number.");
                    }
                });
            });
        } catch ( NfcNotAvailable e) {
            if (e.isDisabled()) {
                callbackContext.error("Error #002: Android NFC is turned off.");
            } else {
                callbackContext.error("Error #003: This device is not supported.");
            }
        }
    }

    public void stopNFCDiscovery(CallbackContext callbackContext) {
        YubiKitManager yubiKitManager = new YubiKitManager(cordova.getContext());
        yubiKitManager.stopNfcDiscovery(cordova.getActivity());
        callbackContext.success("NFC Discovery stopped");
    }

    public void getOTP(CallbackContext callbackContext){
        cordova.startActivityForResult(this, new Intent(cordova.getContext(), OtpActivity.class), 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            String otp = data.getStringExtra("otp");
            this.callbackContext.success(otp);
        } else {
            callbackContext.error("Error #004: Could not read OTP.");
        }
    }
}
