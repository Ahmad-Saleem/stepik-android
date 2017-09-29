package org.stepic.droid.ui.custom

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import org.stepic.droid.R
import org.stepic.droid.util.ColorUtil

class LoadingView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    private val baseColorDefault = ColorUtil.getColorArgb(R.color.loading_view_base_color)
    private val deepColorDefault = ColorUtil.getColorArgb(R.color.loading_view_deep_color)
    private val durationDefault = 1500L
    private val intervalDefault = 0L

    private val animator: ValueAnimator =
            ValueAnimator.ofFloat(0f, 1f).apply {
                addUpdateListener {
                    frame = it.animatedFraction
                    postInvalidate()
                }
            }

    private var frame = 0f
    private var radius: Float = 0f
    private var progressLength: Float = 0f
    private var baseColor = baseColorDefault
    private var deepColor = deepColorDefault
    private var durationOfPass = durationDefault
    private var interval = intervalDefault
    private var autoStart = true

    private var basePaint: Paint
    private var deepPaintLeft: Paint
    private var deepPaintRight: Paint

    private var rect = RectF()
    private var path = Path()

    private val screenWidth: Int by lazy {
        context.resources.displayMetrics.widthPixels
    }

    private var localMatrix: Matrix = Matrix()

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.LoadingView)
        try {
            with(array) {
                radius = getDimensionPixelOffset(R.styleable.LoadingView_radius, resources.getDimensionPixelOffset(R.dimen.loading_view_radius_default)).toFloat()
                durationOfPass = getInt(R.styleable.LoadingView_duration, durationDefault.toInt()).toLong()
                interval = getInt(R.styleable.LoadingView_interval, intervalDefault.toInt()).toLong()
                baseColor = getColor(R.styleable.LoadingView_baseColor, baseColorDefault)
                deepColor = getColor(R.styleable.LoadingView_deepColor, deepColorDefault)
                progressLength = getDimensionPixelOffset(R.styleable.LoadingView_progressLength, resources.getDimensionPixelOffset(R.dimen.loading_view_progress_length_default)).toFloat()
                autoStart = getBoolean(R.styleable.LoadingView_autoStart, true)
            }
        } finally {
            array.recycle()
        }

        basePaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            color = baseColor
        }
        deepPaintLeft = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            shader = LinearGradient(0f, 0f, progressLength / 2, 0f, baseColor, deepColor, Shader.TileMode.CLAMP)
        }
        deepPaintRight = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            shader = LinearGradient(0f, 0f, progressLength / 2, 0f, deepColor, baseColor, Shader.TileMode.CLAMP)
        }
        if (autoStart) {
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val width = width.toFloat()
        val height = height.toFloat()
        canvas.clipPath(path.apply { reset(); addRoundRect(rect.apply { set(0f, 0f, width, height) }, radius, radius, Path.Direction.CW) })
        super.onDraw(canvas)
        canvas.drawRoundRect(rect.apply { set(0f, 0f, width, height) }, radius, radius, basePaint)

        canvas.drawRoundRect(rect.apply { set(screenWidth * frame - x, 0f, screenWidth * frame - x + progressLength / 2, height) }, 0f, 0f, deepPaintLeft.apply { shader.setLocalMatrix(localMatrix.apply { setTranslate(screenWidth * frame - x, 0f) }) })
        canvas.drawRoundRect(rect.apply { set(screenWidth * frame - x + progressLength / 2, 0f, screenWidth * frame - x + progressLength, height) }, 0f, 0f, deepPaintRight.apply { shader.setLocalMatrix(localMatrix.apply { setTranslate(screenWidth * frame - x + progressLength / 2, 0f) }) })

        if (screenWidth - (screenWidth * frame + progressLength) < 0) {
            canvas.drawRoundRect(rect.apply { set(screenWidth * frame - x - screenWidth, 0f, screenWidth * frame - x + progressLength / 2 - screenWidth, height) }, 0f, 0f, deepPaintLeft.apply { shader.setLocalMatrix(localMatrix.apply { setTranslate(screenWidth * frame - x - screenWidth, 0f) }) })
            canvas.drawRoundRect(rect.apply { set(screenWidth * frame - x + progressLength / 2 - screenWidth, 0f, screenWidth * frame - x + progressLength - screenWidth, height) }, 0f, 0f, deepPaintRight.apply { shader.setLocalMatrix(localMatrix.apply { setTranslate(screenWidth * frame - x + progressLength / 2 - screenWidth, 0f) }) })
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }

    private fun start() =
            with(animator) {
                duration = durationOfPass
                startDelay = interval
                repeatCount = ObjectAnimator.INFINITE
                start()
            }

    private fun stop() = with(animator) { if (isRunning) cancel() }
}
