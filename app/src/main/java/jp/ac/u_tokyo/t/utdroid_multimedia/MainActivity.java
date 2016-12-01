package jp.ac.u_tokyo.t.utdroid_multimedia;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.VideoView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    /* Viewを格納する変数 */
    private SeekBar seekBarMusic;
    private Button buttonMusicPlay;
    private Button buttonMusicPause;
    private Button buttonMusicStop;
    private VideoView videoView;

    /* 音楽を再生するための変数 */
    private MediaPlayer mediaPlayer;

    /* 再生時間を示すスライドバーを制御するための変数 */
    private Timer timer;
    private Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* ボリュームキーの対象を着信音量からメディア音量に変更 */
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        /* それぞれの名前に対応するViewを取得する */
        seekBarMusic = (SeekBar) findViewById(R.id.seekBarMusic);
        buttonMusicPlay = (Button) findViewById(R.id.buttonMusicPlay);
        buttonMusicPause = (Button) findViewById(R.id.buttonMusicPause);
        buttonMusicStop = (Button) findViewById(R.id.buttonMusicStop);
        videoView = (VideoView) findViewById(R.id.videoView);

        /* スライドバーを操作した時の動作を指定する */
        seekBarMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                /* 一時停止 */
                mediaPlayer.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                /* 位置をセット */
                int msec = seekBar.getProgress();
                mediaPlayer.seekTo(msec);

                /* 再生 */
                mediaPlayer.start();
            }
        });

        buttonMusicPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* 再生 */
                mediaPlayer.start();
            }
        });

        buttonMusicPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* 一時停止 */
                mediaPlayer.pause();
            }
        });

        buttonMusicStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* 停止 */
                mediaPlayer.pause();

                /* 先頭に戻す */
                mediaPlayer.seekTo(0);
                seekBarMusic.setProgress(0);
            }
        });

        /**
         * ここから動画関連
         */
        /* 再生ボタンやシークバーの作成をOSに任せる */
        videoView.setMediaController(new MediaController(this));

        /* 動画プレーヤーの準備（res/rawフォルダ内のファイルを再生する場合）*/
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.aurora));

        /* 動画プレーヤーの準備（SDカード内のファイルを再生する場合）*/
        /* videoView.setVideoPath(Environment.getExternalStorageDirectory() + "/filename.mp4"); */
    }

    /**
     * アプリケーションの再開時に呼ばれるメソッド
     */
    @Override
    public void onStart() {
        super.onStart();

        /* 音楽プレーヤーの準備（rawフォルダから読み出す場合） */
        mediaPlayer = MediaPlayer.create(this, R.raw.beethoven);

        /* 音楽プレイヤーの準備（SDカードから読み出す場合） */
        /* mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(Environment.getExternalStorageDirectory() + "/filename.mp3");
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        } */

        /* スライドバーの最大値を曲の長さ（ms単位）に */
        seekBarMusic.setMax(mediaPlayer.getDuration());
    }

    /**
     * アプリケーションの再開時に呼ばれるメソッド
     */
    @Override
    public void onResume() {
        super.onResume();

        /* シークバーをタイマーで定期更新する */
        handler = new Handler();
        timer = new Timer();
        /* 第1引数に実行したいタスク、第2引数に初期遅延、第3引数に間隔を指定する */
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                /* UIを触るのでHandler経由でメインスレッドにタスクを投げる */
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        /* 再生中ならスライドバーを動かす */
                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            seekBarMusic.setProgress(mediaPlayer.getCurrentPosition());
                        }
                    }
                });
            }
        }, 500, 500);
    }

    /**
     * アプリケーションの休止時に呼ばれるメソッド
     */
    @Override
    public void onPause() {
        /* タイマーを解除する */
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        super.onPause();
    }

    /**
     * アプリケーションの終了時に呼ばれるメソッド
     */
    @Override
    public void onStop() {
        /* MediaPlayerの後片付け */
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer = null;
        }

        super.onStop();
    }
}
