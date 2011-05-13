package jp.Alarm;

import android.content.Context;

import android.media.MediaPlayer;
import android.util.Log;

/**
 *	�T�E���h���C�u����
 */
public class Sound {
	private	Context		context;
	private MediaPlayer mMediaPlayer;			// �T�E���h�Đ��f�[�^��ێ����܂��B
	private	int			resID;					// ���\�[�XID

	/**
	 * �R���X�g���N�^�[
	 */
	public Sound(Context context) {
		this.context = context;
	}

	/**
	 * �j��
	 */
	public void destroy(){
		if (null != mMediaPlayer){
			mMediaPlayer.stop();				// �T�E���h���~����
			mMediaPlayer.release();				// �j������
			mMediaPlayer = null;
		}
	}

	/**
	 * ���\�[�X����f�[�^��ǂݍ���
	 *	@param	id	���\�[�X�h�c
	 */
	public void readRes(int id){
		resID = id;
	}


	/**
	 * ���\�[�X����f�[�^�̓ǂݍ��݌�Đ�����
	 *	@param	id	���\�[�X�h�c
	 */
	public void readResPlay(int id, boolean loop){
		readRes(id);
		play(loop);
	}

	/**
	 * ���\�[�X����f�[�^��ǂݍ���ōĐ�����
	 *	@param	id	���\�[�X�h�c
	 *	@param	loop	���[�v�w��
	 */
	public void play(boolean loop){
		if (null != mMediaPlayer){
			mMediaPlayer.release();				// �j������
		}
		mMediaPlayer = MediaPlayer.create(context, resID);
		mMediaPlayer.setLooping(loop);
		mMediaPlayer.start();
	}

	/**
	 * �T�E���h���~����
	 */
	public void stop(){
		if (null != mMediaPlayer){
			mMediaPlayer.stop();				// �T�E���h��~
		}
	}
}
