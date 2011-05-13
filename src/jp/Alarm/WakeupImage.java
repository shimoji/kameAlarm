package jp.Alarm;

import android.view.View;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;

/**
 *	ƒ^ƒCƒ}[‹N“®‚µAƒJƒƒƒ{‚ğƒRƒ“ƒgƒ[ƒ‹’†‚É•\¦‚³‚ê‚é”wŒi‚ğ•\¦‚·‚éƒNƒ‰ƒXB
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
	 *	ƒRƒ“ƒXƒgƒ‰ƒNƒ^[
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
	 *	‰Šú‰»
 	 */
	private void init(){
		paint = new Paint();
		image = new Bitmap[imageRes.length];
		for (int i=0; i<imageRes.length; i++){
			image[i] = BitmapFactory.decodeResource(getResources(), imageRes[i]);
		}
		bg = BitmapFactory.decodeResource(getResources(), R.drawable.kame_haikei);		// ”wŒi
		imageNum = 0;
		drawRun = true;
		drawTh = new DrawThread();
		drawTh.start();
	}

	/**
	 * ”jŠü
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
	 *	•`‰æ
 	 */
	@Override
	protected synchronized void onDraw(Canvas canvas){
		width = getWidth();
		height = getHeight();
		if (null != bg){
			canvas.drawRGB(46, 61, 73);
			int	cx = width / 2;
			int	cy = height / 2;
			int x = cx - bg.getWidth() / 2;					// ”wŒi‚Ì’†S‚Í‰æ–Ê‚Ì^‚ñ’†‚É‚·‚é
			int y = cy - bg.getHeight() / 2;
			canvas.drawBitmap(bg, x, y, paint);				// ”wŒi‚ğ•`‰æ‚·‚é

			x = width/2 - image[imageNum].getWidth()/2;
			y = height/2 - image[imageNum].getHeight()/2;
			canvas.drawBitmap(image[imageNum], x, y, paint);	// •`‰æ‚·‚é
		}
	}

	/**
	 * •`‰æ‚ğ’â~‚·‚é
	 */
	public void drawStop(){
		drawRun = false;
	}

	/**
	 * •`‰æ‚ğÄŠJ‚·‚é
	 */
	public void drawRestart(){
		drawRun = true;
		if (null == drawTh){
			drawTh = new DrawThread();
			drawTh.start();
		}
	}

	/**
	 * •`‰æƒXƒŒƒbƒh
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
