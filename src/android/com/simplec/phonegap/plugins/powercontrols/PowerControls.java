package com.simplec.phonegap.plugins.powercontrols;

import java.io.File;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.PowerManager;
import android.util.Log;

public class PowerControls extends CordovaPlugin {
	private static final String LOG_TAG = "AndroidPowerControls";
	private static final String REBOOT = "reboot";
	private static final String EXIT = "exit";
	private static final String GET_VOLUME = "getVolume";
	private static final String SET_VOLUME = "setVolume";
	private static final String SET_VOLUME_ALL_MAX = "setVolumeMax";
	private static final String SET_USE_SPEAKER = "setUseSpeaker";
	private static final String DELETE_DIR_RECURSIVE = "rmDir";

	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
	}
	
	public static String stripFileProtocol(String uriString) {
		if (uriString.startsWith("file://")) {
			return Uri.parse(uriString).getPath();
		}
		return uriString;
	}

	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
		if (DELETE_DIR_RECURSIVE.equals(action)) {
			try {
				CordovaResourceApi resourceApi = webView.getResourceApi();
				String target = args.getString(0);

				String fileUriStr;
				try {
					Uri targetUri = resourceApi.remapUri(Uri.parse(target));
					fileUriStr = targetUri.toString();
				} catch (IllegalArgumentException e) {
					fileUriStr = target;
				}

				Log.v(LOG_TAG, "removing dir: " + fileUriStr);

				final String path = stripFileProtocol(fileUriStr);

				File f = new File(path);
				if (!f.exists()) {
					callbackContext.success();
				} else {
					Log.v(LOG_TAG, "rm -rf " + path);
					Runtime.getRuntime().exec(new String[] {"sh", "-c", "rm -rf " + path});
					Log.v(LOG_TAG, "rm succeeded (?)");
					callbackContext.success();
				}
			} catch (Exception e) {
				Log.i(LOG_TAG, e.getMessage());
				callbackContext.error(e.getMessage());
			}
			
			return true;
		}
        if (REBOOT.equals(action)) {
        	try {
	        	PowerManager pm = (PowerManager) webView.getContext().getSystemService(Context.POWER_SERVICE);
	        	pm.reboot(args.getString(0));
        	} catch (Exception e) {
        		
        	}
        	
        	Intent intent = new Intent("com.simplec.android.service.installer.REBOOT");
        	webView.getContext().sendBroadcast(intent);
        	
            return true;
        }  
        
        if (EXIT.equals(action)) {
        	try {
        		android.os.Process.killProcess(android.os.Process.myPid());
        	} catch (Exception e) {
        	}
        	return true;
        }
        
        if (SET_USE_SPEAKER.equals(action)) {
        	AudioManager am = (AudioManager) webView.getContext().getSystemService(Context.AUDIO_SERVICE);
        	boolean useSpeaker = args.getBoolean(0);
        	
            if(useSpeaker){
            	if (!am.isSpeakerphoneOn() || am.getMode()!=AudioManager.MODE_NORMAL) {
                	am.setMode(AudioManager.MODE_NORMAL); 
                    am.setSpeakerphoneOn(useSpeaker);
            	}
            } else {
            	if (am.isSpeakerphoneOn() || am.getMode()!=AudioManager.MODE_IN_CALL) {
	                am.setMode(AudioManager.MODE_IN_CALL);   
	                am.setSpeakerphoneOn(useSpeaker);     
            	}
            }

			callbackContext.success();
			
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

	        stats.put("volume-alarm-max", am.getStreamMaxVolume(AudioManager.STREAM_ALARM));
	        stats.put("volume-dtmf-max", am.getStreamMaxVolume(AudioManager.STREAM_DTMF));
	        stats.put("volume-music-max", am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
	        stats.put("volume-notification-max", am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));
	        stats.put("volume-ring-max", am.getStreamMaxVolume(AudioManager.STREAM_RING));
	        stats.put("volume-system-max", am.getStreamMaxVolume(AudioManager.STREAM_SYSTEM));
	        stats.put("volume-voice-call-max", am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));
	        
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
    	        cordova.getThreadPool().execute(new SetVolume(callbackContext, streamId, streamVolume));
        	}
        	if (streamType.equalsIgnoreCase("dtmf")) {
        		streamId = AudioManager.STREAM_DTMF;
    	        cordova.getThreadPool().execute(new SetVolume(callbackContext, streamId, streamVolume));
        	} else if (streamType.equalsIgnoreCase("music")) {
        		streamId = AudioManager.STREAM_MUSIC;
    	        cordova.getThreadPool().execute(new SetVolume(callbackContext, streamId, streamVolume));
        	} else if (streamType.equalsIgnoreCase("notification")) {
        		streamId = AudioManager.STREAM_NOTIFICATION;
    	        cordova.getThreadPool().execute(new SetVolume(callbackContext, streamId, streamVolume));
        	} else if (streamType.equalsIgnoreCase("ring")) {
        		streamId = AudioManager.STREAM_RING;
    	        cordova.getThreadPool().execute(new SetVolume(callbackContext, streamId, streamVolume));
        	} else if (streamType.equalsIgnoreCase("system")) {
        		streamId = AudioManager.STREAM_SYSTEM;
    	        cordova.getThreadPool().execute(new SetVolume(callbackContext, streamId, streamVolume));
        	} else if (streamType.equalsIgnoreCase("voice-call")) {
        		streamId = AudioManager.STREAM_VOICE_CALL;
    	        cordova.getThreadPool().execute(new SetVolume(callbackContext, streamId, streamVolume));
        	} else {
    	        cordova.getThreadPool().execute(new SetAllVolumes(callbackContext, streamVolume));
        	}        	

			return true;
        }
        
        if (SET_VOLUME_ALL_MAX.equals(action)) {
	        cordova.getThreadPool().execute(new SetAllVolumes(callbackContext, 1));
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
	
	private class SetAllVolumes implements Runnable {
		private CallbackContext callbackContext;
		private double volume = 1;
		
		public SetAllVolumes(CallbackContext callbackContext, double volume) {
			this.callbackContext = callbackContext;
			this.volume = volume;
		}
		@Override
		public void run() {
			int[] streams = null;
    		try {
    			Class c = Class.forName("android.media.AudioManager");
    			streams = (int[])c.getField("DEFAULT_STREAM_VOLUME").get(null);
    		} catch (Exception e) {
    			streams = new int[] {
    			        4,  // STREAM_VOICE_CALL
    			        7,  // STREAM_SYSTEM
    			        5,  // STREAM_RING
    			        11, // STREAM_MUSIC
    			        6,  // STREAM_ALARM
    			        5,  // STREAM_NOTIFICATION
    			        7,  // STREAM_BLUETOOTH_SCO
    			        7,  // STREAM_SYSTEM_ENFORCED
    			        11, // STREAM_DTMF
    			        11  // STREAM_TTS
    			};
    		}

        	AudioManager am = (AudioManager) webView.getContext().getSystemService(Context.AUDIO_SERVICE);
    		for (int streamId : streams) {
            	setStreamVolume(am, streamId, volume);
    		}

			callbackContext.success();
		}
	}
	
	public void setStreamVolume(AudioManager am, int streamId, double streamVolume) {
		try {
	    	int max = am.getStreamMaxVolume(streamId);
	    	double volume = ((double)max) * streamVolume;
	    	if (Math.round(volume)==0) {
	    		am.setStreamMute(streamId, true);
	    	} else {
	    		try {
	    			for (int i=1; i<50; i++) {
	            		am.setStreamMute(streamId, false);
	    			}
	    		} catch (Exception e) {
	    			
	    		}
	        	am.setStreamVolume(streamId, (int)Math.round(volume), 0);
	    	}
		} catch (Exception e) {
			
		}
	}
}
