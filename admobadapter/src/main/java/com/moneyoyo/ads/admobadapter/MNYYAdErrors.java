package com.moneyoyo.ads.admobadapter;

import com.google.android.gms.ads.AdError;

class MNYYAdErrors {
    private MNYYAdErrors() {
    }

    public static final String CUSTOM_EVENT_ERROR_DOMAIN = "com.moneyoyo.ads.admobadapter";

    public static final int ERROR_INVALID_CONFIGURATION = 101;

    public static final int ERROR_AD_LOAD_FAILURE = 102;

    public static final int ERROR_AD_NOT_AVAILABLE = 103;

    public static AdError newInvalidConfigurationError(String message) {
        return new AdError(ERROR_INVALID_CONFIGURATION, message, CUSTOM_EVENT_ERROR_DOMAIN);
    }

    public static AdError newAdLoadFailureError(String message) {
        return new AdError(ERROR_AD_LOAD_FAILURE, message, CUSTOM_EVENT_ERROR_DOMAIN);
    }

    public static AdError newAdNotAvailableError(String message) {
        return new AdError(ERROR_AD_NOT_AVAILABLE, message, CUSTOM_EVENT_ERROR_DOMAIN);
    }
}
