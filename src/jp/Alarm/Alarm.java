
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
 * 日本アンドロイドの会沖縄支部、目覚まし時計
 *
 * <pre>
 *	アプリケーション起動時
 *		onCreate() → onStart() → onResume()
 *	アプリケーション終了時
 *		onPause() → onStop() → onDestroy()
 *	他のアクティビティが起動され、自身が停止
 *		onPause→ onStop()
 *	停止状態から再開
 *		onRestart() → onStart() → onResume()
 *	再開のサイクル
 *		onPause→ (再開したら)→ onResume()
 * </pre>
 */
public class Alarm extends Activity {
	private static final String COMMON_PREF = "CommonPref";
	private static final String PREF_MODE  = "Mode";
	private static final String PREF_TIME  = "Time";
	private static final String PREF_COUNT = "Count";
	private	static final int	SEC	= 1000;
	private	static final int	MIN = SEC*60;
	private	static final int	SNOOZE_TIME = 5;	// 分
	private	static final int	SOUND_STOP_TIME = 2*MIN;

	public	ClockImage			mClockImage;	// 時計ＵＩ
	public	WakeupImage			mWakeupImage;	// タイム起動の画面
	private AlarmManager		mManager;		// アラームマネージャ
	public static int			windowWidth;	// 画面横サイズ
	public static int			windowHeight;	// 画面縦サイズ

	private	Sound				soundAlarm;		// アラーム
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
	 * アプリ起動		（初起動時に呼ばれる。このあと onStart() → onResume() が呼ばれる）
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		windowWidth = display.getWidth();		// ウィンドウ横幅
		windowHeight = display.getHeight();		//	縦幅
		mManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);	// アラームマネージャを取得

		mode = getMode(true);
		GregorianCalendar mCalendar = new GregorianCalendar();	// 現在時刻を取得
		long tm = mCalendar.getTimeInMillis();
		long wt = getTime(0);
		Log.d("MAIN","onCreate "+wt+", "+tm+", "+mode);
		if ((wt-31000) <= tm && (wt+32000) >= tm && !mode){	// タイマー起動
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
		else{									// タイマー設定モード
			if (0 != wt){
				Toast.makeText(this, "起動タイマーをキャンセルしました。", Toast.LENGTH_SHORT).show();
				resetAlarm();
			}
			mode = true;
			mClockImage = new ClockImage(this);	// 時計ＵＩ
			setContentView(mClockImage);		// 表示を時計ＵＩに移す
			mClockImage.setTime(mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE));
		}
		soundSe = new Sound(this);
		appliStop = false;
	}

	/**
	 * 	アラームの設定
	 * @param	time	アラーム時刻
	 */
	private void setAlarm(long time) {
		mManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent());
		setTime(time);
		Log.d("MAIN","setAlarm "+time);
		mode = false;
		setMode(mode);
	}

	/**
	 * 	アラームの解除
	 */
	private void resetAlarm() {
		mManager.cancel(pendingIntent());
		setTime(0);
		mode = true;
		setMode(mode);
	}

	/**
	 * アラームの設定時刻に発生するインテントの作成
	 */
	private PendingIntent pendingIntent() {
		Intent i = new Intent(getApplicationContext(), Alarm.class);
		PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
		return pi;
	}

	/**
	 *	タッチイベント検出
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event){
		if (appliStop) return false;				// 1.6でアプリ終了直後に呼ばれる事があるので対策する。

		int	act = event.getAction();
		if (mode){
			float touchX = event.getX();
			float touchY = event.getY();
			if (act == MotionEvent.ACTION_DOWN		// タッチした
				|| act == MotionEvent.ACTION_MOVE){	// 移動した
				mClockImage.setTouchPos(touchX, touchY, true);
			}
			else if (act == MotionEvent.ACTION_UP){	// 離した
				int r = mClockImage.setTouchPos(touchX, touchY, false);
				if (1 == r){						// セットボタン
					Calendar mCalendar = new GregorianCalendar();	// 現在時刻を取得
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
							awt = m+"分後";
						}
						else{
							awt = h+"時間"+m+"分後";
						}
						mCalendar.add(Calendar.HOUR, h);
						mCalendar.add(Calendar.MINUTE, m);
						String wt = ""+mCalendar.get(Calendar.HOUR_OF_DAY)+"時"+ mCalendar.get(Calendar.MINUTE)+"分";
						Toast.makeText(this, awt+"の"+wt+"に起動します。", Toast.LENGTH_LONG).show();
					}
					long tm = mCalendar.getTimeInMillis();
					setAlarm(tm);
					setCount(0);
					soundSe.readResPlay(R.raw.efct01, false);
					Log.d("MAIN","finish");
					appliStop = true;
					finish();
				}
				else if (2 == r){					// キャンセルボタン
					GregorianCalendar mCalendar = new GregorianCalendar();	// 現在時刻を取得
					mClockImage.setTime(mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE));
					soundSe.readResPlay(R.raw.efct02, false);
				}
				return true;
			}
		}
		else{
			if (act == MotionEvent.ACTION_DOWN){	// タッチした
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
	 * プリファレンスからモード値を読み出す
	 * @return	モード値
	 */
	private boolean getMode(boolean defMode){
		SharedPreferences activity = getPreferences(MODE_PRIVATE);
		return activity.getBoolean(PREF_MODE, defMode);
	}

	/**
	 * プリファレンスにモード値を書き出す
	 * @param	モード値
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
	 * プリファレンスからtime値を読み出す
	 * @return	time
	 */
	private long getTime(long defTime){
		SharedPreferences activity = getPreferences(MODE_PRIVATE);
		return activity.getLong(PREF_TIME, defTime);
	}

	/**
	 * プリファレンスにtime値を書き出す
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
	 * プリファレンスからCount値を読み出す
	 * @return	time
	 */
	private int getCount(int defCnt){
		SharedPreferences activity = getPreferences(MODE_PRIVATE);
		return activity.getInt(PREF_COUNT, defCnt);
	}

	/**
	 * プリファレンスにCount値を書き出す
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
					Calendar mCalendar = new GregorianCalendar();	// 現在時刻を取得
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
					Calendar mCalendar = new GregorianCalendar();	// 現在時刻を取得
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
	 * アプリ開始		（ユーザーから見えるようになる直前に呼ばれる。）
	 */
	@Override
	protected void onStart(){
		super.onStart();
		Log.d("MAIN", "onStart");
	}

	/**
	 * アプリ再起動		（停止後の再起動時に呼ばれる。onStart() → onResume() が呼ばれる）
	 */
	@Override
	protected void onRestart(){
		super.onRestart();
		Log.d("MAIN", "onRestart");
	}

	/**
	 * アプリレジューム	（ユーザーからの入力が行われるようになったときに呼ばれる）
	 */
	@Override
	protected void onResume() {
		super.onResume();
		Log.d("MAIN", "onResume");
	}

	/**
	 * アプリのポーズ	（終了時と別のアクティビティを開始するときに呼ばれる）
	 */
	@Override
	protected void onPause() {
		super.onPause();
		Log.d("MAIN", "onPause");
	}

	/**
	 * アプリ停止		（ユーザーから見えなくなったときに呼ばれる）
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
	 * アプリ破棄		（破棄されるときに呼ばれる）
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
