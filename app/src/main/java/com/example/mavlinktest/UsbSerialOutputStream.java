package com.example.mavlinktest;

import com.felhr.usbserial.UsbSerialDevice;

import java.io.IOException;
import java.io.OutputStream;

public class UsbSerialOutputStream extends OutputStream {
    // USB 직렬 장치
    private final UsbSerialDevice device;

    // 생성자
    public UsbSerialOutputStream(UsbSerialDevice device) {
        this.device = device;
    }

    @Override
    // 데이터를 USB 직렬 장치로 쓰기
    public void write(int b) throws IOException {
        device.write(new byte[]{(byte) b});
    }
}