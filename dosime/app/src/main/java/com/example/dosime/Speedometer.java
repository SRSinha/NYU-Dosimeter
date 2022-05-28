package com.example.dosime;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

import be.tarsos.dsp.pitch.PitchDetector;

/**
 * Created by bodekjan on 2016/8/8.
 */
public class Speedometer extends androidx.appcompat.widget.AppCompatImageView {

    private float scaleWidth, scaleHeight;
    private int newWidth, newHeight, oldX, oldY, direction=1, left, top, bottom, right;
    private Matrix mMatrix = new Matrix();
    private Bitmap indicatorBitmap;
    private Paint paint = new Paint();
    static final long  ANIMATION_INTERVAL = 20;
    int bitmapWidth, bitmapHeight;
    public Speedometer(Context context) {
        super(context);
    }

    public Speedometer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void init() {
        Bitmap myBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.srs_box);
        bitmapWidth = myBitmap.getWidth();
        bitmapHeight = myBitmap.getHeight();

        newWidth = getWidth();
        newHeight = getHeight();
        left = getLeft();
        top = getTop();
        bottom = getBottom();
        right = getRight();

//        scaleWidth = ((float) newWidth) /(float) bitmapWidth;  // Get the zoom ratio
//        scaleHeight = ((float) newHeight) /(float) bitmapHeight;  //Get the zoom ratio
        scaleWidth = 2;
        scaleHeight = 2;
        oldX = bitmapWidth;
        oldY = bitmapHeight;
        mMatrix.postScale(scaleWidth, scaleHeight);   //Set the scale of mMatrix
        indicatorBitmap = Bitmap.createBitmap(myBitmap, 0, 0, bitmapWidth, bitmapHeight, mMatrix,true);  //Get the same and background width and height of the pointer map bitmap

        paint = new Paint();
        paint.setTextSize(44);
        paint.setTypeface(MainActivity.tf);
        paint.setAntiAlias(true);  //Anti-aliasing
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.WHITE);
    }

    public void refresh() {
        postInvalidateDelayed(ANIMATION_INTERVAL); //Sub-thread refresh view
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (indicatorBitmap == null) {
            init();
        }
//        if(direction==1){
//            oldX+=getDelta(World.dbCount);
//        } else if(direction==2){
//            oldY+=getDelta(World.dbCount);
//        } else if(direction==3){
//            oldX-=getDelta(World.dbCount);
//        } else{
//            oldY-=getDelta(World.dbCount);
//        }
        if(direction==1){
            oldX+=getDelta(World.dbCount);
        } else if(direction==2){
            oldY+=getDelta(World.dbCount);
        } else if(direction==3){
            oldX-=getDelta(World.dbCount);
        } else{
            oldY-=getDelta(World.dbCount);
        }
        if(oldX>=right-2*scaleWidth*bitmapWidth && direction==1){     //right ja raha
            direction=2;   //ab down ja
        } else if(oldY>=bottom-12/scaleHeight*bitmapHeight && direction==2){  //neeche ja raha
            direction=3;   //ab left ja
        } else if(oldX<left-1/scaleWidth*bitmapWidth && direction==3){  //left ja raha
            direction=4;   //ab upar ja
        } else if(oldY<top-2/scaleHeight*bitmapHeight && direction==4){  //upar ja raha
            direction=1;   //ab right ja
        }
        mMatrix.setTranslate(oldX, oldY);
        canvas.drawBitmap(indicatorBitmap, mMatrix, paint);
        canvas.drawText((int)World.dbCount+"", newWidth/2,newHeight*36/46, paint);
        canvas.drawText((int)World.dbCount+"", newWidth/2,newHeight*36/46, paint);

    }

    private float getDelta(float db){
        return Math.abs(db-World.minDB)/10;
    }

}