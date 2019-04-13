package ir.alimahmoodvan.imageeditor;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.media.effect.Effect;
import android.media.effect.EffectContext;
import android.media.effect.EffectFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Modifier;
import java.nio.IntBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
public class EffectsFilterActivity extends Activity implements GLSurfaceView.Renderer {
    private static final String TAG = "ImageEffectsFilterActivity";
    private GLSurfaceView mEffectView;
    private int[] mTextures = new int[2];
    private EffectContext mEffectContext;
    private Effect mEffect;
    private TextureRenderer mTexRenderer = new TextureRenderer();
    private int mImageWidth;
    private int mImageHeight;
    private boolean mInitialized = false;
    String mCurrentEffect;
    String realPath="";
    private volatile boolean saveFrame;

    public void setCurrentEffect(String effect) {
        mCurrentEffect = effect;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_filter);
        /**
         * Initialise the renderer and tell it to only render when
         * Explicit requested with the RENDERMODE_WHEN_DIRTY option
         */
        realPath= getIntent().getStringExtra("realPath");
        mEffectView = (GLSurfaceView) findViewById(R.id.effectsview);
        mEffectView.setEGLContextClientVersion(2);
        mEffectView.setRenderer(this);
        mEffectView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mCurrentEffect = "";
        java.lang.reflect.Field[] effects=EffectFactory.class.getDeclaredFields();
        LinearLayout ll=findViewById(R.id.ll);
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(0xFF00FF00); // Changes this drawbale to use a single color instead of a gradient
        gd.setCornerRadius(5);
        gd.setStroke(3, 0xFF000000);

