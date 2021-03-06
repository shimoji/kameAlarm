package jp.Alarm;

import android.view.View;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;

/**
 *	タイマー起動し、カメロボをコントロール中に表示される背景を表示するクラス。
 */
public class WakeupImage extends View {
	private	WakeupImage		rWakeupImage = this;
	private	int				width;
	private	int				height;
	private	Paint			paint;

	private	Bitmap[]		image;
	private	Bitmap			bg;

	private	int				imageNum;
	private	int				animCnt;
	public	DrawThread		drawTh;
	private	boolean			drawRun;

	private	int				imageRes[] = {
			R.drawable.w01,R.drawable.w02,R.drawable.w03,R.drawable.w04,
			R.drawable.w05,R.drawable.w06,R.drawable.w07,
	};

	private int				animData[][] = {
			{ 0, 2000},
			{ 1,  200},
			{ 0,  400},
			{ 2,  300},
			{ 0, 1000},
			{ 3,  200},
			{ 4,  200},
			{ 5,  500},
			{ 6,  300},
			{ 5,  200},
			{ 6,  300},
			{ 5,  400},
			{ 4,  200},
			{ 3,  200},
			{ 0, 2000}
	};

	/**
	 *	コンストラクター
 	 */
	public WakeupImage(Context context){
		super(context);
		init();
	}

	public WakeupImage(Context context, AttributeSet attrs){
		super(context, attrs);
		init();
	}

	public WakeupImage(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		init();
	}

	/**
	 *	初期化
 	 */
	private void init(){
		paint = new Paint();
		image = new Bitmap[imageRes.length];
		for (int i=0; i<imageRes.length; i++){
			image[i] = BitmapFactory.decodeResource(getResources(), imageRes[i]);
		}
		bg = BitmapFactory.decodeResource(getResources(), R.drawable.kame_haikei);		// 背景
		imageNum = 0;
		drawRun = true;
		drawTh = new DrawThread();
		drawTh.start();
	}

	/**
	 * 破棄
	 */
	public void destroy(){
		if (null != bg) bg.recycle();
		if (null != image){
			for (int i=0; i<image.length; i++){
				if (null != image[i]) image[i].recycle();
			}
		}
	}

	/**
	 *	描画
 	 */
	@Override
	protected synchronized void onDraw(Canvas canvas){
		width = getWidth();
		height = getHeight();
		if (null != bg){
			canvas.drawRGB(46, 61, 73);
			int	cx = width / 2;
			int	cy = height / 2;
			int x = cx - bg.getWidth() / 2;					// 背景の中心は画面の真ん中にする
			int y = cy - bg.getHeight() / 2;
			canvas.drawBitmap(bg, x, y, paint);				// 背景を描画する

			x = width/2 - image[imageNum].getWidth()/2;
			y = height/2 - image[imageNum].getHeight()/2;
			canvas.drawBitmap(image[imageNum], x, y, paint);	// 描画する
		}
	}

	/**
	 * 描画を停止する
	 */
	public void drawStop(){
		drawRun = false;
	}

	/**
	 * 描画を再開する
	 */
	public void drawRestart(){
		drawRun = true;
		if (null == drawTh){
			drawTh = new DrawThread();
			drawTh.start();
		}
	}

	/**
	 * 描画スレッド
	 */
	class DrawThread extends Thread{
		DrawThread(){
			super();
		}

		@Override
		public void run(){
			animCnt = 0;
			while(drawRun){
				for (animCnt=0; animCnt<animData.length; animCnt++){
					imageNum = animData[animCnt][0];
					rWakeupImage.postInvalidate();
					try {
						Thread.sleep(animData[animCnt][1]);
					}
					catch(Exception e){
					}
					if (!drawRun) break;
				}
			}
			drawTh = null;
		}
	}
}
