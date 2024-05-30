package com.navico.voicerecord;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;


public class MainActivity extends AppCompatActivity implements RecordListener {
    // Đối tượng RecordLibrary để thực hiện ghi âm.
    private RecordLibrary recordLibrary;

    // Button để bắt đầu và dừng ghi âm.
    private Button startButton, stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo đối tượng RecordLibrary với context của activity và đối tượng lắng nghe sự kiện ghi âm.
        recordLibrary = new RecordLibrary(this, this);

        // Khởi tạo và liên kết các view với mã nguồn.
        startButton = findViewById(R.id.button);
        stopButton = findViewById(R.id.button2);

        // Thiết lập các cài đặt cho RecordLibrary
        recordLibrary.setMinRequiredStorage(10 * 1024 * 1024); // Dung lượng lưu trữ tối thiểu cần thiết để bắt đầu ghi âm (10MB)
        recordLibrary.setMaxRecordingDuration(12000); // Thời gian tối đa cho mỗi lần ghi âm (12 giây)
        recordLibrary.setSpaceCheckInterval(3000); // Kiểm tra mỗi khoảng trống 3 giây
        recordLibrary.setSoundThreshold(60); // Ngưỡng âm thanh (60dB)
        recordLibrary.setSoundCheckInterval(3000); // Thời gian không phát hiện âm thanh (3 giây)

        // Xử lý sự kiện khi nhấn vào nút bắt đầu ghi âm
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordLibrary.startRecording();
            }
        });

        // Xử lý sự kiện khi nhấn vào nút dừng ghi âm
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordLibrary.stopRecording(true);
            }
        });
    }

    // Phương thức được gọi khi quá trình ghi âm bắt đầu.
    @Override
    public void onRecordingStarted() {

    }

    // Phương thức được gọi khi quá trình ghi âm kết thúc.
    @Override
    public void onRecordingStopped(String filePath,boolean isSuccess) {

    }

    // Phương thức được gọi khi có lỗi xảy ra trong quá trình ghi âm.
    @Override
    public void onError(String error) {
        Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
    }

    // Phương thức được gọi khi quyền ghi âm chưa được cấp.
    @Override
    public void onPermissionRequired() {
        // Yêu cầu cấp quyền ghi âm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
            }, 1);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
            }, 1);
        }
    }

    // Phương thức được gọi khi dung lượng lưu trữ không đủ để bắt đầu hoặc tiếp tục ghi âm.
    @Override
    public void onInsufficientStorage() {

    }

    // Phương thức được gọi khi phát hiện âm thanh trong quá trình ghi âm.
    @Override
    public void onSoundDetected() {
        // Có thể thêm xử lý khi phát hiện âm thanh
    }

    // Phương thức được gọi khi nhận kết quả từ yêu cầu cấp quyền ghi âm.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Bắt đầu ghi âm khi quyền được cấp
                recordLibrary.startRecording();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

