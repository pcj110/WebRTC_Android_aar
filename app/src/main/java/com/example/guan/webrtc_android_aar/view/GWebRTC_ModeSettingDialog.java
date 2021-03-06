package com.example.guan.webrtc_android_aar.view;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioGroup;

import com.example.guan.webrtc_android_aar.R;
import com.example.guan.webrtc_android_aar.common.GWebRTC_AppConstant;

/**
 * Created by guan on 3/25/17.
 */

public class GWebRTC_ModeSettingDialog extends Dialog {

    RadioGroup modesetting_radiogrp;
    GWebRTC_ModeSettingDialog.DialogCallback callback;

    public enum Mode {P_2_M, M_2_M}

    public GWebRTC_ModeSettingDialog(@NonNull Context context) {
        super(context,R.style.Dialog_Dim);

        setCustomeDialog();

        //设置dialog的宽度
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = (int) (GWebRTC_AppConstant.SCRRENWIDTH * 0.8); //设置宽度
        getWindow().setAttributes(lp);
    }


    public void setDialogCallback(GWebRTC_ModeSettingDialog.DialogCallback callback) {
        this.callback = callback;
    }

    private void setCustomeDialog() {
        View mView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_modesetting_gwebrtc, null);
        modesetting_radiogrp = (RadioGroup) mView.findViewById(R.id.modesetting_radiogrp);


        modesetting_radiogrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (checkedId==R.id.p_2_m_radiobtn)
                {
                    callback.onClickRadioButton(Mode.P_2_M);

                }else if (checkedId==R.id.m_2_m_radiobtn)
                {
                    callback.onClickRadioButton(Mode.M_2_M);

                }
//                switch (checkedId) {
//                    case R.id.p_2_m_radiobtn:
//                        callback.onClickRadioButton(Mode.P_2_M);
//                        break;
//                    case R.id.m_2_m_radiobtn:
//                        callback.onClickRadioButton(Mode.M_2_M);
//                        break;
//                }
                GWebRTC_ModeSettingDialog.this.dismiss();
            }
        });

        super.setContentView(mView);

    }

    public interface DialogCallback {

        void onClickRadioButton(GWebRTC_ModeSettingDialog.Mode mode);

    }


}
