package com.zeiss.koch.kaffeekasse;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.View;

import java.util.Date;
import java.util.Random;

/**
 * TODO: document your custom view class.
 */
public class ScreenSaverView extends View {
    private static final int UpdateInterval = 10000;
    private static final int StarCount = 100;
    private MyHandler mHandler;
    private Paint mPaintStar;
    private Paint mPaintBackground;
    private Date lastUpdate;
    private Bitmap bitmapStarfield;
    private int paddingLeft;
    private int paddingTop;
    private Random random;



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        this.init(null, 0);
    }

    public ScreenSaverView(Context context) {
        super(context);
        init(null, 0);
    }

    public ScreenSaverView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ScreenSaverView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        mHandler = new MyHandler(this);
        random = new Random();

        if (bitmapStarfield != null) {
            bitmapStarfield.recycle();
            bitmapStarfield = null;
        }

        mPaintStar = new Paint();
        mPaintStar.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaintStar.setStyle(Paint.Style.FILL);
        int colorStar = ResourcesCompat.getColor(getResources(), R.color.white_star, null);
        mPaintStar.setColor(colorStar);

        mPaintBackground = new Paint();
        mPaintBackground.setStyle(Paint.Style.FILL);
        int colorBackground = ResourcesCompat.getColor(getResources(), R.color.black_starfield, null);
        mPaintBackground.setColor(colorBackground);

        lastUpdate = new Date();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmapStarfield == null)
        {
            drawStarfield();
        }

        Date currentDate = new Date();
        long timeDifferenceMS = currentDate.getTime() - lastUpdate.getTime();
        if (timeDifferenceMS >= UpdateInterval)
        {
            drawStarfield();
            lastUpdate = currentDate;
        }

        canvas.drawBitmap(bitmapStarfield, paddingLeft, paddingTop, mPaintStar);

        mHandler.sendEmptyMessageDelayed(1, 2000);

    }

    private void drawStarfield() {
        paddingLeft = getPaddingLeft();
        paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        if (bitmapStarfield != null) {
            bitmapStarfield.recycle();
            bitmapStarfield = null;
        }

        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        bitmapStarfield = Bitmap.createBitmap(contentWidth, contentHeight, conf);
        Canvas bitmapCanvas = new Canvas(bitmapStarfield);
        bitmapCanvas.drawRect(
                new Rect(paddingLeft, paddingTop, paddingLeft + contentWidth, paddingTop + contentHeight), mPaintBackground);

        float radiusScale = 5.5f;
        float minRadius = 0.5f;
        for (int i = 0; i < StarCount; i++)
        {
            float radius = random.nextFloat() * radiusScale + minRadius;
            bitmapCanvas.drawCircle(
                    paddingLeft + (contentWidth) / 2.0f + (random.nextInt() % (contentWidth / 2.0f)),
                    paddingTop + (contentHeight) / 2.0f + (random.nextInt() % (contentHeight / 2.0f)),
                    radius, mPaintStar);
        }
    }

    public class MyHandler extends Handler
    {
        private ScreenSaverView screenSaverView;

        public MyHandler(ScreenSaverView screenSaverView)
        {
            this.screenSaverView = screenSaverView;
        }

        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            screenSaverView.invalidate();
        }
}
}


