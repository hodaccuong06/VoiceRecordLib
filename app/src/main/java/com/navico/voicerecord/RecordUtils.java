package com.navico.voicerecord;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.io.File;

/**
 * Lớp RecordUtils chứa các phương thức tiện ích liên quan đến ghi âm.
 */
public class RecordUtils {
    /**
     * Kiểm tra quyền ghi âm được cấp hay chưa.
     * @param context Context của ứng dụng.
     * @return true nếu quyền ghi âm được cấp, ngược lại là false.
     */
    public static boolean checkAudioPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Kiểm tra dung lượng lưu trữ khả dụng.
     * @param context Context của ứng dụng.
     * @param requiredSpace Dung lượng lưu trữ cần thiết (đơn vị byte).
     * @return true nếu dung lượng lưu trữ khả dụng đủ, ngược lại là false.
     */
    public static boolean getAvailableStorage(Context context, long requiredSpace) {
        long availableStorage = getAvailableMemory(context);
        Log.d("RecordUtils", "Available storage: " + availableStorage + " bytes");
        return availableStorage >= requiredSpace;
    }

    /**
     * Lấy dung lượng lưu trữ khả dụng trên thiết bị.
     * @param context Context của ứng dụng.
     * @return Dung lượng lưu trữ khả dụng (đơn vị byte).
     */
    private static long getAvailableMemory(Context context) {
        File path = context.getCacheDir();
        StatFs stat = new StatFs(path.getAbsolutePath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return availableBlocks * blockSize;
    }
}
