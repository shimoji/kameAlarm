package jp.Alarm;

import android.view.View;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;

/**
 *	短針のみの時計を表示する
 */
public class ClockImage extends View {
	private	int				width;
	private	int				height;
	private	Paint			paint;

	private	Bitmap			bg;						// 背景
	private	int				bgW, bgH;				// 背景の画像サイズ

	private	Bitmap			clock;					// 文字盤
	private	int				clockW, clockH;			// 文字盤の画像サイズ

	private	Bitmap			hand;					// 針
	private	double			handL;					// 針の回転センターから左上までの距離

	private	Bitmap			setBtn1, setBtn2;		// セットボタン
	private	int				setBtnX, setBtnY;		// セットボタン表示位置
	private	int				setBtnW, setBtnH;		// セットボタンの画像サイズ

	private	Bitmap			cancelBtn1, cancelBtn2;	// キャンセルボタン
	private	int				cancelBtnX, cancelBtnY;	// キャンセルボタン表示位置
	private	int				cancelBtnW, cancelBtnH;	// キャンセルボタンの画像サイズ

	private	int				hou;					// 時
	private	int				min;					// 分

	private	boolean			setBtnTouch;
	private	boolean			cancelBtnTouch;

	/**
	 *	コンストラクター
 	 */
	public ClockImage(Context context){
		super(context);
		init();
	}

	public ClockImage(Context context, AttributeSet attrs){
		super(context, attrs);
		init();
	}

	public ClockImage(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		init();
	}

	/**
	 *	初期化
 	 */
	private void init(){
		paint = new Paint();
		bg = BitmapFactory.decodeResource(getResources(), R.drawable.kame_haikei);		// 背景
		clock = BitmapFactory.decodeResource(getResources(), R.drawable.kame_panel);	// 文字盤
		hand = BitmapFactory.decodeResource(getResources(), R.drawable.kame_hand);		// 針

		setBtn1 = BitmapFactory.decodeResource(getResources(), R.drawable.kame_set_1);		// セットボタン
		setBtn2 = BitmapFactory.decodeResource(getResources(), R.drawable.kame_set_2);		// セットボタン
		cancelBtn1 = BitmapFactory.decodeResource(getResources(), R.drawable.kame_cancel_1);	// キャンセルボタン
		cancelBtn2 = BitmapFactory.decodeResource(getResources(), R.drawable.kame_cancel_2);	// キャンセルボタン

		bgW = bg.getWidth();					// 背景の画像サイズ
		bgH = bg.getHeight();

		clockW = clock.getWidth();				// 文字盤の画像サイズ
		clockH = clock.getHeight();

		int h = hand.getHeight();
		handL = Math.sqrt(h*h + h*h) / 2;		// 針の回転センターから左上までの距離
												// +-+-+----------------	今回cの位置を中心とした。
												// + c +       針キャラ		（左上から中心への角度は45）
												// +-+-+----------------

		setBtnW = setBtn1.getWidth();			// セットボタンの画像サイズ
		setBtnH = setBtn1.getHeight();

		cancelBtnW = cancelBtn1.getWidth();		// キャンセルボタンの画像サイズ
		cancelBtnH = cancelBtn1.getHeight();
		setBtnTouch = false;
		cancelBtnTouch = false;
	}

	/**
	 * 破棄
	 */
	public void destroy(){
		if (null != bg) bg.recycle();
		if (null != clock) clock.recycle();
		if (null != setBtn1) setBtn1.recycle();
		if (null != setBtn2) setBtn2.recycle();
		if (null != cancelBtn1) cancelBtn1.recycle();
		if (null != cancelBtn2) cancelBtn2.recycle();
	}

