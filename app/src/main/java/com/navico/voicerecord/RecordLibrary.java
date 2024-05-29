package com.navico.voicerecord;

import android.content.Context;

public class RecordLibrary {
    //Dung lượng tối thiểu cần thiết để bắt đầu ghi âm (10MB)
    private static final long MIN_REQUIRED_STORAGE = 10 * 1024 * 1024;
    //Thời gian tối đa cho mỗi lần ghi âm (1 phút).
    private static final long MAX_RECORDING_DURATION = 60000;
    // Kiểm tra mỗi 3 giây
    private static final long SPACE_CHECK_INTERVAL = 3000;


    public RecordLibrary(Context context, RecordListener listener) {
        //context: Được truyền vào để sử dụng các tài nguyên hệ thống.
        //listener: Được truyền vào để nhận các sự kiện ghi âm.
        // Kiểm tra không gian trống
        // Kiểm tra dung lượng file
        // Lặp lại sau mỗi khoảng thời gian
    }

    public void startRecording() {
        //Kiểm tra quyền ghi âm

        //Kiểm tra dung lượng lưu trữ.

        //Thiết lập MediaRecorder và bắt đầu ghi âm.
    }

    public void stopRecording() {
        //Dừng ghi âm

    }
    private void checkFileSize(String filePath) {
        // Kiểm tra dung lượng file
    }
    private String getTempFilePath() {
        //Xác định folder tạm trên các loại thiết bị
        //Lưu file audio và folder đó
    }
}

