package com.moneyoyo.ads.admobadapter;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.VersionInfo;
import com.google.android.gms.ads.mediation.Adapter;
import com.google.android.gms.ads.mediation.InitializationCompleteCallback;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationBannerAd;
import com.google.android.gms.ads.mediation.MediationBannerAdCallback;
import com.google.android.gms.ads.mediation.MediationBannerAdConfiguration;
import com.google.android.gms.ads.mediation.MediationConfiguration;
import com.moneyoyo.ads.sdk.AdSdk;
import com.moneyoyo.ads.sdk.AdSdkConfig;

import java.util.HashSet;
import java.util.List;

public class MNYYMediationAdapter extends Adapter {
    private static final String TAG = MNYYMediationAdapter.class.getSimpleName();


    @Override
    public void initialize(@NonNull Context context,
                           @NonNull InitializationCompleteCallback initializationCompleteCallback,
                           @NonNull List<MediationConfiguration> list) {

        final HashSet<String> sdkKeys = new HashSet<>();
        for (final MediationConfiguration configuration : list) {
            final String param = configuration.getServerParameters().getString(MediationConfiguration.CUSTOM_EVENT_SERVER_PARAMETER_FIELD);
            final MNYYServerParameter serverParameter = MNYYServerParameter.parse(param);
            if (serverParameter.isValid()) {
                sdkKeys.add(serverParameter.appKey);
            }
        }

        if (sdkKeys.isEmpty()) {
            final String message = "No valid SDK keys found.";
            Log.w(TAG, message);
            initializationCompleteCallback.onInitializationFailed("No valid SDK keys found.");
            return;
        }

        final String sdkKey = sdkKeys.iterator().next();
        if (sdkKeys.size() > 1) {
            Log.w(TAG, String.format(
                    "Found more than one MNYY SDK key. Using %s. Please update your app's ad unit"
                            + " mappings on Admob/GAM UI to use a single SDK key for ad serving to work as"
                            + " expected.",
                    sdkKey));
        }

        final AdSdkConfig config = AdSdkConfig.builder(sdkKey).build();
        AdSdk.init(context, config).
                thenRun(initializationCompleteCallback::onInitializationSucceeded).
                exceptionally(err -> {
                    final String message = err.getMessage() == null ? "Error: " + err.getClass().getName() : err.getMessage();
                    initializationCompleteCallback.onInitializationFailed(message);
                    return null;
                });

    }

    @NonNull
    @Override
    public VersionInfo getVersionInfo() {
        return parseVersion(com.moneyoyo.ads.admobadapter.BuildConfig.VERSION_NAME);
    }

    @NonNull
    @Override
    public VersionInfo getSDKVersionInfo() {
        return parseVersion(AdSdk.getVersion());
    }

    private static VersionInfo parseVersion(String versionString) {
        final String[] splits = versionString.split("\\.");
        if (splits.length >= 3) {
            final int major = Integer.parseInt(splits[0]);
            final int minor = Integer.parseInt(splits[1]);
            final int micro = Integer.parseInt(splits[2]);
            return new VersionInfo(major, minor, micro);
        }
        return new VersionInfo(0, 0, 0);
    }

    @Override
    public void loadBannerAd(@NonNull MediationBannerAdConfiguration adConfiguration,
                             @NonNull MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback> callback) {
        final MNYYBannerAdLoader bannerLoader = new MNYYBannerAdLoader(adConfiguration, callback);
        bannerLoader.loadAd();
    }
}
