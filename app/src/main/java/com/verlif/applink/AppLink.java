package com.verlif.applink;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.util.Date;
import java.util.Map;

public abstract class AppLink {

    public static final int WHAT_SHOW = 1001;
    public static final int WHAT_FILE = 1002;

    /**
     * 连接的App包名
     */
    public static final String PACKAGE_NAME = "com.verlif.futurei";
    /**
     * 连接的App启动活动完整包名
     */
    public static final String CLASS_NAME = "com.verlif.futurei.model.main.MainActivity";
    /**
     * 连接App的Service的接收动作名
     */
    public static final String ACTION_NAME = "com.verlif.futurei.messenger";

    private Context context;

    public AppLink(Context context) {
        this.context = context;
    }

    private Messenger mService;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    private boolean bindServiceInvoked() {
        Intent intent = new Intent();
        intent.setAction(ACTION_NAME);
        intent.setPackage(PACKAGE_NAME);
        try {
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    public boolean launchApp() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClassName(PACKAGE_NAME, CLASS_NAME);
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    /**
     * 发送简单的内存数据
     *
     * @param map 数据的map整合
     */
    public void sendMessage(Map<String, String> map) {
        Bundle bundle = new Bundle();
        for (String s : map.keySet()) {
            bundle.putString(s, map.get(s));
        }
        send(WHAT_SHOW, bundle);
    }

    /**
     * 发送文件
     *
     * @param filePath 文件路径
     */
    public void sendMessage(String... filePath) {
        Bundle bundle = new Bundle();
        for (int i = 0; i < filePath.length; i++) {
            bundle.putString("file" + i, filePath[i]);
        }
        send(WHAT_FILE, bundle);
    }

    private void send(int what, Bundle bundle) {
        if (bindServiceInvoked()) {
            Message message = new Message();
            message.what = what;
            message.getData().putBundle("bundle", bundle);
            new Thread(() -> {
                boolean ifConn = false;
                long startTime = new Date().getTime();
                while (!ifConn) {
                    ifConn = mService != null;
                    // 超时处理
                    if ((startTime + 4000) < new Date().getTime()) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(this::overTime);
                        break;
                    }
                }
                if (mService != null) {
                    try {
                        mService.send(message);
                        context.unbindService(connection);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(this::overTime);
        }
    }

    /**
     * 连接超时回调
     */
    public abstract void overTime();
}
