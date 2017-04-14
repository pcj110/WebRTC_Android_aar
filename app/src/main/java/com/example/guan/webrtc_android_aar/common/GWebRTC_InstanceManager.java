package com.example.guan.webrtc_android_aar.common;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.example.guan.webrtc_android_aar.activity.GWebRTC_CallActivity;
import com.example.guan.webrtc_android_aar.utils.GWebRTC_AsyncHttpURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;

import java.util.LinkedList;

import static com.example.guan.webrtc_android_aar.common.GWebRTC_AppRTC_Common.AUDIO_CODEC_ISAC;
import static com.example.guan.webrtc_android_aar.common.GWebRTC_AppRTC_Common.ROOM_MESSAGE;
import static com.example.guan.webrtc_android_aar.common.GWebRTC_AppRTC_Common.ROOM_QUERY;
import static com.example.guan.webrtc_android_aar.common.GWebRTC_AppRTC_Common.preferredVideoCodec;
import static com.example.guan.webrtc_android_aar.common.GWebRTC_Helpers.preferCodec;
import static com.example.guan.webrtc_android_aar.common.GWebRTC_JsonHelper.jsonPut;
import static com.example.guan.webrtc_android_aar.common.GWebRTC_JsonHelper.toJsonCandidate;

/**
 * Created by guan on 3/18/17.
 */

public class GWebRTC_InstanceManager {

    private String localInstanceId;
    private String remoteInstanceId;//设置到了MessageParameters中

    private String messageUrl;
    //private String leaveUrl;
    private String queryUrl;
    private GWebRTC_AppRTC_Common.RoomState roomState;
    private GWebRTC_AppRTC_Common.SignalingParameters sigParms;
    private GWebRTC_AppRTC_Common.MessageParameters messageParameters;
    private LinkedList<IceCandidate> queuedRemoteCandidates = new LinkedList<>();

    private GWebRTC_ClientManager clientManager;
    private PeerConnection peerConnection;
    private GWebRTC_WebSocketClient wsClient;
    private GWebRTC_CallActivity.WSMessageEvent wsMessageEvent;
    private GWebRTC_CallActivity.PCObserver pcObserver;
    private GWebRTC_CallActivity.SDPObserver sdpObserver;

    private SurfaceViewRenderer remoteRenderer;
    private ImageView mute_imgv;
    private ImageView speaker_imgv;
    private ImageView microphone_imgv;
    private MediaStream mediaStream;

    private boolean peerInitiator;
    private boolean isMute;
    private boolean isClosed;
    private boolean localAudioState=true;
    private boolean remoteAudioState=true;

    //AppRTC_Common.ICall iCall;

    private Handler handler;

    private Context mContext;


    private static String TAG = GWebRTC_AppRTC_Common.TAG_COMM + "InstanceManager";


    public GWebRTC_InstanceManager(Context mContext,
                                   GWebRTC_AppRTC_Common.SignalingParameters sigParms,
                                   String localInstanceId,
                                   GWebRTC_ClientManager clientManager,
                                   Handler handler) {

        this.mContext = mContext;

        this.messageUrl = GWebRTC_AppRTC_Common.selected_WebRTC_URL + "/" + ROOM_MESSAGE + "/" +
                GWebRTC_AppRTC_Common.selected_roomId + "/" + sigParms.clientId + "/" + localInstanceId;

        //this.leaveUrl = AppRTC_Common.selected_WebRTC_URL + "/" + ROOM_LEAVE + "/" + AppRTC_Common.selected_roomId + "/" + sigParms.clientId + "/" + localInstanceId;

        this.queryUrl = GWebRTC_AppRTC_Common.selected_WebRTC_URL + "/" + ROOM_QUERY + "/" +
                GWebRTC_AppRTC_Common.selected_roomId + "/" + sigParms.clientId + "/" + localInstanceId;


        this.roomState = GWebRTC_AppRTC_Common.RoomState.CONNECTED;

        this.sigParms = sigParms;
        //this.iCall = iCall;
        this.clientManager = clientManager;
        this.handler = handler;
        this.localInstanceId = localInstanceId;


        /**
         *从信令服务器中获得的一系列参数中，有一个是clientId :
         *  clientId是构成messageUrl的一部分，而messageUrl是客户端发送offer的地址；
         *  每个客户端，即使进入同一个房间（即输入同一个房间号roomId），但是信令服务器返回的clientId也是不同的。
         */
        Log.d(TAG, "Message URL: " + this.messageUrl);
        //Log.d(TAG, "Leave URL: " + this.leaveUrl);
        Log.d(TAG, "Query URL: " + this.queryUrl);

    }

