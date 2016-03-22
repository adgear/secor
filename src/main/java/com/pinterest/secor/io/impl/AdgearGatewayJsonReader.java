package com.pinterest.secor.io.impl;

import com.pinterest.secor.common.SecorConfig;
import com.pinterest.secor.io.AdgearReader;
import com.pinterest.secor.io.KeyValue;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

// Converts gateway JSON to Beh TSV
public class AdgearGatewayJsonReader implements AdgearReader {
    private final String timestampFieldname;
    private final boolean logGeo;
    private final String logGeoInclude;

    public AdgearGatewayJsonReader(SecorConfig secorConfig) {
        timestampFieldname = secorConfig.getMessageTimestampName();
        logGeo = secorConfig.getSecorAdgearLogFieldsGeo();
        logGeoInclude = secorConfig.getSecorAdgearLogFieldsGeoInclude();
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

        // Extra fields, logged if present
        String country = null, region = null;
        if (logGeo) {
            String c = (String) getAtPath(jsonObject, "bid_request.device.geo.country");

            // Apply whitelist if set
            if (logGeoInclude == null || c == logGeoInclude) {
                country = c;
                region = (String) getAtPath(jsonObject, "bid_request.device.geo.region");
            }
        }

        if (timestamp == null || cookieId == null || urlDomain == null) {
            return null;
        }

        StringBuffer output = new StringBuffer();
        output
                .append(cookieId).append('\t')
                .append(Math.round(timestamp)).append('\t')
                .append("urld:").append(urlDomain);

        // FIXME: Duplicated code (see sibling class)
        // FIXME: Add validation?
        if (logGeo) {
            if (country != null) {
                output.append(",country:").append(country);
            }
            if (region != null) {
                output.append(",region:").append(region);
            }
        }

        output.append("\n");
        return output.toString();
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
