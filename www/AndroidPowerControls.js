
var AndroidPowerControls = function() {
};

AndroidPowerControls.prototype.reboot = function(reason, successCallback, failureCallback) {
	cordova.exec(successCallback, failureCallback, 'AndroidPowerControls', 'reboot', [reason]);
};

module.exports = new AndroidPowerControls();