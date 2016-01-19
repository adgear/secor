package com.pinterest.secor.io.impl;

import com.pinterest.secor.common.SecorConfig;
import com.pinterest.secor.io.AdgearReader;
import com.pinterest.secor.io.KeyValue;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

// Converts delivery JSON to Beh TSV
public class AdgearDeliveryJsonReader implements AdgearReader {
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

        if (timestamp == null || buyerId == null || cookieId == null || segmentId == null
            || segmentIsNew == null || !segmentIsNew) {
            return null;
        }

        return String.format("%s\t%d\t%d:seg:%d\n",
                             cookieId, Math.round(timestamp), buyerId, segmentId);
    }
}
