package ir.alimahmoodvan.imageeditor;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.GradientDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.media.effect.Effect;
import android.media.effect.EffectContext;
import android.media.effect.EffectFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
public class EffectsFilterActivity extends Activity implements GLSurfaceView.Renderer {
    private static final String TAG = "EffectsFilterActivity";
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
    private int effectViewHeight;
    int llHeight;
    int btnHeight;
    int textViewSize=10;
    int marginBtn=1;
    private Bitmap mainBitmap;
    int saveClicked=0;
    private int shareClicked=0;
    private int maxSize;

    public void setCurrentEffect(String effect) {
        mCurrentEffect = effect;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_filter);
        try {
            responsive();
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            final LinearLayout ll = findViewById(R.id.ll);
            ViewGroup.LayoutParams layoutParams=ll.getLayoutParams();
            layoutParams.height=llHeight;
            ll.setLayoutParams(layoutParams);
            realPath = getIntent().getStringExtra("realPath");
            mEffectView = (GLSurfaceView) findViewById(R.id.effectsview);
             layoutParams=mEffectView.getLayoutParams();
            layoutParams.height=effectViewHeight;
            //mEffectView.setLayoutParams(layoutParams);
            mEffectView.setEGLContextClientVersion(2);
            mEffectView.setRenderer(this);
            mEffectView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            mCurrentEffect = "";
            java.lang.reflect.Field[] effects = EffectFactory.class.getDeclaredFields();
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(0xFF00FF00);
            gd.setStroke(marginBtn, 0xFF000000);

            ImageButton btn_save=findViewById(R.id.btn_save);
            btn_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveClicked=1;
//                    setCurrentEffect(mCurrentEffect);
                    mEffectView.requestRender();
                    Toast toast= Toast.makeText(getApplicationContext(),getResources().getString(R.string.save_message),Toast.LENGTH_SHORT);
                    View view = toast.getView();
                    TextView text = view.findViewById(android.R.id.message);
                    text.setTextColor(getResources().getColor(android.R.color.holo_blue_bright));
                    toast.show();
                }
            });
            ImageButton btn_share=findViewById(R.id.btn_share);
            btn_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareClicked=1;
//                    setCurrentEffect(mCurrentEffect);
//                    mEffectView.requestRender();
//                    Intent intent = new Intent(Intent.ACTION_SEND);
//                    String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), mainBitmap,"title", null);
//                    Uri bitmapUri = Uri.parse(bitmapPath);
//                    intent.putExtra(Intent.EXTRA_STREAM, bitmapUri );
//                    intent.setType("image/*");
//
//                    startActivity(Intent.createChooser(intent , "Share"));
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("image/*");

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "title");
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            values);
                    OutputStream outstream;
                    try {
                        outstream = getContentResolver().openOutputStream(uri);
                        mainBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
                        outstream.close();
                    } catch (Exception e) {
                        System.err.println(e.toString());
                    }
                    share.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_message));
                    share.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(Intent.createChooser(share, "Share Image"));
                }
            });

            
            List<String> effectFa= Arrays.asList("تعمیر خودکار",
                    "لایه",
                    "سیاه سفید",
                    "روشنایی",
                    "تضاد",
                    "زراعت",
                    "ادغام",
                    "مستند",
                    "دوتونه",
                    "پر کردن",
                    "ماهیگیر",
                    "FLIP",
                    "غلات",
                    "گریسکل",
                    "هویت",
                    "لیموش",
                    "منفی",
                    "بسته ها",
                    "بستن",
                    "چرخاندن",
                    "انعطاف پذیری",
                    "سپتامبر",
                    "شفاف",
                    "استراحت",
                    "درجه حرارت",
                    "رنگ کردن",
                    "ویگنته"
                    
            );
                    List<String> effectEn= Arrays.asList("AUTOFIX"
                    ,"BITMAPOVERLAY"
                    ,"BLACKWHITE"
                    ,"BRIGHTNESS"
                    ,"CONTRAST"
                    ,"CROP"
                    ,"CROSS PROCESS"
                    ,"DOCUMENTARY"
                    ,"DUOTONE"
                    ,"FILLLIGHT"
                    ,"FISHEYE"
                    ,"FLIP"
                    ,"GRAIN"
                    ,"GRAYSCALE"
                    ,"IDENTITY"
                    ,"LOMOISH"
                    ,"NEGATIVE"
                    ,"PACKAGES"
                    ,"POSTERIZE"
                    ,"ROTATE"
                    ,"SATURATE"
                    ,"SEPIA"
                    ,"SHARPEN"
                    ,"STRAIGHTEN"
                    ,"TEMPERATURE"
                    ,"TINT"
                    ,"VIGNETTE");
            for (int i = 0; i < effects.length; i++) {
                final java.lang.reflect.Field effect = effects[i];
                int index=effectEn.indexOf(effect.getName().replace("EFFECT_", ""));
                if (effect.getName().contains("EFFECT")&&index!=-1) {
                    LinearLayout effectll = new LinearLayout(this);
                    effectll.setOrientation(LinearLayout.VERTICAL);
                    effectll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));


                    final ImageButton btn = new ImageButton(this);
                    ViewGroup.MarginLayoutParams marginLayoutParams=(ViewGroup.MarginLayoutParams)new LinearLayout.LayoutParams(btnHeight, btnHeight);
                    marginLayoutParams.setMargins(marginBtn,marginBtn,marginBtn,marginBtn);
                    btn.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    btn.setLayoutParams(marginLayoutParams);

                    btn.setImageBitmap(getBitmapFromURL("https://images.ganeshaspeaks.com/GS-V4/images/womenDay/thumb200/capricorn-women.jpg"));
                    final TextView effectName = new TextView(this);