    //======================

    public PeerConnection getPeerConnection() {
        return peerConnection;
    }

    public void setPeerConnection(PeerConnection peerConnection) {
        this.peerConnection = peerConnection;
    }

    public GWebRTC_WebSocketClient getWsClient() {
        return wsClient;
    }

    public void setWsClient(GWebRTC_WebSocketClient wsClient) {
        this.wsClient = wsClient;
    }

    public GWebRTC_CallActivity.PCObserver getPcObserver() {
        return pcObserver;
    }

    public void setPcObserver(GWebRTC_CallActivity.PCObserver pcObserver) {
        //这里要保证remoteRenderer和wsClient已经被实例化
        this.pcObserver = pcObserver;
    }

    public GWebRTC_CallActivity.SDPObserver getSdpObserver() {
        return sdpObserver;
    }

    public void setSdpObserver(GWebRTC_CallActivity.SDPObserver sdpObserver) {
        this.sdpObserver = sdpObserver;
    }

    public SurfaceViewRenderer getRemoteRenderer() {
        return remoteRenderer;
    }

    public void setRemoteRenderer(SurfaceViewRenderer remoteRenderer) {
        this.remoteRenderer = remoteRenderer;
    }

    public ImageView getMute_imgv() {
        return mute_imgv;
    }

    public void setMute_imgv(ImageView mute_imgv) {
        this.mute_imgv = mute_imgv;
    }

    public ImageView getSpeaker_imgv() {
        return speaker_imgv;
    }

    public void setSpeaker_imgv(ImageView speaker_imgv) {
        this.speaker_imgv = speaker_imgv;
    }

    public ImageView getMicrophone_imgv() {
        return microphone_imgv;
    }

    public void setMicrophone_imgv(ImageView microphone_imgv) {
        this.microphone_imgv = microphone_imgv;
    }

    public GWebRTC_AppRTC_Common.RoomState getRoomState() {
        return roomState;
    }

    public void setRoomState(GWebRTC_AppRTC_Common.RoomState roomState) {
        this.roomState = roomState;
    }

    public GWebRTC_CallActivity.WSMessageEvent getWsMessageEvent() {
        return wsMessageEvent;
    }

    public void setWsMessageEvent(GWebRTC_CallActivity.WSMessageEvent wsMessageEvent) {
        this.wsMessageEvent = wsMessageEvent;
    }

    public LinkedList<IceCandidate> getQueuedRemoteCandidates() {
        return queuedRemoteCandidates;
    }

    public void setQueuedRemoteCandidates(LinkedList<IceCandidate> queuedRemoteCandidates) {
        this.queuedRemoteCandidates = queuedRemoteCandidates;
    }

    public MediaStream getMediaStream() {
        return mediaStream;
    }

    public void setMediaStream(MediaStream mediaStream) {
        this.mediaStream = mediaStream;
    }

    public boolean isPeerInitiator() {
        return peerInitiator;
    }

    public void setPeerInitiator(boolean peerInitiator) {
        this.peerInitiator = peerInitiator;
    }

    public boolean isMute() {
        return isMute;
    }

    public void setMute(boolean mute) {
        isMute = mute;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }



    //===============================


    public String getLocalInstanceId() {
        return localInstanceId;
    }


    public GWebRTC_AppRTC_Common.SignalingParameters getSigParms() {
        return sigParms;
    }

    public void setRemoteInstanceId(String remoteInstanceId) {

        this.remoteInstanceId = remoteInstanceId;
    }

    public String getRemoteInstanceId() {

        return remoteInstanceId;
    }

    public boolean isLocalAudioState() {
        return localAudioState;
    }

    public boolean isRemoteAudioState() {
        return remoteAudioState;
    }
    //=====================================================

