
package jp.Alarm;

import java.util.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Build;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.DialogInterface;

import android.util.Log;
import android.view.*;
import android.widget.Toast;

/**
 * ���{�A���h���C�h�̉��x���A�ڊo�܂����v
 *
 * <pre>
 *	�A�v���P�[�V�����N����
 *		onCreate() �� onStart() �� onResume()
 *	�A�v���P�[�V�����I����
 *		onPause() �� onStop() �� onDestroy()
 *	���̃A�N�e�B�r�e�B���N������A���g����~
 *		onPause�� onStop()
 *	��~��Ԃ���ĊJ
 *		onRestart() �� onStart() �� onResume()
 *	�ĊJ�̃T�C�N��
 *		onPause�� (�ĊJ������)�� onResume()
 * </pre>
 */
public class Alarm extends Activity {
	private static final String COMMON_PREF = "CommonPref";
	private static final String PREF_MODE  = "Mode";
	private static final String PREF_TIME  = "Time";
	private static final String PREF_COUNT = "Count";
	private	static final int	SEC	= 1000;
	private	static final int	MIN = SEC*60;
	private	static final int	SNOOZE_TIME = 5;	// ��
	private	static final int	SOUND_STOP_TIME = 2*MIN;

	public	ClockImage			mClockImage;	// ���v�t�h
	public	WakeupImage			mWakeupImage;	// �^�C���N���̉��
	private AlarmManager		mManager;		// �A���[���}�l�[�W��
	public static int			windowWidth;	// ��ʉ��T�C�Y
	public static int			windowHeight;	// ��ʏc�T�C�Y

	private	Sound				soundAlarm;		// �A���[��
	private	Sound				soundSe;		// SE

	private	boolean				mode;
	private	boolean				appliStop = false;

	private	int					touchCount;
	private	long				touchTime;
	private	SnoozeTh			snoozeTh;
	private	boolean				snoozeRun;
	private	int					sndReqCnt;
	private	SoundStopTh			soundStopTh;
	private	boolean				soundStopRun;

