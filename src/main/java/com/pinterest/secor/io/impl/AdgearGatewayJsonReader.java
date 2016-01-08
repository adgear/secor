package com.pinterest.secor.io.impl;

import com.pinterest.secor.common.SecorConfig;
import com.pinterest.secor.io.AdgearReader;
import com.pinterest.secor.io.KeyValue;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

// Converts gateway JSON to Beh TSV
public class AdgearGatewayJsonReader implements AdgearReader {
    private final String timestampFieldname;

    public AdgearGatewayJsonReader(SecorConfig secorConfig) {
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
        String cookieId = (String) getAtPath(jsonObject, "bid_request.user.buyeruid");
        String urlDomain = (String) getAtPath(jsonObject,"bid_request.site.domain");

        if (timestamp == null || cookieId == null || urlDomain == null) {
            return null;
        }

        return String.format("%s\t%d\turld:%s\n",
                             cookieId, Math.round(timestamp), urlDomain);
    }

    // 1. Horrible
    // 2. Why isn't this part of json-smart?
    private static Object getAtPath(JSONObject json, String path) {
        String[] components = path.split("\\.");
        Object object = json;
        for (String component : components) {
            if (!(object instanceof JSONObject)) return null; // That's really an error.
            object = ((JSONObject) object).get(component);
            if (object == null) return null;
        }

        return object;
    }
}
