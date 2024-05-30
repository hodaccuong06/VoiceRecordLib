package com.navico.voicerecord;

public interface RecordListener {
    /**
     * Được gọi khi quá trình ghi âm bắt đầu.
     */
    void onRecordingStarted();

    /**
     * Được gọi khi quá trình ghi âm kết thúc.
     * @param filePath Đường dẫn của tệp âm thanh đã ghi.
     * @param isSuccess true nếu quá trình ghi âm thành công, ngược lại là false.
     */
    void onRecordingStopped(String filePath, boolean isSuccess);

    /**
     * Được gọi khi có lỗi xảy ra trong quá trình ghi âm.
     * @param error Thông báo lỗi.
     */
    void onError(String error);

    /**
     * Được gọi khi quyền ghi âm chưa được cấp.
     */
    void onPermissionRequired();

    /**
     * Được gọi khi dung lượng lưu trữ không đủ để bắt đầu hoặc tiếp tục ghi âm.
     */
    void onInsufficientStorage();

    /**
     * Được gọi khi phát hiện âm thanh trong quá trình ghi âm.
     */
    void onSoundDetected();
}