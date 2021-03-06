package com.pinterest.secor.io.impl;

import com.pinterest.secor.common.SecorConfig;
import com.pinterest.secor.io.AdgearReader;
import com.pinterest.secor.io.AdgearReaderUtils;
import com.pinterest.secor.io.KeyValue;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

// Converts delivery JSON to Beh TSV
public class AdgearDeliveryJsonReader implements AdgearReader {
    private static final Logger LOG = LoggerFactory.getLogger(AdgearDeliveryJsonReader.class);

    private final String timestampFieldname;
    private final boolean logGeo;

    public AdgearDeliveryJsonReader(SecorConfig secorConfig) {
        timestampFieldname = secorConfig.getMessageTimestampName();
        logGeo = secorConfig.getSecorAdgearLogFieldsGeo();
    }

    public String convert(KeyValue kv) {
        JSONObject jsonObject = null;
        Object value = JSONValue.parse(kv.getValue());
        if (value instanceof JSONObject) {
            jsonObject = (JSONObject) value;
        } else {
            return null;
        }

        Double timestamp = (Double) jsonObject.get(timestampFieldname);
        String cookieId = (String) jsonObject.get("uid");
        Integer buyerId = (Integer) jsonObject.get("buyer_id");
        Integer browser_id = (Integer) jsonObject.get("browser_id");
        Integer os_id = (Integer) jsonObject.get("operating_system_id");
        Integer segmentId = (Integer) jsonObject.get("segment_id");
        Boolean segmentIsNew = (Boolean) jsonObject.get("segment_new");
        // Boolean uidIsNew = (Boolean) jsonObject.get("uid_new");
        Boolean uidIsSticky = (Boolean) jsonObject.get("uid_sticky");

        // Extra fields, logged if present
        String country = null, region = null, city = null;
        if (logGeo) {
            country = (String) jsonObject.get("country");
            region = (String) jsonObject.get("region");
            city = (String) jsonObject.get("city");
        }

        // Drop cookie IDs we will never see again
        if (uidIsSticky == null || !uidIsSticky) {
            return null;
        }

        if (timestamp == null || buyerId == null || cookieId == null || segmentId == null
            || segmentIsNew == null || !segmentIsNew) {
            return null;
        }

        try {
            UUID.fromString(cookieId);
        } catch (IllegalArgumentException e) {
            LOG.warn("Skipping bad UUID: {}", cookieId);
            return null;
        }

        StringBuffer output = new StringBuffer();
        output
            .append(cookieId).append('\t')
            .append(Math.round(timestamp)).append('\t')
            .append(buyerId).append(":seg:").append(segmentId);

        AdgearReaderUtils.appendIfSet(output, "browser_id", browser_id);
        AdgearReaderUtils.appendIfSet(output, "os_id", os_id);

        // FIXME: Duplicated code (see sibling class)
        // FIXME: Add validation?
        if (logGeo) {
            AdgearReaderUtils.appendIfSet(output, "country", country);
            AdgearReaderUtils.appendIfSet(output, "region", region);
            AdgearReaderUtils.appendIfSet(output, "city", city);
        }

        output.append("\n");
        return output.toString();
    }
}