        for (int i=0;i<effects.length;i++){
            final java.lang.reflect.Field effect=effects[i];
            if(effect.getName().contains("EFFECT")) {
                Button btn = new Button(this);
                btn.setWidth(240);
                btn.setHeight(240);
                btn.setText(effect.getName());
                try{btn.setBackground(gd);}catch (Exception ex){Log.d(TAG, "onCreate: "+ex.getMessage());}
                btn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                btn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        try {
                            effect.setAccessible(true);
                            Class<?> targetType = effect.getType();
                            Object objectValue = targetType.newInstance();
                            Object value = effect.get(objectValue);
                            setCurrentEffect((String) (value));
                            mEffectView.requestRender();
                        }catch (Exception ex){

                        }
                    }
                });
                ll.addView(btn);
            }
        }
    }
    private void loadTextures() {
        // Generate textures
        GLES20.glGenTextures(2, mTextures, 0);
        // Load input bitmap
        Log.d(TAG, "loadTextures: "+realPath);
        File imgFile = new File(realPath);
        Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        mImageWidth = bitmap.getWidth();
        mImageHeight = bitmap.getHeight();
        mTexRenderer.updateTextureSize(mImageWidth, mImageHeight);
        // Upload to texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        // Set texture parameters
        GLToolbox.initTexParams();
    }
    private void initEffect() {
        EffectFactory effectFactory = mEffectContext.getFactory();
        Log.d(TAG, "initEffect: "+mCurrentEffect+":"+EffectFactory.EFFECT_AUTOFIX+":"+(EffectFactory.EFFECT_AUTOFIX==mCurrentEffect));
        if (mEffect != null) {
            mEffect.release();
        }
        /**
         * Initialize the correct effect based on the selected menu/action item
         */
        switch (mCurrentEffect) {
            case "":
                break;
            case EffectFactory.EFFECT_AUTOFIX:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_AUTOFIX);
                mEffect.setParameter("scale", 0.5f);
                break;
            case EffectFactory.EFFECT_BLACKWHITE:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_BLACKWHITE);
                mEffect.setParameter("black", .1f);
                mEffect.setParameter("white", .7f);
                break;
            case EffectFactory.EFFECT_BRIGHTNESS:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_BRIGHTNESS);
                mEffect.setParameter("brightness", 2.0f);
                break;
            case EffectFactory.EFFECT_CONTRAST:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_CONTRAST);
                mEffect.setParameter("contrast", 1.4f);
                break;
            case  EffectFactory.EFFECT_CROSSPROCESS:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_CROSSPROCESS);
                break;
            case EffectFactory.EFFECT_DOCUMENTARY:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_DOCUMENTARY);
                break;
            case EffectFactory.EFFECT_DUOTONE:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_DUOTONE);
                mEffect.setParameter("first_color", Color.YELLOW);
                mEffect.setParameter("second_color", Color.DKGRAY);
                break;
            case EffectFactory.EFFECT_FILLLIGHT:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_FILLLIGHT);
                mEffect.setParameter("strength", .8f);
                break;
            case  EffectFactory.EFFECT_FISHEYE:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_FISHEYE);
                mEffect.setParameter("scale", .5f);
                break;
            case EffectFactory.EFFECT_FLIP:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_FLIP);
                mEffect.setParameter("vertical", true);
                break;
            case EffectFactory.EFFECT_GRAIN:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_GRAIN);
                mEffect.setParameter("strength", 1.0f);
                break;
            case  EffectFactory.EFFECT_GRAYSCALE:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_GRAYSCALE);
                break;
            case EffectFactory.EFFECT_LOMOISH:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_LOMOISH);
                break;
            case EffectFactory.EFFECT_NEGATIVE:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_NEGATIVE);
                break;
            case EffectFactory.EFFECT_POSTERIZE:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_POSTERIZE);
                break;
            case EffectFactory.EFFECT_ROTATE:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_ROTATE);
                mEffect.setParameter("angle", 180);
                break;
            case EffectFactory.EFFECT_SATURATE:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_SATURATE);
                mEffect.setParameter("scale", .5f);
                break;
            case EffectFactory.EFFECT_SEPIA:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_SEPIA);
                break;
            case EffectFactory.EFFECT_SHARPEN:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_SHARPEN);
                break;
            case EffectFactory.EFFECT_TEMPERATURE:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_TEMPERATURE);
                mEffect.setParameter("scale", .9f);
                break;
            case EffectFactory.EFFECT_TINT:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_TINT);
                mEffect.setParameter("tint", Color.MAGENTA);
                break;
            case EffectFactory.EFFECT_VIGNETTE:
                mEffect = effectFactory.createEffect(
                        EffectFactory.EFFECT_VIGNETTE);
                mEffect.setParameter("scale", .5f);
                break;
            default:
                mEffect = effectFactory.createEffect(mCurrentEffect);
                break;
        }
    }
    private void applyEffect() {
        Log.d(TAG, "applyEffect: "+mEffect);
        mEffect.apply(mTextures[0], mImageWidth, mImageHeight, mTextures[1]);
    }
    private void renderResult() {
        if (!mCurrentEffect.isEmpty()) {
            // if no effect is chosen, just render the original bitmap
            mTexRenderer.renderTexture(mTextures[1]);
        }
        else {
            saveFrame=true;
            // render the result of applyEffect()
            mTexRenderer.renderTexture(mTextures[0]);
        }
    }
    @Override
    public void onDrawFrame(GL10 gl) {
        Log.d(TAG, "onDrawFrame: ");
        if (!mInitialized) {
            //Only need to do this once
            mEffectContext = EffectContext.createWithCurrentGlContext();
            mTexRenderer.init();
            loadTextures();
            mInitialized = true;
        }
        if (!mCurrentEffect.isEmpty()) {
            //if an effect is chosen initialize it and apply it to the texture
            initEffect();
            applyEffect();
        }
        renderResult();
        if (saveFrame) {
//            saveBitmap(takeScreenshot(gl));
        }
    }
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (mTexRenderer != null) {
            mTexRenderer.updateViewSize(width, height);
        }
    }
    private void saveBitmap(Bitmap bitmap) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-"+ n +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            Log.i("TAG", "Image SAVED=========="+file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public Bitmap takeScreenshot(GL10 mGL) {
        final int mWidth = mEffectView.getWidth();
        final int mHeight = mEffectView.getHeight();
        IntBuffer ib = IntBuffer.allocate(mWidth * mHeight);
        IntBuffer ibt = IntBuffer.allocate(mWidth * mHeight);
        mGL.glReadPixels(0, 0, mWidth, mHeight, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);
        // Convert upside down mirror-reversed image to right-side up normal
        // image.
        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                ibt.put((mHeight - i - 1) * mWidth + j, ib.get(i * mWidth + j));
            }
        }
        Bitmap mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        mBitmap.copyPixelsFromBuffer(ibt);
        return mBitmap;
    }
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main, menu);
//        return true;
//    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        setCurrentEffect(item.getItemId());
//        mEffectView.requestRender();
        return true;
    }
}

