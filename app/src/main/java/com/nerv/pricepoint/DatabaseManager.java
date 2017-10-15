package com.nerv.pricepoint;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.telecom.Call;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationException;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.aad.adal.PromptBehavior;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NERV on 10.10.2017.
 */

public class DatabaseManager {

    public static final String TAG = MainActivity.class.getSimpleName();

    public interface AuthCallback {
        void authCallback();
    }

    public enum LogInResult {
        WRONG_PASSWORD, USER_NOT_FOUND, OK
    }

    public interface LogInCallback {
        void logInCallback(LogInResult result);
    }

    public interface Callback {
        void callback();
    }

    private final static String CLIENT_ID = "e5bd4077-60fc-4d61-a359-a8c80de8d17a";
    private final static String AUTHORITY = "https://login.microsoftonline.com/common/oauth2/authorize";
    private final static String RESOURCE = "https://pointbox.sharepoint.com";
    private final static String REDIRECT_URI = "http://pricepointmobile";
    private final static String APP_PREFERENCES = "appSettings";

    private AuthenticationCallback<AuthenticationResult> callback = new AuthenticationCallback<AuthenticationResult>() {

        @Override
        public void onError(Exception exc) {
            if (exc instanceof AuthenticationException) {
                Log.d(TAG, "Cancelled");
            } else {
                Log.d(TAG, "Authentication error:" + exc.getMessage());
            }
        }

        @Override
        public void onSuccess(AuthenticationResult result) {
            authRes = result;

            if (result == null || result.getAccessToken() == null
                    || result.getAccessToken().isEmpty()) {
                Log.d(TAG, "Token is empty");
            } else {
                aadUserId = authRes.getUserInfo().getUserId();
                saveAADUserId();
                authCallback.authCallback();
            }
        }
    };


    private String siteId;
    private String taskListId;
    private String cityListId;
    private String userListId;
    private String stockListId;

    private Activity activity;
    private AuthCallback authCallback;
    private DBRequest dbRequest;

    private int userId = -1;
    private String userLogin;
    private String userPassword;

    public ArrayList<Order> orders;

    public AuthenticationContext authContext;
    private AuthenticationResult authRes;
    private String aadUserId = "";
    private SharedPreferences appSettings;

    public DatabaseManager(Activity activity) {
        this.activity = activity;

        authContext = new AuthenticationContext(activity, AUTHORITY, true);
        appSettings = activity.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    private void saveAADUserId() {
        SharedPreferences.Editor editor = appSettings.edit();
        editor.putString("aadUserId", aadUserId);
        editor.apply();
    }

    private void getAADUserId() {
        aadUserId = appSettings.getString("aadUserId", "");
    }

    public void retrieveUserTasks(final Callback callback) {
        if (userId == -1) {
            return;
        }

        Utils.requestJSONObject(activity, authRes.getAccessToken(), Request.Method.GET
                , "https://pointbox.sharepoint.com/boxpoint/_api/web/lists/GetByTitle('Task')/items" +
                        "?$filter=task_idman eq " + String.valueOf(userId)
                , new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            orders = Order.getOrders(response.getJSONObject("d").getJSONArray("results"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        callback.callback();
                    }
                }
                , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
    }

    public void getItemAttachments(String itemId) {
        /*Utils.requestJSONObject(activity, authResult.getAccessToken(), Request.Method.GET
                , BASE_URL + siteId + "/drive/root/children"
                , new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("", "");
                    }
                }
                , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("", "");
                    }
                });*/
    }

    public void checkLoginPassword(final String login, final String password, final LogInCallback callback) {
        if (callback == null) {
            return;
        }

        Utils.requestJSONObject(activity, authRes.getAccessToken(), Request.Method.GET
                , "https://pointbox.sharepoint.com/boxpoint/_api/web/lists/GetByTitle('User')/items" +
                        "?$filter=user_mail eq '" + login + "'" +
                        "&$select=user_id,user_pass"
                , new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray lists = response.getJSONObject("d").getJSONArray("results");

                            if (lists.length() == 0) {
                                callback.logInCallback(LogInResult.USER_NOT_FOUND);
                            } else {
                                JSONObject fields = lists.getJSONObject(0);
                                String user_pass = fields.getString("user_pass");

                                if (password.equals(user_pass)) {
                                    userId = fields.getInt("user_id");
                                    userLogin = login;
                                    userPassword = user_pass;

                                    callback.logInCallback(LogInResult.OK);
                                } else {
                                    callback.logInCallback(LogInResult.WRONG_PASSWORD);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });

        /*dbRequest.retrieveListItems(userListId, new DBRequest.RequestCallback() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray lists = response.getJSONArray("value");

                    if (lists.length() == 0) {
                        callback.logInCallback(LogInResult.USER_NOT_FOUND);
                    } else {
                        JSONObject fields = lists.getJSONObject(0).getJSONObject("fields");
                        String user_pass = fields.getString("user_pass");

                        if (password.equals(user_pass)) {
                            userId = fields.getInt("user_id");
                            userLogin = login;
                            userPassword = user_pass;

                            callback.logInCallback(LogInResult.OK);
                        } else {
                            callback.logInCallback(LogInResult.WRONG_PASSWORD);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("", "");
            }
        });

        dbRequest.expandFields("user_pass,user_id");
        dbRequest.filter("fields/user_mail eq '" + login + "'");
        dbRequest.request();*/
    }

    public boolean silentConnect(AuthCallback authCallback) {
        getAADUserId();

        if (!aadUserId.isEmpty()) {
            this.authCallback = authCallback;
            authContext.acquireTokenSilentAsync(RESOURCE, CLIENT_ID, aadUserId, callback);
            return true;
        }

        return false;
    }

    public void interactiveConnect(AuthCallback authCallback) {
        this.authCallback = authCallback;

        authContext.acquireToken(activity, RESOURCE, CLIENT_ID, REDIRECT_URI, "", PromptBehavior.Auto, "", callback);
    }

    private void retrieveListsInfo() {
        /*if (authResult.getAccessToken() == null) {
            return;
        }

        Utils.requestJSONObject(activity, authResult.getAccessToken(), Request.Method.GET
                , BASE_URL + "pointbox.sharepoint.com:/boxpoint"
                , new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            siteId = response.getString("id");

                            dbRequest = new DBRequest(activity, siteId, authResult.getAccessToken());

                            getListsInfo();
                        } catch (JSONException e) {

                        }
                    }
                }
                , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });*/
    }

    private void getListsInfo() {
        dbRequest.retrieveLists(new DBRequest.RequestCallback() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray lists = response.getJSONArray("value");
                    for (int i = 0; i < lists.length(); i++) {
                        JSONObject list = (JSONObject) lists.get(i);
                        String name = list.getString("name");
                        String id = list.getString("id");

                        switch (name) {
                            case "Task":
                                taskListId = id;
                                break;
                            case "City":
                                cityListId = id;
                                break;
                            case "User":
                                userListId = id;
                                break;
                            case "Stock":
                                stockListId = id;
                                break;
                        }
                    }

                    if (authCallback != null) {
                        authCallback.authCallback();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        dbRequest.request();
    }
}
