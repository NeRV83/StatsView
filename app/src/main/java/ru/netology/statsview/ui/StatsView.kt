package ru.netology.statsview.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import ru.netology.statsview.R
import ru.netology.statsview.utils.AndroidUtils
import kotlin.math.min
import kotlin.random.Random
import androidx.core.graphics.withSave

class StatsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(
    context,
    attributeSet,
    defStyleAttr,
    defStyleRes,
) {

    private var textSize = AndroidUtils.dp(context, 40).toFloat()
    private var lineWidth = AndroidUtils.dp(context, 20)
    private var colors = emptyList<Int>()

    private var isSequential = false
    private var pendingSequential: Boolean? = null
    private var progress = 0F
    private var rotation = 0F

    private val drawAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 2000
        interpolator = LinearInterpolator()
        addUpdateListener { animation ->
            progress = animation.animatedValue as Float
            rotation = progress * 360F
            invalidate()
        }
    }

    private val pauseAnimator = ValueAnimator.ofFloat(1f, 1f).apply {
        duration = 500
    }

    private val animatorSet = AnimatorSet().apply {
        playSequentially(drawAnimator, pauseAnimator)

        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                pendingSequential?.let {
                    isSequential = it
                    pendingSequential = null
                }
                start()
            }
        })
    }

    init {
        context.withStyledAttributes(
            attributeSet,
            R.styleable.StatsView
        ) {
            textSize = getDimension(R.styleable.StatsView_textSize, textSize)
            lineWidth = getDimension(R.styleable.StatsView_LineWidth, lineWidth.toFloat()).toInt()
            colors = listOf(
                getColor(R.styleable.StatsView_color1, generateRandomColor()),
                getColor(R.styleable.StatsView_color2, generateRandomColor()),
                getColor(R.styleable.StatsView_color3, generateRandomColor()),
                getColor(R.styleable.StatsView_color4, generateRandomColor())
            )
            isSequential = getInt(R.styleable.StatsView_animationType, 0) == 1
        }
    }

    fun setAnimationType(isSequential: Boolean) {
        pendingSequential = isSequential
    }

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            if (value.isNotEmpty()) {
                animatorSet.cancel()
                animatorSet.start()
            }
            invalidate()
        }

    private var radius = 0F
    private var center = PointF()
    private var oval = RectF()

    private val paint: Paint = Paint(
        Paint.ANTI_ALIAS_FLAG,
    ).apply {
        strokeWidth = lineWidth.toFloat()
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint: Paint = Paint(
        Paint.ANTI_ALIAS_FLAG,
    ).apply {
        textSize = this@StatsView.textSize
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius,
            center.y - radius,
            center.x + radius,
            center.y + radius
        )
    }

//    override fun onDraw(canvas: Canvas) {
//        if (data.isEmpty()) return
//        val total = data.sum()
//        if (total <= 0F) return
//
//        var startAngle = -90F
//        data.forEachIndexed { index, datum ->
//            val angle = datum / total * 360F
//            paint.color = colors.getOrElse(index) { generateRandomColor() }
//            canvas.drawArc(oval, startAngle, angle, false, paint)
//            startAngle += angle
//        }
//
//        val firstAngle = (data[0] / total * 360F) / 2
//        paint.color = colors.getOrElse(0) { generateRandomColor() }
//        canvas.drawArc(oval, -90F, firstAngle, false, paint)
//
//        canvas.drawText(
//            "%.2f%%".format(100.00),
//            center.x,
//            center.y + textPaint.textSize / 4,
//            textPaint
//        )
//    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) return
        val total = data.sum()
        if (total <= 0F) return

        canvas.withSave {
            if (!isSequential) {
                rotate(rotation, center.x, center.y)
            }

            var startAngle = -90F

            data.forEachIndexed { index, datum ->
                val fullSectorAngle = datum / total * 360F

                val drawAngle = if (isSequential) {
                    val segmentSize = 1f / data.size
                    val segmentStart = index * segmentSize
                    val localProgress = ((progress - segmentStart) / segmentSize).coerceIn(0F, 1F)

                    fullSectorAngle * localProgress
                } else {
                    fullSectorAngle * progress
                }

                paint.color = colors.getOrElse(index) { generateRandomColor() }
                drawArc(oval, startAngle, drawAngle, false, paint)

                startAngle += fullSectorAngle

                val firstAngle = (data[0] / total * 360F) / 100
                paint.color = colors.getOrElse(0) { generateRandomColor() }
                drawArc(oval, -90F, firstAngle, false, paint)
            }

        }

        canvas.drawText(
            "%.2f%%".format(100.00),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint
        )
    }

    private fun generateRandomColor(): Int = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())

}