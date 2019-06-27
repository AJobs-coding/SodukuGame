package com.jlyx.sodukugame.customViews

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Message
import android.support.annotation.RequiresApi
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.jlyx.app.controllers.AnimationController
import com.jlyx.app.tools.LogPrint
import com.jlyx.app.tools.ToastShow
import com.jlyx.sodukugame.MainActivity
import com.jlyx.sodukugame.R
import java.lang.Exception

/**
 * 宽高比=1：1，宽度为9的倍数
 */
class SodukuView : View {
    private var mToast: Toast? = null
    private var mWhetherResumeGameDialog: AlertDialog? = null
    private var bt_restart_game: Button? = null
    //画笔
    private var mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    // Face border width in pixels
    private var mPaintNormalWidth = 4.0f
    private var mPaintTextSize = 60f
    private var mGrayColor = Color.GRAY
    private var mBlackColor = Color.BLACK
    @RequiresApi(Build.VERSION_CODES.M)
    private var mPinkColor = resources.getColor(R.color.color_2, null)
    private var mAccentColor = resources.getColor(R.color.colorAccent, null)
    private var mCanDrawText: Boolean = false
    //九宫格
    private var mMeshNumber = 9
    //private var mDra
    private var mViewWidth = 0
    private var mViewStartX = 0f
    private var mViewStartY = 0f
    private var mViewEndX = 0f
    private var mViewEndY = 0f
    private var mDisBetweenLines = 0

    private var mKeyboardCanUse = false

    private var mTouchedColumnLineNumber = -1
    private var mTouchedRowLineNumber = -1
    private var mAnswerDataNumberArray = Array(mMeshNumber) {
        IntArray(mMeshNumber)
    }
    private var mInitDataNumberArray = Array(mMeshNumber) {
        IntArray(mMeshNumber)
    }
    //该数组的目的是记住一开始初始化的数字
    private var mCopyInitDataNumberArray = Array(mMeshNumber) {
        IntArray(mMeshNumber)
    }

    private var mBaseDataNumberArray = IntArray(mMeshNumber) {
        it + 1
    }

    private var mKeyboardList = mutableListOf<Button>()

