package com.jlyx.sodukugame

import android.media.MediaPlayer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.widget.Button
import com.jlyx.app.controllers.AnimationController
import com.jlyx.sodukugame.customViews.SodukuView

class MainActivity : AppCompatActivity() {
    private var mMediaPlayer: MediaPlayer? = null
    private var sodukuV_main_activity: SodukuView? = null
    var mMusicIsPlaying = true

    companion object {
        var ctx: MainActivity? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ctx = this
        setContentView(R.layout.activity_main)
        initUI()
    }

    override fun onResume() {
        super.onResume()
        initMediaPlayer()
    }

    private fun initUI() {
        sodukuV_main_activity = findViewById(R.id.sodukuV_main_activity)
        numberBtCmd()
        musicBtCmd()
        restartGameBtCmd()
        resumeOrPauseGameBtCmd()
        initChoiceModelBtCmd()
    }

    private fun numberBtCmd() {
        sodukuV_main_activity?.numberBtCmd(this@MainActivity)
    }

    private fun resumeOrPauseGameBtCmd() {
        sodukuV_main_activity?.resumeOrPauseGameBtCmd(this@MainActivity)
    }

    private fun restartGameBtCmd() {
        sodukuV_main_activity?.restartGameBtCmd(this@MainActivity)
    }

    private fun initMediaPlayer() {
        mMediaPlayer = MediaPlayer()
        mMediaPlayer?.run {
            setDataSource(resources.openRawResourceFd(R.raw.flower_dance))
            isLooping = true
            setOnPreparedListener {
                it.start()
            }
            prepare()
        }
    }
    private fun initChoiceModelBtCmd(){
        sodukuV_main_activity?.choiceModelBtCmd(this@MainActivity)
    }

    private fun musicBtCmd() {
        findViewById<Button>(R.id.bt_music_setting)?.run {
            setOnClickListener {
                AnimationController.startScaleAnimation(it)
                AlertDialog.Builder(this@MainActivity).apply {
                    setTitle(R.string.prompt)
                    setMessage(R.string.music_open_or_close)
                    setPositiveButton(R.string.open) { dialog, _ ->
                        this@MainActivity.mMusicIsPlaying = true
                        //打开音乐
                        this@MainActivity.musicIsPlaying(true)
                        dialog.dismiss()
                    }
                    setNegativeButton(R.string.close) { dialog, _ ->
                        this@MainActivity.mMusicIsPlaying = false

                        //关闭音乐
                        this@MainActivity.musicIsPlaying(false)
                        dialog.dismiss()
                    }
                }.create().show()
            }
        }
    }

    fun musicIsPlaying(isPlaying: Boolean) {

        //播放音乐
        if (isPlaying) {
            mMediaPlayer?.run {
                if (!this.isPlaying) {
                    start()
                }
            }
        } else {
            mMediaPlayer?.run {
                if (this.isPlaying) {
                    pause()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayer?.run {
            stop()
            release()
        }
        sodukuV_main_activity?.disMissTheWhetherResumeGameDialog()
    }

}
