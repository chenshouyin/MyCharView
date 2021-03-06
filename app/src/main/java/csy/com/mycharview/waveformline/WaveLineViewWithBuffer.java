package csy.com.mycharview.waveformline;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.List;

import csy.com.mycharview.utils.Dbug;
import csy.com.mycharview.waveformline.bean.WavePoint;

/**
 * Created by user on 2017-6-14.
 */

public class WaveLineViewWithBuffer extends BaseWaveLineView {

    private int smallSpaceX = 20;//水平方向一小格代表多少像素
    private int smallSpaceY = 20;//垂直方向一小格代表多少像素

    public int getPerPointXSpase() {
        return perPointXSpase;
    }

    public void setPerPointXSpase(int perPointXSpase) {
        this.perPointXSpase = perPointXSpase;
    }

    private int perPointXSpase =0;//每个点水平方向代表的小格子 由数据点的间隔决定
    private int perPointYSpase =0;
    private String gridPaintColor;//网格颜色
    private int gridPaintStrokeWidth;//网格画笔粗细
    private String dataPaintColor;//数据颜色
    private int dataPaintStrokeWidth;//数据粗细
    private String xPaintColor;//X轴数据颜色
    private int xPaintStrokeWidth;//X轴数据粗细
    private float actionDownStartX = 0;//手指按下时的x坐标
    private float actionMoveDis = 0;//手指水平方向移动的距离
    private float moveMaxX = 0;//第一个点可移动的最大距离
    private float moveMinX = 0;//第一个点可移动的最小距离
    private float xRoundPoint = 0;//圆点坐标 移动中可能会变

    private Bitmap bubufferBitmap;//缓冲图片,先花在此图片上,画完之后一次性显示在屏幕
    private Canvas bufferCanvas;
    public int getBaseLine() {
        return baseLine;
    }

    public void setBaseLine(int baseLine) {
        this.baseLine = baseLine;
        Dbug.d(getClass().getSimpleName(),"==="+baseLine);
    }
    public int getPerPointYSpase() {
        return perPointYSpase;
    }

    public void setPerPointYSpase(int perPointYSpase) {
        this.perPointYSpase = perPointYSpase;
    }
    private int baseLine = 0;//基线

    public void setPoints(List<WavePoint> pointsList) {
        this.pointsList = pointsList;
    }

    private List<WavePoint> pointsList;

    public WaveLineViewWithBuffer(Context context) {
        this(context, null);
    }