    private val RESTART_GAME = 1
    private var mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message?) {
            when (msg?.arg1) {
                RESTART_GAME -> {
                    invalidate()
                    bt_restart_game?.isClickable = true
                }
            }
        }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)


    /**
     *@function: 初始化画笔
     *@param:
     *@return:
     */
    private fun setPaintStyle(color: Int, paintWidth: Float, style: Paint.Style) {
        mPaint.color = color
        mPaint.strokeWidth = paintWidth
        mPaint.style = style
    }

    /**
     *@function: 重新设置view的宽高
     *@param:
     *@return:
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var widthPrevious = MeasureSpec.getSize(widthMeasureSpec)
        mViewWidth = widthPrevious / mMeshNumber * mMeshNumber
        mDisBetweenLines = mViewWidth / mMeshNumber
        setMeasuredDimension(mViewWidth, mViewWidth)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mViewEndX = mViewWidth.toFloat()
        mViewEndY = mViewWidth.toFloat()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        //canvas?.drawText()
        //画九宫格
        drawMesh(canvas)
        //当点击一个方块时显示该方块为粉红色
        drawPinkRect(canvas)
        //该方法只调用一次
        if (!mCanDrawText)
            beginInitRandomData()
        if (mCanDrawText)
            drawDataText(canvas)
    }

    private fun drawDataText(canvas: Canvas?) {
        mPaint.textSize = mPaintTextSize
        for ((row, value) in mInitDataNumberArray.withIndex()) {
            for ((column, v) in value.withIndex()) {
                if (v != 0) {
                    if (v == mCopyInitDataNumberArray[row][column])
                        setPaintStyle(mBlackColor, mPaintNormalWidth, Paint.Style.FILL)
                    else
                        setPaintStyle(mAccentColor, mPaintNormalWidth, Paint.Style.FILL)
                    var rect = Rect()
                    mPaint.getTextBounds(v.toString(), 0, 1, rect)
                    var x = mDisBetweenLines * column.toFloat() + (mDisBetweenLines - rect.width()) / 2
                    var y = mDisBetweenLines * row.toFloat() + (mDisBetweenLines - rect.height()) / 2 + rect.height()
                    canvas?.drawText(
                        v.toString(),
                        x,
                        y,
                        mPaint
                    )
                }
            }
        }
    }

    private fun beginInitRandomData() {
        object : Thread() {
            override fun run() {
                this@SodukuView.initData()
            }
        }.start()
    }

    private fun drawPinkRect(canvas: Canvas?) {
        if (mTouchedRowLineNumber > -1) {
            setPaintStyle(mPinkColor, mPaintNormalWidth, Paint.Style.STROKE)
            var l = mTouchedColumnLineNumber * mDisBetweenLines.toFloat()
            var t = mTouchedRowLineNumber * mDisBetweenLines.toFloat()
            canvas?.drawRect(
                l, t, l + mDisBetweenLines, t + mDisBetweenLines, mPaint
            )
        }
    }

    /**
     *@function: 画九宫格
     *@param:
     *@return:
     */
    private fun drawMesh(canvas: Canvas?) {
        setPaintStyle(mGrayColor, mPaintNormalWidth, Paint.Style.STROKE)
        //row lines
        for (i in 0..mMeshNumber) {
            var y = mViewStartY + mDisBetweenLines * i
            canvas?.drawLine(mViewStartX, y, mViewEndX, y, mPaint)
        }
        //column lines
        for (i in 0..mMeshNumber) {
            var x = mViewStartX + mDisBetweenLines * i
            canvas?.drawLine(x, mViewStartY, x, mViewEndY, mPaint)
        }
    }

    private fun showToast(res: Int) {
        mToast?.apply {
            cancel()
            this@SodukuView.mToast = null
        }
        mToast = ToastShow.show(MainActivity.ctx!!, res)
    }

    /**
     *@function: 重写触摸事件
     *@param:
     *@return:
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!mCanDrawText)
            return true
        when (event?.action) {
            MotionEvent.ACTION_UP -> {
                //得到按下的坐标
                var clickX = event.x
                var clickY = event.y
                //判断触摸的位置在哪一个格子
                mTouchedColumnLineNumber = (clickX / mDisBetweenLines).toInt()
                mTouchedRowLineNumber = (clickY / mDisBetweenLines).toInt()
                //判断该位置是否会有初始化数据: 没有就刷新
                if (mCopyInitDataNumberArray[mTouchedRowLineNumber][mTouchedColumnLineNumber] == 0) {
                    this.mKeyboardCanUse = true
                    invalidate()
                } else {
                    this.mKeyboardCanUse = false
                    showToast(R.string.can_not_fill_number)
                }
            }
        }
        return true
    }

    private fun initData() {
        var columnDataList = mutableListOf<MutableList<Int>>()
        var rowDataList = mutableListOf<Int>()
        var rowAndColumnUsedNumList = mutableListOf<Int>()
        var thisPositionCanUseNumList = mutableListOf<Int>()
        for (i in 0 until mMeshNumber)
            columnDataList.add(mutableListOf())
        try {
            for (rowLine in 0 until mAnswerDataNumberArray.size) {
                //列
                for (column in 0 until mMeshNumber) {
                    //将当前行的数据放到rowAndColumnUsedNumList集合中
                    for (v in rowDataList) {
                        rowAndColumnUsedNumList.add(v)
                    }
                    //将当前列中不在rowAndColumnUsedNumList的数据放到rowAndColumnUsedNumList中
                    for (r in 0 until rowLine) {
                        var cdl = columnDataList[r]
                        if (cdl[column] !in rowAndColumnUsedNumList)
                            rowAndColumnUsedNumList.add(cdl[column])
                    }
                    //将剩余的有效数据提取出来
                    mBaseDataNumberArray.forEach {
                        if (it !in rowAndColumnUsedNumList)
                            thisPositionCanUseNumList.add(it)
                    }
                    //最后一行可能会出现无剩余数可选的情况
                    if (thisPositionCanUseNumList.size == 0) {
                        var lastCanUseNumber = 0
                        //取该行剩余的数据
                        mBaseDataNumberArray.forEach {
                            if (it !in rowDataList)
                                lastCanUseNumber = it
                        }
                        //遍历该行已有数据
                        c@ for (c in 0 until rowDataList.size) {
                            //判断最后一个数据在当前行数据所在的列上是否有效
                            for (r in 0 until rowLine) {
                                if (lastCanUseNumber == columnDataList[r][c])
                                    continue@c
                            }
                            //判断当前列的行数据在最后一列是否有效
                            var currentRowData = rowDataList[c]
                            for (r in 0 until rowLine) {
                                if (currentRowData == columnDataList[r][column])
                                    continue@c
                            }
                            //程序运行到这就是可以交换数据
                            var temp = lastCanUseNumber
                            thisPositionCanUseNumList.add(currentRowData)
                            mAnswerDataNumberArray[rowLine][c] = temp
                            columnDataList[rowLine][c] = temp
                            break
                        }
                    }
                    //添加数据到mAnswerDataNumberArray
                    var randomNum = thisPositionCanUseNumList[(Math.random() * thisPositionCanUseNumList.size).toInt()]
                    mAnswerDataNumberArray[rowLine][column] = randomNum
                    columnDataList[rowLine].add(randomNum)
                    rowDataList.add(randomNum)
                    thisPositionCanUseNumList.clear()
                    rowAndColumnUsedNumList.clear()
                }
                rowDataList.clear()
            }
            for (i in mAnswerDataNumberArray) {
                for (v in i)
                    print("$v, ")
                println()
            }
            initRandomNumber()
        } catch (e: Exception) {
            initData()
        }

    }

    private fun initRandomNumber() {
        var columnL = mutableListOf<Int>()
        for ((row, arr) in mAnswerDataNumberArray.withIndex()) {
            columnL.clear()
            var initColumnIndex = (Math.random() * mMeshNumber).toInt()
            var lineMaxNumber = (Math.random() * (mMeshNumber - 1)).toInt() + 1
            var num = 1
            while (num <= lineMaxNumber) {
                num++
                while (initColumnIndex in columnL)
                    initColumnIndex = (Math.random() * mMeshNumber).toInt()
                columnL.add(initColumnIndex)
                mInitDataNumberArray[row][initColumnIndex] = arr[initColumnIndex]
                mCopyInitDataNumberArray[row][initColumnIndex] = arr[initColumnIndex]
            }
        }
        mCanDrawText = true
        mHandler.sendMessage(Message.obtain().apply {
            arg1 = this@SodukuView.RESTART_GAME
        })
    }

    /**
     *@function: 数字键盘的点击操作
     *@param:
     *@return:
     */
    fun numberBtCmd(context: AppCompatActivity) {
        var ids = intArrayOf(
            R.id.bt_1, R.id.bt_2, R.id.bt_3, R.id.bt_4,
            R.id.bt_5, R.id.bt_6, R.id.bt_7, R.id.bt_8, R.id.bt_9
        )
        for (id in ids) {
            context.findViewById<Button>(id).apply {
                this@SodukuView.mKeyboardList.add(this)
                setOnClickListener {
                    if (!this@SodukuView.mKeyboardCanUse)
                        return@setOnClickListener
                    when (text) {
                        "1" -> {
                            this@SodukuView.judgeInputNumberWhetherLegal(1)
                        }
                        "2" -> {
                            this@SodukuView.judgeInputNumberWhetherLegal(2)
                        }
                        "3" -> {
                            this@SodukuView.judgeInputNumberWhetherLegal(3)
                        }
                        "4" -> {
                            this@SodukuView.judgeInputNumberWhetherLegal(4)
                        }
                        "5" -> {
                            this@SodukuView.judgeInputNumberWhetherLegal(5)
                        }
                        "6" -> {
                            this@SodukuView.judgeInputNumberWhetherLegal(6)
                        }
                        "7" -> {
                            this@SodukuView.judgeInputNumberWhetherLegal(7)
                        }
                        "8" -> {
                            this@SodukuView.judgeInputNumberWhetherLegal(8)
                        }
                        "9" -> {
                            this@SodukuView.judgeInputNumberWhetherLegal(9)
                        }
                    }
                }
            }
        }
    }

    private fun judgeInputNumberWhetherLegal(num: Int) {
        mInitDataNumberArray[mTouchedRowLineNumber][mTouchedColumnLineNumber] = 0
        //判断改行
        for (rowV in mInitDataNumberArray[mTouchedRowLineNumber]) {
            if (rowV == num) {
                showToast(R.string.input_number_illegal)
                invalidate()
                return
            }
        }
        //判断该列
        for (row in 0 until mMeshNumber) {
            if (mInitDataNumberArray[row][mTouchedColumnLineNumber] == num) {
                showToast(R.string.input_number_illegal)
                invalidate()
                return
            }
        }
        mInitDataNumberArray[mTouchedRowLineNumber][mTouchedColumnLineNumber] = num
        //判断合法之后，刷新
        invalidate()
    }

    fun resumeOrPauseGameBtCmd(context: AppCompatActivity) {
        context.findViewById<Button>(R.id.bt_resume_pause_game)?.apply {
            setOnClickListener {
                AnimationController.startScaleAnimation(it)
                this@SodukuView.showWhetherResumeGameDialog()
                MainActivity.ctx?.musicIsPlaying(false)
            }
        }
    }

    private fun showWhetherResumeGameDialog() {
        mWhetherResumeGameDialog = AlertDialog.Builder(MainActivity.ctx!!).apply {
            setMessage(R.string.pause_game)
            setOnDismissListener {
                if (MainActivity.ctx!!.mMusicIsPlaying)
                    MainActivity.ctx?.musicIsPlaying(true)
            }
        }.create()
        mWhetherResumeGameDialog?.show()
    }

    /**
     *@function: 该方法在MainActivity中的onDestroy中调用
     *@param:
     *@return:
     */
    fun disMissTheWhetherResumeGameDialog() {
        mWhetherResumeGameDialog?.run {
            if (isShowing)
                dismiss()
        }
    }

    fun restartGameBtCmd(context: AppCompatActivity) {
        bt_restart_game = context.findViewById<Button>(R.id.bt_restart_game)?.apply {
            isClickable = false
            setOnClickListener {
                isClickable = false
                AnimationController.startScaleAnimation(it)
                this@SodukuView.resetData()
            }
        }

    }

    private fun resetData() {
        mDisBetweenLines = mViewWidth / mMeshNumber
        mCanDrawText = false
        for ((row, arr) in mAnswerDataNumberArray.withIndex())
            for (column in 0 until arr.size)
                arr[column] = 0
        for ((row, arr) in mInitDataNumberArray.withIndex())
            for (column in 0 until arr.size)
                arr[column] = 0
        for ((row, arr) in mCopyInitDataNumberArray.withIndex())
            for (column in 0 until arr.size)
                arr[column] = 0
        mTouchedColumnLineNumber = -1
        mTouchedRowLineNumber = -1
        mKeyboardCanUse = false
        invalidate()
    }

    fun choiceModelBtCmd(context: AppCompatActivity) {
        context.findViewById<Button>(R.id.bt_change_model).run {
            setOnClickListener {
                AnimationController.startScaleAnimation(it)
                AlertDialog.Builder(context).apply {
                    setItems(arrayOf("4*4", "5*5", "6*6", "9*9")) { dialog, which ->
                        when (which) {
                            0 -> {
                                this@SodukuView.mMeshNumber = 4
                            }
                            1 -> {
                                this@SodukuView.mMeshNumber = 5
                            }
                            2 -> {
                                this@SodukuView.mMeshNumber = 6
                            }
                            3 -> {
                                this@SodukuView.mMeshNumber = 9
                            }
                        }
                        dialog.dismiss()
                    }
                    setOnDismissListener {
                        this@SodukuView.setKeyboardCanUseKey(this@SodukuView.mMeshNumber)
                        this@SodukuView.resetDataAndChangeArray()
                    }
                }.create().show()
            }
        }
    }

    private fun resetDataAndChangeArray() {
        mDisBetweenLines = mViewWidth / mMeshNumber
        mCanDrawText = false
        mAnswerDataNumberArray = Array(mMeshNumber) {
            IntArray(mMeshNumber)
        }
        mInitDataNumberArray = Array(mMeshNumber) {
            IntArray(mMeshNumber)
        }
        //该数组的目的是记住一开始初始化的数字
        mCopyInitDataNumberArray = Array(mMeshNumber) {
            IntArray(mMeshNumber)
        }

        mBaseDataNumberArray = IntArray(mMeshNumber) {
            it + 1
        }
        mTouchedColumnLineNumber = -1
        mTouchedRowLineNumber = -1
        mKeyboardCanUse = false
        invalidate()
    }

    private fun setKeyboardCanUseKey(gameModel: Int) {
        fun setBtClickable(canClickUseEndIndex: Int) {
            for (index in 0 until canClickUseEndIndex) {
                mKeyboardList[index].run {
                    isClickable = true
                    alpha = 1f
                }
            }
            for (index in canClickUseEndIndex until mKeyboardList.size) {
                mKeyboardList[index].run {
                    isClickable = false
                    alpha = 0.3f
                }
            }
        }
        when (gameModel) {
            4 -> {
                setBtClickable(4)
            }
            5 -> {
                setBtClickable(5)
            }
            6 -> {
                setBtClickable(6)
            }
            9 -> {
                setBtClickable(9)
            }
        }
    }

}