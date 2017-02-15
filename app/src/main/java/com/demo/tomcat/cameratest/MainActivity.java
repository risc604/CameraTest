package com.demo.tomcat.cameratest;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;


public class MainActivity extends AppCompatActivity
{
    private static final String TAG = MainActivity.class.getSimpleName();

    private ImageView   mImg;
    private Button      mCamera, mPhoto;
    private DisplayMetrics mPhone;
    private final static int CAMERA = 66;
    private final static int PHOTO = 99;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intiView();
        initControl();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {

            case PHOTO:
            case CAMERA:
                if (data != null)
                {
                    Uri uri = data.getData();
                    ContentResolver cr = this.getContentResolver();

                    String[] mMediaDATA = {MediaStore.Images.Media.DATA};
                    Cursor mCursor = managedQuery(uri, mMediaDATA, null, null, null);
                    int mImageIndex = mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    mCursor.moveToFirst();
                    String mPath = mCursor.getString(mImageIndex);
                    File mFile = new File(mPath);
                    Log.d(TAG, "mPath: " + mPath);


                    try
                    {
                        Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                        if (bitmap.getWidth() > bitmap.getHeight())
                            ScalePic(bitmap, mPhone.heightPixels);
                    }
                    catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }


                }
                break;

            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    //--- user define code.
    private void intiView()
    {
        mImg = (ImageView)findViewById(R.id.img);
        mCamera = (Button)findViewById(R.id.camera);
        mPhoto = (Button)findViewById(R.id.photo);
    }

    private void initControl()
    {
        mPhone = new  DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mPhone);

        mCamera.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ContentValues value = new ContentValues();
                value.put(MediaStore.Audio.Media.MIME_TYPE, "image/png");
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                                        value);
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                startActivityForResult(intent, CAMERA);
            }
        });

        mPhoto.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PHOTO);
            }
        });

    }

    private void ScalePic(Bitmap bitmap, int phone)
    {
        float mScale = 1;

        if (bitmap.getWidth() > phone)
        {
            mScale = (float) phone / (float)bitmap.getWidth();

            Matrix mMat = new Matrix();
            mMat.setScale(mScale, mScale);
            mMat.postRotate(90.0f);

            Bitmap mScaleBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), mMat, false);
            mImg.setImageBitmap(mScaleBitmap);
        }
        else
            mImg.setImageBitmap(bitmap);
    }


}