	/**
	 * �A�v���N��		�i���N�����ɌĂ΂��B���̂��� onStart() �� onResume() ���Ă΂��j
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		windowWidth = display.getWidth();		// �E�B���h�E����
		windowHeight = display.getHeight();		//	�c��
		mManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);	// �A���[���}�l�[�W�����擾

		mode = getMode(true);
		GregorianCalendar mCalendar = new GregorianCalendar();	// ���ݎ������擾
		long tm = mCalendar.getTimeInMillis();
		long wt = getTime(0);
		Log.d("MAIN","onCreate "+wt+", "+tm+", "+mode);
		if ((wt-31000) <= tm && (wt+32000) >= tm && !mode){	// �^�C�}�[�N��
			mWakeupImage = new WakeupImage(this);
			setContentView(mWakeupImage);
			soundAlarm = new Sound(this);
			soundAlarm.readRes(R.raw.alarm);
			soundAlarm.play(true);
			soundStopTh = new SoundStopTh();
			soundStopTh.start();
			sndReqCnt = getCount(0) + 1;
			setCount(sndReqCnt);
		}
		else{									// �^�C�}�[�ݒ胂�[�h
			if (0 != wt){
				Toast.makeText(this, "�N���^�C�}�[���L�����Z�����܂����B", Toast.LENGTH_SHORT).show();
				resetAlarm();
			}
			mode = true;
			mClockImage = new ClockImage(this);	// ���v�t�h
			setContentView(mClockImage);		// �\�������v�t�h�Ɉڂ�
			mClockImage.setTime(mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE));
		}
		soundSe = new Sound(this);
		appliStop = false;
	}

	/**
	 * 	�A���[���̐ݒ�
	 * @param	time	�A���[������
	 */
	private void setAlarm(long time) {
		mManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent());
		setTime(time);
		Log.d("MAIN","setAlarm "+time);
		mode = false;
		setMode(mode);
	}

	/**
	 * 	�A���[���̉���
	 */
	private void resetAlarm() {
		mManager.cancel(pendingIntent());
		setTime(0);
		mode = true;
		setMode(mode);
	}

	/**
	 * �A���[���̐ݒ莞���ɔ�������C���e���g�̍쐬
	 */
	private PendingIntent pendingIntent() {
		Intent i = new Intent(getApplicationContext(), Alarm.class);
		PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
		return pi;
	}

	/**
	 *	�^�b�`�C�x���g���o
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event){
		if (appliStop) return false;				// 1.6�ŃA�v���I������ɌĂ΂�鎖������̂ő΍􂷂�B

		int	act = event.getAction();
		if (mode){
			float touchX = event.getX();
			float touchY = event.getY();
			if (act == MotionEvent.ACTION_DOWN		// �^�b�`����
				|| act == MotionEvent.ACTION_MOVE){	// �ړ�����
				mClockImage.setTouchPos(touchX, touchY, true);
			}
			else if (act == MotionEvent.ACTION_UP){	// ������
				int r = mClockImage.setTouchPos(touchX, touchY, false);
				if (1 == r){						// �Z�b�g�{�^��
					Calendar mCalendar = new GregorianCalendar();	// ���ݎ������擾
					Date trialTime = new Date();
					mCalendar.setTime(trialTime);
					int sh = mClockImage.getHours() % 12;
					int sm = mClockImage.getMinutes();
					int ch = mCalendar.get(Calendar.HOUR_OF_DAY) % 12;
					int cm = mCalendar.get(Calendar.MINUTE);
					int h = ((sh+24) - ch) % 12;
					int m = sm - cm;
//					h = 0;
//					m = 1;
					if (0 > m){
						m += 60;
						h--;
						if (0 > h) h += 12;
					}
					if (0 != h || 0 != m){
						String	awt = "";
						if (0 == h){
							awt = m+"����";
						}
						else{
							awt = h+"����"+m+"����";
						}
						mCalendar.add(Calendar.HOUR, h);
						mCalendar.add(Calendar.MINUTE, m);
						String wt = ""+mCalendar.get(Calendar.HOUR_OF_DAY)+"��"+ mCalendar.get(Calendar.MINUTE)+"��";
						Toast.makeText(this, awt+"��"+wt+"�ɋN�����܂��B", Toast.LENGTH_LONG).show();
					}
					long tm = mCalendar.getTimeInMillis();
					setAlarm(tm);
					setCount(0);
					soundSe.readResPlay(R.raw.efct01, false);
					Log.d("MAIN","finish");
					appliStop = true;
					finish();
				}
				else if (2 == r){					// �L�����Z���{�^��
					GregorianCalendar mCalendar = new GregorianCalendar();	// ���ݎ������擾
					mClockImage.setTime(mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE));
					soundSe.readResPlay(R.raw.efct02, false);
				}
				return true;
			}
		}
		else{
			if (act == MotionEvent.ACTION_DOWN){	// �^�b�`����
				mWakeupImage.drawStop();
//				setMode(true);
				soundAlarm.stop();
				soundSe.readResPlay(R.raw.efct03, false);
				long time = System.currentTimeMillis();
				if (0 == touchCount || (touchTime+SEC) > time){
					touchCount++;
				}
				else{
					touchCount = 1;
				}
				touchTime = time;
				if (3 == touchCount || 4 <= sndReqCnt){
					snoozeRun = false;
					soundStopRun = false;
					setMode(true);
					setTime(0);
					finish();
				}
				else{
					if (null == snoozeTh){
						Log.d("MAIN","snooze "+touchCount);
						snoozeRun = true;
						snoozeTh = new SnoozeTh();
						snoozeTh.start();
					}
				}
			}
		}
		return false;
	}

	/**
	 * �v���t�@�����X���烂�[�h�l��ǂݏo��
	 * @return	���[�h�l
	 */
	private boolean getMode(boolean defMode){
		SharedPreferences activity = getPreferences(MODE_PRIVATE);
		return activity.getBoolean(PREF_MODE, defMode);
	}

	/**
	 * �v���t�@�����X�Ƀ��[�h�l�������o��
	 * @param	���[�h�l
	 */
	private void setMode(boolean mode){
		Log.d("MAIN","setMode "+mode);
		SharedPreferences common = getSharedPreferences(COMMON_PREF, MODE_WORLD_READABLE | MODE_WORLD_WRITEABLE);
		Editor editor = common.edit();
		SharedPreferences activity = getPreferences(MODE_PRIVATE);
		editor = activity.edit();
		editor.putBoolean(PREF_MODE, mode);
		editor.commit();
	}

	/**
	 * �v���t�@�����X����time�l��ǂݏo��
	 * @return	time
	 */
	private long getTime(long defTime){
		SharedPreferences activity = getPreferences(MODE_PRIVATE);
		return activity.getLong(PREF_TIME, defTime);
	}

	/**
	 * �v���t�@�����X��time�l�������o��
	 * @param	time
	 */
	private void setTime(long time){
		SharedPreferences common = getSharedPreferences(COMMON_PREF, MODE_WORLD_READABLE | MODE_WORLD_WRITEABLE);
		Editor editor = common.edit();
		SharedPreferences activity = getPreferences(MODE_PRIVATE);
		editor = activity.edit();
		editor.putLong(PREF_TIME, time);
		editor.commit();
	}

	/**
	 * �v���t�@�����X����Count�l��ǂݏo��
	 * @return	time
	 */
	private int getCount(int defCnt){
		SharedPreferences activity = getPreferences(MODE_PRIVATE);
		return activity.getInt(PREF_COUNT, defCnt);
	}

	/**
	 * �v���t�@�����X��Count�l�������o��
	 * @param	time
	 */
	private void setCount(int cnt){
		SharedPreferences common = getSharedPreferences(COMMON_PREF, MODE_WORLD_READABLE | MODE_WORLD_WRITEABLE);
		Editor editor = common.edit();
		SharedPreferences activity = getPreferences(MODE_PRIVATE);
		editor = activity.edit();
		editor.putInt(PREF_COUNT, cnt);
		editor.commit();
	}

	/**
	 * Snooze
	 */
	class SnoozeTh extends Thread{
		private	int		tc;

		SnoozeTh(){
			super();
			tc = touchCount;
		}

		@Override
		public void run(){
			while(snoozeRun){
				long t = touchTime+SEC - System.currentTimeMillis();
				try {
					Thread.sleep(t);
				}
				catch(Exception e){
				}
				if (tc != touchCount){
					tc = touchCount;
				}
				else{
					Calendar mCalendar = new GregorianCalendar();	// ���ݎ������擾
					mCalendar.setTime(new Date());
					mCalendar.add(Calendar.MINUTE, SNOOZE_TIME);
					setAlarm(mCalendar.getTimeInMillis());
					appliStop = true;
					finish();
					break;
				}
			}
			snoozeTh = null;
		}
	}

	/**
	 * Sound stop
	 */
	class SoundStopTh extends Thread{
		SoundStopTh(){
			super();
			soundStopRun = true;
		}

		@Override
		public void run(){
			try {
				Thread.sleep(SOUND_STOP_TIME);
			}
			catch(Exception e){
			}
			if (soundStopRun){
				mWakeupImage.drawStop();
//				setMode(true);
				soundAlarm.stop();
				if (3 == touchCount || 4 <= sndReqCnt){
					snoozeRun = false;
					soundStopRun = false;
					setMode(true);
					setTime(0);
					finish();
				}
				else{
					Calendar mCalendar = new GregorianCalendar();	// ���ݎ������擾
					mCalendar.setTime(new Date());
					mCalendar.add(Calendar.MINUTE, SNOOZE_TIME);
					setAlarm(mCalendar.getTimeInMillis());
					appliStop = true;
					finish();
				}
			}
			soundStopTh = null;
		}
	}

	/**
	 * �A�v���J�n		�i���[�U�[���猩����悤�ɂȂ钼�O�ɌĂ΂��B�j
	 */
	@Override
	protected void onStart(){
		super.onStart();
		Log.d("MAIN", "onStart");
	}

	/**
	 * �A�v���ċN��		�i��~��̍ċN�����ɌĂ΂��BonStart() �� onResume() ���Ă΂��j
	 */
	@Override
	protected void onRestart(){
		super.onRestart();
		Log.d("MAIN", "onRestart");
	}

	/**
	 * �A�v�����W���[��	�i���[�U�[����̓��͂��s����悤�ɂȂ����Ƃ��ɌĂ΂��j
	 */
	@Override
	protected void onResume() {
		super.onResume();
		Log.d("MAIN", "onResume");
	}

	/**
	 * �A�v���̃|�[�Y	�i�I�����ƕʂ̃A�N�e�B�r�e�B���J�n����Ƃ��ɌĂ΂��j
	 */
	@Override
	protected void onPause() {
		super.onPause();
		Log.d("MAIN", "onPause");
	}

	/**
	 * �A�v����~		�i���[�U�[���猩���Ȃ��Ȃ����Ƃ��ɌĂ΂��j
	 */
	@Override
	public void onStop(){
		super.onStop();
		Log.d("MAIN", "onStop");
		if (!mode && null != mWakeupImage){
			mWakeupImage.drawStop();
//			setMode(true);
			soundAlarm.stop();
			long time = System.currentTimeMillis();
			if (0 == touchCount || (touchTime+SEC) > time){
				touchCount++;
			}
			else{
				touchCount = 1;
			}
			touchTime = time;
			if (3 == touchCount || 4 <= sndReqCnt){
				snoozeRun = false;
				soundStopRun = false;
				setTime(0);
			}
			else{
				if (null == snoozeTh){
					snoozeRun = true;
					snoozeTh = new SnoozeTh();
					snoozeTh.start();
				}
			}
		}
	}

	/**
	 * �A�v���j��		�i�j�������Ƃ��ɌĂ΂��j
	 */
	@Override
	public void onDestroy(){
		super.onDestroy();
		Log.d("MAIN","onDestroy");
		snoozeRun = false;
		soundStopRun = false;
		if (null != soundAlarm){
			soundAlarm.stop();
			soundAlarm.destroy();
		}
		if (null != mClockImage){
			mClockImage.destroy();
		}
		if (null != mWakeupImage){
			mWakeupImage.destroy();
		}
	}
}
