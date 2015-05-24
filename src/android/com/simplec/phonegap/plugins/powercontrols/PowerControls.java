package com.simplec.phonegap.plugins.powercontrols;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.os.PowerManager;

public class PowerControls extends CordovaPlugin {
	private static final String REBOOT = "reboot";
	private static final String EXIT = "exit";

	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
	}

	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (REBOOT.equals(action)) {
        	PowerManager pm = (PowerManager) webView.getContext().getSystemService(Context.POWER_SERVICE);
        	pm.reboot(args.getString(0));
            return true;
        }  
        
        if (EXIT.equals(action)) {
        	try {
        		android.os.Process.killProcess(android.os.Process.myPid());
        	} catch (Exception e) {
        	}
        	return true;
        }

    	callbackContext.error(action + " is not a supported function.");
    	return false;
    }
}
