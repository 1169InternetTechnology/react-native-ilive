package com.rrsapp;

import android.util.Log;

import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.views.view.ReactViewGroup;

import java.util.Map;

/**
 * Created by eric on 2018/9/26.
 */

public class ILiveViewManager extends SimpleViewManager<ILiveView>{

    public static final String TAG = "RNTLiveView";
    private ILiveView view;

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    protected ILiveView createViewInstance(ThemedReactContext reactContext) {
        view = new ILiveView(reactContext);
        return view;
    }

    public Map getExportedCustomBubblingEventTypeConstants() {
        return MapBuilder.builder()
                .put("onJoinSuccess", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onJoinSuccess")))
                .put("onJoinFailed", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onJoinFailed")))
                .put("onNewMessage", MapBuilder.of("phasedRegistrationNames", MapBuilder.of("bubbled", "onNewMessage")))
                .build();
    }


    @ReactProp(name = "roomId")
    public void setRoomId(ILiveView view, int id){
        Log.d(TAG, "set roomId:"+id);
        if(id==0)return;
        view.setRoomId(id);
    }

    @ReactProp(name = "noVideo", defaultBoolean = true)
    public void setNoVideo(ILiveView view, boolean video){
        Log.d(TAG, "set noVideo:"+video);
        view.setVideo(video);
    }

}