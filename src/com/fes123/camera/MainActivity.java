package com.fes123.camera;

import java.nio.ByteBuffer;
import java.util.List;








import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

















import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class MainActivity extends Activity implements CameraView.CameraReadyCallback, Callback{


	private static final String TAG = "Camera123";
	Bitmap VideoBit = Bitmap.createBitmap(140, 250, Bitmap.Config.ARGB_8888);

    private ReentrantLock previewLock = new ReentrantLock();
    private CameraView cameraView = null;
    ExecutorService executor = Executors.newFixedThreadPool(3);
    private final int PictureWidth = 353; //353*288
    private final int PictureHeight = 288;
    private OverlayView overlayView = null;


	private Camera camera_ = null;
    private SurfaceHolder surfaceHolder1 = null;
    private SurfaceHolder surfaceHolder2 = null;
	private SurfaceView surfaceView2;

    boolean inProcessing = false;

    byte[] yuvFrame = new byte[1920*1280*2];
    byte[] tempyuvFrame = new byte[1920*1280*2];
    byte[] mPixel = new byte[480*640*2];

    VideoEncodingTask videoTask = new  VideoEncodingTask();
 
 	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		
		setContentView(R.layout.activity_main);
		
		//OverlayView olv = new OverlayView(this, null);
		
		

		surfaceView2 = (SurfaceView) this.findViewById(R.id.surfaceView2);
//		surfaceHolder1 = surfaceView1.getHolder();
		surfaceHolder2 = surfaceView2.getHolder();
//		surfaceHolder1.addCallback(this);
		surfaceHolder2.addCallback(this);
		initCamera();;
 
	}
	
	   private void initCamera() {
	       SurfaceView cameraSurface = (SurfaceView)findViewById(R.id.surfaceView1);
	        cameraView = new CameraView(cameraSurface);
	        cameraView.setCameraReadyCallback(this);
//	        overlayView = (OverlayView)findViewById(R.id.surface_overlay);
	        
	    }

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}
	
	   private PreviewCallback previewCb = new PreviewCallback() {
	        public void onPreviewFrame(byte[] frame, Camera c) {
	            previewLock.lock();
	            doVideoEncode(frame);
	            c.addCallbackBuffer(frame);
	            previewLock.unlock();
	        }
	    };

	    private void doVideoEncode(byte[] frame) {
//	        if ( inProcessing == true) {
//	            return;
//	        }
	        inProcessing = true;
//	        Log.i("test123","doVideoEncode");
	        int picWidth = cameraView.Width();
	        int picHeight = cameraView.Height();
	    
	        int size = picWidth*picHeight + picWidth*picHeight/2;
	        
	        // 下面3种方法旋转90度都花屏
	     //   frame = draw90yuv2(frame, picWidth,picHeight);
	      //  frame = draw90yuv(frame,picWidth,picHeight);
	      //     byte[] temp = new byte[size];
	      //     temp = frame;
	           rotateYUV240SP(frame, tempyuvFrame,picWidth,picHeight);
	    
	        
	        System.arraycopy(tempyuvFrame, 0, yuvFrame, 0, size);

	       executor.execute(videoTask);
	    };

	    private class VideoEncodingTask implements Runnable {
	        private int[] resultNal = new int[yuvFrame.length];//[1024*1024];
//	        private byte[] resultNal1 =  new byte[yuvFrame.length];;
	        public VideoEncodingTask() {
	        }

	        public void run() {

	            int picWidth = cameraView.Width();
	            int picHeight = cameraView.Height();
	            int surWidth = surfaceView2.getMeasuredWidth();
	    	    int surHeight = surfaceView2.getMeasuredHeight();
	        //    Log.i("test123", "w="+surWidth+" h="+surHeight);
	    	    float widthRate = surWidth/picWidth;
	    	    float heightRate = surHeight/picHeight;
	    	//    Log.i("test123", "w="+surWidth+" h="+heightRate);
	            // 353*288
	        //    decodeYUV420SP(mPixel,yuvFrame,picWidth,picHeight);

	         //   postInvalidate();
	        //	mPixel = Integer.
	       //     int ret = nativeDoVideoEncode(yuvFrame, resultNal, intraFlag);
	        	//postInvalidate();
	            
	            Matrix matrix = new Matrix(); 
	         //   matrix.setRotate(10.0f);
	        //    matrix.postScale(2.5f,1.1f); //长和宽放大缩小的比例

	            // 下面这个好像有问题
	           // decodeYUV420SPQuarterRes(resultNal,yuvFrame,picWidth, picHeight);
	            
	           //   rotateYUV240SP(yuvFrame, resultNal1,picWidth,picHeight);
	              resultNal = decodeYUV420SP_1(yuvFrame,picWidth,picHeight);
	              //   frame = draw90yuv2(frame, picWidth,picHeight);
	    	      //  frame = draw90yuv(frame,picWidth,picHeight);
	    	      //     byte[] temp = new byte[size];
	    	      //     temp = frame;
	    	      //    
	    	  
	          //  decodeYUV420SP(resultNal1,yuvFrame,picWidth,picHeight);
	            
	            //	Bitmap VideoBit = Bitmap.createBitmap(140, 250, Bitmap.Config.ARGB_8888);

		//		Bitmap bt = Bitmap.createBitmap(200, 300, Bitmap.Config.ARGB_8888);
			
	              
	              // 这是正确的, 宽变为高, 为什么成镜像的？
	              Bitmap bmp = Bitmap.createBitmap(resultNal,picHeight,picWidth, Bitmap.Config.ARGB_8888);
	             // bmp = Bitmap.createBitmap(bmp,0,0, picWidth, picHeight, matrix,true);
	  			
		          	
	              //这里处理的图像大小会是截取部分的，因为remoteView的大小与取得的图像不同。所以如果要显示全像，要再处理图像的缩放：
				//bmp = Bitmap.createScaledBitmap(bmp, 200, 200, true);
				
				Canvas canvas = surfaceHolder2.lockCanvas();
			//	canvas.drawBitmap(bmp, 0,0, null);
				canvas.drawBitmap(bmp, matrix, null);
				surfaceHolder2.unlockCanvasAndPost(canvas);
	            }
	            
	        }

	    public static void rotateYUV240SP(byte[] src,byte[] des,int width,int height)  
        {  
              
            int wh = width * height;  
            //旋转Y  
            int k = 0;  
            for(int i=0;i<width;i++) {  
                for(int j=0;j<height;j++)   
                {  
                      des[k] = src[width*j + i];              
                      k++;  
                }  
            }  
               
            for(int i=0;i<width;i+=2) {  
                for(int j=0;j<height/2;j++)   
                {     
                      des[k] = src[wh+ width*j + i];      
                      des[k+1]=src[wh + width*j + i+1];  
                      k+=2;  
                }  
            }  
               
        }  	    
	    

	    
	    public static void rotateYUV240SP2(byte[] src,byte[] des,int width,int height)  
	    {  
	         
	        int wh = width * height;  
	        //旋转Y  
	        int k = 0;  
	        for(int i=0;i<width;i++) {  
	            for(int j=0;j<height;j++)   
	            {  
	                  des[k] = src[width*j + i];              
	                  k++;  
	            }  
	        }  
	          
	        for(int i=0;i<width;i+=2) {  
	            for(int j=0;j<height/2;j++)   
	            {     
	                  des[k] = src[wh+ width*j + i];      
	                  des[k+1]=src[wh + width*j + i+1];  
	                  k+=2;  
	            }  
	        }  
	          
	          
	    }  
	    
	    private synchronized byte[] draw90yuv(byte[] data,int w,int h){
	    	// w = 3;
	    	// h=4;
	    	byte[] temp = new byte[w*h*3/2];
	    	int n=0,s=0;
	    	// Log.i(TAG, " data:"+data.length+",temp:"+temp.length);
	    	//YYYYYYYYYYYYYYYYYYYYYYYYY
	    	n=176-1;//+16;
	    	s=0;
	    	for(int i=0;i<144;i++){
	    	for(int j=0;j<176;j++){
	    	// Log.i(TAG, "YY: temp:"+temp.length+",n:"+n);
	    	temp[n] = data[s];
	    	--n;
	    	s +=144;
	    	}
	    	s=i+1;
	    	n +=(176+176);
	    	}
	    	 
	    	//UUUUUUUUUUUUUUUUUUUUUUUUU
	    	n=w*h+88-1;
	    	s=w*h;
	    	for(int i=0;i<72;i++){
	    	for(int j=0;j<88;j++){
	    	// Log.i(TAG, "UUU temp:"+temp.length+",s:"+s);
	    	temp[n] = data[s];
	    	--n;
	    	s +=72;
	    	}
	    	s=w*h+i+1;
	    	n +=(88+88);
	    	}
	    	//////// //VVVVVVVVVVVVVVVVVVVVVVVVVV
	    	n=w*h*5/4+88-1;
	    	s=w*h*5/4;
	    	for(int i=0;i<72;i++){
	    	for(int j=0;j<88;j++){
	    	// Log.i(TAG, "VVV temp:"+temp.length+",n:"+n+",s="+s);
	    	temp[n] = data[s];
	    	--n;
	    	s+=72;
	    	}
	    	s=w*h*5/4+i+1;
	    	n +=(88+88);
	    	}
	    	 
	    	return temp;
	    	}
	    	 
	    	private synchronized byte[] draw90yuv2(byte[] data,int w,int h){
	    	// w = 3;
	    	// h=4;
	    	byte[] temp = new byte[w*h*3/2];
	    	int n=0,s=0;
	    	// Log.i(TAG, " data:"+data.length+",temp:"+temp.length);
	    	//YYYYYYYYYYYYYYYYYYYYYYYYY
	    	for(int i=0;i<144;i++){
	    	for(int j=176-1;j>=0;j--){
	    	// Log.i(TAG, "YY: temp:"+temp.length+",s:"+s+",n="+n);
	    	temp[n++] = data[j*144+i];
	    	}
	    	}
	    	//UUUUUUUUUUUUUUUUUUUUUUUUU
	    	s=w*h;
	    	for(int i=0;i<72;i++){
	    	for(int j=88-1;j>=0;j--){
	    	// Log.i(TAG, "UUU temp:"+temp.length+",s:"+s);
	    	temp[n++] = data[s+72*j+i];
	    	}
	    	}
	    	//VVVVVVVVVVVVVVVVVVVVVVVVVV
	    	s=w*h*5/4;
	    	for(int i=0;i<72;i++){
	    	for(int j=88-1;j>=0;j--){
	    	// Log.i(TAG, "VVV temp:"+temp.length+",s:"+s);
	    	temp[n++] = data[s+72*j+i];
	    	}
	    	}
	    	//
	    	return temp;
	    	}
	    	 	   
	    
	    public int[] decodeYUV420SP_1(byte[] yuv420sp, int width, int height) {
	    	 
	    	final int frameSize = width * height;
	    	 
	    	int rgb[] = new int[width * height];
	    	for (int j = 0, yp = 0; j < height; j++) {
	    	int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
	    	for (int i = 0; i < width; i++, yp++) {
	    	int y = (0xff & ((int) yuv420sp[yp])) - 16;
	    	if (y < 0) y = 0;
	    	if ((i & 1) == 0) {
	    	v = (0xff & yuv420sp[uvp++]) - 128;
	    	u = (0xff & yuv420sp[uvp++]) - 128;
	    	}
	    	 
	    	int y1192 = 1192 * y;
	    	int r = (y1192 + 1634 * v);
	    	int g = (y1192 - 833 * v - 400 * u);
	    	int b = (y1192 + 2066 * u);
	    	 
	    	if (r < 0) r = 0;
	    	else if (r > 262143) r = 262143;
	    	if (g < 0) g = 0;
	    	else if (g > 262143) g = 262143;
	    	if (b < 0) b = 0;
	    	else if (b > 262143) b = 262143;
	    	 
	    	rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) &
	    	0xff00) | ((b >> 10) & 0xff);
	    	 
	    	}
	    	}
	    	return rgb;
	    	}	
	    
	    static public void decodeYUV420SP(byte[] rgbBuf, byte[] yuv420sp, int width, int height) {  
	        final int frameSize = width * height;  
	    if (rgbBuf == null)  
	        throw new NullPointerException("buffer 'rgbBuf' is null");  
	    if (rgbBuf.length < frameSize * 3)  
	        throw new IllegalArgumentException("buffer 'rgbBuf' size "  
	                + rgbBuf.length + " < minimum " + frameSize * 3);  
	      
	    if (yuv420sp == null)  
	        throw new NullPointerException("buffer 'yuv420sp' is null");  
	      
	    if (yuv420sp.length < frameSize * 3 / 2)  
	        throw new IllegalArgumentException("buffer 'yuv420sp' size " + yuv420sp.length  
	                + " < minimum " + frameSize * 3 / 2);  
	          
	        int i = 0, y = 0;  
	        int uvp = 0, u = 0, v = 0;  
	        int y1192 = 0, r = 0, g = 0, b = 0;  
	          
	        for (int j = 0, yp = 0; j < height; j++) {  
	            uvp = frameSize + (j >> 1) * width;  
	            u = 0;  
	            v = 0;  
	            for (i = 0; i < width; i++, yp++) {  
	                y = (0xff & ((int) yuv420sp[yp])) - 16;  
	                if (y < 0) y = 0;  
	                if ((i & 1) == 0) {  
	                    v = (0xff & yuv420sp[uvp++]) - 128;  
	                    u = (0xff & yuv420sp[uvp++]) - 128;  
	                }  
	                  
	                y1192 = 1192 * y;  
	                r = (y1192 + 1634 * v);  
	                g = (y1192 - 833 * v - 400 * u);  
	                b = (y1192 + 2066 * u);  
	                  
	                if (r < 0) r = 0; else if (r > 262143) r = 262143;  
	                if (g < 0) g = 0; else if (g > 262143) g = 262143;  
	                if (b < 0) b = 0; else if (b > 262143) b = 262143;  
	                  
	                rgbBuf[yp * 3] = (byte)(r >> 10);  
	                rgbBuf[yp * 3 + 1] = (byte)(g >> 10);  
	                rgbBuf[yp * 3 + 2] = (byte)(b >> 10);  
	            }  
	        }  
	      }  

    public static void decodeYUV420SPQuarterRes(int[] rgb, byte[] yuv420sp, int width, int height) {
        final int frameSize = width * height;

        for (int j = 0, ypd = 0; j < height; j += 4) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i += 4, ypd++) {
                int y = (0xff & (yuv420sp[j * width + i])) - 16;
                if (y < 0) {
                    y = 0;
                }
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                    uvp += 2;  // Skip the UV values for the 4 pixels skipped in between
                }
                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) {
                    r = 0;
                } else if (r > 262143) {
                    r = 262143;
                }
                if (g < 0) {
                    g = 0;
                } else if (g > 262143) {
                    g = 262143;
                }
                if (b < 0) {
                    b = 0;
                } else if (b > 262143) {
                    b = 262143;
                }

                rgb[ypd] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) |
                        ((b >> 10) & 0xff);
            }
        }
    }

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Release();
		
	}

    public void Release() {
        if ( camera_ != null) {
            camera_.stopPreview();
            camera_.release();
            camera_ = null;
        }
    }
    
    public void onCameraReady() {
        cameraView.StopPreview();
        cameraView.setupCamera(PictureWidth, PictureHeight, 4, 25.0, previewCb);

        Log.i("test123","onCameraReady");
  

        cameraView.StartPreview();
    }
    
    
// //   public native void drawYUV(byte[] frame);
//    
//    static{
//    	System.loadLibrary("surfaceview-jni");
//    }
//    
    
    

}
