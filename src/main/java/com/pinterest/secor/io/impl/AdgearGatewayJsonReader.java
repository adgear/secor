package com.pinterest.secor.io.impl;

import com.pinterest.secor.common.SecorConfig;
import com.pinterest.secor.io.AdgearReader;
import com.pinterest.secor.io.AdgearReaderUtils;
import com.pinterest.secor.io.KeyValue;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import java.util.Arrays;
import java.util.HashSet;

// Converts gateway JSON to Beh TSV
public class AdgearGatewayJsonReader implements AdgearReader {
    private final String timestampFieldname;
    private final boolean logGeo;
    private final HashSet<String> logGeoInclude;

    public AdgearGatewayJsonReader(SecorConfig secorConfig) {
        timestampFieldname = secorConfig.getMessageTimestampName();
        logGeo = secorConfig.getSecorAdgearLogFieldsGeo();

        String logFieldsGeoInclude = secorConfig.getSecorAdgearLogFieldsGeoInclude();
        if (logFieldsGeoInclude == null) {
            logGeoInclude = null;
        } else {
            // Split on literal pipes
            logGeoInclude = new HashSet<String>(Arrays.asList(logFieldsGeoInclude.split("\\|")));
        }
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
        Integer browser_id = (Integer) getAtPath(jsonObject, "bid_request.device.ext.browser_id");
        Integer os_id = (Integer) getAtPath(jsonObject,
                                            "bid_request.device.ext.operating_system_id");

        // Extra fields, logged if present
        String country = null, region = null, city = null;
        if (logGeo) {
            String c = (String) getAtPath(jsonObject, "bid_request.device.geo.country");

            // Apply whitelist if set
            if (logGeoInclude == null || logGeoInclude.contains(c)) {
                country = c;
                region = (String) getAtPath(jsonObject, "bid_request.device.geo.region");
                city = (String) getAtPath(jsonObject, "bid_request.device.geo.ext.normalized_city");
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
