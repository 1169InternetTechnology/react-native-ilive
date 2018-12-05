package com.rrsapp;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.views.view.ReactViewGroup;
import com.google.gson.Gson;
import com.growingio.android.sdk.utils.rom.RomChecker;
import com.tencent.av.sdk.AVView;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.ILiveConstants;
import com.tencent.ilivesdk.ILiveMemStatusLisenter;
import com.tencent.ilivesdk.core.ILiveRoomManager;
import com.tencent.ilivesdk.core.ILiveRoomOption;
import com.tencent.ilivesdk.data.ILiveMessage;
import com.tencent.ilivesdk.data.msg.ILiveTextMessage;
import com.tencent.ilivesdk.listener.ILiveMessageListener;
import com.tencent.ilivesdk.view.AVRootView;
import com.tencent.ilivesdk.view.AVVideoView;
import com.tencent.ilivesdk.view.BaseVideoView;

/**
 * Created by eric on 2018/9/26.
 */

public class ILiveView extends FrameLayout implements ILiveMessageListener{
    public static final String TAG = "ILiveView";
    private int roomId;
    private boolean video = true;

    public static final String SUCCESS = "onJoinSuccess";
    public static final String FAILED = "onJoinFailed";
    public static final String MESSAGE = "onNewMessage";
    private AVRootView avRootView;

    public ILiveView(Context context) {
        super(context);
        Log.d(TAG, "ILiveView init");
        LayoutInflater.from(context).inflate(R.layout.live_service, this);
        initRoomView();
    }

    private void joinRoom(){
        final ILiveRoomOption option = new ILiveRoomOption()
                .autoRender(true)      //设置是否自动渲染用户视频
                .imsupport(true)    // 开启IM功能
                .groupType("AVChatRoom") //使用互动直播聊天室
                .exceptionListener(new ILiveRoomOption.onExceptionListener() {
                    @Override
                    public void onException(int exceptionId, int errCode, String errMsg) {
                        Log.d(TAG, "exception, code:"+errCode+",msg:"+errMsg);
                    }
                })  // 监听异常事件处理
                .roomDisconnectListener(new ILiveRoomOption.onRoomDisconnectListener() {
                    @Override
                    public void onRoomDisconnect(int errCode, String errMsg) {

                    }
                })   // 监听房间中断事件
                .controlRole("user")    // 使用user角色
                .setRoomMemberStatusLisenter(new ILiveMemStatusLisenter() {
                    @Override
                    public boolean onEndpointsUpdateInfo(int eventid, String[] updateList) {
                        return false;
                    }
                }) // 监听房间内音视频事件
                .setRequestViewLisenter(new ILiveRoomOption.onRequestViewListener() {
                    @Override
                    public void onComplete(String[] userIdList, AVView[] viewList, int count, int result, String errMsg) {
                        Log.d(TAG, "onComplete:"+userIdList);
                        if (ILiveConstants.NO_ERR == result){
                            for (int i=0; i<userIdList.length; i++){
                                avRootView.renderVideoView(true, userIdList[i], viewList[i].videoSrcType, true);
                            }
                        }else {
                            // Todo 失败处理
                        }

                    }
                })   // 监听视频请求回调
                .autoCamera(true)       // 进房间后自动打开摄像头并上行
                .autoMic(false);         // 进房间后自动要开Mic并上行

        MainApplication.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                ILiveRoomManager.getInstance().joinRoom(roomId, option, new ILiveCallBack() {
                    @Override
                    public void onSuccess(Object data) {
                        Log.d(TAG, "join success");
                        onReceiveNativeEvent(SUCCESS, Arguments.createMap());
                        setRenderingMode(0, true);
                        setRenderingMode(1, true);

                        MessageObservable.getInstance().addObserver(ILiveView.this);
                    }

                    @Override
                    public void onError(String module, int errCode, String errMsg) {
                        Log.d(TAG, "join failed:"+errMsg);
                        WritableMap event = Arguments.createMap();
                        event.putString("module", module);
                        event.putString("errCode", errCode+"");
                        event.putString("errMsg", errMsg);
                        onReceiveNativeEvent(FAILED,event);
                    }
                });
            }
        });
    }

    private void initRoomView(){
        avRootView = findViewById(R.id.av_root_view);
        // 设置没有渲染时的背景色为蓝色(注意不支持在布局中直接设置)
        avRootView.getVideoGroup().setBackgroundColor(Color.BLACK);
        // 设置渲染控件
        avRootView.setSubCreatedListener(new AVRootView.onSubViewCreatedListener() {
            @Override
            public void onSubViewCreated() {
                Log.d(TAG, "onSubViewCreated");
                for (int i = 1; i< ILiveConstants.MAX_AV_VIDEO_NUM; i++){
                    final int index = i;
                    final AVVideoView videoView = avRootView.getViewByIndex(i);
                    videoView.setDragable(true);        // 可拖动
                    videoView.setGestureListener(new GestureDetector.SimpleOnGestureListener(){
                        @Override
                        public boolean onSingleTapConfirmed(MotionEvent e) {        // 小屏点击交换
                            avRootView.swapVideoView(0, index);
                            return super.onSingleTapConfirmed(e);
                        }
                    });
                }
            }
        });
        MainApplication.getMainHandler().post(new Runnable() {
            @Override
            public void run() {

                ILiveRoomManager.getInstance().initAvRootView(avRootView);
            }
        });

    }

    public void onReceiveNativeEvent(String eventName, WritableMap event) {
        ReactContext reactContext = (ReactContext)getContext();
        reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(getId(), eventName, event);
    }

    public void setRoomId(int id){
        roomId = id;
        joinRoom();
    }

    public void setVideo(boolean video){
        this.video = video;
    }

    //存储视频渲染模式
    public void setRenderingMode(int index,boolean isFull){
        AVVideoView videoViews = avRootView.getViewByIndex(index);
        if (videoViews!=null){
            setRenderMode(videoViews,(isFull? BaseVideoView.BaseRenderMode.SCALE_TO_FIT: BaseVideoView.BaseRenderMode.BLACK_TO_FILL));
        }

    }

    //设置渲染模式
    private void setRenderMode(AVVideoView view,BaseVideoView.BaseRenderMode renderMode){
        view.setSameDirectionRenderMode(renderMode);
        view.setDiffDirectionRenderMode(renderMode);
    }

    @Override
    public void onNewMessage(ILiveMessage iLiveMessage) {
        Log.d(TAG, "receive message");

        String message = new Gson().toJson(iLiveMessage);
        WritableArray array = Arguments.createArray();
        array.pushString(message);
        WritableMap event = Arguments.createMap();
        event.putArray("msgsArr", array);
        onReceiveNativeEvent(MESSAGE, event);
    }
}
