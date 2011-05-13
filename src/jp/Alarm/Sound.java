package jp.Alarm;

import android.content.Context;

import android.media.MediaPlayer;
import android.util.Log;

/**
 *	サウンドライブラリ
 */
public class Sound {
	private	Context		context;
	private MediaPlayer mMediaPlayer;			// サウンド再生データを保持します。
	private	int			resID;					// リソースID

	/**
	 * コンストラクター
	 */
	public Sound(Context context) {
		this.context = context;
	}

	/**
	 * 破棄
	 */
	public void destroy(){
		if (null != mMediaPlayer){
			mMediaPlayer.stop();				// サウンドを停止する
			mMediaPlayer.release();				// 破棄する
			mMediaPlayer = null;
		}
	}

	/**
	 * リソースからデータを読み込む
	 *	@param	id	リソースＩＤ
	 */
	public void readRes(int id){
		resID = id;
	}


	/**
	 * リソースからデータの読み込み後再生する
	 *	@param	id	リソースＩＤ
	 */
	public void readResPlay(int id, boolean loop){
		readRes(id);
		play(loop);
	}

	/**
	 * リソースからデータを読み込んで再生する
	 *	@param	id	リソースＩＤ
	 *	@param	loop	ループ指定
	 */
	public void play(boolean loop){
		if (null != mMediaPlayer){
			mMediaPlayer.release();				// 破棄する
		}
		mMediaPlayer = MediaPlayer.create(context, resID);
		mMediaPlayer.setLooping(loop);
		mMediaPlayer.start();
	}

	/**
	 * サウンドを停止する
	 */
	public void stop(){
		if (null != mMediaPlayer){
			mMediaPlayer.stop();				// サウンド停止
		}
	}
}
