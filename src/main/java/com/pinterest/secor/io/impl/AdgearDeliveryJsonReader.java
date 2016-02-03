package com.pinterest.secor.io.impl;

import com.pinterest.secor.common.SecorConfig;
import com.pinterest.secor.io.AdgearReader;
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

    public AdgearDeliveryJsonReader(SecorConfig secorConfig) {
        timestampFieldname = secorConfig.getMessageTimestampName();
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
        Integer segmentId = (Integer) jsonObject.get("segment_id");
        Boolean segmentIsNew = (Boolean) jsonObject.get("segment_new");

        // Extra fields, logged if present
        String country = (String) jsonObject.get("country");
        String region = (String) jsonObject.get("region");

        if (timestamp == null || buyerId == null || cookieId == null || segmentId == null
            || segmentIsNew == null || !segmentIsNew) {
            return null;
        }

        try {
            UUID.fromString(cookieId);
        } catch (IllegalArgumentException e) {
            LOG.warn("Skipping bad UUID: {}", cookieId);
        }

        StringBuffer output = new StringBuffer();
        output
            .append(cookieId).append('\t')
            .append(Math.round(timestamp)).append('\t')
            .append("seg:").append(segmentId);

        // FIXME: Duplicated code (see sibling class)
        // FIXME: Add validation?
        if (country != null) { output.append(",country:").append(country); }
        if (region != null)  { output.append(",region:").append(region); }

        output.append("\n");

        return output.toString();
    }
}
