package com.pinterest.secor.io;

public class AdgearReaderUtils {
    public static void appendIfSet(StringBuffer buf, String fieldName, String fieldValue) {
        if (fieldValue != null) {
            buf
                .append(",")
                .append(fieldName)
                .append(":")
                .append(fieldValue);
        }
    }

    public static void appendIfSet(StringBuffer buf, String fieldName, Integer fieldValue) {
        if (fieldValue != null) {
            buf
                .append(",")
                .append(fieldName)
                .append(":")
                .append(fieldValue);
        }
    }
}
