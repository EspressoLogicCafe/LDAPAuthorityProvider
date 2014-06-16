package com.kahuna.logic.lib.rest;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.mozilla.javascript.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Copyright 2014 - Espresso Logic
 * Make a simple REST call from JavaScript. These are quite basic. If you need
 * more control, you should probably call a more complete library.
 * Usage:
 * <code>
 * var restCaller = new com.kahuna.logic.lib.rest.RestCaller(this);
 * var result = restCaller.get(url, ...);
 * var json = JSON.parse(resultFromRestCaller);
 * // NOTE, this may be necessary as RestCaller returns java.lang.String
 * var str = String(resultFromRestCaller);
 * OR
 * You can use the SysUtility version, simply as
 * var str = SysUtility.restGet(...);
 * var json = JSON.parse(str);
 * </code>
 */
public class RestCaller {

    private Scriptable start;
    private final static HttpClient client = new DefaultHttpClient();
    protected static String API_KEY = "OUXKJKKW82ZX2IRMTPPBVYDQX" + ":" + "F99Sz5llVDM0Y4X7FVSZCgYGTos5rJ2/A5nPLkB476U";
  //  private final static Header API_KEY_HEADER = new BasicHeader("type ", API_KEY);

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHENTICATION_SCHEME = "Basic";

    public RestCaller(Scriptable start) {
        this.start = start;
    }

    /**
     * Make a basic GET call to a REST API.
     *
     * @param url      The URL to hit
     * @param params   The parameters as a JavaScript object
     * @param settings The settings as a JavaScript object
     * @return Whatever the REST call returned.
     */
    public String get(String url, NativeObject params, NativeObject settings) {

       // HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(url);
        //

        get.setHeader("Content-Type", "application/json");
        consumeSettings(settings, get);
        consumeParameters(params, get);

        try {

            HttpResponse response = client.execute(get);
            String result = readContent(response.getEntity().getContent());
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    /**
     * Make a POST call to a REST API.
     *
     * @param url      The URL to hit
     * @param params   The parameters as a JavaScript object
     * @param settings The settings as a JavaScript object
     * @param data     The data to be POSTed
     * @return Whatever the REST call returned.
     */
    public String post(String url, NativeObject params, NativeObject settings, NativeObject data) {
        return postOrPut(true, url, params, data, settings);
    }

    /**
     * Make a PUT call to a REST API.
     *
     * @param url      The URL to hit
     * @param params   The parameters as a JavaScript object
     * @param settings The settings as a JavaScript object
     * @param data     The data to be PUT
     * @return Whatever the REST call returned.
     */
    public String put(String url, NativeObject params, NativeObject settings, NativeObject data) {
        return postOrPut(false, url, params, data, settings);
    }

    /**
     * Make a DELETE call to a REST API.
     *
     * @param url      The URL to hit
     * @param params   The parameters as a JavaScript object
     * @param settings The settings as a JavaScript object
     * @return Whatever the REST call returned.
     */
    public String delete(String url, NativeObject params, NativeObject settings) {
        HttpClient client = new DefaultHttpClient();
        HttpDelete req = new HttpDelete(url);

        req.setHeader("Content-Type", "application/json");
        consumeSettings(settings, req);
        consumeParameters(params, req);

        try {
            HttpResponse response = client.execute(req);
            String result = readContent(response.getEntity().getContent());
            return result;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String postOrPut(boolean isPost, String url, NativeObject params,
                             NativeObject data, NativeObject settings) {

        HttpClient client = new DefaultHttpClient();
        HttpEntityEnclosingRequestBase req = isPost ? new HttpPost(url) : new HttpPut(url);

        req.setHeader("Content-Type", "application/json");
        consumeSettings(settings, req);
        consumeParameters(params, req);

        String json = convertToJson(data);
        try {
            StringEntity entity = new StringEntity(json);
            entity.setContentType("application/json");
            req.setEntity(entity);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }

        try {
            HttpResponse response = client.execute(req);
            String result = readContent(response.getEntity().getContent());
            return result;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String readContent(InputStream content) throws IOException {
        InputStreamReader inStr = new InputStreamReader(content);
        BufferedReader rd = new BufferedReader(inStr);
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    private void consumeSettings(NativeObject settings, HttpRequestBase req) {
        Object rawHeaders = settings.get("headers", (Scriptable) start);
        if (rawHeaders != null && rawHeaders != UniqueTag.NOT_FOUND && rawHeaders != UniqueTag.NULL_VALUE) {
            NativeObject headers = (NativeObject) rawHeaders;
            Object[] allIds = headers.getAllIds();
            for (Object o : allIds) {
                String name = o.toString();
                Object value = headers.get(name, (Scriptable) start);
                if (value instanceof NativeJavaObject) {
                    value = ((NativeJavaObject) value).unwrap();
                }
                String valueStr = value.toString();
                req.setHeader(name, valueStr);
            }
        }
    }

    private void consumeParameters(NativeObject params, HttpRequestBase req) {
        if (params == null)
            return;
        Object[] allIds = params.getAllIds();
        HttpParams httpParams = new BasicHttpParams();
        for (Object o : allIds) {
            String name = o.toString();
            Object value = params.get(name, (Scriptable) start);
            if (value instanceof NativeJavaObject) {
                value = ((NativeJavaObject) value).unwrap();
            }
            httpParams.setParameter(name, value);
        }
        req.setParams(httpParams);
    }

    private String convertToJson(NativeObject obj) {
        StringBuffer sb = new StringBuffer();
        convertToJson(obj, sb);
        return sb.toString();
    }

    private void convertObjectToJson(NativeObject obj, StringBuffer sb) {
        sb.append("{");
        Object[] allIds = obj.getAllIds();
        int numAtts = 0;
        for (Object key : allIds) {
            String name = key.toString();
            if (numAtts > 0)
                sb.append(",");
            sb.append("\"" + name + "\":");
            Object value = obj.get(name, (Scriptable) start);
            convertToJson(value, sb);
            numAtts++;
        }
        sb.append("}");
    }

    private void convertToJson(Object obj, StringBuffer sb) {
        if (obj == null) {
            sb.append("null");
            return;
        }
        if (obj instanceof NativeArray) {
            sb.append("[");
            NativeArray a = (NativeArray) obj;
            long l = a.getLength();
            for (int i = 0; i < l; i++) {
                if (i > 0)
                    sb.append(",");
                Object o = a.get(i, start);
                convertToJson(o, sb);
            }
            sb.append("]");
        } else if (obj instanceof NativeObject) {
            convertObjectToJson((NativeObject) obj, sb);
        } else {
            String valStr = objectToString(obj);
            sb.append(valStr);
        }
    }

    private static String objectToString(Object o) {
        if (o == null)
            return "null";

        if (o instanceof NativeJavaObject)
            o = ((NativeJavaObject) o).unwrap();

        if (o instanceof ConsString) {
            o = o.toString();
        }

        if (o instanceof String) {
            String s = (String) o;
            s = s.replace("\\", "\\\\");
            s = s.replace("\"", "\\\"");
            s = s.replace("\n", "\\n");
            s = s.replace("\t", "\\t");
            s = s.replace("\r", "\\n");

            return "\"" + s + "\"";
        }

        if (o instanceof Boolean) {
            Boolean b = (Boolean) o;
            return b.toString();
        }

        if (o instanceof Number) {
            Number n = (Number) o;
            return n.toString();
        }

        if (o instanceof Undefined)
            return "null";

        throw new RuntimeException("Object from JavaScript has unknown type: " + o.getClass());
    }

}
