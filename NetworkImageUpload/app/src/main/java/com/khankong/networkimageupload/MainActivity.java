package com.khankong.networkimageupload;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    final static String TAG = "MainActivity";
    ImageView imageView = null;
    Button button = null;
    private final int REQ_CODE_SELECT_IMAGE = 300; // Gallery Return Code
    private String img_path = null; // 최종 file name
    private String f_ext = null;    // 최종 file extension
    File tempSelectFile;

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //          Tomcat Server의 IP Address와 Package이름은 수정 하여야 함
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    String devicePath = Environment.getDataDirectory().getAbsolutePath() + "/data/com.khankong.networkimageupload/";
    String urlAddr = "http://192.168.219.101:8080/jsp/multipartRequest.jsp";
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //
        //          사용자에게 사진(Media) 사용 권한 받기
        //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MODE_PRIVATE);
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        imageView = findViewById(R.id.iv);
        button = findViewById(R.id.upload_btn);

        imageView.setOnClickListener(mClickListener);
        button.setOnClickListener(mClickListener);
    }

    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                //////////////////////////////////////////////////////////////////////////////////////////////
                //
                //          Photo App.으로 이동
                //
                //////////////////////////////////////////////////////////////////////////////////////////////
                case R.id.iv:
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                    intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, REQ_CODE_SELECT_IMAGE);
                    break;
                //////////////////////////////////////////////////////////////////////////////////////////////
                //
                //           Upload
                //
                //////////////////////////////////////////////////////////////////////////////////////////////
                case R.id.upload_btn:
                    NetworkTask networkTask = new NetworkTask(MainActivity.this, imageView, img_path, urlAddr);
                    //////////////////////////////////////////////////////////////////////////////////////////////
                    //
                    //              NetworkTask Class의 doInBackground Method의 결과값을 가져온다.
                    //
                    //////////////////////////////////////////////////////////////////////////////////////////////
                    try {
                        Integer result = networkTask.execute(100).get();
                        //////////////////////////////////////////////////////////////////////////////////////////////
                        //
                        //              doInBackground의 결과값으로 Toast생성
                        //
                        //////////////////////////////////////////////////////////////////////////////////////////////
                        switch (result){
                            case 1:
                                Toast.makeText(MainActivity.this, "Success!", Toast.LENGTH_SHORT).show();

                                //////////////////////////////////////////////////////////////////////////////////////////////
                                //
                                //              Device에 생성한 임시 파일 삭제
                                //
                                //////////////////////////////////////////////////////////////////////////////////////////////
                                File file = new File(img_path);
                                file.delete();
                                break;
                            case 0:
                                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                break;
                        }
                        //////////////////////////////////////////////////////////////////////////////////////////////
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //                   Photo App.에서 Image 선택후 작업내용
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.v(TAG, "Data :" + String.valueOf(data));

        if (requestCode == REQ_CODE_SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
            try {
                //이미지의 URI를 얻어 경로값으로 반환.
                img_path = getImagePathToUri(data.getData());
                Log.v(TAG, "image path :" + img_path);
                Log.v(TAG, "Data :" +String.valueOf(data.getData()));

                //이미지를 비트맵형식으로 반환
                Bitmap image_bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());

                //image_bitmap 으로 받아온 이미지의 사이즈를 임의적으로 조절함. width: 400 , height: 300
                Bitmap image_bitmap_copy = Bitmap.createScaledBitmap(image_bitmap, 400, 300, true);
                imageView.setImageBitmap(image_bitmap_copy);

                // 파일 이름 및 경로 바꾸기(임시 저장, 경로는 임의로 지정 가능)
                String date = new SimpleDateFormat("yyyyMMddHmsS").format(new Date());
                String imageName = date + "." + f_ext;
                tempSelectFile = new File(devicePath , imageName);
                OutputStream out = new FileOutputStream(tempSelectFile);
                image_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

                // 임시 파일 경로로 위의 img_path 재정의
                img_path = devicePath + imageName;
                Log.v(TAG,"fileName :" + img_path);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //              사용자가 선택한 이미지의 정보를 받아옴
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private String getImagePathToUri(Uri data) {

        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(data, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        //이미지의 경로 값
        String imgPath = cursor.getString(column_index);
        Log.v(TAG, "Image Path :" + imgPath);

        //이미지의 이름 값
        String imgName = imgPath.substring(imgPath.lastIndexOf("/") + 1);

        // 확장자 명 저장
        f_ext = imgPath.substring(imgPath.length()-3, imgPath.length());

        return imgPath;
    }





} // ---------------