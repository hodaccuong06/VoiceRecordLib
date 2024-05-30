package com.navico.voicerecord;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class RecordLibrary {
    // Dung lượng tối thiểu cần thiết để bắt đầu ghi âm
    private static long MIN_REQUIRED_STORAGE;
    // Thời gian tối đa cho mỗi lần ghi âm
    private static long MAX_RECORDING_DURATION;
    // Kiểm tra mỗi ? giây
    private static long SPACE_CHECK_INTERVAL;
    // Ngưỡng âm thanh (dB)
    private static int SOUND_THRESHOLD;
    // Thời gian không phát hiện âm thanh
    private static long SOUND_CHECK_INTERVAL;



    // Đối tượng MediaRecorder để ghi âm âm thanh.
    private MediaRecorder recorder;

    // Handler để thực hiện kiểm tra không gian lưu trữ.
    private Handler spaceCheckHandler;

    // Handler để quản lý việc dừng ghi âm sau một khoảng thời gian.
    private Handler stopRecordingHandler;

    // Handler để thực hiện kiểm tra âm thanh trong quá trình ghi âm.
    private Handler soundCheckHandler;

    // Runnable để thực hiện kiểm tra không gian lưu trữ.
    private Runnable spaceCheckRunnable;

    // Runnable để thực hiện kiểm tra âm thanh.
    private Runnable soundCheckRunnable;

    // Đối tượng Context để sử dụng trong lớp.
    private Context context;

    // Đối tượng RecordListener để gửi sự kiện ghi âm.
    private RecordListener listener;

    // Đường dẫn đến tệp âm thanh đang được ghi.
    private String outputFilePath;

    // Danh sách các tệp âm thanh đã được ghi.
    private List<File> recordedFiles;

    // Đối tượng MediaPlayer để phát các âm thanh thông báo.
    private MediaPlayer mediaPlayer;

    // URI của âm thanh thông báo thành công.
    private Uri successSoundUri;

    // URI của âm thanh thông báo thất bại.
    private Uri failureSoundUri;

    // Biến đánh dấu xem người dùng có đang nói trong quá trình ghi âm không.
    private boolean isSpeaking = false;

    // Đối tượng AudioRecord để thực hiện kiểm tra âm thanh.
    private AudioRecord audioRecord;

    /**
     * Thiết lập ngưỡng âm thanh.
     * @param threshold ngưỡng âm thanh mới.
     */
    public void setSoundThreshold(int threshold) {
        SOUND_THRESHOLD = threshold;
    }

    /**
     * Thiết lập dung lượng lưu trữ tối thiểu cần thiết.
     * @param minRequiredStorage dung lượng lưu trữ tối thiểu (đơn vị byte).
     */
    public void setMinRequiredStorage(int minRequiredStorage) {
        MIN_REQUIRED_STORAGE = minRequiredStorage;
    }

    /**
     * Thiết lập thời gian ghi âm tối đa.
     * @param maxRecordingDuration thời gian ghi âm tối đa (đơn vị millisecond).
     */
    public void setMaxRecordingDuration(int maxRecordingDuration) {
        MAX_RECORDING_DURATION = maxRecordingDuration;
    }

    /**
     * Thiết lập khoảng thời gian giữa các lần kiểm tra không gian lưu trữ.
     * @param spaceCheckInterval khoảng thời gian giữa các lần kiểm tra (đơn vị millisecond).
     */
    public void setSpaceCheckInterval(int spaceCheckInterval) {
        SPACE_CHECK_INTERVAL = spaceCheckInterval;
    }

    /**
     * Thiết lập khoảng thời gian giữa các lần kiểm tra âm thanh.
     * @param soundCheckInterval khoảng thời gian giữa các lần kiểm tra (đơn vị millisecond).
     */
    public void setSoundCheckInterval(int soundCheckInterval) {
        SOUND_CHECK_INTERVAL = soundCheckInterval;
    }

    /**
     * Constructor của lớp RecordLibrary.
     * @param context context của ứng dụng.
     * @param listener đối tượng lắng nghe sự kiện ghi âm.
     */
    public RecordLibrary(Context context, RecordListener listener) {
        this.context = context;
        this.listener = listener;
        this.recordedFiles = new ArrayList<>();
    }


    public void setSuccessSound(Uri soundUri) {
        this.successSoundUri = soundUri;
    }

    public void setFailureSound(Uri soundUri) {
        this.failureSoundUri = soundUri;
    }


    public void startRecording() {
        // Kiểm tra quyền ghi âm
        if (!RecordUtils.checkAudioPermission(context)) {
            listener.onPermissionRequired();
            return;
        }
        // Kiểm tra dung lượng lưu trữ
        if (!RecordUtils.getAvailableStorage(context, MIN_REQUIRED_STORAGE)) {
            listener.onInsufficientStorage();
            listener.onError("Insufficient storage ");
            return;
        }

        try {
            Toast.makeText(context, "Recording started", Toast.LENGTH_SHORT).show();
            outputFilePath = getTempFilePath();
            setupRecorder(outputFilePath);
            recorder.start();
            listener.onRecordingStarted();
            startSpaceCheck();
            startSoundCheck();
            scheduleStopRecording();
        } catch (IOException e) {
            listener.onError("Failed to start recording: " + e.getMessage());
        }
    }

    public void stopRecording(boolean isSuccess) {
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
            if (isSuccess && successSoundUri != null) {
                playSound(successSoundUri);
            } else if (!isSuccess && failureSoundUri != null) {
                playSound(failureSoundUri);
            }
            if (isSuccess) {
                recordedFiles.add(new File(outputFilePath));
            } else {
                // Xóa file ghi âm vì không thành công
                File file = new File(outputFilePath);
                if (file.exists()) {
                    file.delete();
                }
            }
            listener.onRecordingStopped(outputFilePath, isSuccess);
            Toast.makeText(context, "Recording stopped.", Toast.LENGTH_LONG).show();
            if (isSuccess) {
                Toast.makeText(context, "File Save at" + outputFilePath, Toast.LENGTH_SHORT).show();
            } else {
                listener.onError("Không nhân được âm thanh");
            }
        }
        stopSpaceCheck();
        stopStopRecordingHandler();
        stopSoundCheck();
    }


    private void setupRecorder(String filePath) throws IOException {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(filePath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setMaxDuration((int) MAX_RECORDING_DURATION);
        recorder.prepare();
    }

    private void startSpaceCheck() {
        spaceCheckHandler = new Handler();
        spaceCheckHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!RecordUtils.getAvailableStorage(context,MIN_REQUIRED_STORAGE)) {
                    // Dừng ghi âm nếu không đủ dung lượng lưu trữ
                    stopRecording(false);
                    listener.onInsufficientStorage();
                } else {
                    // Tiếp tục kiểm tra liên tục
                    spaceCheckHandler.postDelayed(this, SPACE_CHECK_INTERVAL);
                }
            }
        }, SPACE_CHECK_INTERVAL);
    }

    private void stopSpaceCheck() {
        if (spaceCheckHandler != null) {
            spaceCheckHandler.removeCallbacksAndMessages(null);
            spaceCheckHandler = null;
        }
    }

    private void scheduleStopRecording() {
        stopRecordingHandler = new Handler();
        stopRecordingHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopRecording(true);
            }
        }, MAX_RECORDING_DURATION);
    }

    private void stopStopRecordingHandler() {
        if (stopRecordingHandler != null) {
            stopRecordingHandler.removeCallbacksAndMessages(null);
        }
    }

    private void startSoundCheck() {
        soundCheckHandler = new Handler();
        soundCheckRunnable = new Runnable() {
            @Override
            public void run() {
                // Tạo và khởi tạo AudioRecord
                int bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling ActivityCompat#requestPermissions
                    return;
                }
                audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

                short[] buffer = new short[bufferSize];
                audioRecord.startRecording();

                boolean soundDetected = false;
                long startTime = System.currentTimeMillis();

                while (System.currentTimeMillis() - startTime < SOUND_CHECK_INTERVAL) { // 3 giây đầu tiên
                    int read = audioRecord.read(buffer, 0, bufferSize);
                    int amplitude = 0;
                    for (int i = 0; i < read; i++) {
                        amplitude += Math.abs(buffer[i]);
                    }
                    amplitude /= read;
                    int db = (int) (20 * Math.log10((double) Math.abs(amplitude)));// Chuyển đổi sang đơn vị dB
                    Log.d("SoundCheck", "Amplitude: " + amplitude + ", dB: " + db);

                    if (db > SOUND_THRESHOLD) {
                        soundDetected = true;
                        break;
                    }
                }

                audioRecord.stop();
                audioRecord.release();

                if (!soundDetected) {
                    stopRecording(false); // Dừng ghi âm nếu không phát hiện âm thanh

                    stopSoundCheck();
                } else {
                    isSpeaking = true;
                    listener.onSoundDetected(); // Có thể thêm phương thức thông báo phát hiện âm thanh nếu cần
                }
            }
        };
        soundCheckHandler.post(soundCheckRunnable);
    }



    private void stopSoundCheck() {
        if (soundCheckHandler != null) {
            soundCheckHandler.removeCallbacks(soundCheckRunnable);
            if (!isSpeaking) { // Nếu không phát hiện âm thanh
                if (recorder != null) {
                    recorder.stop(); // Dừng ghi âm
                    recorder.release();
                    recorder = null;
                    // Xóa file ghi âm vì không phát hiện âm thanh
                    File file = new File(outputFilePath);
                    if (file.exists()) {
                        file.delete();
                    }
                    listener.onRecordingStopped(outputFilePath, false); // Thông báo rằng ghi âm đã dừng và không lưu
                }
            }
        }
    }


    private void playSound(Uri soundUri) {
        mediaPlayer = MediaPlayer.create(context, soundUri);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
                mediaPlayer = null;
            }
        });
        mediaPlayer.start();
    }

    private String getTempFilePath() {
        File folder = context.getCacheDir();
        if (folder != null && !folder.exists()) {
            folder.mkdirs();
        }
        return new File(folder, "recording_" + System.currentTimeMillis() + ".mp3").getAbsolutePath();
    }


}
