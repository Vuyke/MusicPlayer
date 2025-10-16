package com.example.musicplayer.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.musicplayer.R
import com.google.android.material.slider.RangeSlider

class Trimmer @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr){

    private var audioDuration: Float = 100f
    private val rangeSlider = RangeSlider(context, attrs).apply {
        valueFrom = 0f
        valueTo = 100f
        values = listOf(0f, 100f)
        isTickVisible = false
        thumbElevation = 0f
        trackTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.light_blue))
        thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.dark_blue))
    }
    private val indicator = PlaybackPositionIndicator(context).apply {
        visibility = View.VISIBLE
    }

    fun setAudioDuration(duration: Float) {
        if(duration != audioDuration) {
            audioDuration = duration
            rangeSlider.valueTo = duration
            rangeSlider.values = listOf(0f, duration)
        }
    }

    private var currentPosition: Float = 0f
        set(value) {
            field = value
            updatePlaybackIndicatorPosition()
            invalidate()
        }

    fun setPlaybackPosition(position: Float) {
        currentPosition = position.coerceAtLeast(0f).coerceAtMost(audioDuration)
    }

    private fun relative(x: Float) = rangeSlider.trackSidePadding + rangeSlider.x +
        (rangeSlider.width - 2 * rangeSlider.trackSidePadding) * x

    private fun updatePlaybackIndicatorPosition() {
        if (width == 0 || audioDuration == 0f) return
        indicator.x = relative(currentPosition / audioDuration) - (indicator.width / 2)
    }

    init {
        addView(rangeSlider, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            topToTop = LayoutParams.PARENT_ID
            bottomToBottom = LayoutParams.PARENT_ID
            startToStart = LayoutParams.PARENT_ID
            endToEnd = LayoutParams.PARENT_ID
        })

        addView(indicator, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT).apply {
            topToTop = LayoutParams.PARENT_ID
            bottomToBottom = LayoutParams.PARENT_ID
        })
        post { updatePlaybackIndicatorPosition() }
    }

    fun start(): Float = rangeSlider.values[0]

    fun end(): Float = rangeSlider.values[1]

    fun setOnRangeChangeListener(listener: RangeSlider.OnChangeListener) {
        rangeSlider.addOnChangeListener(listener)
    }
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            updatePlaybackIndicatorPosition()
        }
    }

}

class PlaybackPositionIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.RED
        strokeWidth = 8f
        isAntiAlias = true
    }

    init {
        minimumWidth = context.resources.displayMetrics.density.toInt() * 2
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec).coerceAtLeast(minimumWidth)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        canvas.drawLine(centerX, 0f, centerX, height.toFloat(), paint)
    }
}