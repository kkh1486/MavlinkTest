package com.example.mavlinktest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mavlinktest.databinding.ActivityMainBinding;
import com.felhr.usbserial.UsbSerialDevice;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import io.dronefleet.mavlink.MavlinkConnection;
import io.dronefleet.mavlink.MavlinkMessage;
import io.dronefleet.mavlink.common.GpsRawInt;
import io.dronefleet.mavlink.common.RawImu;
import io.dronefleet.mavlink.minimal.Heartbeat;

public class MainActivity extends AppCompatActivity {
    private static final String ACTION_USB_PERMISSION = "com.example.mavlinktest.USB_PERMISSION";

    // 뷰 바인딩 객체
    private ActivityMainBinding mBinding;

    // USB 및 Mavlink 처리를 위한 도우미 클래스
    private UsbHelper usbHelper;
    private MavlinkDataProcessor mavlinkDataProcessor;
    private UsbManager usbManager;

    // USB 권한 요청에 대한 응답을 처리하는 BroadcastReceiver
    private final BroadcastReceiver usbPermissionReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            onDeviceSelected(device);
                        }
                    } else {
                        Log.d("TAG", "permission denied for device " + device);
                    }
                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        // 시스템 서비스로부터 UsbManager 인스턴스 가져오기
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        usbHelper = new UsbHelper(this, usbPermissionReceiver);

        // 연결 버튼 클릭 리스너 설정
        mBinding.connectButton.setOnClickListener(v -> usbHelper.showUsbDeviceList(this::onDeviceSelected));
    }

    // USB 장치가 선택되었을 때 호출되는 메소드
    private void onDeviceSelected(UsbDevice device) {
        UsbDeviceConnection connection = usbManager.openDevice(device);
        if (connection != null) {
            UsbSerialDevice serialDevice = UsbSerialDevice.createUsbSerialDevice(device, connection);
            if (serialDevice != null) {
                if (serialDevice.open()) {
                    serialDevice.setBaudRate(57600);

                    try {
                        PipedOutputStream pipedOut = new PipedOutputStream();
                        PipedInputStream pipedIn = new PipedInputStream(pipedOut);

                        serialDevice.read(data -> {
                            try {
                                pipedOut.write(data);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                        MavlinkConnection mavlinkConnection = MavlinkConnection.create(pipedIn, new UsbSerialOutputStream(serialDevice));
                        mavlinkDataProcessor = new MavlinkDataProcessor(mavlinkConnection);
                        mavlinkDataProcessor.requestRawImuData(24);
                        mavlinkDataProcessor.requestRawImuData(27);
                        mavlinkDataProcessor.startMavlinkMessageListener(this::processMAVLinkData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // MAVLink 데이터를 처리하는 메소드
    private void processMAVLinkData(MavlinkMessage message) {
        if (message.getPayload() instanceof Heartbeat) {
            Heartbeat heartbeatMessage = (Heartbeat) message.getPayload();
            runOnUiThread(() -> {
                String heartbeatText = "System Type: " + heartbeatMessage.type().entry() + "\n"
                        + "Autopilot Type: " + heartbeatMessage.autopilot().entry() + "\n"
                        + "Base Mode: " + heartbeatMessage.baseMode().entry() + "\n"
                        + "Custom Mode: " + heartbeatMessage.customMode() + "\n"
                        + "System Status: " + heartbeatMessage.systemStatus().entry();

                mBinding.textViewHeartbeat.setText(heartbeatText);
            });
        }
        else if (message.getPayload() instanceof RawImu) {
            RawImu imuMessage = (RawImu) message.getPayload();
            runOnUiThread(() -> {
                int magX = imuMessage.xmag();  // 지자기 X 값
                int magY = imuMessage.ymag();  // 지자기 Y 값
                int magZ = imuMessage.zmag();  // 지자기 Z 값

                mBinding.x.setText("Mag X: " + magX);
                mBinding.y.setText("Mag Y: " + magY);
                mBinding.z.setText("Mag Z: " + magZ);
            });
        }
        else if (message.getPayload() instanceof GpsRawInt) {
            GpsRawInt gpsMessage = (GpsRawInt) message.getPayload();
            runOnUiThread(() -> {
                double latitude = gpsMessage.lat() / 1E7;  // 위도
                double longitude = gpsMessage.lon() / 1E7;  // 경도
                double altitude = gpsMessage.alt() / 1E3;  // 고도

                mBinding.textViewLatitude.setText("Latitude: " + latitude);
                mBinding.textViewLongitude.setText("Longitude: " + longitude);
                mBinding.textViewAltitude.setText("Altitude: " + altitude + "m");
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // BroadcastReceiver의 등록 해제
        unregisterReceiver(usbPermissionReceiver);
    }
}