//                    effectName.setText(effect.getName().replace("EFFECT_", ""));
                    effectName.setText(effectFa.get(index));
                    effectName.setTextSize(TypedValue.COMPLEX_UNIT_SP, textViewSize);
                    effectName.setGravity(Gravity.CENTER);
//                btn.setWidth(240);setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
//                btn.setHeight(240);
//                btn.setText(effect.getName().replace("EFFECT_",""));
//                    try {
//                        btn.setBackground(gd);
//                    } catch (Exception ex) {
//                        Log.d(TAG, "onCreate: " + ex.getMessage());
//                    }
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
                            } catch (Exception ex) {

                            }
                        }
                    });
                    effectll.addView(effectName);
                    effectll.addView(btn);
                    ll.addView(effectll);
                }
            }
            Log.d(TAG, "onCreate: ");
        } catch (Exception e) {
            Log.d(TAG, "onCreate: " + e.getMessage());
        }
    }
    @Override
    public void onBackPressed() {
        finish();
    }
    private void responsive() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        int realWidth;
        int realHeight;

        if (Build.VERSION.SDK_INT >= 17){
            //new pleasant way to get real metrics
            DisplayMetrics realMetrics = new DisplayMetrics();
            display.getRealMetrics(realMetrics);
            realWidth = realMetrics.widthPixels;
            realHeight = realMetrics.heightPixels;

        } else if (Build.VERSION.SDK_INT >= 14) {
            //reflection for this weird in-between time
            try {
                Method mGetRawH = Display.class.getMethod("getRawHeight");
                Method mGetRawW = Display.class.getMethod("getRawWidth");
                realWidth = (Integer) mGetRawW.invoke(display);
                realHeight = (Integer) mGetRawH.invoke(display);
            } catch (Exception e) {
                //this may not be 100% accurate, but it's all we've got
                realWidth = display.getWidth();
                realHeight = display.getHeight();
                Log.e("Display Info", "Couldn't use reflection to get the real display metrics.");
            }

        } else {
            //This should be close, as lower API devices should not have window navigation bars
            realWidth = display.getWidth();
            realHeight = display.getHeight();
        }
        effectViewHeight =(int)( realHeight * 0.7);
        llHeight =(int)( realHeight * 0.2);
        btnHeight=llHeight-(3*textViewSize)-(2*marginBtn)-(2*marginBtn);
        maxSize=realHeight<realWidth?realHeight:realWidth;
