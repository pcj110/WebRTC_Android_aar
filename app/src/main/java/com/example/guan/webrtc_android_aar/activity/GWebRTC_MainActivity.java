package com.example.guan.webrtc_android_aar.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.guan.webrtc_android_aar.R;
import com.example.guan.webrtc_android_aar.common.GWebRTC_AppConstant;
import com.example.guan.webrtc_android_aar.common.GWebRTC_AppRTC_Common;
import com.example.guan.webrtc_android_aar.utils.GWebRTC_ClickUtil;
import com.example.guan.webrtc_android_aar.view.GWebRTC_ModeSettingDialog;
import com.example.guan.webrtc_android_aar.view.GWebRTC_ServerSettingDialog;
import com.example.guan.webrtc_android_aar.view.GWebRTC_YesOrNoDialog;

import java.util.ArrayList;
import java.util.Iterator;


public class GWebRTC_MainActivity extends AppCompatActivity {

    String TAG = GWebRTC_AppRTC_Common.TAG_COMM + "MainActivity";

    ArrayList<String> permissionList = new ArrayList<String>();


    Button startCall_btn;
    Button serverSetting_btn;
    EditText roomid_et;
    TextView selectedServer_tv;
    ImageButton modeSetting_imgbtn;
    TextView selectedMode_tv;
    RadioGroup role_radiogrp;
    GWebRTC_AppRTC_Common.RoomRole role;
    String roomid;
    Context mContext;

