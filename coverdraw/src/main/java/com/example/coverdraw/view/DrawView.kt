package com.example.coverdraw.view

import android.content.Context
import android.content.ContextWrapper
import android.graphics.*
import android.util.AttributeSet
import android.view.ViewConfiguration
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.example.coverdraw.model.Drawing
import java.io.*
import kotlin.math.abs


class DrawView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val TOUCH_TOLERANCE: Int = ViewConfiguration.get(context).scaledTouchSlop
    private val STROKE_WIDTH = 7F

    lateinit var bitmap: Bitmap
    lateinit var clearedBitmap: Bitmap

    private var clearedPathMap: ArrayList<Drawing> = ArrayList()
    private var pathMap: ArrayList<Drawing> = ArrayList()
    private var undo: ArrayList<Drawing> = ArrayList()

    private var paintLine: Paint = Paint().apply{
        color = Color.BLACK
        // Smooths out edges of what is drawn without affecting shape.
        isAntiAlias = true
        // Dithering affects how colors with higher-precision than the device are down-sampled.
        isDither = true
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = STROKE_WIDTH // default: Hairline-width (really thin)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (::bitmap.isInitialized) {
            draw(Canvas(bitmap))
        } else {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            draw(Canvas(bitmap))
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
       // canvas.save()
        canvas.drawBitmap(bitmap, 0F, 0F, paintLine)
        for(drawing in pathMap){
            drawing.let {
                paintLine.color = it.color
                paintLine.strokeWidth = it.strokeWidth
                paintLine.maskFilter = null
                canvas.drawPath(it.path, paintLine)
            }
        }
 //       canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val actionIndex = event.actionIndex // pointer(finger, mouse..)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStarted(event.getX(actionIndex), event.getY(actionIndex))
            }
            MotionEvent.ACTION_UP -> {
                touchEnded(event.getPointerId(actionIndex))
            }
            MotionEvent.ACTION_MOVE -> {
                touchMoved(event)
            }
        }
        invalidate()//redraw tge screen

        return true
    }

    private fun touchStarted(x: Float, y: Float) {
        val mPath = Path() // store path for given touch
        val mPoint = Point() // store the last point in the path

        val draw = Drawing(paintLine.color, paintLine.strokeWidth, mPath, mPoint)
        pathMap.add(draw)

        //move to the coordinates of the touch
        mPath.moveTo(x, y)
        mPoint.x = x.toInt()
        mPoint.y = y.toInt()
    }


    private fun touchMoved(event: MotionEvent) {

            val path = pathMap.last()
                val newX = event.x
                val newY = event.y

                //calculate how far the user moved from the last update
                val deltaX = abs(newX - path.point.x)
                val deltaY = abs(newY - path.point.y)

                //if the distance is significant enough to be considered a movement
                if(deltaX >= TOUCH_TOLERANCE || deltaY >= TOUCH_TOLERANCE){
                    //move path to the new location
                    path.path.quadTo(path.point.x.toFloat(), path.point.y.toFloat(), (newX + path.point.x)/2, (newY + path.point.y)/2)

                    //store the new coordinates
                    path.point.x = newX.toInt()
                    path.point.y = newY.toInt()
                }
    }

    private fun touchEnded(pointerId: Int) {
        val path = pathMap[pointerId]
        path.path.lineTo(path.point.x.toFloat(), path.point.y.toFloat())
    }

    fun setDrawingColor(color: Int){
        paintLine.color = color
    }

    fun getDrawingColor(): Int{
        return paintLine.color
    }

    fun setLineWidth(width: Int){
        paintLine.strokeWidth = width.toFloat()
    }

    fun getLineWidth(): Float{
        return paintLine.strokeWidth
    }

    fun clear() {
        if(pathMap.size > 0) {
            clearedPathMap = pathMap.clone() as ArrayList<Drawing>
            pathMap.clear()// removes all of the paths
            undo.clear()
            clearedBitmap = Bitmap.createBitmap(bitmap)
            bitmap.eraseColor(Color.WHITE)
            invalidate() // refresh the screen
        } else {
            Toast.makeText(context, "Nothing to clear", Toast.LENGTH_LONG).show()
        }
    }

    fun undo() {
        when {
            clearedPathMap.size > 0 && pathMap.size == 0 -> {//undo clear
                pathMap = clearedPathMap.clone() as ArrayList<Drawing>
                if (::clearedBitmap.isInitialized) {
                    bitmap = Bitmap.createBitmap(clearedBitmap)
                    clearedBitmap.eraseColor(Color.WHITE)
                }
                clearedPathMap.clear()
                invalidate()
            }
            pathMap.size > 0 -> {
                undo.add(pathMap.removeAt(pathMap.size - 1))
                invalidate() // add
            }
            else -> {
                Toast.makeText(context, "Nothing to undo", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun redo() {
        when {
            undo.size > 0 -> {
                val removed = undo.removeAt(undo.size - 1)
                pathMap.add(removed)
                invalidate()
            }
            else -> {
                Toast.makeText(context, "Nothing to redo", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun getCanvas(): Bitmap {
        val currBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(currBitmap)
        canvas.drawColor(Color.WHITE)
        draw(canvas)
        return currBitmap
    }
}