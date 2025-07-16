plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName = "zkey_pack_rarimo"
    dynamicDelivery {
        deliveryType = "fast-follow"
    }
} 