package com.example.contractfarmingapp.network;

import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.*;
import java.util.*;

public abstract class VolleyMultipartRequest extends Request<NetworkResponse> {

    private final Response.Listener<NetworkResponse> mListener;
    private final Map<String, String> headers;

    public VolleyMultipartRequest(int method, String url,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.headers = new HashMap<>();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            // Add text params
            Map<String, String> params = getParams();
            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    writeTextPart(bos, entry.getKey(), entry.getValue());
                }
            }

            // Add file params
            Map<String, DataPart> data = getByteData();
            if (data != null) {
                for (Map.Entry<String, DataPart> entry : data.entrySet()) {
                    writeFilePart(bos, entry.getKey(), entry.getValue());
                }
            }

            // Finish multipart
            bos.write(("--" + boundary + "--\r\n").getBytes());

            return bos.toByteArray();

        } catch (IOException e) {
            throw new AuthFailureError("Error while creating multipart body -> " + e.getMessage());
        }
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    public abstract Map<String, String> getParams() throws AuthFailureError;

    public abstract Map<String, DataPart> getByteData() throws AuthFailureError;

    // == Multipart setup ==
    private final String boundary = "apiclient-" + System.currentTimeMillis();

    private void writeTextPart(OutputStream out, String name, String value) throws IOException {
        out.write(("--" + boundary + "\r\n").getBytes());
        out.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes());
        out.write((value + "\r\n").getBytes());
    }

    private void writeFilePart(OutputStream out, String name, DataPart data) throws IOException {
        out.write(("--" + boundary + "\r\n").getBytes());
        out.write(("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + data.getFileName() + "\"\r\n").getBytes());
        out.write(("Content-Type: " + data.getType() + "\r\n\r\n").getBytes());
        out.write(data.getContent());
        out.write("\r\n".getBytes());
    }

    // Helper class for file data
    public static class DataPart {
        private final String fileName;
        private final byte[] content;
        private final String type;

        public DataPart(String fileName, byte[] content) {
            this(fileName, content, "application/octet-stream");
        }

        public DataPart(String fileName, byte[] content, String type) {
            this.fileName = fileName;
            this.content = content;
            this.type = type;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getContent() {
            return content;
        }

        public String getType() {
            return type;
        }
    }
}
