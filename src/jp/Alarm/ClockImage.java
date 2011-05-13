package jp.Alarm;

import android.view.View;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;

/**
 *	�Z�j�݂̂̎��v��\������
 */
public class ClockImage extends View {
	private	int				width;
	private	int				height;
	private	Paint			paint;

	private	Bitmap			bg;						// �w�i
	private	int				bgW, bgH;				// �w�i�̉摜�T�C�Y

	private	Bitmap			clock;					// ������
	private	int				clockW, clockH;			// �����Ղ̉摜�T�C�Y

	private	Bitmap			hand;					// �j
	private	double			handL;					// �j�̉�]�Z���^�[���獶��܂ł̋���

	private	Bitmap			setBtn1, setBtn2;		// �Z�b�g�{�^��
	private	int				setBtnX, setBtnY;		// �Z�b�g�{�^���\���ʒu
	private	int				setBtnW, setBtnH;		// �Z�b�g�{�^���̉摜�T�C�Y

	private	Bitmap			cancelBtn1, cancelBtn2;	// �L�����Z���{�^��
	private	int				cancelBtnX, cancelBtnY;	// �L�����Z���{�^���\���ʒu
	private	int				cancelBtnW, cancelBtnH;	// �L�����Z���{�^���̉摜�T�C�Y

	private	int				hou;					// ��
	private	int				min;					// ��

	private	boolean			setBtnTouch;
	private	boolean			cancelBtnTouch;

	/**
	 *	�R���X�g���N�^�[
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
	 *	������
 	 */
	private void init(){
		paint = new Paint();
		bg = BitmapFactory.decodeResource(getResources(), R.drawable.kame_haikei);		// �w�i
		clock = BitmapFactory.decodeResource(getResources(), R.drawable.kame_panel);	// ������
		hand = BitmapFactory.decodeResource(getResources(), R.drawable.kame_hand);		// �j

		setBtn1 = BitmapFactory.decodeResource(getResources(), R.drawable.kame_set_1);		// �Z�b�g�{�^��
		setBtn2 = BitmapFactory.decodeResource(getResources(), R.drawable.kame_set_2);		// �Z�b�g�{�^��
		cancelBtn1 = BitmapFactory.decodeResource(getResources(), R.drawable.kame_cancel_1);	// �L�����Z���{�^��
		cancelBtn2 = BitmapFactory.decodeResource(getResources(), R.drawable.kame_cancel_2);	// �L�����Z���{�^��

		bgW = bg.getWidth();					// �w�i�̉摜�T�C�Y
		bgH = bg.getHeight();

		clockW = clock.getWidth();				// �����Ղ̉摜�T�C�Y
		clockH = clock.getHeight();

		int h = hand.getHeight();
		handL = Math.sqrt(h*h + h*h) / 2;		// �j�̉�]�Z���^�[���獶��܂ł̋���
												// +-+-+----------------	����c�̈ʒu�𒆐S�Ƃ����B
												// + c +       �j�L����		�i���ォ�璆�S�ւ̊p�x��45�j
												// +-+-+----------------

		setBtnW = setBtn1.getWidth();			// �Z�b�g�{�^���̉摜�T�C�Y
		setBtnH = setBtn1.getHeight();

		cancelBtnW = cancelBtn1.getWidth();		// �L�����Z���{�^���̉摜�T�C�Y
		cancelBtnH = cancelBtn1.getHeight();
		setBtnTouch = false;
		cancelBtnTouch = false;
	}

