
var AndroidPowerControls = function() {
};

AndroidPowerControls.prototype.reboot = function(reason, successCallback, failureCallback) {
	cordova.exec(successCallback, failureCallback, 'AndroidPowerControls', 'reboot', [reason]);
};

AndroidPowerControls.prototype.exit = function(successCallback, failureCallback) {
	cordova.exec(successCallback, failureCallback, 'AndroidPowerControls', 'exit', []);
};

AndroidPowerControls.prototype.getVolume = function(successCallback, failureCallback) {
	cordova.exec(successCallback, failureCallback, 'AndroidPowerControls', 'getVolume', []);
};

AndroidPowerControls.prototype.setVolumeMax = function(successCallback, failureCallback) {
	cordova.exec(successCallback, failureCallback, 'AndroidPowerControls', 'setVolumeMax', []);
};

AndroidPowerControls.prototype.setVolume = function(streamName, intVolumeMax100, successCallback, failureCallback) {
	cordova.exec(successCallback, failureCallback, 'AndroidPowerControls', 'setVolume', [streamName, intVolumeMax100]);
};

AndroidPowerControls.prototype.setUseSpeaker = function(successCallback, failureCallback, useSpeaker) {
	cordova.exec(successCallback, failureCallback, 'AndroidPowerControls', 'setUseSpeaker', [useSpeaker]);
};

module.exports = new AndroidPowerControls();