package com.appzone.formvalidator;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by deepak on 8/1/15.
 */
public class WebUtils {

    public static boolean getJsonFromServer(String url, File outputFile) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                FileOutputStream fileoutputStream = new FileOutputStream(outputFile);
                InputStream inputStream = connection.getInputStream();
                byte[] buffer = new byte[1024];
                int count = 0;
                while ((count = inputStream.read(buffer)) >= 0) {
                    fileoutputStream.write(buffer, 0, count);
                }
                fileoutputStream.flush();
                fileoutputStream.close();
                return true;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static boolean isOnLine(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }


    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }


    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /*   public static JSONObject getServerStreamEntity(String url, HashMap<String, String> parameter, int mReqType) {
           HttpClient httpclient = new DefaultHttpClient();
           HttpUriRequest httpRequest;
           String strresponse = "";
           JSONObject jobj = null;
           try {
               // Add your data
               if (mReqType == REQUEST_TYPE_POST) {
                   httpRequest = new HttpPost(url);
                   MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                   //List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                   Set<String> keys = parameter.keySet();
                   for (String keysString : keys) {
                       if (keysString.equalsIgnoreCase("profile_pic") || keysString.equalsIgnoreCase("doc_filename")) {
                           try {
                               entity.addPart(keysString, new FileBody(new File(parameter.get(keysString))));
                           } catch (Exception e) {
                               e.printStackTrace();
                           }
                       } else
                           entity.addPart(keysString, new StringBody(parameter.get(keysString)));
                   }

                   ((HttpPost) httpRequest).setEntity(entity);
               } else {
                   httpRequest = new HttpGet(url);
               }

               // Execute HTTP Post Request
               HttpResponse response = httpclient.execute(httpRequest);
               //	streamBean.setResponseCode(response.getStatusLine().getStatusCode());
               Log.e("XX", "response.getStatusLine().getStatusCode()" + response.getStatusLine().getStatusCode());
               //streamBean.setServerResponse(response.getEntity().getContent());
               strresponse = EntityUtils.toString(response.getEntity());
               jobj = new JSONObject(strresponse);

           } catch (ClientProtocolException e) {
               e.printStackTrace();
           } catch (IOException e) {
               e.printStackTrace();
           } catch (JSONException e) {
               e.printStackTrace();
           }
           return jobj;
       }

       public static JSONObject getServerStreamEntity(String url, String parameter, int mReqType) {

           HttpClient httpclient = new DefaultHttpClient();
           HttpUriRequest httpRequest;
           String strresponse = "";
           JSONObject jobj = null;

           try {
               // Add your data
               if (mReqType == REQUEST_TYPE_POST) {
                   httpRequest = new HttpPost(url);
                   StringEntity params = new StringEntity(parameter);
                   //	httpRequest.addHeader("content-type", "application/x-www-form-urlencoded");
                   ((HttpPost) httpRequest).setEntity(params);

               } else {
                   httpRequest = new HttpGet(url);
               }

               // Execute HTTP Post Request
               HttpResponse response = httpclient.execute(httpRequest);
               //	streamBean.setResponseCode(response.getStatusLine().getStatusCode());
               Log.e("XX", "response.getStatusLine().getStatusCode()" + response.getStatusLine().getStatusCode());
               //streamBean.setServerResponse(response.getEntity().getContent());
               strresponse = EntityUtils.toString(response.getEntity());
               jobj = new JSONObject(strresponse);

           } catch (ClientProtocolException e) {
               e.printStackTrace();
           } catch (IOException e) {
               e.printStackTrace();
           } catch (JSONException e) {
               e.printStackTrace();
           }
           return jobj;
       }

      */


    public static ServerResponse getServerStream(String url, String parameter, String reqtype) throws UnsupportedEncodingException {

        HttpURLConnection connection = null;

        DataOutputStream printout;
        ServerResponse streamBean = new ServerResponse();
        try {
            connection = (HttpURLConnection) (new URL(url)).openConnection();

            connection.setReadTimeout(5000);
            connection.setConnectTimeout(20000);
            connection.setRequestMethod(reqtype);
            connection.addRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("Accept", "application/json");
            if (reqtype == NetworkLoader.POST) {
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setFixedLengthStreamingMode(parameter.length());
                connection.setUseCaches(false);

                if (parameter != null) {
                    byte[] outputInBytes = parameter.getBytes("UTF-8");
                    OutputStream os = connection.getOutputStream();
                    os.write(outputInBytes);
                    os.close();

                }
            }
            connection.connect();
            System.out.println("Response Code: " + connection.getResponseCode());
            if (connection.getResponseCode() == 200 && connection.getInputStream() != null) {
                streamBean.setResponseCode(connection.getResponseCode());
                streamBean.setServerResponse(convertStreamToString(connection.getInputStream()));
                return streamBean;
            } else {
                streamBean.setResponseCode(connection.getResponseCode());
                streamBean.setServerResponse(convertStreamToString(connection.getErrorStream()));
                return streamBean;
            }
        } catch (IOException exception) {
            streamBean.setException(exception);
            exception.printStackTrace();
        } catch (Exception exception) {
            streamBean.setException(exception);
            exception.printStackTrace();
        }
        return streamBean;
    }

    public static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                //FlurryAgent.onError(Definitions.FLURRY_ERROR_NETWORK_OPERATION, e.getMessage(), e.getClass().getName());
                //throw new RuntimeException(e.getMessage());
            }
        }
        return sb.toString();
    }

    public static long getDateDiffrence(Date newdate, Date dob) {

        long different = newdate.getTime() - dob.getTime();
        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;
        long yearsInMilli = daysInMilli * 365;

        //		long elapsedDays = different / daysInMilli;
        //		different = different % daysInMilli;
        //
        //		long elapsedHours = different / hoursInMilli;
        //		different = different % hoursInMilli;
        //
        //		long elapsedMinutes = different / minutesInMilli;
        //		different = different % minutesInMilli;

        //	    long elapsedSeconds = different / secondsInMilli;
        return different / yearsInMilli;
    }


    public static boolean isEmailValid(CharSequence email) {
        Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$");
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
    }

    public static boolean isValidMobile(String phone) {
        boolean check = false;
        if (!Pattern.matches("[a-zA-Z]+", phone))
            check = !(phone.length() < 6 || phone.length() > 13);
        else check = false;
        return check;
    }

    public static boolean isUserValid(CharSequence str) {
        Pattern USER_PATTERN = Pattern.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}");
        return USER_PATTERN.matcher(str).matches();
    }

    public static boolean isUserNameValid(String str) {
        Matcher matcher = Pattern.compile("^[a-z0-9_-]{3,15}$").matcher(str);
        return matcher.matches();
    }

    public static boolean isDocNameValid(CharSequence str) {
        Pattern USER_PATTERN = Pattern.compile("[a-zA-Z0-9\\+\\.\\_\\-\\+\\+s]{1,256}");
        return USER_PATTERN.matcher(str).matches();
    }

}
