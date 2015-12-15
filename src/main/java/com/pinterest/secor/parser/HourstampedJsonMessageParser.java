package com.pinterest.secor.parser;

import com.pinterest.secor.common.SecorConfig;

import java.util.Date;

public class HourstampedJsonMessageParser extends JsonMessageParser {
    public HourstampedJsonMessageParser(SecorConfig config) {
        super(config);
    }

    @Override
    protected String[] generatePartitions(long timestampMillis, boolean usingHourly)
        throws Exception {
        Date date = new Date(timestampMillis);
        String dt = getmDtFormatter().format(date);
        String hr = getmHrFormatter().format(date);
        if (usingHourly) {
            return new String[]{dt + "-" + hr};
        } else {
            return new String[]{dt};
        }
    }
}
