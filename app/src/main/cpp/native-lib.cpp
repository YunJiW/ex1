#include <jni.h>
#include <opencv2/opencv.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/core/core.hpp>
#include <vector>
#include <android/log.h>
#include <string>
#include <math.h>
using namespace cv;
using namespace std;
//HSV 픽셀 검출용

int Color_distance(int r, int g, int b);

extern "C"
JNIEXPORT jdouble JNICALL
Java_com_example_ex1_MainActivity_HSVdetectorJNI(JNIEnv *env, jobject instance, jlong inputimage,jlong  outputImage, jint th1,jint th2) {

    Mat &inputMat = *(Mat *) inputimage;
    Mat &outputMat = *(Mat *) outputImage;

    cvtColor(inputMat,outputMat,COLOR_BGR2RGB);
    Rect rectangle(0,0,outputMat.cols -20,outputMat.rows -20);

    Mat result;
    Mat bgModel,fgModel;

    grabCut(outputMat,result,rectangle,bgModel,fgModel,5,GC_INIT_WITH_RECT);

    compare(result,GC_PR_FGD,result,CMP_EQ);
    Mat foreground(outputMat.size(),CV_8UC3,Scalar(0,0,0));
    outputMat.copyTo(foreground,result);
    Mat img;//색 다시 변환
    cvtColor(foreground,img,COLOR_RGB2BGR);
    img.copyTo(outputMat);

    int width=img.cols;
    int height=img.rows;

    int l_meat = 0;
    int f_meat = 0;
    int White = 0;

    Scalar red(0,0,255);
    Mat Color_red = Mat(1,1,CV_8UC3,red);
    int r,g,b;
    int check=0;
    for(int i = 0; i< height;i++)
    {
        for(int j = 0 ; j < width; j++)
        {
            r = img.at<Vec3b>(i,j)[2];
            g = img.at<Vec3b>(i,j)[1];
            b = img.at<Vec3b>(i,j)[0];

            int check = Color_distance(r,g,b);

            if(30 < check && check < 150)
                f_meat++ ; //지방
            else if(150 < check && check < 380)
                l_meat++ ; //고기부분

            if(check == 0)
                White ++;

        }
    }

    double check_fat = check_fat = ((double)f_meat / ((double)l_meat + (double)f_meat));

    return check_fat;
// TODO: implement HSVdetectorJNI()
}

int Color_distance(int r, int g, int b) {
    int parameter = (255 - r) * (255 - r) + (255 - g) * (255 - g) + (255 - b) * (255 - b);
    int distance = sqrt(parameter);

    return distance;
}