    /**
     * 以下是回调方法
     */
    //=====================================================
    public void gainMessage(int count) {
        GWebRTC_AsyncHttpURLConnection httpURLConnection = new GWebRTC_AsyncHttpURLConnection("POST", queryUrl + "/" + count,
                "", new GWebRTC_AsyncHttpURLConnection.AsyncHttpEvents() {
            @Override
            public void onHttpError(String errorMessage) {
                Log.e(TAG, "获取offer/candidate失败: " + errorMessage);
                //Toast.makeText( , "获取offer/candidate失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onHttpComplete(String response) {

                messageParameters = GWebRTC_JsonHelper.messageHttpResponseParameters(response);
                Log.e(TAG, localInstanceId + "/" + getRemoteInstanceId());
                setRemoteInstanceId(messageParameters.remoteInstanceId);
                if (messageParameters != null) {

                    if (messageParameters.iceCandidates != null) {
                        // Add remote ICE candidates from room.
                        Log.e(TAG, localInstanceId + "/" + getRemoteInstanceId() + " 设置remote iceCandidates");
                        LinkedList<IceCandidate> queuedRemoteCandidates = getQueuedRemoteCandidates();
                        for (IceCandidate iceCandidate : messageParameters.iceCandidates) {
                            if (queuedRemoteCandidates != null) {
                                queuedRemoteCandidates.add(iceCandidate);
                            } else {
                                Log.e(TAG, "queuedRemoteCandidates is null");
                                //getPeerConnection().addIceCandidate(iceCandidate);
                            }
                        }
                    }

                    if (messageParameters.offerSdp != null) {
                        //relay offer SDP
                        Log.e(TAG, localInstanceId + "/" + getRemoteInstanceId() + "sigParams.offerSdp != null =====设置remote SDP====");
                        SessionDescription sdp = new SessionDescription(messageParameters.offerSdp.type
                                , messageParameters.offerSdp.description);
                        setRemoteDescription(sdp);
                    }


                }
            }
        });

        httpURLConnection.send();

    }

    /**
     * 断开链接
     */
    public void disconnect() {
        Log.e(TAG, "========Disconnect=======");

        //disconnectRoomForInstance();

        Log.e(TAG, "Closing room: " + sigParms.roomId + "\tCloseing instance: " + localInstanceId + "\tRoom state: " + roomState);


        Log.e(TAG, "=====Closing mediaStream.======");
        if (mediaStream != null) {
            //mediaStream.dispose();
            mediaStream = null;
        }

        Log.e(TAG, "Closing peer connection.");
        if (peerConnection != null) {
            //peerConnection.dispose();
            peerConnection.close();
            peerConnection = null;
        }

    }

