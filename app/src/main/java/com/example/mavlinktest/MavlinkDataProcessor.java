package com.example.mavlinktest;

import java.io.IOException;
import java.util.function.Consumer;

import io.dronefleet.mavlink.MavlinkConnection;
import io.dronefleet.mavlink.MavlinkMessage;
import io.dronefleet.mavlink.common.CommandLong;
import io.dronefleet.mavlink.common.MavCmd;

public class MavlinkDataProcessor {
    // Mavlink 연결 객체
    private MavlinkConnection mavlinkConnection;

    // 생성자
    public MavlinkDataProcessor(MavlinkConnection mavlinkConnection) {
        this.mavlinkConnection = mavlinkConnection;
    }

    // Mavlink 메시지 수신 리스너 시작
    public void startMavlinkMessageListener(Consumer<MavlinkMessage> onMessageReceived) {
        new Thread(() -> {
            while (true) {
                try {
                    MavlinkMessage message = mavlinkConnection.next();
                    if (message != null) {
                        onMessageReceived.accept(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }

    // Raw IMU 데이터 요청
    public void requestRawImuData(int num) {
        int systemId = 255;
        int componentId = 0;
        CommandLong commandLong = CommandLong.builder()
                .targetSystem(1)
                .targetComponent(1)
                .command(MavCmd.MAV_CMD_SET_MESSAGE_INTERVAL)
                .param1(num) // 메시지 ID
                .param2(1000000) // 10Hz (단위: 마이크로초)
                .confirmation(0)
                .build();

        try {
            if (mavlinkConnection != null) {
                mavlinkConnection.send2(systemId, componentId, commandLong);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}