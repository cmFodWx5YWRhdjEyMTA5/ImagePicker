package com.example.rajeev.imagepicker;
import android.Manifest;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
public class MainActivity extends AppCompatActivity {
    final static int PICK_IMAGE_MULTIPLE = 11;
    private static final int PICK_VIDEOS_MULTIPLE = 10;
    private static boolean isImagePick = true;
    final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102;
    Button pick_images, pick_videos;
    private List<String> imagePathList;

    public static String getPath(final Context context, final Uri uri) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pick_images = findViewById(R.id.pick_images);
        pick_videos = findViewById(R.id.pick_videos);
        pick_images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isImagePick = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                    } else {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE);
                    }
                } else {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE);
                }
            }
        });
        pick_videos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isImagePick = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.setType("video/mp4");
                        startActivityForResult(intent, PICK_VIDEOS_MULTIPLE);
                    }
                } else {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setType("video/mp4");
                    startActivityForResult(intent, PICK_VIDEOS_MULTIPLE);
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK && null != data) {
                imagePathList = new ArrayList<>();
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        getImageFilePath(imageUri);
                    }
                } else if (data.getData() != null) {
                    Uri imgUri = data.getData();
                    getImageFilePath(imgUri);
                }
                Log.v("LOG_TAG", "Selected Images : " + imagePathList.size() + " " + imagePathList);
                Toast.makeText(this, "Selected Images : " + imagePathList.size() + " \n" + imagePathList, Toast.LENGTH_SHORT).show();
            } else if (requestCode == PICK_VIDEOS_MULTIPLE && resultCode == RESULT_OK && null != data) {
                List<String> selectedVideos = getSelectedVideos(data);
                Toast.makeText(this, "Selected Videos : " + selectedVideos.size() + " \n" + selectedVideos, Toast.LENGTH_SHORT).show();
                Log.v("LOG_TAG", "Selected Videos : " + selectedVideos.size() + " " + selectedVideos);
            }
            super.onActivityResult(requestCode, resultCode, data);
        } catch (Exception ignored) {
        }
    }

    private List<String> getSelectedVideos(Intent data) {
        List<String> result = new ArrayList<>();
        ClipData clipData = data.getClipData();
        if (clipData != null) {
            for (int i = 0; i < clipData.getItemCount(); i++) {
                ClipData.Item videoItem = clipData.getItemAt(i);
                Uri videoURI = videoItem.getUri();
                String filePath = getPath(this, videoURI);
                result.add(filePath);
            }
        } else {
            Uri videoURI = data.getData();
            String filePath = getPath(this, videoURI);
            result.add(filePath);
        }
        return result;
    }

    public void getImageFilePath(Uri uri) {
        File file = new File(uri.getPath());
        String[] filePath = file.getPath().split(":");
        String image_id = filePath[filePath.length - 1];
        Cursor cursor = getContentResolver().query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", new String[]{image_id}, null);
        if (cursor != null) {
            cursor.moveToFirst();
            String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            imagePathList.add(imagePath);
            cursor.close();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean canUseExternalStorage = false;
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                try {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        canUseExternalStorage = true;
                    }
                    if (!canUseExternalStorage) {
                        final android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(MainActivity.this);
                        alertDialogBuilder.setTitle("Permissions Required")
                                .setMessage("Please enable storage  Permission for pick up images/videos.")
                                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            if (dialog != null) {
                                                dialog.dismiss();
                                            }
                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                    Uri.fromParts("package", MainActivity.this.getPackageName(), null));
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        } catch (Exception ignored) {
                                        }
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (dialog != null) {
                                            dialog.dismiss();
                                        }
                                    }
                                })
                                .setCancelable(false)
                                .create()
                                .show();
                    } else {
                        try {
                            if (isImagePick) {
                                Intent intent = new Intent();
                                intent.setType("image/*");
                                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE);
                            } else {
                                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                intent.setType("video/mp4");
                                startActivityForResult(intent, PICK_VIDEOS_MULTIPLE);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }
}
