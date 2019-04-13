package ir.alimahmoodvan.imageeditor;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.media.effect.Effect;
import android.media.effect.EffectContext;
import android.media.effect.EffectFactory;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.lang.reflect.Field;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class EffectActivity extends Activity {
    private static final String TAG = "PhotoerMainActivity";
    private static final String STATE_CURRENT_EFFECT = "current_effect";

    private GLSurfaceView mEffectView;
    private int[] mTextures = new int[2];
    private EffectContext mEffectContext;
    private Effect mEffect;
    private int mImageWidth;
    private int mImageHeight;
    private boolean mInitialized = false;
    private int mCurrentEffect;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_effect);
        String realPath= getIntent().getStringExtra("realPath");
        Log.d(TAG, "onCreate: "+realPath);
        //  PhotoEditorView mPhotoEditorView = findViewById(R.id.photoEditorView);
        //  mPhotoEditorView.getSource().setImageResource(R.drawable.got_s);
        java.lang.reflect.Field[] effects=EffectFactory.class.getDeclaredFields();
        LinearLayout ll=findViewById(R.id.ll);
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(0xFF00FF00); // Changes this drawbale to use a single color instead of a gradient
        gd.setCornerRadius(5);
        gd.setStroke(3, 0xFF000000);
//        ImageView effective=findViewById(R.id.img_target);
//        File imgFile = new File(realPath);
//
//        if(imgFile.exists()){
//            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
//            effective.setImageBitmap(myBitmap);
//        }
//
//        for (int i=0;i<effects.length;i++){
//            final java.lang.reflect.Field effect=effects[i];
//            if(effect.getName().contains("EFFECT")) {
//                Button btn = new Button(this);
//                btn.setWidth(240);
//                btn.setHeight(240);
//                btn.setText(effect.getName());
//                try{btn.setBackground(gd);}catch (Exception ex){Log.d(TAG, "onCreate: "+ex.getMessage());}
//                btn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
//                btn.setOnClickListener(new View.OnClickListener() {
//                    public void onClick(View v) {
//                        adjustEffect(v, effect);
//                    }
//                });
//                ll.addView(btn);
//            }
//        }


//        PhotoEditor mPhotoEditor = new PhotoEditor.Builder(this, mPhotoEditorView)
//                .setPinchTextScalable(true)
//                .build();
//        mPhotoEditor.setFilterEffect(PhotoFilter.BRIGHTNESS);
    }
    private void adjustEffect(View v, Field effect) {
        mEffectContext = EffectContext.createWithCurrentGlContext();
        mEffectView = (GLSurfaceView) findViewById(R.id.effectsview);
        mEffectView.setEGLContextClientVersion(2);
        mEffectView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {
                // Nothing to do here
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
//
            }

            @Override
            public void onDrawFrame(GL10 gl) {

            }
        });
        mEffectView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        EffectFactory effectFactory = mEffectContext.getFactory();
        if (mEffect != null) {
            mEffect.release();
        }
                mEffect = effectFactory.createEffect(EffectFactory.EFFECT_AUTOFIX);
                mEffect.setParameter("scale", 0.5f);
        }
    }
