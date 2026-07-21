# Applovin Adapter

Moneyoyo Adapter for Applovin mediation.

## How To

Add this library into your project, then create and configure a custom network adapter.

### Configure custom network

Follow the instruction [here](https://support.applovin.com/en/max/mediated-network-guides/integrating-custom-sdk-networks)

- Network Type: `SDK`
- Android Class Name: `com.moneyoyo.ads.applovinadapter.MNYYMediationAdapter`

### Configure AD Unit

- The `AppID` is the Moneyoyo App Key. This is mandatory.
- The `Placement ID` is the Moneyoyo Zone ID.

## Note

Since v0.1.2 the package name was changed from `com.moneyoyo:applovin-mediation` to
`com.moneyoyo.ads:applovin-mediation-adapter`