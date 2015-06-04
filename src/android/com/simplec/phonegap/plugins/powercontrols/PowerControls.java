package com.simplec.phonegap.plugins.powercontrols;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.media.AudioManager;
import android.os.PowerManager;

public class PowerControls extends CordovaPlugin {
	private static final String REBOOT = "reboot";
	private static final String EXIT = "exit";
	private static final String GET_VOLUME = "getVolume";
	private static final String SET_VOLUME = "setVolume";
	private static final String SET_VOLUME_ALL_MAX = "setVolumeMax";

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
        
        if (GET_VOLUME.equals(action)) {
        	AudioManager am = (AudioManager) webView.getContext().getSystemService(Context.AUDIO_SERVICE);

			JSONObject stats = new JSONObject();

	        stats.put("volume-alarm", am.getStreamVolume(AudioManager.STREAM_ALARM));
	        stats.put("volume-dtmf", am.getStreamVolume(AudioManager.STREAM_DTMF));
	        stats.put("volume-music", am.getStreamVolume(AudioManager.STREAM_MUSIC));
	        stats.put("volume-notification", am.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
	        stats.put("volume-ring", am.getStreamVolume(AudioManager.STREAM_RING));
	        stats.put("volume-system", am.getStreamVolume(AudioManager.STREAM_SYSTEM));
	        stats.put("volume-voice-call", am.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
	        
			callbackContext.success(stats);
			return true;
        }

        if (SET_VOLUME.equals(action)) {
        	if (args.length()!=2) {
        		callbackContext.error("Must send stream type (string) and volume (double)");
        		return false;
        	}
        	
        	String streamType = args.getString(0);
        	double streamVolume = ((double)args.getInt(1))/100;
        	if (streamVolume<0) streamVolume=0;
        	if (streamVolume>1) streamVolume=1;
        	
        	int streamId = -1;
        	if (streamType.equalsIgnoreCase("alarm")) {
        		streamId = AudioManager.STREAM_ALARM;
        	}
        	if (streamType.equalsIgnoreCase("dtmf")) {
        		streamId = AudioManager.STREAM_DTMF;
        	}
        	if (streamType.equalsIgnoreCase("music")) {
        		streamId = AudioManager.STREAM_MUSIC;
        	}
        	if (streamType.equalsIgnoreCase("notification")) {
        		streamId = AudioManager.STREAM_NOTIFICATION;
        	}
        	if (streamType.equalsIgnoreCase("ring")) {
        		streamId = AudioManager.STREAM_RING;
        	}
        	if (streamType.equalsIgnoreCase("system")) {
        		streamId = AudioManager.STREAM_SYSTEM;
        	}
        	if (streamType.equalsIgnoreCase("voice-call")) {
        		streamId = AudioManager.STREAM_VOICE_CALL;
        	}
	        cordova.getThreadPool().execute(new SetVolume(callbackContext, streamId, streamVolume));

			return true;
        }
        
        if (SET_VOLUME_ALL_MAX.equals(action)) {
	        cordova.getThreadPool().execute(new SetAllVolumesMax(callbackContext));
	        return true;
        }

    	callbackContext.error(action + " is not a supported function.");
    	return false;
    }

	private class SetVolume implements Runnable {
		private CallbackContext callbackContext;
		private int streamId;
		private double streamVolume;
		public SetVolume(CallbackContext callbackContext, int streamId, double streamVolume) {
			this.callbackContext = callbackContext;
			this.streamId = streamId;
			this.streamVolume = streamVolume;
		}
		@Override
		public void run() {
        	AudioManager am = (AudioManager) webView.getContext().getSystemService(Context.AUDIO_SERVICE);
        	setStreamVolume(am, streamId, streamVolume);
        	
			callbackContext.success();
		}
	}
	
	private class SetAllVolumesMax implements Runnable {
		private CallbackContext callbackContext;
		public SetAllVolumesMax(CallbackContext callbackContext) {
			this.callbackContext = callbackContext;
		}
		@Override
		public void run() {
        	AudioManager am = (AudioManager) webView.getContext().getSystemService(Context.AUDIO_SERVICE);
        	setStreamVolume(am, AudioManager.STREAM_ALARM, 1);
        	setStreamVolume(am, AudioManager.STREAM_DTMF, 1);
        	setStreamVolume(am, AudioManager.STREAM_MUSIC, 1);
        	setStreamVolume(am, AudioManager.STREAM_NOTIFICATION, 1);
        	setStreamVolume(am, AudioManager.STREAM_RING, 1);
        	setStreamVolume(am, AudioManager.STREAM_SYSTEM, 1);
        	setStreamVolume(am, AudioManager.STREAM_VOICE_CALL, 1);

			callbackContext.success();
		}
	}
	
	public void setStreamVolume(AudioManager am, int streamId, double streamVolume) {
		try {
	    	int max = am.getStreamMaxVolume(streamId);
	    	double volume = ((double)max) * streamVolume;
	    	if (Math.floor(volume)==0) {
	    		am.setStreamMute(streamId, true);
	    	} else {
	    		try {
	    			for (int i=1; i<50; i++) {
	            		am.setStreamMute(streamId, false);
	    			}
	    		} catch (Exception e) {
	    			
	    		}
	        	am.setStreamVolume(streamId, (int)Math.floor(volume), 0);
	    	}
		} catch (Exception e) {
			
		}
	}
}
