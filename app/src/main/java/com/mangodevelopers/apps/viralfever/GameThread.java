package com.mangodevelopers.apps.viralfever;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class GameThread extends Thread
{
    private static final String TAG = "GameThread";

    private Blip[] mBlips;

    private SurfaceHolder mSurfaceHolder;
    private Paint background;

    private int mCanvasWidth;
    private int mCanvasHeight;

    private boolean mRun = false;
    private boolean mTouched = false;
    private int mTouchX;
    private int mTouchY;

    private long mLastTime;
    private int mFrameCount;

    //Board Size/Dimension
    private int boardDimension=8;
    private Cell[][] board;

    //Players details
    private int playerTurn=0;
    private int totalPlayerCount=2;
    private static final int[] colors = new int[] {
            0xd0edd400,
            0xd073d216,
            0xd0c17d11,
            0xd0f57900,
            0xd075507b,
            0xd0cc0000,
            0xd03465a4,
    };

    public GameThread(SurfaceHolder surfaceHolder)
    {
        background = new Paint();
        background.setColor(0xffeeeeec);

        mSurfaceHolder = surfaceHolder;

    }

    public void doStart()
    {
        synchronized (mSurfaceHolder) {
            createBlips();
            createCells();
            mLastTime = System.currentTimeMillis();
            mFrameCount = 0;
            mRun = true;
        }
    }

    @Override
    public void run()
    {
        Log.d(TAG, "run()");
        while(mRun) {
            long now = System.currentTimeMillis();

            Canvas c = null;
            try {
                c = mSurfaceHolder.lockCanvas();
                synchronized (mSurfaceHolder) {
                    updatePhysics();
                    updateScreen(c);
                }
            } finally {
                if (c != null) {
                    mSurfaceHolder.unlockCanvasAndPost(c);
                }
            }

            mFrameCount++;
            int delta = (int)(now - mLastTime);

            if (delta > 5000) {
                double deltaSeconds = delta / 1000.0;
                double fps = mFrameCount / deltaSeconds;
                //Log.d(TAG, "FPS: " + fps + " average over " + deltaSeconds + " seconds.");

                mLastTime = System.currentTimeMillis();
                mFrameCount = 0;
            }
        }

        Log.d(TAG, "run(): done");
    }

    public void setSurfaceSize(int width, int height)
    {
        mCanvasWidth = width;
        mCanvasHeight = height;
    }

    public void createBlips()
    {
        // Switch to some type of list instead of array
        int num_blips = 0;
        mBlips = new Blip[num_blips + 1];

        for (int i = 0; i < mBlips.length - 1; i++) {
            mBlips[i] = new Blip(mCanvasWidth, mCanvasHeight);
        }
    }
    public void createCells()
    {
        //TODO Switch to some type of list instead of array
        board=new Cell[boardDimension][boardDimension];
        for(int i=0;i<boardDimension;i++){
            for(int j=0;j<boardDimension;j++){
                board[i][j]=new Cell();
            }
        }
    }

    public void setRunning(boolean b)
    {
        mRun = b;
    }

    public void updatePhysics()
    {
        for (int i = 0; i < mBlips.length; i++) {
            if (mBlips[i] == null) continue; // Remove soon!
            mBlips[i].step(mBlips);
        }

    }

    public void updateScreen(Canvas canvas)
    {
        // Optimization: drawColor() ?
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(),
                        background);
        //Draw grid
        drawGrid(canvas);

        //Draw Players Name
        drawPlayersName(canvas);

        //populate board with viruses
        drawBoardContent(canvas);

        //
        for (int i = 0; i < mBlips.length; i++) {
            if (mBlips[i] == null) continue;

            // These paint objects should obviously be cached.
            Paint foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
            foreground.setColor(mBlips[i].color);

            Path path = new Path();
            path.addCircle((int)mBlips[i].x, (int)mBlips[i].y, mBlips[i].radius, Direction.CW);

            canvas.drawPath(path, foreground);

        }

        if (mTouched) {
            Log.d(TAG, "Drawing circle at " + mTouchX + "," + mTouchY);

            Paint foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
            foreground.setColor(0x80729fcf);

            Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
            stroke.setColor(0x80204a87);
            stroke.setStyle(Paint.Style.STROKE);
            Log.d(TAG, "default stroke width: " + stroke.getStrokeWidth());
            stroke.setStrokeWidth(2);

            Path path = new Path();
            path.addCircle(mTouchX, mTouchY, 60, Direction.CW);

            canvas.drawPath(path, foreground);
            canvas.drawPath(path, stroke);
        }

    }

    public void onTouch(int action, int x, int y)
    {
        synchronized (mSurfaceHolder) {

            if (action == MotionEvent.ACTION_UP) {
                Log.d(TAG, "ACTION_UP");
                //TODO declare these variables globally
                float cellWidth=mCanvasWidth/(boardDimension+2);
                float cellHeight=mCanvasWidth/(boardDimension+2);
                float marginTop=mCanvasHeight/(boardDimension+2);
                float marginLeft=mCanvasWidth/(boardDimension+2);
                if(y>marginTop&&y<(marginTop+cellHeight*boardDimension)&&x>marginLeft&&x<marginLeft+cellWidth*boardDimension)
                {
                    int cellX=(int)((x-marginLeft)/cellWidth);
                    int cellY=(int)((y-marginTop)/cellHeight);
                    Log.d(TAG, "cellX"+cellX+" cellY"+cellY);

                    if(board[cellX][cellY].getCellOwner()==-1){
                        board[cellX][cellY].setCellOwner(playerTurn);
                    }
                    if(board[cellX][cellY].getCellOwner()==playerTurn){
                        board[cellX][cellY].increaseVirus();
                        changeTurn();
                    }

                    Blip blip = new Blip();
                    blip.x = x;
                    blip.y = y;
                    blip.explode();
                    mBlips[mBlips.length-1] = blip;


                }
                mTouched = false; // Work-around to not show the touch circle
            } else if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                mTouched = true;
                mTouchX = x;
                mTouchY = y;
            }
        }
    }

    //draws the grid matrix on canvas
    public void drawGrid(Canvas canvas){
        //TODO declare these variables globally
        float cellWidth=mCanvasWidth/(boardDimension+2);
        float cellHeight=mCanvasWidth/(boardDimension+2);
        float marginTop=mCanvasHeight/(boardDimension+2);
        float marginLeft=mCanvasWidth/(boardDimension+2);

        Paint paint = new Paint();
        paint.setStrokeWidth(10);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);

        //Draw all cells
        for(int i=0;i<boardDimension;i++){
            for(int j=0;j<boardDimension;j++){
                canvas.drawRect(marginLeft+(i*cellWidth), marginTop+(j*cellHeight), marginLeft+(i+1)*cellWidth, marginTop+(j+1)*cellHeight, paint);
            }
        }

    }

    //draws players name and color at bottom on canvas
    public void drawPlayersName(Canvas canvas){
        Paint paint = new Paint();

        paint.setColor(colors[playerTurn]);
        paint.setTextSize(mCanvasWidth/12);
        canvas.drawText("Player " + playerTurn+"'s turn." , mCanvasWidth/2-mCanvasWidth/4 , mCanvasHeight*9/10, paint);
    }

    //switches turn to next player
    public void changeTurn(){
        playerTurn=(playerTurn+1)%totalPlayerCount;
    }

    //draws all the content of the cells of board
    public void drawBoardContent(Canvas canvas){
        //TODO declare these variables globally
        float cellWidth=mCanvasWidth/(boardDimension+2);
        float cellHeight=mCanvasWidth/(boardDimension+2);
        float marginTop=mCanvasHeight/(boardDimension+2);
        float marginLeft=mCanvasWidth/(boardDimension+2);

        for(int i=0;i<boardDimension;i++){
            for(int j=0;j<boardDimension;j++){
                Cell cell = board[i][j];

                if(cell.getNumberOfViruses()>0){

                    // Initial draw-properties of circle or virus
                    Paint foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
                    foreground.setColor(colors[cell.getCellOwner()]);
                    Path path = new Path();
                    int radius=10;

                    switch (cell.getNumberOfViruses()){
                        case 1:
                            path.addCircle( (int)(marginLeft+(i+0.5)*cellWidth), (int)(marginTop+(j+0.5)*cellHeight), radius, Direction.CW);
                            canvas.drawPath(path, foreground);
                            break;
                        case 2:
                            path.addCircle( (int)(marginLeft+(i+0.25)*cellWidth), (int)(marginTop+(j+0.25)*cellHeight), radius, Direction.CW);
                            path.addCircle( (int)(marginLeft+(i+0.75)*cellWidth), (int)(marginTop+(j+0.75)*cellHeight), radius, Direction.CW);
                            canvas.drawPath(path, foreground);
                            break;
                        case 3:
                            path.addCircle( (int)(marginLeft+(i+0.25)*cellWidth), (int)(marginTop+(j+0.33)*cellHeight), radius, Direction.CW);
                            path.addCircle( (int)(marginLeft+(i+0.75)*cellWidth), (int)(marginTop+(j+0.33)*cellHeight), radius, Direction.CW);
                            path.addCircle( (int)(marginLeft+(i+0.5)*cellWidth), (int)(marginTop+(j+0.66)*cellHeight), radius, Direction.CW);
                            canvas.drawPath(path, foreground);
                            break;
                        default:
                    }
                }
            }
        }
    }
}
