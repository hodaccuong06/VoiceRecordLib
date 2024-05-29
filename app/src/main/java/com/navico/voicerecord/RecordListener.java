package com.navico.voicerecord;

public interface RecordListener {
    //Được gọi khi quá trình ghi âm bắt đầu.
    void onRecordingStarted();
    //Được gọi khi quá trình ghi âm kết thúc. Trả về đường dẫn của file audio đã ghi.
    void onRecordingStopped(String filePath);
    //Được gọi khi có lỗi xảy ra trong quá trình ghi âm.
    void onError(String error);
    //Được gọi khi quyền ghi âm chưa được cấp.
    void onPermissionRequired();
    //Được gọi khi dung lượng lưu trữ không đủ để bắt đầu hoặc tiếp tục ghi âm.
    void onInsufficientStorage();
}