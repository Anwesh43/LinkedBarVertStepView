package com.anwesh.uiprojects.barvertstepview

/**
 * Created by anweshmishra on 05/10/18.
 */

import android.app.Activity
import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.content.Context

val nodes : Int = 5

fun Canvas.drawBVSNode(i : Int, paint : Paint, scale : Float) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap = w / (nodes + 1)
    val size : Float = gap / 3
    paint.color = Color.parseColor("#0D47A1")
    save()
    translate(gap * i + gap, h/2)
    for (j in 0..1) {
        val sf : Float = 1f - 2 * j
        val sc : Float = Math.min(0.5f, Math.max(scale - 0.5f * j, 0f)) * 2
        save()
        scale(1f, sf)
        drawRect(RectF(-size, -h/2, size, -h/2 + (h/2) * sc), paint)
        restore()
    }
    restore()
}

class BarVertStepView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += 0.05f * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f- 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class BVSNode(var i : Int, val state : State = State()) {

        private var next : BVSNode? = null
        private var prev : BVSNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = BVSNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawBVSNode(i, paint, state.scale)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : BVSNode {
            var curr : BVSNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class BarVertStep(var i : Int) {

        private var root : BVSNode = BVSNode(0)
        private var curr : BVSNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : BarVertStepView) {
        private val animator : Animator = Animator(view)
        private val bvs : BarVertStep = BarVertStep(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#BDBDBD"))
            bvs.draw(canvas, paint)
            animator.animate {
                bvs.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            bvs.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : BarVertStepView {
            val view : BarVertStepView = BarVertStepView(activity)
            activity.setContentView(view)
            return view
        }
    }
}