//        Log.d(TAG, "responsive: "+realHeight);
//        Log.d(TAG, "responsive: "+effectViewHeight);
//        Log.d(TAG, "responsive: "+llHeight);
//        Log.d(TAG, "responsive: "+btnHeight);
    }

    public Bitmap getBitmapFromURL(String src) {
        try {
//            java.net.URL url = new java.net.URL(src);
//            HttpURLConnection connection = (HttpURLConnection) url
//                    .openConnection();
//            connection.setDoInput(true);
//            connection.connect();
//            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.min3);
//            Log.d(TAG, "responsive: "+btnHeight);
//            Log.d(TAG, "responsive: "+myBitmap.getHeight());
//            Log.d(TAG, "responsive: "+myBitmap.getWidth());

            return myBitmap;
        }/* catch (IOException e) {
            Log.d(TAG, "getBitmapFromURL: "+e.getMessage());
            return null;
        }*/catch (Exception e) {
            Log.d(TAG, "getBitmapFromURL: "+e.getMessage());
            return null;
        }
    }
    public Bitmap getResizedBitmap(File imageFile) {
        Bitmap image = BitmapFactory.decodeFile(imageFile.getPath());
        int imageRotation = getImageRotation(imageFile);
//        Bitmap image=getRotateImage(imageFile);
        int width = image.getWidth();
        int height = image.getHeight();
//        maxSize=effectViewHeight;
        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        if (imageRotation != 0)
            return getBitmapRotatedByDegree(Bitmap.createScaledBitmap(image, width, height, true), imageRotation);
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
    private void loadTextures() {
        // Generate textures
        try {
            GLES20.glGenTextures(2, mTextures, 0);
            // Load input bitmap
            Log.d(TAG, "loadTextures: " + realPath);
            File imgFile = new File(realPath);
            mainBitmap =getResizedBitmap(imgFile);
            mImageWidth = mainBitmap.getWidth()/2;
            mImageHeight = mainBitmap.getHeight()/2;

//            ViewGroup.LayoutParams layoutParams=mEffectView.getLayoutParams();
//            layoutParams.height=mImageHeight;
//            mEffectView.setLayoutParams(layoutParams);

            mTexRenderer.updateTextureSize(mImageWidth, mImageHeight);
            // Upload to texture
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mainBitmap, 0);
            // Set texture parameters
            GLToolbox.initTexParams();
        }catch (Exception e){
            Log.d(TAG, "loadTextures: "+e.getMessage());
        }
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
        if (saveFrame/*&&saveClicked==1*/) {
            saveClicked=0;
            saveBitmap(takeScreenshot(gl));
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
        String fname =mCurrentEffect+ ".jpg";
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
        final int iWidth = mainBitmap.getWidth();
        final int iHeight = mainBitmap.getHeight();
        final int mWidth = mEffectView.getWidth();
        final int mHeight = mEffectView.getHeight();
        int bWidth =(mWidth-iWidth)/2;
        int bHeight = (mHeight-iHeight)/2;
//        int wminus=bWidth<0?bWidth:0;
//        int hminus=bHeight<0?bHeight:0;
        Log.d(TAG, "takeScreenshot: "+mWidth+":::"+mHeight);
        Log.d(TAG, "takeScreenshot: "+iWidth+":::"+iHeight);
        Log.d(TAG, "takeScreenshot: "+bWidth+":::"+bHeight);
        IntBuffer ib = IntBuffer.allocate(iWidth * iHeight);
        IntBuffer ibt = IntBuffer.allocate(iWidth * iHeight);
        mGL.glReadPixels(bWidth, bHeight, iWidth, iHeight, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);
        // Convert upside down mirror-reversed image to right-side up normal
        // image.
        for (int i = 0; i < iHeight; i++) {
            for (int j = 0; j < iWidth; j++) {
                ibt.put((iHeight - i - 1) * iWidth + j, ib.get(i * iWidth + j));
            }
        }
        Bitmap mBitmap = Bitmap.createBitmap(iWidth, iHeight, Bitmap.Config.ARGB_8888);
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
    public static Bitmap getRotateImage(final File imageFile) {
        Bitmap photoBitmap = BitmapFactory.decodeFile(imageFile.getPath());
        int imageRotation = getImageRotation(imageFile);
        if (imageRotation != 0)
            photoBitmap = getBitmapRotatedByDegree(photoBitmap, imageRotation);
        return photoBitmap;
    }
    private static int getImageRotation(final File imageFile) {

        ExifInterface exif = null;
        int exifRotation = 0;

        try {
            exif = new ExifInterface(imageFile.getPath());
            exifRotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (exif == null)
            return 0;
        else
            return exifToDegrees(exifRotation);
    }

    private static int exifToDegrees(int rotation) {
        if (rotation == ExifInterface.ORIENTATION_ROTATE_90)
            return 90;
        else if (rotation == ExifInterface.ORIENTATION_ROTATE_180)
            return 180;
        else if (rotation == ExifInterface.ORIENTATION_ROTATE_270)
            return 270;

        return 0;
    }

    private static Bitmap getBitmapRotatedByDegree(Bitmap bitmap, int rotationDegree) {
        Matrix matrix = new Matrix();
        matrix.preRotate(rotationDegree);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}

