plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName = "zkey_pack_0"
    dynamicDelivery {
        deliveryType = "fast-follow"
    }
}