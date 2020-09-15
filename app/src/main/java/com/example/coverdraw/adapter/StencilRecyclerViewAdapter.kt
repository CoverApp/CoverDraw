package com.example.coverdraw.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.coverdraw.R
import com.example.coverdraw.databinding.StencilBinding
import com.example.coverdraw.model.Stencil
import kotlinx.android.synthetic.main.stencil.view.*

class StencilRecyclerViewAdapter(private val clickListener:(Stencil)->Unit): RecyclerView.Adapter<StencilViewHolder>(){

    private val stencilList = ArrayList<Stencil>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StencilViewHolder{
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding : StencilBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.stencil,parent,false)
        return StencilViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StencilViewHolder, position: Int) {
        holder.bind(stencilList[position],clickListener)
    }

    override fun getItemCount(): Int {
        return stencilList.size
    }

    fun setList(stencil: List<Stencil>) {
        stencilList.clear()
        stencilList.addAll(stencil)
    }

}

class StencilViewHolder(val binding: StencilBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(stencil: Stencil, clickListener: (Stencil) -> Unit) {
        when(stencil.image){
            null -> binding.stencilImageId.setImageResource(R.drawable.ic_baseline_broken_image_24)
            else -> binding.stencilImageId.setImageBitmap(stencil.image)
        }
        binding.stencilImageId.setOnClickListener {
            clickListener(stencil)
        }
    }
}