	/**
	 *	描画
 	 */
	@Override
	protected synchronized void onDraw(Canvas canvas){
		width = getWidth();
		height = getHeight();
		if (null != clock){
			canvas.drawRGB(46, 61, 73);

			int	cx = width / 2;
			int	cy = height / 2;
			int x = cx - bgW / 2;					// 背景の中心は画面の真ん中にする
			int y = cy - bgH / 2;
			canvas.drawBitmap(bg, x, y, paint);		// 背景を描画する

			x = cx - clockW / 2;					// 文字盤の中心は画面の真ん中にする
			y = cy - clockH / 2;
			canvas.drawBitmap(clock, x, y, paint);	// 文字盤を描画する

			float h = hou % 12;			float m = min;
			float a = (360f / 12f * h) + (360f / 12f / 60f * m) - 90; // 針の角度を計算する。（キャラが横向きなので９０度補正する）

			Matrix	matrix = new Matrix();
			matrix.postRotate(a);					// 針用の回転マトリックス

			int ang = (int)(a - 45 + 360) % 360;	// 左上
			x = (int)(Math.sin(ang * Math.PI / 180) * handL);	// 針の左上の位置を計算する
			y = (int)(Math.cos(ang * Math.PI / 180) * handL);
			matrix.postTranslate(cx + x, cy - y);		// マトリックスに対して移動を設定する
			canvas.drawBitmap(hand, matrix, paint);		// 針を描画する

			setBtnX = cx - clockW / 2;					// 文字盤の左側に合わせる
			setBtnY = cancelBtnY = cy + clockH / 2;		// 文字盤の下
			cancelBtnX = cx + clockW / 2 - cancelBtnW;	// 文字盤の右側に合わせる

			if (!setBtnTouch){
				canvas.drawBitmap(setBtn1, setBtnX, setBtnY, paint);		// セットボタンを描画
			}
			else{
				canvas.drawBitmap(setBtn2, setBtnX, setBtnY, paint);		// セットボタンを描画
			}
			if (!cancelBtnTouch){
				canvas.drawBitmap(cancelBtn1, cancelBtnX, cancelBtnY, paint);	// キャンセルボタンを描画
			}
			else{
				canvas.drawBitmap(cancelBtn2, cancelBtnX, cancelBtnY, paint);	// キャンセルボタンを描画
			}
		}
	}

	/**
	 *	時間をセットし再描画をリクエストする
	 *	@param	h	時
	 *	@param	m	分
	 */
	public void setTime(int h, int m){
		hou = h;
		min = m;
	}

	/**
	 *	タッチ位置から時間に変換し、画面再描画をリクエストする。
	 *	@param	tx	タッチ位置Ｘ
	 *	@param	ty	タッチ位置Ｙ
	 *	@param	touchon	タッチした時と移動でtrue,離したときfalse
	 *	@retun 0=文字盤 1=セット 2=キヤンセル -1=それ以外
	 */
	public int setTouchPos(float tx, float ty, boolean touchon){
		if (0 != width && 0 != height){
			tx -= (Alarm.windowWidth - width);		// タッチ位置と表示位置のズレを補正
			ty -= (Alarm.windowHeight - height);

			float centerX = width / 2;				// 画面中心
			float centerY = height / 2;

			float x = centerX - tx;
			float y = centerY - ty;
			float w = clockW / 2;

			if ((w*w) >= (x*x + y*y)){				// 文字盤の中
				double rad = Math.atan2(-x, y);
				int ang = ((int)(rad * 180 / Math.PI) + 360) % 360;
				int h = ang / 30;
				int m = (ang - h*30) * 2;
				setTime(h, m);						// 時間をセット
				this.postInvalidate();				// 描画
				return 0;							// 文字盤
			}
			else{									// 文字盤以外
				if (setBtnX <= tx && (setBtnX+setBtnW) >= tx
					&& setBtnY <= ty && (setBtnY+setBtnH) >= ty){
					setBtnTouch = touchon;
					this.postInvalidate();			// 描画
					return 1;						// セットボタン
				}
				if (cancelBtnX <= tx && (cancelBtnX+cancelBtnW) >= tx
					&& cancelBtnY <= ty && (cancelBtnY+cancelBtnH) >= ty){
					cancelBtnTouch = touchon;
					this.postInvalidate();			// 描画
					return 2;						// キャンセルボタン
				}
			}
		}
		return -1;
	}

	/**
	 *	設定された時間（時）を得る
	 */
	public int getHours(){
		return hou;
	}

	/**
	 *	設定された時間（分）を得る
	 */
	public int getMinutes() {
		return min;
	}
}
