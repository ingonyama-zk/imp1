plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName = "zkey_pack_1"
    dynamicDelivery {
        deliveryType = "fast-follow"
    }
} 