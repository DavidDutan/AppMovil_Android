#include <jni.h>
#include <string>
#include <opencv2/video.hpp>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/features2d.hpp>
#include <opencv2/objdetect.hpp>
#include <opencv2/videoio.hpp>
#include <opencv2/highgui.hpp>
#include "android/bitmap.h"
#include <opencv2/opencv.hpp>
#include <jni.h>


using namespace cv;

void bitmapToMat(JNIEnv * env, jobject bitmap, cv::Mat &dst, jboolean
needUnPremultiplyAlpha){
    AndroidBitmapInfo info;
    void* pixels = 0;
    try {
        CV_Assert( AndroidBitmap_getInfo(env, bitmap, &info) >= 0 );
        CV_Assert( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                   info.format == ANDROID_BITMAP_FORMAT_RGB_565 );
        CV_Assert( AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0 );
        CV_Assert( pixels );
        dst.create(info.height, info.width, CV_8UC4);
        if( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 )
        {
            cv::Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if(needUnPremultiplyAlpha) cvtColor(tmp, dst, cv::COLOR_mRGBA2RGBA);
            else tmp.copyTo(dst);
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            cv::Mat tmp(info.height, info.width, CV_8UC2, pixels);
            cvtColor(tmp, dst, cv::COLOR_BGR5652RGBA);
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch(const cv::Exception& e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        //jclass je = env->FindClass("org/opencv/core/CvException");
        jclass je = env->FindClass("java/lang/Exception");
        //if(!je) je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nBitmapToMat}");
        return;
    }
}
void matToBitmap(JNIEnv * env, cv::Mat src, jobject bitmap, jboolean needPremultiplyAlpha) {
    AndroidBitmapInfo info;
    void* pixels = 0;
    try {
        CV_Assert( AndroidBitmap_getInfo(env, bitmap, &info) >= 0 );
        CV_Assert( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                   info.format == ANDROID_BITMAP_FORMAT_RGB_565 );
        CV_Assert( src.dims == 2 && info.height == (uint32_t)src.rows && info.width ==
                                                                         (uint32_t)src.cols );
        CV_Assert( src.type() == CV_8UC1 || src.type() == CV_8UC3 || src.type() == CV_8UC4 );
        CV_Assert( AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0 );
        CV_Assert( pixels );
        if( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 )
        {
            cv::Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if(src.type() == CV_8UC1)
            {
                cvtColor(src, tmp, cv::COLOR_GRAY2RGBA);
            } else if(src.type() == CV_8UC3){
                cvtColor(src, tmp, cv::COLOR_RGB2RGBA);
            } else if(src.type() == CV_8UC4){
                if(needPremultiplyAlpha) cvtColor(src, tmp, cv::COLOR_RGBA2mRGBA);
                else src.copyTo(tmp);
            }
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            cv::Mat tmp(info.height, info.width, CV_8UC2, pixels);
            if(src.type() == CV_8UC1)
            {
                cvtColor(src, tmp, cv::COLOR_GRAY2BGR565);
            } else if(src.type() == CV_8UC3){
                cvtColor(src, tmp, cv::COLOR_RGB2BGR565);
            } else if(src.type() == CV_8UC4){
                cvtColor(src, tmp, cv::COLOR_RGBA2BGR565);
            }
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch(const cv::Exception& e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        //jclass je = env->FindClass("org/opencv/core/CvException");
        jclass je = env->FindClass("java/lang/Exception");
        //if(!je) je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nMatToBitmap}");
        return;
    }
}


extern "C"
JNIEXPORT void JNICALL
Java_ec_edu_eup_appmovil_Camara1_Grises(JNIEnv *env, jobject /* this */, jobject in,
                                       jobject out) {
    cv::Mat src;
    cv::Mat negated;
    bitmapToMat(env, in, src, false);
    cv::cvtColor(src, src, cv::COLOR_BGR2GRAY);
    negated = 250 - src;
    matToBitmap(env, negated, out, false);

}

extern "C" {

JNIEXPORT void JNICALL
Java_ec_edu_eup_appmovil_Camara6_Suavizado(JNIEnv *env, jobject /* this */, jobject in,
                                           jobject out) {
    cv::Mat src;
    cv::Mat blurred;
    bitmapToMat(env, in, src, false);
    cv::GaussianBlur(src, blurred, cv::Size(5, 5), 0);
    matToBitmap(env, blurred, out, false);
}

}
extern "C" {

JNIEXPORT void JNICALL
Java_ec_edu_eup_appmovil_Camera3_DeteccionBordes(JNIEnv *env, jobject /* this */, jobject in,
                                                 jobject out) {
    cv::Mat src;
    cv::Mat edges;
    bitmapToMat(env, in, src, false);
    cv::Canny(src, edges, 100, 200);
    matToBitmap(env, edges, out, false);
}

}

extern "C" {

JNIEXPORT void JNICALL
Java_ec_edu_eup_appmovil_Camara2_DeteccionBordesSobel(JNIEnv *env, jobject /* this */, jobject in,
                                                      jobject out) {
    cv::Mat src;
    cv::Mat gray;
    cv::Mat grad_x, grad_y;
    cv::Mat abs_grad_x, abs_grad_y;
    cv::Mat edges;

    bitmapToMat(env, in, src, false);
    cv::cvtColor(src, gray, cv::COLOR_BGR2GRAY);

    cv::Sobel(gray, grad_x, CV_16S, 1, 0);
    cv::Sobel(gray, grad_y, CV_16S, 0, 1);

    cv::convertScaleAbs(grad_x, abs_grad_x);
    cv::convertScaleAbs(grad_y, abs_grad_y);

    cv::addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, edges);

    matToBitmap(env, edges, out, false);
}

}

extern "C" {

JNIEXPORT void JNICALL
Java_ec_edu_eup_appmovil_Camara4_TransformacionPerspectiva(JNIEnv *env, jobject /* this */, jobject in,
                                                           jobject out) {
    cv::Mat src;
    cv::Mat transformed;

    bitmapToMat(env, in, src, false);

    // Puntos de la imagen de entrada
    std::vector<cv::Point2f> srcPoints;
    srcPoints.push_back(cv::Point2f(100, 100));  // Esquina superior izquierda
    srcPoints.push_back(cv::Point2f(200, 100));  // Esquina superior derecha
    srcPoints.push_back(cv::Point2f(200, 200));  // Esquina inferior derecha
    srcPoints.push_back(cv::Point2f(100, 200));  // Esquina inferior izquierda

    // Puntos correspondientes en la imagen de salida
    std::vector<cv::Point2f> dstPoints;
    dstPoints.push_back(cv::Point2f(0, 0));
    dstPoints.push_back(cv::Point2f(src.cols, 0));
    dstPoints.push_back(cv::Point2f(src.cols, src.rows));
    dstPoints.push_back(cv::Point2f(0, src.rows));

    // Calcula la matriz de transformación perspectiva
    cv::Mat perspectiveMatrix = cv::getPerspectiveTransform(srcPoints, dstPoints);

    // Aplica la transformación perspectiva a la imagen de entrada
    cv::warpPerspective(src, transformed, perspectiveMatrix, src.size());

    matToBitmap(env, transformed, out, false);
}

}

extern "C" {

JNIEXPORT void JNICALL
Java_ec_edu_eup_appmovil_Camara5_DeteccionMovimiento(JNIEnv *env, jobject /* this */, jobject in,
                                                     jobject out) {
    cv::Mat frame;
    cv::Mat foregroundMask;
    cv::Mat result;

    // Convertir la imagen de entrada en una matriz de OpenCV
    bitmapToMat(env, in, frame, false);

    // Crear el detector de movimiento
    cv::Ptr<cv::BackgroundSubtractor> bgSubtractor = cv::createBackgroundSubtractorMOG2();

    // Obtener la máscara de primer plano que indica las áreas de movimiento
    bgSubtractor->apply(frame, foregroundMask);

    // Realizar operaciones de postprocesamiento en la máscara si es necesario

    // Aplicar la máscara de primer plano a la imagen original
    frame.copyTo(result, foregroundMask);

    // Guardar el resultado en la imagen de salida
    matToBitmap(env, result, out, false);
}

}

extern "C" {

JNIEXPORT void JNICALL
Java_ec_edu_eup_appmovil_Camara5_DeteccionLineas(JNIEnv *env, jobject /* this */, jobject in,
                                                 jobject out) {
    cv::Mat frame;
    cv::Mat grayImage;
    cv::Mat edges;

    // Convertir la imagen de entrada en una matriz de OpenCV en escala de grises
    bitmapToMat(env, in, frame, false);
    cv::cvtColor(frame, grayImage, cv::COLOR_BGR2GRAY);

    // Aplicar el algoritmo de detección de bordes (Canny)
    cv::Canny(grayImage, edges, 50, 150);

    // Detección de líneas utilizando la transformada de Hough
    std::vector<cv::Vec2f> lines;
    cv::HoughLines(edges, lines, 1, CV_PI / 180, 100);

    // Cambiar el color de las líneas detectadas a rojo
    cv::Mat result = frame.clone();
    for (size_t i = 0; i < lines.size(); i++) {
        float rho = lines[i][0];
        float theta = lines[i][1];
        double cosTheta = cos(theta);
        double sinTheta = sin(theta);
        cv::Point pt1, pt2;
        double x0 = rho * cosTheta;
        double y0 = rho * sinTheta;
        pt1.x = cvRound(x0 + 1000 * (-sinTheta));
        pt1.y = cvRound(y0 + 1000 * (cosTheta));
        pt2.x = cvRound(x0 - 1000 * (-sinTheta));
        pt2.y = cvRound(y0 - 1000 * (cosTheta));
        cv::line(result, pt1, pt2, cv::Scalar(0, 0, 255), 3);
    }

    // Guardar el resultado en la imagen de salida
    matToBitmap(env, result, out, false);
}

}

extern "C" {

JNIEXPORT void JNICALL
Java_ec_edu_eup_appmovil_Camara6_CambiarColoresFuertes(JNIEnv *env, jobject /* this */, jobject in,
                                                       jobject out) {
    cv::Mat frame;
    cv::Mat result;

    // Convertir la imagen de entrada en una matriz de OpenCV
    bitmapToMat(env, in, frame, false);

    // Convertir la imagen de entrada a espacio de color HSV
    cv::Mat hsvImage;
    cv::cvtColor(frame, hsvImage, cv::COLOR_BGR2HSV);

    // Definir el rango de colores fuertes a modificar
    cv::Scalar lowerThreshold = cv::Scalar(0, 100, 100);  // Valor mínimo de H, S y V
    cv::Scalar upperThreshold = cv::Scalar(20, 255, 255); // Valor máximo de H, S y V

    // Aplicar la máscara para detectar los colores fuertes
    cv::Mat mask;
    cv::inRange(hsvImage, lowerThreshold, upperThreshold, mask);

    // Cambiar los colores fuertes por el color deseado (azul en este caso)
    cv::Mat maskedImage;
    frame.copyTo(maskedImage);
    maskedImage.setTo(cv::Scalar(255, 0, 0), mask);

    // Combinar la imagen modificada con la imagen original
    cv::bitwise_and(frame, ~mask, result);
    cv::bitwise_or(maskedImage, result, result);

    // Guardar el resultado en la imagen de salida
    matToBitmap(env, result, out, false);
}

}

extern "C" {

JNIEXPORT void JNICALL
Java_ec_edu_eup_appmovil_Camara6_CambiarColoresVerdes(JNIEnv *env, jobject /* this */, jobject in,
                                                      jobject out) {
    cv::Mat frame;
    cv::Mat result;

    // Convertir la imagen de entrada en una matriz de OpenCV
    bitmapToMat(env, in, frame, false);

    // Convertir la imagen de entrada a espacio de color HSV
    cv::Mat hsvImage;
    cv::cvtColor(frame, hsvImage, cv::COLOR_BGR2HSV);

    // Definir el rango de colores verdes a detectar
    cv::Scalar lowerThreshold = cv::Scalar(35, 100, 100); // Valor mínimo de H, S y V
    cv::Scalar upperThreshold = cv::Scalar(85, 255, 255); // Valor máximo de H, S y V

    // Aplicar la máscara para detectar los colores verdes
    cv::Mat mask;
    cv::inRange(hsvImage, lowerThreshold, upperThreshold, mask);

    // Cambiar los colores verdes por rojo
    cv::Mat maskedImage;
    frame.copyTo(maskedImage);
    maskedImage.setTo(cv::Scalar(0, 0, 255), mask);

    // Combinar la imagen modificada con la imagen original
    cv::bitwise_and(frame, ~mask, result);
    cv::bitwise_or(maskedImage, result, result);

    // Guardar el resultado en la imagen de salida
    matToBitmap(env, result, out, false);
}

}

extern "C" {

JNIEXPORT void JNICALL
Java_ec_edu_eup_appmovil_Camara7_FindFeactures(JNIEnv* jniEnv, jobject, jlong addrGray, jlong addrRGB) {
    Mat* mGray = (Mat*)addrGray;
    Mat* mRGB = (Mat*)addrRGB   ;

    std::vector<Point2f> corners;

    goodFeaturesToTrack(*mGray, corners, 20, 0.01,10,Mat() ,3,false,0.04 );

    for(int i=0;i<corners.size();i++)
        circle(*mRGB, corners[i],10, Scalar(8,255,0),2);

}

}



extern "C" {
CascadeClassifier face_cascade;
JNIEXPORT void JNICALL
Java_ec_edu_eup_appmovil_Camara7_InitFaceDetector(JNIEnv* jniEnv, jobject, jstring jFilePath) {

    const char * jnamestr = jniEnv->GetStringUTFChars(jFilePath,NULL);
    std::string filePath(jnamestr);
    face_cascade.load(filePath);

}

}

extern "C" {

JNIEXPORT void JNICALL
Java_ec_edu_eup_appmovil_Camara7_DetecFaces(JNIEnv* jniEnv, jobject, jlong addrGray, jlong addrRGB) {
    Mat* mGray = (Mat*)addrGray;
    Mat* mRGB = (Mat*)addrRGB   ;

   std::vector<Rect> faces;
   face_cascade.detectMultiScale(*mGray,faces);

   for(int i=0; i<faces.size();i++){
       rectangle(*mRGB,Point(faces[i].x,faces[i].y), Point(faces[i].x+faces[i].width, faces[i].y+faces[i].height),Scalar(0,255,0),2);

   }
}

}

