package com.pinterest.secor.parser;

import com.pinterest.secor.common.SecorConfig;

import java.util.Date;

public class NoDateStampJsonMessageParser extends JsonMessageParser {
    public NoDateStampJsonMessageParser(SecorConfig config) {
        super(config);
    }

    @Override
    protected String[] generatePartitions(long timestampMillis, boolean usingHourly)
        throws Exception {
            return new String[]{};
    }
}
