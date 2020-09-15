package com.example.coverdraw

import android.app.Activity
import android.content.Intent
import android.graphics.*
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
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.coverdraw.adapter.StencilRecyclerViewAdapter
import com.example.coverdraw.data.DataManager
import com.example.coverdraw.databinding.AddStencilDialogBinding
import com.example.coverdraw.model.Stencil
import com.example.coverdraw.ui.AddStencilFragment
import com.example.coverdraw.view.DrawView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import yuku.ambilwarna.AmbilWarnaDialog


class MainActivity : AppCompatActivity() {

    private lateinit var drawView: DrawView
    private lateinit var currentAlertDialog: AlertDialog.Builder
    private lateinit var  dialogLineWidth: AlertDialog
    private var defaultColor = Color.BLACK
    private lateinit var binding: AddStencilDialogBinding
    private lateinit var adapterStencilList: StencilRecyclerViewAdapter
    private lateinit var addStencilFragment: AddStencilFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawView = findViewById(R.id.drawView)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)

        addStencilFragment = AddStencilFragment()
        //binding.lifecycleOwner = viewLifecycleOwner
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.clearId -> drawView.clear()
            R.id.saveId -> drawView.saveToInternalStorage()
            R.id.colorId -> openColourPicker()
            R.id.lineStrokeId -> showLineWidthDialog()
            R.id.undoId -> drawView.undo()
            R.id.redoId -> drawView.redo()
            R.id.addTemplateId -> addStencilFragment.show(supportFragmentManager, "Add_stencil")
            else -> Log.e("ERROR", "Error Occured")
        }

        return super.onOptionsItemSelected(item)
    }

    fun selectStencil(selectedItem: Stencil) {
        selectedItem.image?.let { drawView.setStencilImage(it) }
        addStencilFragment.dismiss()
    }

//    private fun showAddStencilDialog() {
//        currentAlertDialog = AlertDialog.Builder(this)
//        val view = layoutInflater.inflate(R.layout.add_stencil_dialog, null)
//        val cancelButton = view.findViewById<Button>(R.id.stencil_cancel_buttonId)
//        cancelButton.setOnClickListener {
//            dialogAddStencil.dismiss()
//        }
//        currentAlertDialog.setView(view)
//        dialogAddStencil = currentAlertDialog.create()
//        dialogAddStencil.show()
//    }

    fun showLineWidthDialog(){
        currentAlertDialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.stroke_width_dialog, null)
        val widthSeekBar = view.findViewById<SeekBar>(R.id.strokeWidthId)
        val setLineWidthButton = view.findViewById<Button>(R.id.widthDialogButton)
        val widthImageView = view.findViewById<ImageView>(R.id.editStrokeImageId)
        setLineWidthButton.setOnClickListener {
            drawView.setLineWidth(widthSeekBar.progress)
            dialogLineWidth.dismiss()
            //currentAlertDialog
        }
        widthSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            val bm = Bitmap.createBitmap(400, 100, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bm)

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                
                val p = Paint()
                p.color = drawView.getDrawingColor()
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
        widthSeekBar.progress = drawView.getLineWidth().toInt()
        currentAlertDialog.setView(view)
        dialogLineWidth = currentAlertDialog.create()
        dialogLineWidth.setTitle("Set Line Width")
        dialogLineWidth.show()
    }

    private fun openColourPicker() {
        val ambilWarnaDialog =
            AmbilWarnaDialog(this, defaultColor, object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog) {
                    Toast.makeText(this@MainActivity, "Unavailable", Toast.LENGTH_LONG).show()
                }

                override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                    defaultColor = color
                    drawView.setDrawingColor(color)
                }
            })
        ambilWarnaDialog.show() // add
    }
}