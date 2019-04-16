package ir.alimahmoodvan.imageeditor;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import co.ronash.pushe.Pushe;

public class MainActivity extends Activity {
    private static final String TAG = "PhotoerImageActivity";
    int galleryRequest=1;
    int cameraRequest=1;
    private String realPath="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Pushe.initialize(this,true);
        setContentView(R.layout.activity_main);
        ImageButton btn_gallery=findViewById(R.id.btn_gallery);
        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, galleryRequest);
            }
        });
        ImageButton btn_camera=findViewById(R.id.btn_camera);
        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = timeStamp + ".jpg";
                File storageDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);
                realPath = storageDir.getAbsolutePath() + "/" + imageFileName;
                File file = new File(realPath);
                Uri outputFileUri = Uri.fromFile(file);
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                startActivityForResult(cameraIntent, cameraRequest);
            }
        });
    }
    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (galleryRequest == reqCode) {
                try {
                    realPath = ImageFilePath.getPath(MainActivity.this, data.getData());
                    Log.d(TAG, "onActivityResult: " + realPath);
                    Intent intent = new Intent(getBaseContext(), EffectsFilterActivity.class);
                    intent.putExtra("realPath", realPath);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.d(TAG, "onActivityResult: " + e.getMessage());
                }
            }
            if (cameraRequest == reqCode) {
                try {
                    File imgFile = new  File(realPath);
                    Log.d(TAG, "onActivityResult: "+realPath);
                    if(imgFile.exists()) {
                        Intent intent = new Intent(getBaseContext(), EffectsFilterActivity.class);
                        intent.putExtra("realPath", realPath);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "onActivityResult: " + e.getMessage());
                }
            }
        } else {
        }
    }
}
