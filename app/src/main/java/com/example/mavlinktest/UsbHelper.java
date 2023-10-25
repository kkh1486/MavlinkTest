package com.example.mavlinktest;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class UsbHelper {

    // USB 권한 액션 정의
    private static final String ACTION_USB_PERMISSION = "com.example.mavlinktest.USB_PERMISSION";

    // USB 관리자
    private UsbManager usbManager;
    // 액티비티 참조
    private Activity activity;
    // USB 권한 BroadcastReceiver
    private final BroadcastReceiver usbPermissionReceiver;

    // 생성자
    public UsbHelper(Activity activity, BroadcastReceiver receiver) {
        this.activity = activity;
        this.usbPermissionReceiver = receiver;
        // 시스템 서비스에서 USB 관리자 가져오기
        usbManager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
    }

    // USB 장치 목록을 보여주는 메서드
    public void showUsbDeviceList(Consumer<UsbDevice> onDeviceSelected) {
        // 연결된 USB 장치들의 목록을 가져옴
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        List<UsbDevice> deviceList = new ArrayList<>();

        // USB 장치들이 있으면
        if (!usbDevices.isEmpty()) {
            deviceList.addAll(usbDevices.values());

            // 장치 이름 배열 생성
            String[] deviceNames = new String[deviceList.size()];
            for (int i = 0; i < deviceList.size(); i++) {
                UsbDevice device = deviceList.get(i);
                deviceNames[i] = device.getManufacturerName() + " - " + device.getProductName() + " (VID: " + device.getVendorId() + ", PID: " + device.getProductId() + ")";
            }

            // 다이얼로그로 USB 장치 목록을 보여줌
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("장치 선택");
            builder.setItems(deviceNames, (dialog, which) -> {
                UsbDevice selectedDevice = deviceList.get(which);

                // BroadcastReceiver 등록
                IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                activity.registerReceiver(usbPermissionReceiver, filter);

                // 해당 장치에 대한 권한 요청
                PendingIntent permissionIntent = PendingIntent.getBroadcast(activity, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_MUTABLE);
                usbManager.requestPermission(selectedDevice, permissionIntent);
            });
            builder.show();
        }
    }
}