    GWebRTC_YesOrNoDialog ynDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_gwebrtc);
        initData();
        initUI();
        checkAndRequestPermissions(permissionList);
    }

    private void initData() {
        mContext = this;
        saveScreenSize();
        permissionList.add(Manifest.permission.CAMERA);
        permissionList.add(Manifest.permission.RECORD_AUDIO);
        permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void initUI() {

        Window window = this.getWindow();
        window.setStatusBarColor(Color.LTGRAY);

        startCall_btn = (Button) this.findViewById(R.id.Call_btn);
        serverSetting_btn = (Button) this.findViewById(R.id.serversetting_btn);
        roomid_et = (EditText) this.findViewById(R.id.RoomID_et);
        selectedServer_tv = (TextView) this.findViewById(R.id.serversetting_tv);
        role_radiogrp = (RadioGroup) this.findViewById(R.id.role_radiogrp);
        modeSetting_imgbtn = (ImageButton) this.findViewById(R.id.selectmode_imgbtn);
        selectedMode_tv = (TextView) this.findViewById(R.id.modesetting_tv);


        role_radiogrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (checkedId==R.id.Master_radiobtn)
                {
                    role = GWebRTC_AppRTC_Common.RoomRole.MASTER;

                }else if(checkedId==R.id.Slave_radiobtn){
                    role = GWebRTC_AppRTC_Common.RoomRole.SLAVE;

                }

//                switch (checkedId) {
//                    case R.id.Master_radiobtn:
//                        break;
//                    case R.id.Slave_radiobtn:
//                        break;
//                }
            }
        });

        startCall_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GWebRTC_ClickUtil.isFastDoubleClick()) {
                    return;
                }

                roomid = roomid_et.getText().toString().trim();

                if (GWebRTC_AppRTC_Common.selected_WebRTC_URL.equals("") || GWebRTC_AppRTC_Common.selected_turnServer_URL.equals("")) {
                    Toast.makeText(mContext, "请选择服务器", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (null == role || role.equals("")) {
                    Toast.makeText(mContext, "请选择角色", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (null == roomid || roomid.equals("")) {
                    Toast.makeText(mContext, "请输入房间号", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(GWebRTC_MainActivity.this, GWebRTC_CallActivity.class);
                GWebRTC_AppRTC_Common.selected_roomId = roomid;
                GWebRTC_AppRTC_Common.selected_role = role;
                Log.d(TAG, "roomurl:" + GWebRTC_AppRTC_Common.selected_WebRTC_URL +
                        "\troomid:" + GWebRTC_AppRTC_Common.selected_roomId +
                        "\trole:" + GWebRTC_AppRTC_Common.selected_role +
                        "\tmode: " + GWebRTC_AppRTC_Common.selectedMode);


                //startActivity(intent);
                startActivityForResult(intent, GWebRTC_AppConstant.MAINACTIVITY_REQUEST_CODE);
            }
        });

        serverSetting_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GWebRTC_ServerSettingDialog dialog = new GWebRTC_ServerSettingDialog(mContext);
                dialog.setCanceledOnTouchOutside(true);//触摸dialog以外的区域，dialog会消失
                dialog.setDialogCallback(new GWebRTC_ServerSettingDialog.DialogCallback() {
                    @Override
                    public void onClickRadioButton(GWebRTC_ServerSettingDialog.SelectedServer s) {
                        if (s == GWebRTC_ServerSettingDialog.SelectedServer.Lab) {
                            GWebRTC_AppRTC_Common.selected_WebRTC_URL = GWebRTC_AppRTC_Common.labServer_WebRTC_URL;
                            GWebRTC_AppRTC_Common.selected_turnServer_URL = GWebRTC_AppRTC_Common.labServer_turnServer_URL;
                        } else if (s == GWebRTC_ServerSettingDialog.SelectedServer.Aliyun) {
                            GWebRTC_AppRTC_Common.selected_WebRTC_URL = GWebRTC_AppRTC_Common.aliyun_WebRTC_URL;
                            GWebRTC_AppRTC_Common.selected_turnServer_URL = GWebRTC_AppRTC_Common.aliyun_turnServer_URL;
                        }

                    }
                });
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        selectedServer_tv.setText(GWebRTC_AppRTC_Common.selected_WebRTC_URL);

                    }
                });
                dialog.show();
            }
        });
        selectedServer_tv.setText(GWebRTC_AppRTC_Common.selected_WebRTC_URL);


        modeSetting_imgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GWebRTC_ModeSettingDialog dialog = new GWebRTC_ModeSettingDialog(mContext);
                dialog.setCanceledOnTouchOutside(true);//触摸dialog以外的区域，dialog会消失
                dialog.setDialogCallback(new GWebRTC_ModeSettingDialog.DialogCallback() {
                    @Override
                    public void onClickRadioButton(GWebRTC_ModeSettingDialog.Mode mode) {
                        if (mode == GWebRTC_ModeSettingDialog.Mode.M_2_M) {
                            GWebRTC_AppRTC_Common.selectedMode = GWebRTC_AppRTC_Common.M_2_M;
                        } else if (mode == GWebRTC_ModeSettingDialog.Mode.P_2_M) {
                            GWebRTC_AppRTC_Common.selectedMode = GWebRTC_AppRTC_Common.P_2_M;
                        }
                    }
                });
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        //showSelectedServerOnActivity();
                        selectedMode_tv.setText(GWebRTC_AppRTC_Common.selectedMode);
                    }
                });
                dialog.show();
            }
        });
        selectedMode_tv.setText(GWebRTC_AppRTC_Common.selectedMode);


    }


    private void checkAndRequestPermissions(ArrayList<String> permissionList) {

        ArrayList<String> list = new ArrayList<>(permissionList);
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String permission = it.next();
            int hasPermission = ContextCompat.checkSelfPermission(this, permission);
            if (hasPermission == PackageManager.PERMISSION_GRANTED) {
                it.remove();
            }
        }

        if (list.size() == 0) {
            return;
        }
        String[] permissions = list.toArray(new String[0]);
        ActivityCompat.requestPermissions(this, permissions, GWebRTC_AppConstant.MY_PERMISSIONS_REQUEST);

    }

    private void saveScreenSize() {
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;     // 屏幕宽度（像素）
        int height = metric.heightPixels;   // 屏幕高度（像素）
        //if(!ScreenUtil.islandscape(LoginActivity.this))
        GWebRTC_AppConstant.SCRRENWIDTH = width;
        GWebRTC_AppConstant.SCREENHEIGHT = height;
        Log.d(TAG, "SCRRENWIDTH:\t" + width + "\tSCREENHEIGHT:\t" + height);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case GWebRTC_AppConstant.MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                int length = grantResults.length;
                boolean re_request = false;
                for (int i = 0; i < length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "权限授予成功:" + permissions[i]);
                    } else {
                        Log.e(TAG, "权限授予失败:" + permissions[i]);
                        re_request = true;
                    }
                }
                if (re_request) {
                    //弹出对话框，提示用户重新授予权限
                    final GWebRTC_YesOrNoDialog permissionDialog = new GWebRTC_YesOrNoDialog(mContext);
                    permissionDialog.setCanceledOnTouchOutside(false);
                    permissionDialog.setMeesage("请授予相关权限，否则程序无法运行。\n\n点击确定，重新授予权限。\n点击取消，立即终止程序。\n");
                    permissionDialog.setCallback(new GWebRTC_YesOrNoDialog.YesOrNoDialogCallback() {
                        @Override
                        public void onClickButton(GWebRTC_YesOrNoDialog.ClickedButton button, String message) {
                            if (button == GWebRTC_YesOrNoDialog.ClickedButton.POSITIVE) {
                                permissionDialog.dismiss();
                                //此处需要弹出手动修改权限的系统界面
                                checkAndRequestPermissions(permissionList);
                            } else if (button == GWebRTC_YesOrNoDialog.ClickedButton.NEGATIVE) {
                                permissionDialog.dismiss();
                                GWebRTC_MainActivity.this.finish();
                            }
                        }
                    });

                    permissionDialog.show();
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        //根据requestCode，resultCode判断后，再进行操作。此处省略
        roomid_et.getText().clear();
        Log.e(TAG, "MainActivity finished successfully");
    }

}
