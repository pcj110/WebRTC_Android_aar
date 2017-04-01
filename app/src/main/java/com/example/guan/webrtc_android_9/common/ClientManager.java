package com.example.guan.webrtc_android_9.common;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.guan.webrtc_android_9.activity.CallActivity;

import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SurfaceViewRenderer;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import static com.example.guan.webrtc_android_9.common.Helpers.createInstanceId;

/**
 * Created by guan on 3/24/17.
 */

public class ClientManager {

    static String TAG = AppRTC_Common.TAG_COMM + "ClientManager";

    public final int maxInstance;
    //public final String clientId;

    AppRTC_Common.SignalingParameters sigParms;

    private HashMap<String, InstanceManager> map_instaces = new HashMap<>();

    private Stack<SurfaceViewRenderer> stack_AvailableRemoteRender = new Stack<>();
    private Stack<ImageView> stack_AvailableMuteImgview = new Stack<>();


    private Context mContext;

    private Handler handler;

    private PeerConnectionFactory factory;

    public ClientManager(Context mContext, int maxInstance, Handler handler) {
        this.mContext = mContext;
        this.maxInstance = maxInstance;
        this.handler = handler;
    }


    //=====================================

    public HashMap<String, InstanceManager> getMap_instaces() {
        return map_instaces;
    }

    public AppRTC_Common.SignalingParameters getSigParms() {
        return sigParms;
    }

    public void setSigParms(AppRTC_Common.SignalingParameters sigParms) {
        this.sigParms = sigParms;
    }

    public Stack<SurfaceViewRenderer> getStack_AvailableRemoteRender() {
        return stack_AvailableRemoteRender;
    }

    public Stack<ImageView> getStack_AvailableMuteImgview() {
        return stack_AvailableMuteImgview;
    }

    public void setFactory(PeerConnectionFactory factory) {
        this.factory = factory;
    }
//=====================================

    public void init() {

        if (sigParms == null) {
            Log.e(TAG, "初始化ClientManager失败——sigParms==null:访问房间服务器的返回的参数为空。无法连接");
            return;
        }

        if (stack_AvailableRemoteRender.size() == 0) {
            Log.e(TAG, "初始化ClientManager失败——stack_AvailableRemoteRender.size()==0");
            return;
        }

        sigParms.toString();

        for (int i = 0; i < maxInstance; i++) {

            String instanceId = createInstanceId();
            if (map_instaces.containsKey(instanceId)) {
                i--;
                Log.e(TAG, "生成重复的instanceId。重新初始化");
                continue;
            }
            InstanceManager instanceManager = new InstanceManager(mContext, sigParms,
                    instanceId, this,
                    handler);

            map_instaces.put(instanceManager.getLocalInstanceId(), instanceManager);

        }

        Log.d(TAG, "初始化多个Instance成功----map_instaces.size() = " + map_instaces.size());
    }

    public InstanceManager getInstanceManager(String instanceId) {
        if (map_instaces.containsKey(instanceId)) {
            return map_instaces.get(instanceId);
        } else {
            //Log.e(TAG, "没有找到该实例，instanceId=" + instanceId);
            return null;
        }
    }

    /**
     * 获取一个可用并且已经初始化好的Render
     *
     * @return
     */
    public SurfaceViewRenderer getAvailableRemoteRender() {
        //Log.e(TAG, "===========调用一次getAvailableRemoteRender=============");
        if (!stack_AvailableRemoteRender.empty()) {
            Log.e(TAG, "Size of Remote Render Stack is: " + stack_AvailableRemoteRender.size());
            return stack_AvailableRemoteRender.pop();
        } else {
            Log.e(TAG, "无可用的RemoteRender");
            return null;
        }
    }

    public ImageView getAvailableImageView(){
        return stack_AvailableMuteImgview.pop();
    }


    public void recreatePeerConnection(InstanceManager instanceManager) {
        String localInstanceId = instanceManager.getLocalInstanceId();

        //根据信令服务器传回来的iceserver参数，生成PeerConnection的配置
        PeerConnection.RTCConfiguration rtcConfig =
                new PeerConnection.RTCConfiguration(instanceManager.getSigParms().iceServers);
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        // Use ECDSA encryption.
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
        MediaConstraints pcConstraints = new MediaConstraints();

        //创建并指定PeerConnection
        PeerConnection peerConnection = factory.createPeerConnection(rtcConfig,
                pcConstraints,
                instanceManager.getPcObserver());// 注意 pcObserver 的动作
        instanceManager.setPeerConnection(peerConnection);
        if (null == peerConnection) {
            Log.e(TAG, localInstanceId + ":Failed to Create Peer connection.");
            return;
        }
        Log.d(TAG, localInstanceId + ":Peer connection created.");
        peerConnection.addStream(instanceManager.getMediaStream());

    }

    public boolean isAllInstanceClosed() {
        for (Map.Entry<String, InstanceManager> entry : map_instaces.entrySet()) {
            if (!entry.getValue().isClosed()) {
                return false;
            }
        }
        return true;
    }


    /**
     * 如果走到这里，说明该InstanceManager维持的链接已死。
     * 处理方式和“接收到bye消息”的方式相同
     *
     * @param localInstanceId
     * @param errorMessage
     */
    public void reportError(String localInstanceId, String errorMessage) {
        Log.e(TAG, "localInstanceId:" + localInstanceId + "\treportError: " + errorMessage);

//        InstanceManager instanceManager=map_instaces.get(localInstanceId);
//        ViewGroup.LayoutParams params=instanceManager.getRemoteRenderer().getLayoutParams();
//        instanceManager.getRemoteRenderer().setLayoutParams(params);


        //requestConnectionAsInitiator(localInstanceId);

//
//        if (AppRTC_Common.map_roomState.get(roomId) != ConnectionState.ERROR) {
//            AppRTC_Common.map_roomState.put(roomId, ConnectionState.ERROR);
//            Log.e(TAG, errorMessage);
//        }

    }

    /**
     * 如果一个InstanceManager接收到了Bye消息，说明对方已经端开了链接。
     * 处理方式：回收该Instance资源，并销毁该Instance。重新创建一个InstanceManager，并发起offer
     */
    public void requestConnectionAsInitiator(String localInstanceId) {
        if (!map_instaces.containsKey(localInstanceId)) {
            return;
        }
        InstanceManager instanceManager = map_instaces.get(localInstanceId);

        //执行断开，并回收render
        //instanceManager.disconnect();
        //stack_AvailableRemoteRender.push(instanceManager.getRemoteRenderer());
        instanceManager.setPeerInitiator(true);
        instanceManager.setClosed(false);
        recreatePeerConnection(instanceManager);
        instanceManager.getPeerConnection().createOffer(instanceManager.getSdpObserver(), new MediaConstraints());
        //销毁该instance，并生成一个新的instance，利用旧的sigParms重新发起连接，发送offer

    }


}
