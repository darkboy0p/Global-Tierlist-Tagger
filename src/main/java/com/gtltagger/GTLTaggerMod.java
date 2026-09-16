package com.gtltagger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Small shared-constants holder. GTLTagger is a client-only mod
 * (see fabric.mod.json "environment": "client") — all real logic
 * lives under {@link com.gtltagger.client}.
 */
public final class GTLTaggerMod {
    public static final String MOD_ID = "gtltagger";
    public static final Logger LOGGER = LoggerFactory.getLogger("GTLTagger");

    private GTLTaggerMod() {
    }
}
