plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName = "zkey_pack_zkp2p"
    dynamicDelivery {
        deliveryType = "fast-follow"
    }
} 