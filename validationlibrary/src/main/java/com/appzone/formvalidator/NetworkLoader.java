package com.appzone.formvalidator;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.WeakHashMap;

public class NetworkLoader extends AsyncTaskLoader<ServerResponse> {
    public static final String POST = "POST";
    public static final String GET = "GET";
    public static final String REQUEST_TYPE = "request_type";
    public static final String PARAMETER = "parameter";
    public static final String JSON_REQ_STR = "json_req_str";
    public static String URL_PARAM = "url";
    String json_req_Str;
    boolean isJson = true;
    private boolean mIsRunning;
    private ServerResponse mLoginResponseBean;
    private String mUrl;
    private HashMap<String, String> mParameter;
    private String mReqType;

    public NetworkLoader(Context context, String url, HashMap<String, String> parameter, String requestTypePost) {
        super(context);
        mUrl = url;
        mParameter = parameter;
        mReqType = requestTypePost;
        isJson = false;
    }

    public NetworkLoader(Context context, String url, String json_req_Str, String requestType) {
        super(context);
        mUrl = url;
        this.json_req_Str = json_req_Str;
        mReqType = requestType;
    }

    @Override
    public ServerResponse loadInBackground() {
        mIsRunning = true;
        ServerResponse streamBean = null;
        if (isJson) {
            try {
                if (mReqType == POST) {
                    streamBean = WebUtils.getServerStream(mUrl, json_req_Str, mReqType);
                } else {
                    streamBean = WebUtils.getServerStream(mUrl, null, mReqType);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }/* else
            streamBean = WebUtils.getServerStream(mUrl, mParameter, mReqType);*/
        mLoginResponseBean = streamBean;
        mIsRunning = false;
        return streamBean;
    }

    @Override
    protected void onStartLoading() {
        if (mLoginResponseBean != null) {
            deliverResult(mLoginResponseBean);
        } else {
            if (!mIsRunning) {
                forceLoad();
            }
        }
    }

    @Override
    protected void onReset() {
        WeakHashMap<ServerResponse, ServerResponse> weakHashMap = new WeakHashMap<ServerResponse, ServerResponse>();
        weakHashMap.put(mLoginResponseBean, mLoginResponseBean);
        mLoginResponseBean = null;
        super.onReset();
    }

    @Override
    public void deliverResult(ServerResponse data) {
        if (isStarted()) {
            super.deliverResult(mLoginResponseBean);
        }
    }

}