    public void sendByeToPeer() {
        Log.e(TAG, "======WebSocketSendBye======");
        try {
            JSONObject json = new JSONObject();
            jsonPut(json, "type", "bye");
            jsonPut(json, "receiverId", remoteInstanceId);
            jsonPut(json, "senderId", localInstanceId);
            Log.e(TAG, "WebSocket Send Bye: " + json.toString());
            wsClient.send(json.toString());

            isClosed = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 静音开关
     * 设置本地音效开关，并通知远端
     */
    public void sendAudioSwitch() {


        final JSONObject json = new JSONObject();
        jsonPut(json, "switch", isMute);
        jsonPut(json, "type", "muteswitch");
        jsonPut(json, "receiverId", remoteInstanceId);
        jsonPut(json, "senderId", localInstanceId);
        Log.e(TAG, "send changeAndSendAudioSwitch: localInstanceId:" + localInstanceId +
                "\tremote: " + remoteInstanceId);

        wsClient.send(json.toString());


    }


    public void changeLocalAudioSwitch(boolean state) {
        localAudioState=state;
        if (mediaStream!=null)
        {
            mediaStream.audioTracks.get(0).setEnabled(state);
        }

    }

    public void changeRemoteAudioSwitch(boolean state)
    {
        remoteAudioState=state;

        MediaStream mediaStream =pcObserver.getRemoteMediaStream();
        if (mediaStream!=null)
        {
            mediaStream.audioTracks.get(0).setEnabled(state);
        }
    }

    /**
     * 暂停视频画面
     */
    public void mediastreamStill() {

    }


    //==============配合PCObserver使用的方法===========================
    // Send Ice candidate to the other participant.
    public void sendLocalIceCandidate(final IceCandidate candidate) {

        //Log.e(TAG, "send Local IceCandidate");

        JSONObject json = new JSONObject();

        jsonPut(json, "type", "candidate");
        jsonPut(json, "label", candidate.sdpMLineIndex);
        jsonPut(json, "id", candidate.sdpMid);
        jsonPut(json, "candidate", candidate.sdp);


        if (peerInitiator) {
            // Call initiator sends ice candidates to GAE server.
            if (roomState != GWebRTC_AppRTC_Common.RoomState.CONNECTED) {
                Log.e(TAG, "Sending ICE candidate in non connected state.");
                return;
            }
            sendPostMessage(GWebRTC_AppRTC_Common.MessageType.MESSAGE,
                    messageUrl,
                    json.toString(),
                    localInstanceId);

        } else {
            // Call receiver sends ice candidates to websocket server.
            jsonPut(json, "receiverId", remoteInstanceId);
            jsonPut(json, "senderId", localInstanceId);
            wsClient.send(json.toString());
        }

    }

    // Send removed Ice candidates to the other participant.
    public void sendLocalIceCandidateRemovals(final IceCandidate[] candidates) {

        Log.e(TAG, "send Local IceCandidate Removals");
        JSONObject json = new JSONObject();
        jsonPut(json, "type", "remove-candidates");
        JSONArray jsonArray = new JSONArray();
        for (final IceCandidate candidate : candidates) {
            //删除备份
            //localQueuedRemoteCandidates_backup.remove(candidate);

            jsonArray.put(toJsonCandidate(candidate));
        }
        jsonPut(json, "candidates", jsonArray);
        //jsonPut(json, "instanceId", sigParms.remoteInstanceId);

        if (peerInitiator) {
            // Call initiator sends ice candidates to GAE server.
            if (roomState != GWebRTC_AppRTC_Common.RoomState.CONNECTED) {
                Log.e(TAG, "Sending ICE candidate removals in non connected state.");
                return;
            }
            sendPostMessage(GWebRTC_AppRTC_Common.MessageType.MESSAGE,
                    messageUrl,
                    json.toString(),
                    localInstanceId);

        } else {
            // Call receiver sends ice candidates to websocket server.
            jsonPut(json, "receiverId", remoteInstanceId);
            jsonPut(json, "senderId", localInstanceId);
            wsClient.send(json.toString());
        }

    }


    public void addRemoteIceCandidate(IceCandidate candidate) {
        // Log.e(TAG, "添加远端Ice:addRemoteIceCandidate--------" + candidate);
        if (peerConnection != null) {

            if (queuedRemoteCandidates != null && queuedRemoteCandidates.size() != 0) {
                queuedRemoteCandidates.add(candidate);
            } else {
                peerConnection.addIceCandidate(candidate);
            }
        }

    }


    public void removeRemoteIceCandidates(IceCandidate[] candidates) {
        //Log.e(TAG, "删除远端Ice:removeRemoteIceCandidates--------");
        if (queuedRemoteCandidates != null) {
            Log.d(TAG, "Add " + queuedRemoteCandidates.size() + " remote candidates");
            for (IceCandidate candidate : queuedRemoteCandidates) {
                peerConnection.addIceCandidate(candidate);
            }
            //queuedRemoteCandidates = null;
            queuedRemoteCandidates.clear();
        }
        for (IceCandidate ice : candidates) {
            Log.d(TAG, "ice : " + ice);
        }
        peerConnection.removeIceCandidates(candidates);
    }


    //==============配合SdpObserver使用的方法===========================
    public void setLocalDescription(final SessionDescription sdp) {

        if (peerConnection != null) {
            Log.d(TAG, "Set local SDP from " + sdp.type);

            //保存本地的SessionDescription
            peerConnection.setLocalDescription(sdpObserver, sdp);
        }

    }

    public void setRemoteDescription(final SessionDescription sdp) {

//        if (sdp==null)
//        {
//            peerConnection.setRemoteDescription(sdpObserver, null);
//            return;
//        }

        Log.e(TAG, "localInstanceId:" + localInstanceId + "remoteInstanceId:" + remoteInstanceId +
                "===setRemoteDescription===");
        if (peerConnection == null) {
            return;
        }
        String sdpDescription = sdp.description;

        sdpDescription = preferCodec(sdpDescription, AUDIO_CODEC_ISAC, true);
        sdpDescription = preferCodec(sdpDescription, preferredVideoCodec, false);

        Log.d(TAG, "Set remote SDP.");
        SessionDescription sdpRemote = new SessionDescription(sdp.type, sdpDescription);
        peerConnection.setRemoteDescription(sdpObserver, sdpRemote);
    }


    /**
     * Send local offer SDP to the other participant.
     * 将本地的 offer sdp 发送给其他的参与者
     *
     * @param sdp
     */
    public void sendOfferSdp(final SessionDescription sdp) {

        if (roomState != GWebRTC_AppRTC_Common.RoomState.CONNECTED) {
            clientManager.reportError(localInstanceId, "Sending offer SDP in non connected state.");
            return;
        }


        JSONObject json = new JSONObject();
        jsonPut(json, "sdp", sdp.description);
        jsonPut(json, "type", "offer");

        //加入clientID
        //====
        Log.e(TAG, "send OfferSdp: local:" + localInstanceId + "\tmessageUrl: " + messageUrl);

        sendPostMessage(GWebRTC_AppRTC_Common.MessageType.MESSAGE,
                messageUrl,
                json.toString(),
                localInstanceId);
    }

    /**
     * Send local answer SDP to the other participant.
     * 将本地的 answer sdp 发送给其他的参与者
     *
     * @param sdp
     */
    public void sendAnswerSdp(final SessionDescription sdp) {

        //做备份
        //localSdp_backup = sdp;

        JSONObject json = new JSONObject();
        jsonPut(json, "sdp", sdp.description);
        jsonPut(json, "type", "answer");
        jsonPut(json, "receiverId", remoteInstanceId);
        jsonPut(json, "senderId", localInstanceId);
        Log.e(TAG, "send AnswerSdp: localInstanceId:" + localInstanceId + "\tremote: " + remoteInstanceId);

        wsClient.send(json.toString());

    }

    /**
     * 问题：同样是send offer/answer , 为什么采取的发送方式不同：send offer时使用的是http；send answer时使用的是websocket
     * <p>
     * 答：发送的地址不一样。offer发送到messageUrl地址中，answer发送到WebSocket服务器中。而WebSocket服务器的地址是在connect/register确定的
     */

    //======================Helpers==============================

    /**
     * 本方法目的只是向服务器发送，并不注重返回的消息
     *
     * @param messageType
     * @param url
     * @param message
     * @param localInstanceId
     */
    public void sendPostMessage(final GWebRTC_AppRTC_Common.MessageType messageType,
                                final String url,
                                final String message,
                                final String localInstanceId) {
        String logInfo = url;
        if (message != null) {
            logInfo += ". Message: " + message;
        }
        Log.d(TAG, "localInstanceId" + localInstanceId + "\tC->GAE: " + logInfo);

        GWebRTC_AsyncHttpURLConnection httpConnection = new GWebRTC_AsyncHttpURLConnection(
                "POST", url, message, new GWebRTC_AsyncHttpURLConnection.AsyncHttpEvents() {
            @Override
            public void onHttpError(String errorMessage) {
                Log.e(TAG, "onHttpError:" + errorMessage);
            }

            @Override
            public void onHttpComplete(String response) {
                if (messageType == GWebRTC_AppRTC_Common.MessageType.MESSAGE) {
                    try {
                        JSONObject roomJson = new JSONObject(response);
                        String result = roomJson.getString("result");
                        if (!result.equals("SUCCESS")) {
                            clientManager.reportError(localInstanceId, "GAE POST error: " + result);
                        } else {
                            //Log.d(TAG,"sendPostMessage success");
                        }
                    } catch (JSONException e) {
                        clientManager.reportError(localInstanceId, "GAE POST JSON error: " + e.toString());
                    }
                }
            }
        });
        httpConnection.send();
    }


}