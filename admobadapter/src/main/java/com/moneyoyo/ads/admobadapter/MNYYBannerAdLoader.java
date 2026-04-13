package com.moneyoyo.ads.admobadapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationBannerAd;
import com.google.android.gms.ads.mediation.MediationBannerAdCallback;
import com.google.android.gms.ads.mediation.MediationBannerAdConfiguration;
import com.google.android.gms.ads.mediation.MediationConfiguration;
import com.moneyoyo.ads.sdk.AdInteractionListener;
import com.moneyoyo.ads.sdk.AdSize;
import com.moneyoyo.ads.sdk.banner.BannerAdRequest;

public class MNYYBannerAdLoader implements MediationBannerAd {
    private static final String TAG = MNYYBannerAdLoader.class.getSimpleName();

    private View bannerAdView;
    private final MediationBannerAdConfiguration configuration;
    private final MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback> callback;

    public MNYYBannerAdLoader(MediationBannerAdConfiguration configuration, MediationAdLoadCallback<MediationBannerAd,
            MediationBannerAdCallback> callback) {
        this.configuration = configuration;
        this.callback = callback;
    }


    @NonNull
    @Override
    public View getView() {
        return bannerAdView;
    }

    /**
     * Loads a banner ad from the third party ad network.
     */
    public void loadAd() {
        Log.i(TAG, "Begin loading banner ad.");

        final String param = configuration.getServerParameters().getString(MediationConfiguration.CUSTOM_EVENT_SERVER_PARAMETER_FIELD);
        final MNYYServerParameter serverParameter = MNYYServerParameter.parse(param);
        if (!serverParameter.isValid()) {
            callback.onFailure(MNYYAdErrors.newInvalidConfigurationError("Invalid server parameter: " + param));
            return;
        }

        final Context context = configuration.getContext();
        final com.google.android.gms.ads.AdSize adSize = configuration.getAdSize();

        new BannerAdRequest(context)
                .enableAutoRefresh(false)
                .setSize(AdSize.ofFixed(adSize.getWidth(), adSize.getHeight()))
                .load(serverParameter.zoneID)
                .thenApplyAsync(ad -> {
                    if (!ad.hasAd()) {
                        if (ad.getLastLoadError() != null) {
                            final String message = "Banner ad " + serverParameter.zoneID + " failed to load";
                            Log.w(TAG, message, ad.getLastLoadError());
                            callback.onFailure(MNYYAdErrors.newAdLoadFailureError(message));
                            return null;
                        }

                        callback.onFailure(MNYYAdErrors.newAdLoadFailureError("Banner ad " + serverParameter.zoneID + " no ad available"));
                        bannerAdView = emptyView(context, adSize);
                        return null;
                    }

                    final MediationBannerAdCallback bannerAdCallback = callback.onSuccess(this);
                    ad.setAdInteractionListener(new AdInteractionListener() {
                        @Override
                        public void onDisplay() {
                            bannerAdCallback.onAdOpened();
                        }

                        @Override
                        public void onImpression() {
                            bannerAdCallback.reportAdImpression();
                        }

                        @Override
                        public void onClose() {
                            bannerAdCallback.onAdClosed();
                        }

                        @Override
                        public void onClick() {
                            bannerAdCallback.reportAdClicked();
                        }
                    });
                    bannerAdView = ad;
                    return null;
                }).exceptionally(err -> {
                    final String message = "Banner ad " + serverParameter.zoneID + " failed to load";
                    Log.w(TAG, message, err);
                    callback.onFailure(MNYYAdErrors.newInvalidConfigurationError(message));
                    bannerAdView = emptyView(context, adSize);
                    return null;
                });
    }

    private View emptyView(Context context, com.google.android.gms.ads.AdSize adSize) {
        final FrameLayout.LayoutParams adViewLayoutParams = new FrameLayout.LayoutParams(adSize.getWidthInPixels(context), adSize.getHeightInPixels(context));
        final FrameLayout adViewWrapper = new FrameLayout(context);
        adViewWrapper.setLayoutParams(adViewLayoutParams);
        return adViewWrapper;
    }
}
