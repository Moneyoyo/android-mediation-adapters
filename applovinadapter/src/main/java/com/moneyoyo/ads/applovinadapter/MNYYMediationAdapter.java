package com.moneyoyo.ads.applovinadapter;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.applovin.mediation.MaxAdFormat;
import com.applovin.mediation.adapter.MaxAdViewAdapter;
import com.applovin.mediation.adapter.MaxAdapterError;
import com.applovin.mediation.adapter.MaxInterstitialAdapter;
import com.applovin.mediation.adapter.listeners.MaxAdViewAdapterListener;
import com.applovin.mediation.adapter.listeners.MaxInterstitialAdapterListener;
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
import com.moneyoyo.ads.sdk.interstitial.InterstitialAdRequest;

import java.util.concurrent.atomic.AtomicBoolean;

public class MNYYMediationAdapter extends com.applovin.mediation.adapters.MediationAdapterBase implements MaxAdViewAdapter, MaxInterstitialAdapter {
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
            interstitialAd.destroy();
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
                .thenApplyAsync(ad -> {
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

    @Override
    public void loadInterstitialAd(MaxAdapterResponseParameters parameters, Activity activity, MaxInterstitialAdapterListener listener) {
        new InterstitialAdRequest(activity)
                .enablePreloading()
                .load(parameters.getThirdPartyAdPlacementId())
                .thenApply(ad -> {
                    ad.preload()
                            .thenRunAsync(() -> {
                                if (!ad.hasReadyAd()) {
                                    listener.onInterstitialAdLoadFailed(MaxAdapterError.NO_FILL);
                                    return;
                                }
                                listener.onInterstitialAdLoaded();
                            }, ContextCompat.getMainExecutor(activity))
                            .exceptionally(err -> {
                                log("interstitial ad " + parameters.getThirdPartyAdPlacementId() + " failed to load", err);
                                listener.onInterstitialAdLoadFailed(MaxAdapterError.INVALID_CONFIGURATION);
                                return null;
                            });
                    return null;
                }).exceptionally(err -> {
                    log("interstitial ad" + parameters.getThirdPartyAdPlacementId() + " failed to load", err);
                    listener.onInterstitialAdLoadFailed(MaxAdapterError.INVALID_CONFIGURATION);
                    return null;
                });
    }

    @Override
    public void showInterstitialAd(MaxAdapterResponseParameters parameters, Activity activity, MaxInterstitialAdapterListener listener) {
        if (interstitialAd == null) {
            listener.onInterstitialAdDisplayFailed(MaxAdapterError.AD_NOT_READY);
        }
        if (!interstitialAd.hasReadyAd()) {
            if (interstitialAd.hasPreloadedAd()) {
                listener.onInterstitialAdDisplayFailed(MaxAdapterError.AD_EXPIRED);
                return;
            }
            listener.onInterstitialAdDisplayFailed(MaxAdapterError.AD_NOT_READY);
        }
        interstitialAd.setAdInteractionListener(new AdInteractionListener() {
            @Override
            public void onDisplay() {
                listener.onInterstitialAdDisplayed();
            }

            @Override
            public void onClick() {
                listener.onInterstitialAdClicked();
            }

            @Override
            public void onClose() {
                listener.onInterstitialAdHidden();
            }
        });
        interstitialAd.show(activity);
    }
}
