package com.example.guan.webrtc_android_aar.view;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.guan.webrtc_android_aar.R;
import com.example.guan.webrtc_android_aar.common.GWebRTC_AppConstant;


public class GWebRTC_YesOrNoDialog extends Dialog {
    private Button positiveButton, negativeButton;
    private TextView message;
    private YesOrNoDialogCallback callback;

    public enum ClickedButton {POSITIVE, NEGATIVE}

    public GWebRTC_YesOrNoDialog(Context context) {
        super(context, R.style.Dialog_Dim);
        setCustomDialog();
        getWindow().setLayout((int) (GWebRTC_AppConstant.SCRRENWIDTH * 0.7), (int) (GWebRTC_AppConstant.SCREENHEIGHT * 0.3));
    }

    public void setCallback(YesOrNoDialogCallback callback) {
        this.callback = callback;
    }

    private void setCustomDialog() {
        View mView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_yesorno_gwebrtc, null);
        message = (TextView) mView.findViewById(R.id.title);
        positiveButton = (Button) mView.findViewById(R.id.accept_btn);
        negativeButton = (Button) mView.findViewById(R.id.refuse_btn);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onClickButton(ClickedButton.POSITIVE,"");
            }
        });
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onClickButton(ClickedButton.NEGATIVE,"");
            }
        });

        //ViewGroup.LayoutParams params=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        super.setContentView(mView);
    }

    @Override
    public void setContentView(int layoutResID) {
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
    }

    @Override
    public void setContentView(View view) {
    }


    public void setMeesage(String mes) {
        message.setText(mes);
    }


    public interface YesOrNoDialogCallback {
        void onClickButton(ClickedButton button, String message);
    }

}
