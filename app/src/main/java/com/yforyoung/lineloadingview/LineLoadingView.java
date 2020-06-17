package com.yforyoung.lineloadingview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class LineLoadingView extends View {
    private final String TAG = "MusicLoadingView";
    private Paint paint;
    private RectF rectF;
    private boolean anim = false;
    //属性
    private int lineSpace;  //两条线之间的宽度
    private float changeStep; //动画幅度
    private int lineCount;  //堆数，每堆5根线条
    private int lineWidth;  //线宽度
    private int changeSpeed;    //变化速度  次/秒
    private int color;  //颜色
    private int width;  //dp
    private int maxHeight;  //最高高度
    private int minHeight;  //最低高度
    private float radius;   //线条弧度

    //draw用到的中间参数
    private int tempHeight = 0; //
    private int minTempHeight;  //最低线条的高度记录(0,4)
    private boolean minLowing;
    private int middleTempHeight;   //中等高度线条的高度记录（1,3）
    private boolean middleLowing;
    private int maxTempHeight;  //最高线条的高度记录（2）
    private boolean maxLowing;
    private List<Integer> minList = new ArrayList<>();
    private List<Integer> middleList = new ArrayList<>();
    private List<Integer> maxList = new ArrayList<>();

    private Runnable runnable;

    public LineLoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LineLoadingView);
        init(typedArray);
    }

    public void startAnim() {
        if (!anim) {
            anim = true;
            if (runnable == null) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        while (anim) {
                            rectF.left = 0;
                            rectF.right = lineWidth;
                            postInvalidate();
                            try {
                                Thread.sleep(1000 / changeSpeed);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };

            }
            post(new Runnable() {
                @Override
                public void run() {
                    new Thread(runnable).start();
                }
            });
        }
    }

    public void pauseAnim() {
        anim = false;
    }

    public boolean isPlaying() {
        return anim;
    }

    public void stopAnim() {
        minTempHeight = minHeight;
        minLowing = false;
        middleTempHeight = (minHeight + maxHeight) / 2;
        middleLowing = true;
        maxTempHeight = maxHeight;
        maxLowing = true;
        anim = false;
        postInvalidate();
    }


    private void init(TypedArray typedArray) {

        lineCount = typedArray.getInteger(R.styleable.LineLoadingView_lineCount, 1);//默认1
        lineWidth = (int) typedArray.getDimension(R.styleable.LineLoadingView_lineWidth, dip2px(3));    //默认3
        lineSpace = (int) typedArray.getDimension(R.styleable.LineLoadingView_lineSpace, dip2px(7));    //默认7
        color = typedArray.getColor(R.styleable.LineLoadingView_lineColor, Color.parseColor("#FFFFE105"));
        maxHeight = (int) typedArray.getDimension(R.styleable.LineLoadingView_lineMaxHeight, dip2px(50));
        minHeight = (int) typedArray.getDimension(R.styleable.LineLoadingView_lineMinHeight, dip2px(10));
        changeStep = typedArray.getFloat(R.styleable.LineLoadingView_changeStep, 0.1f) * maxHeight;    //默认0.1
        changeSpeed = typedArray.getInt(R.styleable.LineLoadingView_changeSpeed, dip2px(6));
        radius = typedArray.getDimension(R.styleable.LineLoadingView_lineRadius, dip2px(3));

        width = lineCount * 4 * (lineSpace + lineWidth) + lineWidth;

        minTempHeight = minHeight;
        minLowing = false;
        middleTempHeight = (minHeight + maxHeight) / 2;
        middleLowing = true;
        maxTempHeight = maxHeight;
        maxLowing = true;

        Log.i(TAG, "MusicLoadingView: " + lineCount
                + " 变化： " + changeStep
                + "  线宽： " + lineWidth
                + "  线空隙： " + lineSpace
                + "  高度：" + maxHeight
                + " 最低： " + minHeight
                + "  width:" + width);

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setColor(color);
        rectF = new RectF();
        rectF.left = 0;
        rectF.right = lineWidth;

        for (int i = 0; i <= lineCount * 4; i++) {
            if (i % 4 == 0) {
                //min
                minList.add(i);
            } else if (i % 2 != 0) {
                //middle
                middleList.add(i);
            } else {
                //max
                maxList.add(i);
            }
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(width, maxHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {  //第0秒
        super.onDraw(canvas);
        //5个一组

        /**
         * 动画就是改变tempHeight  即某个线条的高度
         * 线条高度逐减，减到不能减时（minHeight）逐增
         * 增到不能增（maxHeight）逐减
         *
         * */
        int top;
        minTempHeight = changeHeight(minTempHeight, minLowing);
        minLowing = changeLowing(minTempHeight, minLowing);
        top = (maxHeight - tempHeight) / 2;
        for (int i : minList) {
            draw(canvas, i, top);
        }
        middleTempHeight = changeHeight(middleTempHeight, middleLowing);
        middleLowing = changeLowing(middleTempHeight, middleLowing);
        top = (maxHeight - tempHeight) / 2;
        for (int i : middleList) {
            draw(canvas, i, top);
        }
        maxTempHeight = changeHeight(maxTempHeight, maxLowing);
        maxLowing = changeLowing(maxTempHeight, maxLowing);
        top = (maxHeight - tempHeight) / 2;
        for (int i : maxList) {
            draw(canvas, i, top);
        }
    }

    private void draw(Canvas canvas, int i, int top) {  //i 标识
        rectF.left = i * (lineWidth + lineSpace);
        rectF.right = rectF.left + lineWidth;
        top = (maxHeight - tempHeight) / 2;
        rectF.top = top;
        rectF.bottom = top + tempHeight;
        canvas.drawRoundRect(rectF, radius, radius, paint);
    }


    private int changeHeight(int height, boolean lowing) {
        if (lowing) {
            //逐减中
            height = (int) Math.max(height - changeStep, minHeight);

        } else {
            height = (int) Math.min(height + changeStep, maxHeight);

        }
        tempHeight = height;
        return height;
    }

    private boolean changeLowing(int height, boolean lowing) {
        if (lowing) {
            if (height <= minHeight) {  //减到最低了
                lowing = false;   //开始逐增
            }
        } else {
            if (height >= maxHeight) {  //增到最大了
                lowing = true;  //开始逐渐减少
            }
        }
        return lowing;
    }


    public int dip2px(float dipValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
