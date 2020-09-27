package com.example.coverdraw

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.coverdraw.view.DrawView
import kotlinx.android.synthetic.main.activity_draw.*
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.ByteArrayOutputStream


class DrawActivity : AppCompatActivity() {

    private lateinit var currentAlertDialog: AlertDialog.Builder
    private lateinit var  dialogLineWidth: AlertDialog
    private var defaultColor = Color.BLACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draw)

        setupTools()
    }

    private fun setupTools(){
        clearId.setOnClickListener {
            draw_View.clear()
        }
        saveId.setOnClickListener {
            draw_View.saveToInternalStorage()
        }
        colorId.setOnClickListener {
            openColourPicker()
        }
        lineStrokeId.setOnClickListener {
            showLineWidthDialog()
        }
        undoId.setOnClickListener {
            draw_View.undo()
        }
        redoId.setOnClickListener {
            draw_View.redo()
        }
        closeId.setOnClickListener {
            this.finish()
        }
    }


    private fun showLineWidthDialog(){
        currentAlertDialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.stroke_width_dialog, null)
        val widthSeekBar = view.findViewById<SeekBar>(R.id.strokeWidthId)
        val setLineWidthButton = view.findViewById<Button>(R.id.widthDialogButton)
        val widthImageView = view.findViewById<ImageView>(R.id.editStrokeImageId)
        setLineWidthButton.setOnClickListener {
            draw_View.setLineWidth(widthSeekBar.progress)
            dialogLineWidth.dismiss()
            //currentAlertDialog
        }
        widthSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            val bm = Bitmap.createBitmap(400, 100, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bm)

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                
                val p = Paint()
                p.color = draw_View.getDrawingColor()
                p.strokeCap = Paint.Cap.ROUND
                p.strokeWidth = progress.toFloat()

                bm.eraseColor(Color.WHITE)
                canvas.drawLine(30F,50F, 370F, 50F, p)
                widthImageView.setImageBitmap(bm)

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // called when tracking the seekbar is started
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // called when tracking the seekbar is stopped
            }
        })
        widthSeekBar.progress = draw_View.getLineWidth().toInt()
        currentAlertDialog.setView(view)
        dialogLineWidth = currentAlertDialog.create()
        dialogLineWidth.setTitle("Set Line Width")
        dialogLineWidth.show()
    }

    private fun openColourPicker() {
        val ambilWarnaDialog =
            AmbilWarnaDialog(this, defaultColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog) {
                    Toast.makeText(this@DrawActivity, "Unavailable", Toast.LENGTH_LONG).show()
                }

                override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                    defaultColor = color
                    draw_View.setDrawingColor(color)
                }
            })
        ambilWarnaDialog.show() // add
    }

    fun loadProfileImage(bitmap: Bitmap){
        val bStream = ByteArrayOutputStream()
        val bitmapSave = draw_View.bitmap
        bitmapSave.compress(Bitmap.CompressFormat.PNG, 100, bStream)
        val byteArray = bStream.toByteArray()
        val returnIntent = Intent()
        returnIntent.putExtra("cover_bitmap", byteArray)
        setResult(Activity.RESULT_OK,returnIntent)
        finish()
    }
}