package com.demo.tomcat.cameratest;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

//http://www.theappguruz.com/blog/android-take-photo-camera-gallery-code-sample

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
        int degree = 0;
        switch (requestCode)
        {
            case PHOTO:
            case CAMERA:
                if (data != null)
                {
                    Uri uri = data.getData();
                    //ContentResolver cr = this.getContentResolver();

                    String[] mMediaDATA = {MediaStore.Images.Media.DATA};
                    //Cursor mCursor = managedQuery(uri, mMediaDATA, null, null, null);
                    Cursor mCursor = this.getContentResolver().query(uri, mMediaDATA, null, null, null);
                    Log.d(TAG, "uri: " + uri + ", mCursor: " + mCursor + ", counts: " + mCursor.getCount());
                    //for (int i=0; i<mCursor.getCount(); i++)
                    //{
                    //    Log.d(TAG, "mCursor[" + i + "]: " + mCursor.getString(i));
                    //}
                    mCursor.moveToFirst();

                    int mImageIndex = mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    //mCursor.moveToFirst();
                    String mPath = mCursor.getString(mImageIndex);
                    mCursor.close();
                    //Log.d(TAG, "mImageIndex: " + mImageIndex + ", mPath: " + mPath);

                    if ((mPath != null) && (!mPath.equalsIgnoreCase("")))
                    {
                        File mFile = new File(mPath);
                        degree = getImageDegree(mPath);
                        Log.d(TAG, "degree: " + degree);
                    }
                    else
                    {
                        mPath = getImageFilePath(uri);
                        degree = getPictureDegree(uri);
                        if (degree <= 0)
                        {
                            degree = getImageDegree(mPath);
                            Log.d(TAG, "2 degree: " + degree);
                        }
                    }
                    Log.d(TAG, "mImageIndex: " + mImageIndex + ", mPath: " + mPath);

                    try
                    {
                        //Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                        Bitmap bitmap = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(uri));
                        if (bitmap.getWidth() > bitmap.getHeight())
                        {
                            bitmap = ScalePic(bitmap, mPhone.heightPixels, degree);
                        }

                        mImg.setImageBitmap(bitmap);
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

    private Bitmap ScalePic(Bitmap bitmap, int phone, int degree)
    {
        float mScale = 1;

        if (bitmap.getWidth() > phone)
        {
            mScale = (float) phone / (float)bitmap.getWidth();

            Matrix mMat = new Matrix();
            mMat.setScale(mScale, mScale);
            mMat.postRotate((float) degree);

            Bitmap mScaleBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), mMat, false);
            return mScaleBitmap;
            //mImg.setImageBitmap(mScaleBitmap);
        }
        else
        {
            //mImg.setImageBitmap(bitmap);
            return bitmap;
        }
    }

    private void ScalePicOld(Bitmap bitmap, int phone, int degree)
    {
        float mScale = 1;

        if (bitmap.getWidth() > phone)
        {
            mScale = (float) phone / (float)bitmap.getWidth();

            Matrix mMat = new Matrix();
            mMat.setScale(mScale, mScale);
            mMat.postRotate((float) degree);

            Bitmap mScaleBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), mMat, false);
            mImg.setImageBitmap(mScaleBitmap);
        }
        else
            mImg.setImageBitmap(bitmap);
    }

    private String getImageFilePath(Uri uri)
    {
        // Will return "image:x*"
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        column, sel, new String[]{ id }, null);

        String filePath = "";

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst())
        {
            filePath = cursor.getString(columnIndex);
        }

        cursor.close();

        return filePath;
    }

    private int getPictureDegree(Uri uri)
    {
        String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
        Cursor cursor = getContentResolver().query(uri, orientationColumn, null, null, null);
        int orientation = -1;
        if (cursor != null && cursor.moveToFirst())
        {
            orientation = cursor.getInt(cursor.getColumnIndex(orientationColumn[0]));
            Log.d(TAG, "Image Orientation: " + orientation);
        }

        return orientation;
    }


    private int getImageDegree(String photoPath)
    {
        int degree = 0;
        ExifInterface ei = null;
        try
        {
            ei = new ExifInterface(photoPath);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        switch (orientation)
        {

            case ExifInterface.ORIENTATION_ROTATE_90:
                //rotateImage(bitmap, 90);
                degree = 90;
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                //rotateImage(bitmap, 180);
                degree = 180;
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                //rotateImage(bitmap, 270);
                degree = 270;
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                break;
        }

        return degree ;
    }



}
