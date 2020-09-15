package com.example.coverdraw.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.coverdraw.MainActivity
import com.example.coverdraw.R
import com.example.coverdraw.adapter.StencilRecyclerViewAdapter
import com.example.coverdraw.data.DataManager
import com.example.coverdraw.databinding.AddStencilDialogBinding
import com.example.coverdraw.model.Stencil


class AddStencilFragment: DialogFragment() {

    private lateinit var binding: AddStencilDialogBinding
    private lateinit var adapterStencilList: StencilRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.add_stencil_dialog,
            container,
            false
        )
        binding.lifecycleOwner = viewLifecycleOwner
        initRecyclerView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val cancelButton = view.findViewById<Button>(R.id.stencil_cancel_buttonId)
        cancelButton.setOnClickListener {
            this.dismiss()
        }
    }

    private fun initRecyclerView(){
        binding.stencilRecyclerViewId.layoutManager= StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.stencilRecyclerViewId.itemAnimator = DefaultItemAnimator()
        binding.stencilRecyclerViewId.addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.HORIZONTAL))
        binding.stencilRecyclerViewId.addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        adapterStencilList = StencilRecyclerViewAdapter { selectedItem:Stencil->(activity as MainActivity?)!!.selectStencil(selectedItem)}
        binding.stencilRecyclerViewId.adapter = adapterStencilList
        displayStencils()
    }

    private fun displayStencils() {
        val stencils = arrayListOf<Stencil>()
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = false
        val id: Int = R.drawable.ic_book_1_1
        val bitmap = convertToBitmap(id)
        for(stencilUrl in DataManager.imageUrls){
            stencils.add(Stencil(stencilUrl, bitmap))
        }
        adapterStencilList.setList(stencils)
        adapterStencilList.notifyDataSetChanged()
    }

    private fun convertToBitmap(id: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(this.context!!, id)
        //val bitmap = BitmapFactory.decodeResource(resources, id, options)
        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }


}