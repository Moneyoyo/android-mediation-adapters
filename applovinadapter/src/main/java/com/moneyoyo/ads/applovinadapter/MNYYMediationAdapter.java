package com.moneyoyo.ads.applovinadapter;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.applovin.mediation.MaxAdFormat;
import com.applovin.mediation.adapter.MaxAdViewAdapter;
import com.applovin.mediation.adapter.MaxAdapterError;
import com.applovin.mediation.adapter.listeners.MaxAdViewAdapterListener;
import com.applovin.mediation.adapter.parameters.MaxAdapterInitializationParameters;
import com.applovin.mediation.adapter.parameters.MaxAdapterResponseParameters;
import com.applovin.sdk.AppLovinSdk;
import com.moneyoyo.ads.sdk.AdInteractionListener;
import com.moneyoyo.ads.sdk.AdSdk;
import com.moneyoyo.ads.sdk.AdSdkConfig;
import com.moneyoyo.ads.sdk.AdSize;
import com.moneyoyo.ads.sdk.banner.BannerAdRequest;
import com.moneyoyo.ads.sdk.banner.BannerAdSizes;
import com.moneyoyo.ads.sdk.banner.BannerAdView;
import com.moneyoyo.ads.sdk.interstitial.InterstitialAd;

import java.util.concurrent.atomic.AtomicBoolean;

public class MNYYMediationAdapter extends com.applovin.mediation.adapters.MediationAdapterBase implements MaxAdViewAdapter {
    private static final AtomicBoolean initialized = new AtomicBoolean();
    private static InitializationStatus status;

    @Nullable
    private InterstitialAd interstitialAd;

    @Nullable
    private BannerAdView bannerAdView;

    public MNYYMediationAdapter(AppLovinSdk appLovinSdk) {
        super(appLovinSdk);
    }

    @Override
    public void initialize(final MaxAdapterInitializationParameters parameters, Activity activity, OnCompletionListener onCompletionListener) {
        parameters.getServerParameters().getString("app_id", null);

        if (initialized.compareAndSet(false, true)) {
            status = InitializationStatus.INITIALIZING;
            final Bundle serverParameters = parameters.getServerParameters();
            final String appId = serverParameters.getString("app_id");
            log("Initializing SDK with app id: " + appId + "...");

            final AdSdkConfig config = AdSdkConfig.builder(appId).build();

            AdSdk.init(activity, config).thenRun(() -> {
                status = InitializationStatus.INITIALIZED_SUCCESS;
                onCompletionListener.onCompletion(status, null);
            }).exceptionally(err -> {
                log("SDK failed to initialize", err);
                status = InitializationStatus.INITIALIZED_FAILURE;
                onCompletionListener.onCompletion(status, null);
                return null;
            });
        } else {
            onCompletionListener.onCompletion(status, null);
        }
    }

    @Override
    public String getSdkVersion() {
        return AdSdk.getVersion();
    }

    @Override
    public String getAdapterVersion() {
        return com.moneyoyo.ads.applovinadapter.BuildConfig.VERSION_NAME;
    }

    @Override
    public void onDestroy() {
        if (bannerAdView != null) {
            bannerAdView.destroy();
            bannerAdView = null;
        }

        if (interstitialAd != null) {
            interstitialAd = null;
        }
    }

    @Override
    public void loadAdViewAd(MaxAdapterResponseParameters parameters, MaxAdFormat maxAdFormat, Activity activity, MaxAdViewAdapterListener listener) {
        final AdSize size = maxAdFormat.equals(MaxAdFormat.MREC) ? BannerAdSizes.mrecs() : BannerAdSizes.standard();
        new BannerAdRequest(activity)
                .enableAutoRefresh(false)
                .setSize(size)
                .load(parameters.getThirdPartyAdPlacementId())
                .thenApply(ad -> {
                    if (!ad.hasAd()) {
                        if (ad.getLastLoadError() != null) {
                            log(maxAdFormat.getLabel() + " " + parameters.getThirdPartyAdPlacementId() + " failed to load", ad.getLastLoadError());
                            listener.onAdViewAdLoadFailed(MaxAdapterError.INVALID_CONFIGURATION);
                            return null;
                        }

                        listener.onAdViewAdLoadFailed(MaxAdapterError.NO_FILL);
                        return null;
                    }

                    ad.setAdInteractionListener(new AdInteractionListener() {
                        @Override
                        public void onDisplay() {
                           listener.onAdViewAdDisplayed();
                        }

                        @Override
                        public void onClose() {
                            listener.onAdViewAdHidden();
                        }

                        @Override
                        public void onClick() {
                            listener.onAdViewAdClicked();
                        }
                    });
                    listener.onAdViewAdLoaded(ad);
                    return null;
                }).exceptionally(err -> {
                    log(maxAdFormat.getLabel() + " " + parameters.getThirdPartyAdPlacementId() + " failed to load", err);
                    listener.onAdViewAdLoadFailed(MaxAdapterError.INVALID_CONFIGURATION);
                    return null;
                });
    }
}
