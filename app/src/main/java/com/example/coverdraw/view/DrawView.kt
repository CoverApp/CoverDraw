package com.example.coverdraw.view

import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.*
import android.view.MotionEvent.INVALID_POINTER_ID
import android.widget.Toast
import androidx.core.view.MotionEventCompat
import com.example.coverdraw.model.Drawing
import com.example.coverdraw.model.Stencil
import java.io.*
import kotlin.math.abs


class DrawView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    //TODO Resize
    //TODO Add Template

    private val TOUCH_TOLERANCE: Int = ViewConfiguration.get(context).scaledTouchSlop
    private val STROKE_WIDTH = 7F

    private lateinit var bitmap: Bitmap
    private lateinit var bitmapCanvas: Canvas
    private var paintScreen: Paint = Paint()

    private var clearedPathMap: ArrayList<Drawing> = ArrayList()
    private var pathMap: ArrayList<Drawing> = ArrayList()
    private var undo: ArrayList<Drawing> = ArrayList()

//    private var stencilPlaced = false
    private var mActivePointerId = INVALID_POINTER_ID

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
        if (::bitmap.isInitialized) bitmap.recycle()
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmapCanvas = Canvas(bitmap)
        bitmapCanvas.drawColor(Color.WHITE)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        bitmapCanvas.drawColor(Color.WHITE)
        for(drawing in pathMap){
            drawing.let {
                if(drawing.path != null) {
                    paintLine.color = it.color
                    paintLine.strokeWidth = it.strokeWidth!!
                    paintLine.maskFilter = null
                    bitmapCanvas.drawPath(it.path!!, paintLine)
                } else{
                    bitmapCanvas.drawBitmap(drawing.stencil!!.image!!, drawing.point.x.toFloat(), drawing.point.y.toFloat(), paintLine)
                }
            }
        }
        canvas.drawBitmap(bitmap, 0F, 0F, paintScreen)
        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val actionIndex = event.actionIndex // pointer(finger, mouse..)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchStarted(event.getX(actionIndex), event.getY(actionIndex), event.getPointerId(actionIndex))
                // Save the ID of this pointer (for dragging)
                mActivePointerId = event.getPointerId( 0)
            }
            MotionEvent.ACTION_UP -> {
                touchEnded(event.getPointerId(actionIndex))
//                mActivePointerId = INVALID_POINTER_ID
//                stencilPlaced = false
            }
            MotionEvent.ACTION_CANCEL -> {
//                mActivePointerId = INVALID_POINTER_ID
//                stencilPlaced = false
            }
            MotionEvent.ACTION_MOVE -> {
                touchMoved(event)
            }