	/**
	 * �j��
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
	 *	�`��
 	 */
	@Override
	protected synchronized void onDraw(Canvas canvas){
		width = getWidth();
		height = getHeight();
		if (null != clock){
			canvas.drawRGB(46, 61, 73);

			int	cx = width / 2;
			int	cy = height / 2;
			int x = cx - bgW / 2;					// �w�i�̒��S�͉�ʂ̐^�񒆂ɂ���
			int y = cy - bgH / 2;
			canvas.drawBitmap(bg, x, y, paint);		// �w�i��`�悷��

			x = cx - clockW / 2;					// �����Ղ̒��S�͉�ʂ̐^�񒆂ɂ���
			y = cy - clockH / 2;
			canvas.drawBitmap(clock, x, y, paint);	// �����Ղ�`�悷��

			float h = hou % 12;			float m = min;
			float a = (360f / 12f * h) + (360f / 12f / 60f * m) - 90; // �j�̊p�x���v�Z����B�i�L�������������Ȃ̂łX�O�x�␳����j

			Matrix	matrix = new Matrix();
			matrix.postRotate(a);					// �j�p�̉�]�}�g���b�N�X

			int ang = (int)(a - 45 + 360) % 360;	// ����
			x = (int)(Math.sin(ang * Math.PI / 180) * handL);	// �j�̍���̈ʒu���v�Z����
			y = (int)(Math.cos(ang * Math.PI / 180) * handL);
			matrix.postTranslate(cx + x, cy - y);		// �}�g���b�N�X�ɑ΂��Ĉړ���ݒ肷��
			canvas.drawBitmap(hand, matrix, paint);		// �j��`�悷��

			setBtnX = cx - clockW / 2;					// �����Ղ̍����ɍ��킹��
			setBtnY = cancelBtnY = cy + clockH / 2;		// �����Ղ̉�
			cancelBtnX = cx + clockW / 2 - cancelBtnW;	// �����Ղ̉E���ɍ��킹��

			if (!setBtnTouch){
				canvas.drawBitmap(setBtn1, setBtnX, setBtnY, paint);		// �Z�b�g�{�^����`��
			}
			else{
				canvas.drawBitmap(setBtn2, setBtnX, setBtnY, paint);		// �Z�b�g�{�^����`��
			}
			if (!cancelBtnTouch){
				canvas.drawBitmap(cancelBtn1, cancelBtnX, cancelBtnY, paint);	// �L�����Z���{�^����`��
			}
			else{
				canvas.drawBitmap(cancelBtn2, cancelBtnX, cancelBtnY, paint);	// �L�����Z���{�^����`��
			}
		}
	}

	/**
	 *	���Ԃ��Z�b�g���ĕ`������N�G�X�g����
	 *	@param	h	��
	 *	@param	m	��
	 */
	public void setTime(int h, int m){
		hou = h;
		min = m;
	}

	/**
	 *	�^�b�`�ʒu���玞�Ԃɕϊ����A��ʍĕ`������N�G�X�g����B
	 *	@param	tx	�^�b�`�ʒu�w
	 *	@param	ty	�^�b�`�ʒu�x
	 *	@param	touchon	�^�b�`�������ƈړ���true,�������Ƃ�false
	 *	@retun 0=������ 1=�Z�b�g 2=�L�����Z�� -1=����ȊO
	 */
	public int setTouchPos(float tx, float ty, boolean touchon){
		if (0 != width && 0 != height){
			tx -= (Alarm.windowWidth - width);		// �^�b�`�ʒu�ƕ\���ʒu�̃Y����␳
			ty -= (Alarm.windowHeight - height);

			float centerX = width / 2;				// ��ʒ��S
			float centerY = height / 2;

			float x = centerX - tx;
			float y = centerY - ty;
			float w = clockW / 2;

			if ((w*w) >= (x*x + y*y)){				// �����Ղ̒�
				double rad = Math.atan2(-x, y);
				int ang = ((int)(rad * 180 / Math.PI) + 360) % 360;
				int h = ang / 30;
				int m = (ang - h*30) * 2;
				setTime(h, m);						// ���Ԃ��Z�b�g
				this.postInvalidate();				// �`��
				return 0;							// ������
			}
			else{									// �����ՈȊO
				if (setBtnX <= tx && (setBtnX+setBtnW) >= tx
					&& setBtnY <= ty && (setBtnY+setBtnH) >= ty){
					setBtnTouch = touchon;
					this.postInvalidate();			// �`��
					return 1;						// �Z�b�g�{�^��
				}
				if (cancelBtnX <= tx && (cancelBtnX+cancelBtnW) >= tx
					&& cancelBtnY <= ty && (cancelBtnY+cancelBtnH) >= ty){
					cancelBtnTouch = touchon;
					this.postInvalidate();			// �`��
					return 2;						// �L�����Z���{�^��
				}
			}
		}
		return -1;
	}

	/**
	 *	�ݒ肳�ꂽ���ԁi���j�𓾂�
	 */
	public int getHours(){
		return hou;
	}

	/**
	 *	�ݒ肳�ꂽ���ԁi���j�𓾂�
	 */
	public int getMinutes() {
		return min;
	}
}
