package com.rrsapp;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.JSApplicationIllegalArgumentException;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.google.android.gms.tasks.SuccessContinuation;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.ILiveConstants;
import com.tencent.ilivesdk.ILiveSDK;
import com.tencent.ilivesdk.adapter.CommonConstants;
import com.tencent.ilivesdk.core.ILiveLoginManager;
import com.tencent.ilivesdk.core.ILiveRoomConfig;
import com.tencent.ilivesdk.core.ILiveRoomManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.rrsapp.MainApplication.getMainHandler;

/**
 * Created by eric on 2018/9/26.
 */

public class ILiveModule extends ReactContextBaseJavaModule {
    public final static int REQ_PERMISSION_CODE = 0x1000;
    public static final String TAG = "ReactTools";
    private Handler mainHandler;
    private Integer curCameraId=0;

    public ILiveModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return TAG;
    }

    @ReactMethod
    public void loginLive(final String userId, final String userSig, final Callback success, final Callback failed) {
        Log.d(TAG, "Login called:userId="+userId+",userSig="+userSig);
        getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                ILiveSDK.getInstance().setCaptureMode(ILiveConstants.CAPTURE_MODE_SURFACETEXTURE);
                ILiveLoginManager.getInstance().iLiveLogin(userId, userSig, new ILiveCallBack() {
                    @Override
                    public void onSuccess(Object data) {
                        Log.d(TAG, "Login success");
                        success.invoke();
                    }

                    @Override
                    public void onError(String module, int errCode, String errMsg) {
                        Log.d(TAG, "Login failed:"+errCode + ":"+errMsg);
                        failed.invoke(module, errCode, errMsg);
                    }
                });
            }
        });

    }

    @ReactMethod
    public void logoutLive(final Callback success, final Callback failed) {
        Log.d(TAG, "Logout called");
        ILiveLoginManager.getInstance().iLiveLogout(new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                Log.d(TAG, "Logout success");
                success.invoke();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                Log.d(TAG, "Logout failed:"+errCode + ":"+errMsg);
                failed.invoke(module, errCode, errMsg);
            }
        });
    }

    @ReactMethod
    public void quitLiveRoom(final Callback success, final Callback failed){
        Log.d(TAG, "Quit room called");
        getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                ILiveRoomManager.getInstance().exitRoom(new ILiveCallBack() {
                    @Override
                    public void onSuccess(Object data) {
                        Log.d(TAG, "Quit success");
                        success.invoke();
                        MessageObservable.getInstance().clear();
                    }

                    @Override
                    public void onError(String module, int errCode, String errMsg) {
                        Log.d(TAG, "Quit failed:"+errCode + ":"+errMsg);
                        failed.invoke(module, errCode, errMsg);
                    }
                });
            }
        });

    }

    @ReactMethod
    public void enableCamera(final Integer cameraType, final Boolean enabled, final Callback success, final Callback failed) {
        Log.d(TAG, "Enable camera:"+enabled+",type:"+cameraType);
        MainApplication.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                if(curCameraId == cameraType){
                    ILiveRoomManager.getInstance().enableCamera(cameraType, enabled);
                }else {
                    curCameraId=cameraType;
                    ILiveRoomManager.getInstance().switchCamera(cameraType);
                }
            }
        });
        success.invoke();
    }

    @ReactMethod
    public void enableMic(Boolean enabled, final Callback success, final Callback failed) {
        Log.d(TAG, "Enable mic:"+enabled);
        ILiveRoomManager.getInstance().enableMic(enabled);
        success.invoke();
    }

    @ReactMethod
    public void  setAudioMode(Boolean enabled) {
        Log.d(TAG, "Hands free:"+enabled);
        ILiveRoomManager.getInstance().enableSpeaker(enabled);
    }

    @ReactMethod
    public void  initILiveSDK(final int appID) {

        Log.d(TAG, "Init called:"+appID);
        getMainHandler().post(new Runnable() {
            @Override
            public void run() {

            ILiveSDK.getInstance().initSdk(getReactApplicationContext(), appID);
            // 初始化iLiveSDK房间管理模块
            ILiveRoomManager.getInstance().init(new ILiveRoomConfig().setRoomMsgListener(MessageObservable.getInstance()));

            }
        });
    }

    @ReactMethod
    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.RECORD_AUDIO)) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.READ_PHONE_STATE)) {
                permissions.add(Manifest.permission.READ_PHONE_STATE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(getCurrentActivity(),permissions.toArray(new String[0]), REQ_PERMISSION_CODE);
            }
        }

    }

    @ReactMethod
    public void startActivityFromJS(String roomId, String userId, String userSig) {

        try {
            Activity currentActivity = getCurrentActivity();
            if (null != currentActivity) {
                Intent intent = new Intent(currentActivity, VideoServiceActivity.class);
                intent.putExtra("roomId",roomId);
                intent.putExtra("userId", userId);
                intent.putExtra("userSig", userSig);
                currentActivity.startActivity(intent);
            }
        } catch (Exception e) {
            throw new JSApplicationIllegalArgumentException(
                    "不能打开Activity : " + e.getMessage());
        }
    }


}
