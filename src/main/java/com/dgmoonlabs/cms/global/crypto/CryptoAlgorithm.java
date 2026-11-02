package com.dgmoonlabs.cms.global.crypto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.ARIAEngine;
import org.bouncycastle.crypto.engines.SEEDEngine;

import java.util.function.Supplier;

@Getter
@RequiredArgsConstructor
public enum CryptoAlgorithm {
    AES(AESEngine::newInstance),
    SEED(SEEDEngine::new),
    ARIA(ARIAEngine::new);

    private final Supplier<BlockCipher> engine;
}
