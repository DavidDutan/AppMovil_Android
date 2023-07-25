package ec.edu.eup.appmovil;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Camara5 extends CameraActivity {

    private CameraBridgeViewBase cameraBridgeViewBase;
    private Mat entrada;
    private Mat entrada1;

    private ImageView photo;
    private ImageView gallery;

    private ImageView camara;

    private Button filtro1;
    private Button filtro2;
    private Button filtro3;
    private Button filtro4;
    private ImageView resolution_button;
    private ListView set_resolution;

    private int camaraId = 0;
    private int take_foto = 0;
    private ImageView flyt;
    private int show_resolution_list = 0;
    Camera mCamera;
    private MediaRecorder recorder;
    private ImageView video_camera_buton;
    private int video_or_photo = 0;
    private int take_video_or_not = 0;

    private int mHeight;
    private int mWidth;

    private String filtrosSeleccionado = "";
    private Bitmap entradaFrame;
    private Bitmap salidaFrame;


    private int cameraId = CameraBridgeViewBase.CAMERA_ID_BACK;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camara);


        int Mis_Permisos = 0;

        if (ContextCompat.checkSelfPermission(Camara5.this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(Camara5.this, new String[]{Manifest.permission.RECORD_AUDIO}, Mis_Permisos);

        }

        if (ContextCompat.checkSelfPermission(Camara5.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(Camara5.this, new String[]{Manifest.permission.CAMERA}, Mis_Permisos);

        }

        if (ContextCompat.checkSelfPermission(Camara5.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(Camara5.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Mis_Permisos);

        }

        if (ContextCompat.checkSelfPermission(Camara5.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(Camara5.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Mis_Permisos);

        }

        getPermission();


        setContentView(R.layout.activity_camara);

        cameraBridgeViewBase = findViewById(R.id.cameraView);
        flyt = findViewById(R.id.flyt);

        flyt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    flyt.setColorFilter(Color.DKGRAY);
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    flyt.setColorFilter(Color.WHITE);
                    swapCamara();
                    return true;
                }
                return false;
            }
        });


        gallery = findViewById(R.id.gallery);

        gallery.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    gallery.setColorFilter(Color.DKGRAY);
                    return true;
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    gallery.setColorFilter(Color.WHITE);
                    startActivity(new Intent(Camara5.this, GalleryActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    return true;
                }
                return false;
            }
        });


        cameraBridgeViewBase = findViewById(R.id.cameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                entrada = inputFrame.rgba();
                entrada1 = inputFrame.gray();

                if (cameraId == 1) {
                    Core.flip(entrada, entrada, -1);
                    Core.flip(entrada1, entrada1, -1);
                }

                Mat newInput = new Mat();
                entradaFrame = Bitmap.createBitmap(entrada.cols(), entrada.rows(), Bitmap.Config.ARGB_8888);
                salidaFrame = Bitmap.createBitmap(entrada.cols(), entrada.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(entrada, entradaFrame);


                DeteccionLineas(entradaFrame,salidaFrame);
                Utils.bitmapToMat(salidaFrame, newInput);
                entrada = newInput.clone();



                take_foto = tomarFoto(take_foto, entrada);

                mHeight = entrada.height();
                mWidth = entrada.height();


                return entrada;
            }

            private int tomarFoto(int take_foto, Mat entrada) {

                if (take_foto == 1) {
                    Mat save = new Mat();
                    Core.flip(entrada.t(), save, 1);
                    Imgproc.cvtColor(save, save, Imgproc.COLOR_RGBA2BGRA);
                    File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/ImagePro");
                    boolean success = true;
                    if (!folder.exists()) {
                        success = folder.mkdir();
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                    String diaActual = sdf.format(new Date());
                    String fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/ImagePro/" + diaActual + ".jpg";

                    Imgcodecs.imwrite(fileName, save);
                    take_foto = 0;
                }
                return take_foto;
            }

            private int tomarFotoss(int take_foto, Mat entrada1) {
                if (take_foto == 1) {
                    Mat save = new Mat();
                    Core.flip(entrada.t(), save, 1);

                    File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/ImagePro");
                    boolean success = true;
                    if (!folder.exists()) {
                        success = folder.mkdir();
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                    String diaActual = sdf.format(new Date());
                    String fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/ImagePro/" + diaActual + ".jpg";

                    Imgcodecs.imwrite(fileName, save);
                    take_foto = 0;
                }


                return take_foto;
            }


            @Override
            public void onCameraViewStarted(int width, int height) {

            }

            @Override
            public void onCameraViewStopped() {

            }


        });

        if (OpenCVLoader.initDebug()) {
            cameraBridgeViewBase.enableView();
        }

        mCamera = Camera.open();
        Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        ArrayList<String> resolution = new ArrayList<>();
        for (int i = 0; i < sizes.size(); i++) {
            int frameWidth = (int) sizes.get(i).width;
            int frameHeight = (int) sizes.get(i).height;

            String frameWidth_S = Integer.toString(frameWidth);
            String frameHeight_S = Integer.toString(frameHeight);

            resolution.add(frameWidth_S + "x" + frameHeight_S);
            Log.w("Camara", frameWidth_S + "x" + frameHeight_S);


        }
        resolution_button = findViewById(R.id.resolution_button);
        set_resolution = findViewById(R.id.set_resolution);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.resolution, R.id.textx, resolution);
        String null_array[] = {};
        ArrayAdapter<String> null_array_adapter = new ArrayAdapter<>(this, R.layout.resolution, R.id.textx, null_array);

        //set_resolution.setAdapter(null_array_adapter);

        resolution_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    resolution_button.setColorFilter(Color.DKGRAY);
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    resolution_button.setColorFilter(Color.WHITE);
                    if (show_resolution_list == 0) {
                        set_resolution.setAdapter(arrayAdapter);
                        show_resolution_list = 1;
                    } else {
                        set_resolution.setAdapter(null_array_adapter);
                        show_resolution_list = 0;
                    }
                    return true;
                }
                return false;
            }
        });

        set_resolution.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int frameWidth = (int) sizes.get(position).width;
                int frameHeight = (int) sizes.get(position).height;

                cameraBridgeViewBase.disableView();
                cameraBridgeViewBase.setMaxFrameSize(frameWidth, frameHeight);
                cameraBridgeViewBase.enableView();

                set_resolution.setAdapter(null_array_adapter);
                show_resolution_list = 0;
            }
        });
        cameraBridgeViewBase.enableFpsMeter();

        recorder = new MediaRecorder();
        video_camera_buton = findViewById(R.id.resolution_button);

        video_camera_buton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    video_camera_buton.setColorFilter(Color.DKGRAY);
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    video_camera_buton.setColorFilter(Color.WHITE);
                    if (video_or_photo == 0) {
                        camara.setImageResource(R.drawable.circle_button);
                        camara.setColorFilter(Color.WHITE);
                        video_or_photo = 1;
                    } else {
                        camara.setImageResource(R.drawable.camara);
                        video_or_photo = 0;
                    }

                    return true;
                }
                return false;
            }
        });

        camara = findViewById(R.id.camara);

        camara.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (video_or_photo == 0) {
                        if (take_foto == 0) {
                            camara.setColorFilter(Color.DKGRAY);

                        }
                    }
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (video_or_photo == 1) {
                        if (take_video_or_not == 0) {
                            try {
                                File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/ImagePro");
                                boolean success = true;
                                if (!folder.exists()) {
                                    success = folder.mkdir();

                                }
                                camara.setImageResource(R.drawable.circle_button);
                                camara.setColorFilter(Color.RED);
                                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                                recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
                                CamcorderProfile camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
                                recorder.setProfile(camcorderProfile);

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                                String current_Date_and_Time = sdf.format(new Date());
                                String filename = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/ImagePro/" + current_Date_and_Time + ".mp4";
                                recorder.setOutputFile(filename);
                                recorder.setVideoSize(mHeight, mWidth);

                                recorder.prepare();

                                cameraBridgeViewBase.setRecorder(recorder);
                                recorder.start();
                            } catch (IOException e) {
                                e.printStackTrace();

                            }
                            take_video_or_not = 1;

                        } else {
                            camara.setImageResource(R.drawable.circle_button);
                            camara.setColorFilter(Color.WHITE);
                            cameraBridgeViewBase.setRecorder(null);
                            recorder.stop();
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            take_video_or_not = 0;
                        }
                    } else {
                        camara.setColorFilter(Color.WHITE);
                        if (take_foto == 0) {
                            take_foto = 1;
                        } else {
                            take_foto = 0;
                        }
                    }
                    return true;
                }
                return false;
            }
        });


    }


    private void swapCamara() {
        camaraId = camaraId ^ 1;

        cameraBridgeViewBase.disableView();
        cameraBridgeViewBase.setCameraIndex(cameraId);
        cameraBridgeViewBase.enableView();
    }


    @Override
    protected void onResume() {
        super.onResume();
        cameraBridgeViewBase.enableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraBridgeViewBase.disableView();

    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraBridgeViewBase.disableView();
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(cameraBridgeViewBase);
    }

    void getPermission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            getPermission();

        }
    }

    public native void DeteccionLineas(Bitmap in, Bitmap out);
}