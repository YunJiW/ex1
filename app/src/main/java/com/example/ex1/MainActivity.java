package com.example.ex1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Bitmap;


import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc; //cvtColor() ,equalizeHist
import org.opencv.imgcodecs.Imgcodecs; //imread() imwrite() 사용시 필요함

import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AndroidOpenCv";
    private static final int REQ_CODE_SELECT_IMAGE = 100;
    private Bitmap mInputImage;
    private Bitmap mOriginalImage;
    private ImageView mImageView;
    private ImageView mAfterView;
    private TextView pixel;
    private boolean mIsOpenCVReady = false;
    public static final String DATA ="_data";

    //이부분으로 안드로이드 메인에 포함됨
 public native double HSVdetectorJNI(long inputImage,long outputImage,int th1,int th2);


    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }


    //실행되는 함수
    public void detectHSVUsingJNI()
    {
        if(!mIsOpenCVReady){
            return;
        }
        Mat original_image; //색깔 검출할 이미지
        Mat src = new Mat();
        //이미지파일을 Mat으로 변환
        Utils.bitmapToMat(mInputImage,src);
        mImageView.setImageBitmap(mOriginalImage);
        int x  =src.cols();
        int y = src.rows();

        Mat out = new Mat();
        double k = HSVdetectorJNI(src.getNativeObjAddr(),out.getNativeObjAddr(),50,150);
        double Fat = Double.parseDouble(String.format("%.2f",k*100));
        pixel.setText("Fat content : "+String.valueOf(Fat)+ "%");

        
        //mat을 이미지 파일로 변환
        Utils.matToBitmap(out,mInputImage);
        mAfterView.setImageBitmap(mInputImage);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        mImageView =findViewById(R.id.origin_iv);
        mAfterView =findViewById(R.id.after);
        pixel = findViewById(R.id.pixel);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(!hasPermissions(PERMISSIONS)){
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }


    }
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(OpenCVLoader.initDebug()){
            mIsOpenCVReady = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    String path = getImagePathFromURI(data.getData());
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4;
                    mOriginalImage = BitmapFactory.decodeFile(path, options);
                    mInputImage = BitmapFactory.decodeFile(path, options);
                    if (mInputImage != null) {
                        //여기서 실행됨.
                        detectHSVUsingJNI();

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mInputImage.recycle();
        if(mInputImage != null){
            mInputImage = null;
        }
    }

    public void onButtonClicked(View view){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);

        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQ_CODE_SELECT_IMAGE);
    }

    static final int PERMISSIONS_REQUEST_CODE =100;
    String[] PERMISSIONS = {"android.permission.READ_EXTERNAL_STORAGE"};

    private boolean hasPermissions(String[] permissions)
    {
        int result;
        for(String perms : permissions)
        {
            result = ContextCompat.checkSelfPermission(this ,perms);
            if(result == PackageManager.PERMISSION_DENIED){
                return false;
            }
        }
        return true;
    }
    public String getImagePathFromURI(Uri contentUri){

        String[] proj ={MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri,proj,null,null,null);
        if(cursor == null){
            return contentUri.getPath();
        } else {
            int idx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String imgPath = cursor.getString(idx);
            cursor.close();
            return imgPath;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case PERMISSIONS_REQUEST_CODE:
            if(grantResults.length > 0){
                boolean cameraPermissionAccepted = grantResults[0] ==
                         PackageManager.PERMISSION_GRANTED;

                if(!cameraPermissionAccepted)
                    showDialogForPermission("실행을 위해 권한 허가가 필요합니다.");
            }
            break;
        }
    }
    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("알림");
        builder.setTitle(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestPermissions(PERMISSIONS,PERMISSIONS_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                finish();

            }
        });
        builder.create().show();


    }

}