//            MotionEvent.ACTION_POINTER_UP -> {
//                val path = pathMap.last()
//                            // This was our active pointer going up. Choose a new
//                            // active pointer and adjust accordingly.
//                            val newPointerIndex = 1
//                            path.point.x = event.getX(newPointerIndex).toInt()
//                            path.point.y = event.getX(newPointerIndex).toInt()
//                            mActivePointerId = event.getPointerId(newPointerIndex)
//
//                stencilPlaced = false
//            }
        }
        invalidate()//redraw the screen

        return true
    }

    private fun touchStarted(x: Float, y: Float, pointerId: Int) {
        val path = Path()// store path for given touch
        val point = Point() // store the last point in the path

        val draw = Drawing(paintLine.color, paintLine.strokeWidth, path, point)
        pathMap.add(draw)

        //move to the coordinates of the touch
        path.moveTo(x, y)
        point.x = x.toInt()
        point.y = y.toInt()
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
                    path.path!!.quadTo(path.point.x.toFloat(), path.point.y.toFloat(), (newX + path.point.x)/2, (newY + path.point.y)/2)

                    //store the new coordinates
                    path.point.x = newX.toInt()
                    path.point.y = newY.toInt()
//                    if(!stencilPlaced) {
                        bitmapCanvas.drawPath(path.path!!, paintLine)
//                    }
                }
    }

    private fun touchEnded(pointerId: Int) {
        val path = pathMap[pointerId]
 //       path.path.lineTo(path.point.x.toFloat(), path.point.y.toFloat())
        path.path?.let { bitmapCanvas.drawPath(it, paintLine) }
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

    fun setStencilImage(bm: Bitmap){
        val point = Point()
        point.x = 0
        point.y = 0
        val stencil = Stencil("", bm)
        val draw = Drawing(paintLine.color, null, null, point, stencil)
        pathMap.add(draw)
        bitmapCanvas.drawBitmap(bm, 0F, 0F, paintLine)
//        stencilPlaced = true
        invalidate() // refresh the screen
    }

    fun clear() {
        if(pathMap.size > 0) {
            clearedPathMap = ArrayList(pathMap)
            pathMap.clear()// removes all of the paths
            undo.clear()
            bitmap.eraseColor(Color.WHITE)
//            stencilPlaced = false
            invalidate() // refresh the screen
        } else {
            Toast.makeText(context, "Nothing to clear", Toast.LENGTH_LONG).show()
        }
    }

    fun undo() {
        if (pathMap.size > 0) {
            undo.add(pathMap.removeAt(pathMap.size - 1))
//            stencilPlaced = false
            invalidate() // add
        } else {
            Toast.makeText(context, "Nothing to undo", Toast.LENGTH_LONG).show()
        }
    }

    fun redo() {
        when {
            undo.size > 0 -> {
                pathMap.add(undo.removeAt(undo.size - 1))
//                stencilPlaced = false
                invalidate()
            }
            clearedPathMap.size > 0 && pathMap.size == 0 -> {
                pathMap = ArrayList(clearedPathMap)
                clearedPathMap.clear()
//                stencilPlaced = false
                invalidate()
            }
            else -> {
                Toast.makeText(context, "Nothing to redo", Toast.LENGTH_LONG).show()
            }
        }
    }



//    fun saveImage(){
//        val timestamp = System.currentTimeMillis()
//        val filename = "CoverApp$timestamp"
//        val values = ContentValues()
//        values.put(MediaStore.Images.Media.TITLE, filename)
//        values.put(MediaStore.Images.Media.DATE_ADDED, timestamp)
//        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
//
//        //get URI for the location to save the file
//        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
//
//        try {
//            val outputStream = context.contentResolver.openOutputStream(uri!!)
//
//            //copy the bitmap to the output stream
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream) //this is our image
//
//            try {
//                outputStream?.flush()
//                outputStream?.close()
//
//                val message = Toast.makeText(context, "Image saved", Toast.LENGTH_LONG)
//                message.setGravity(Gravity.CENTER, message.xOffset / 2, message.yOffset / 2)
//
//                message.show()
//            } catch (i: IOException){
//                val message = Toast.makeText(context, "Image NOT saved", Toast.LENGTH_LONG)
//                message.setGravity(Gravity.CENTER, message.xOffset / 2, message.yOffset / 2)
//
//                message.show()
//                i.printStackTrace()
//            }
//
//        } catch ( f: FileNotFoundException){
//            val message = Toast.makeText(context, "Image NOT saved", Toast.LENGTH_LONG)
//            message.setGravity(Gravity.CENTER, message.xOffset / 2, message.yOffset / 2)
//
//            message.show()
//            f.printStackTrace()
//        }
//
//    }

//    fun saveToInternalStorage(): String? {
//        val timestamp = System.currentTimeMillis()
//        val filename = "CoverApp$timestamp"
//        val cw = ContextWrapper(context)
//        // path to /data/data/yourapp/app_data/imageDir
//        val directory: File = cw.getDir("imageDir", Context.MODE_PRIVATE)
//        // Create imageDir
//        val mypath = File(directory, "$filename.jpg")
//        var fos: FileOutputStream? = null
//        try {
//            fos = FileOutputStream(mypath)
//            // Use the compress method on the BitMap object to write image to the OutputStream
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        } finally {
//            try {
//                fos?.close()
//                val message = Toast.makeText(context, "Image saved ${directory.absolutePath}", Toast.LENGTH_LONG)
//                message.setGravity(Gravity.CENTER, message.xOffset / 2, message.yOffset / 2)
//
//                message.show()
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }
//        return directory.absolutePath
//    }

    private fun loadImageFromStorage(path: String) {
        try {
            val f = File(path, "profile.jpg")
            val b = BitmapFactory.decodeStream(FileInputStream(f))
//            val img: ImageView = findViewById(R.id.imgPicker) as ImageView
//            img.setImageBitmap(b)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }
}