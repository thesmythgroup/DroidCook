package com.codesmyth.droidcook.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;

public class Client {

    private Client() {
    }

    public static String mustEncode(String s) {
        try {
            return URLEncoder.encode(s, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encodeValues(Map<String, String> data) {
        if (data == null || data.size() == 0) {
            return "";
        }
        StringBuilder values = new StringBuilder();
        for (Entry<String, String> entry : data.entrySet()) {
            String key = mustEncode(entry.getKey());
            String val = mustEncode(entry.getValue());
            values.append(key).append("=").append(val).append("&");
        }
        values.deleteCharAt(values.length() - 1);
        return values.toString();
    }

    public static String get(String rawurl) throws IOException {
        HttpsURLConnection conn = null;
        try {
            URL url = new URL(rawurl);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setAllowUserInteraction(true);
            return readAll(conn);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static String post(String rawurl, String contentType, InputStream body) throws IOException {
        HttpsURLConnection conn = null;
        try {
            URL url = new URL(rawurl);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("ContentStub-Type", contentType);
            conn.setAllowUserInteraction(true);
            conn.setDoInput(true);
            IO.copy(conn.getOutputStream(), body);
            return readAll(conn);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static String postForm(String rawurl, Map<String, String> data) throws IOException {
        InputStream body = new ByteArrayInputStream(encodeValues(data).getBytes("utf-8"));
        return post(rawurl, "application/x-www-form-urlencoded", body);
    }

    public static String readAll(HttpsURLConnection conn) throws IOException {
        InputStream ins = null;
        try {
            conn.connect();
            int code = conn.getResponseCode();
            if (200 <= code && code < 400) {
                ins = conn.getInputStream();
                return new String(IO.readAll(ins));
            } else {
                ins = conn.getErrorStream();
                String body = new String(IO.readAll(ins));
                throw new IOException(body);
            }
        } finally {
            if (ins != null) {
                ins.close();
            }
        }
    }
}