    public WaveLineViewWithBuffer(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveLineViewWithBuffer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);//获取一些自定义属性
        initData();
    }

    @Override
    public void initData() {
        gridPaintColor = "#ee934f";
        gridPaintStrokeWidth = 1;
        dataPaintColor = "#000000";
        dataPaintStrokeWidth = 2;


    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //高度一屏显示完,宽度自适应
        baseLine = getHeight();
        Dbug.d("","==baseLine=="+baseLine);
        moveMinX = getWidth() - pointsList.size() * smallSpaceX;
        Dbug.d("","==baseLine=="+moveMinX);

        if (bubufferBitmap == null){
            bubufferBitmap = Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.ARGB_8888);
            bufferCanvas = new Canvas();
            bufferCanvas.setBitmap(bubufferBitmap);
            drawGrid(bufferCanvas);//现在缓冲区画
            drawData(bufferCanvas);
            drawText(bufferCanvas);
        }else{
            //清屏
            canvas.drawBitmap(bubufferBitmap,0,0,null);//再显示再屏幕
        }

    }


    //画数据 数据转换为像素 *smallSpace
    private void drawData(Canvas canvas) {
        if (pointsList == null && pointsList.size() == 0)
            return;
        getmPaint().setColor(Color.parseColor(dataPaintColor));
        getmPaint().setStrokeWidth(dataPaintStrokeWidth);
        getmPaint().setAntiAlias(true);// 消除锐化  不然线条毛糙
        getmPaint().setStyle(Paint.Style.STROKE);//一定要设置样式为STROKE 空心 不然默认为实心,画出来的一团黑
        //getmPaint().setStyle(Paint.Style.FILL);//实心填充
        //getmPaint().setStyle(Paint.Style.FILL_AND_STROKE);//实心填充

        Path dataPath = new Path();
        for (int i = 0; i < pointsList.size(); i++) {
            //1小格代表数字1 x方向每小格多少像素  y轴方向每小格多少像素
            float y =  pointsList.get(i).getY() * smallSpaceY;
            y = Math.abs(y-baseLine);
            float x = pointsList.get(i).getX() * smallSpaceX;
            //x = x + xRoundPoint*smallSpaceX;
            x = x + xRoundPoint;//偏移量
            if (i==0){
                dataPath.moveTo(x,y);//这样第一个点才会跟着动
            }else{
                dataPath.lineTo(x,y);
            }
            canvas.drawCircle(x,y,5,getmPaint());//画顶上面的圆角

        }
        //dataPath.close();//封闭  和不封闭 的图像相差大 一般用于比如三角形
        canvas.drawPath(dataPath, getmPaint());//绘制路径
    }

    /**
     * 画网格
     *
     * @param canvas
     */
    private void drawGrid(Canvas canvas) {
        getmPaint().setColor(Color.parseColor(gridPaintColor));
        getmPaint().setStrokeWidth(gridPaintStrokeWidth);
        for (int i = 0; i < getHeight(); i = i + smallSpaceX) {//画水平网格
            canvas.drawLine(0, i, getWidth(), i, getmPaint());
        }

        for (int j = 0; j < getWidth(); j = j + smallSpaceY) {//画垂直网格
            canvas.drawLine(j, 0, j, getHeight(), getmPaint());
        }
    }

    private void drawText(Canvas canvas){
        getmPaint().setColor(Color.parseColor(dataPaintColor));
        getmPaint().setStrokeWidth(dataPaintStrokeWidth);
        getmPaint().setAntiAlias(true);// 消除锐化  不然线条毛糙
        getmPaint().setStyle(Paint.Style.STROKE);//一定要设置样式为STROKE 空心 不然默认为实心,画出来的一团黑
        for (int i = 0; i < pointsList.size(); i++) {
            //1小格代表数字1 x方向每小格多少像素  y轴方向每小格多少像素
            float y =  pointsList.get(i).getY() * smallSpaceY;
            y = Math.abs(y-baseLine);
            float x = pointsList.get(i).getX() * smallSpaceX;
            //x = x + xRoundPoint*smallSpaceX;
            x = x + xRoundPoint;//偏移量
            canvas.drawText(""+pointsList.get(i).getX(),x,getHeight()-smallSpaceY,getmPaint());//画上文字

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //Dbug.d(getClass().getSimpleName(),"==ACTION_DOWN=="+event.getX() +"==="+event.getY());
                actionDownStartX = event.getX();
                break;

            case MotionEvent.ACTION_MOVE:
                //如果所有点的宽度不足一屏 则不可滑动
                if (smallSpaceX*pointsList.size()<=getWidth()){
                    return true;
                }
                //需要加判断 移动到左边第一个点的时候不在移动  移动到右边第一个点的时候不让其再移动
                //Dbug.d(getClass().getSimpleName(),"==ACTION_MOVE=="+event.getX() +"==="+event.getY());
                actionMoveDis = (int)(event.getX() - actionDownStartX);
                actionDownStartX = event.getX();//有时候移动不放手
                actionMoveDis = (actionMoveDis%smallSpaceX)*smallSpaceX;//保证移动的为几小格
                if ((int)actionMoveDis==0){
                    actionMoveDis = smallSpaceX;
                }
                xRoundPoint +=actionMoveDis;
                if (xRoundPoint>0 ){
                    xRoundPoint -= actionMoveDis;
                }else if (xRoundPoint< -(pointsList.size()*smallSpaceX*perPointXSpase - getWidth())){
                    xRoundPoint -= actionMoveDis;
                }else{
                    bubufferBitmap = Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.ARGB_8888);
                    bufferCanvas = new Canvas();
                    bufferCanvas.setBitmap(bubufferBitmap);
                    drawGrid(bufferCanvas);//先在缓冲区画
                    drawData(bufferCanvas);
                    drawText(bufferCanvas);
                    invalidate();//再显示在屏幕
                }
                //边界判断
                Dbug.d(getClass().getSimpleName(),"==ACTION_MOVE==dis=="+actionMoveDis);
                break;

            case MotionEvent.ACTION_UP:
                //Dbug.d(getClass().getSimpleName(),"==ACTION_UP=="+event.getX() +"==="+event.getY());
                break;

            default:
                break;
        }
//        return super.onTouchEvent(event);
//        return false;//super和false代表该事件没被消费 会继续往下传递  此处  ACTION_MOVE和 ACTION_UP不会被执行

        return true;//返回true 或者 false 事件就被消费了（终止传递） 此处  ACTION_MOVE和 ACTION_UP会执行   activity里面的onTouchEvent也将不会执行
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Dbug.d(getClass().getSimpleName(),"==dispatchTouchEvent==");
        return super.dispatchTouchEvent(event);
//        return true;//此处返回true不会走onTouchEvent了
    }
}
