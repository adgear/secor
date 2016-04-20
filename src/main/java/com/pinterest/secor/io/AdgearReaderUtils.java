package com.pinterest.secor.io;

public class AdgearReaderUtils {
    private static void appendIfSetGeneric(StringBuffer buf, String fieldName, Object fieldValue) {
        if (fieldValue != null) {
            buf
                .append(",")
                .append(fieldName)
                .append(":")
                .append(fieldValue);
        }
    }

    public static void appendIfSet(StringBuffer buf, String fieldName, String fieldValue) {
        appendIfSetGeneric(buf, fieldName, fieldValue);
    }
    public static void appendIfSet(StringBuffer buf, String fieldName, Integer fieldValue) {
        appendIfSetGeneric(buf, fieldName, fieldValue);
    }
}
