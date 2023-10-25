# MavlinkTest

사용 기기 : Pixhawk 2.4.8, Samsung Galaxy S22 Ultra, SMC-2000, 3DR x6 Telemetry

개발 환경 : Windows 10 PC, Android Studio Electric Eel | 2022.1.1

개발 기간 : 2023.10.10 ~

개발 프로그램 OS : Android

개발 프로그램 기능 : SMC-2000(MBC RTK)가 연결된 Pixhawk와 Android 기기가 Telemetry를 통해 USB-Serial 통신으로 연결 후 MAVLink 프로토콜을 통해 GPS 데이터를 받아오는 기능

 
프로그램 실행 방법

Pixhawk 2.4.8의 TELEM 2 포트에 3DR x6 Telemetry 연결

Pixhawk 2.4.8의 TELEM 1 포트에 세팅이 완료된 SMC-2000의 TELEM 2 포트와 연결

Android 기기에서 해당 프로그램 실행

Android 기기의 USB 포트에 3DR x6 Telemetry 연결

해당 프로그램에서 장치 연결 버튼 클릭

연결된 장치 클릭

권한 허용 클릭

Hearbeat, RawImu, GPS 데이터 출력

