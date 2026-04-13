package com.moneyoyo.ads.admobadapter;

import android.text.TextUtils;

public class MNYYServerParameter {
    private static final int MIN_APP_KEY_LENGTH = 29;
    private static final int ZONE_ID_MIN_LENGTH = 8;
    private static final String DELIMITER = "/";
    final String appKey;
    final String zoneID;

    private MNYYServerParameter(String appKey, String zoneID) {
        this.appKey = appKey;
        this.zoneID = zoneID;
    }

    public boolean isValid() {
        return !TextUtils.isEmpty(appKey) && !TextUtils.isEmpty(zoneID);
    }

    public static MNYYServerParameter parse(final String serverParameter) {
        final int minLength = MIN_APP_KEY_LENGTH + 1 + ZONE_ID_MIN_LENGTH;
        if (serverParameter == null || serverParameter.length() < minLength) {
            return new MNYYServerParameter(null, null);
        }
        int splitIndex = serverParameter.indexOf(DELIMITER);
        if (splitIndex < MIN_APP_KEY_LENGTH) {
            return new MNYYServerParameter(null, null);
        }
        if (splitIndex + 1 + ZONE_ID_MIN_LENGTH < minLength) {
            return new MNYYServerParameter(null, null);
        }
        return new MNYYServerParameter(serverParameter.substring(0, splitIndex), serverParameter.substring(splitIndex + 1));
    }
}
