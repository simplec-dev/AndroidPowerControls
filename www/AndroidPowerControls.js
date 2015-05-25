
var AndroidPowerControls = function() {
};

AndroidPowerControls.prototype.reboot = function(reason, successCallback, failureCallback) {
	cordova.exec(successCallback, failureCallback, 'AndroidPowerControls', 'reboot', [reason]);
};

AndroidPowerControls.prototype.exit = function(successCallback, failureCallback) {
	cordova.exec(successCallback, failureCallback, 'AndroidPowerControls', 'exit', []);
};

module.exports = new AndroidPowerControls();