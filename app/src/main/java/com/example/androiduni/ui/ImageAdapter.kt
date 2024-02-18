package com.example.androiduni.ui

import android.content.Context
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.ImageView
import com.example.androiduni.Client
import com.example.androiduni.PicassoNotSafe
import com.example.androiduni.R
import com.example.androiduni.message.Attachment
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception


class ImageAdapter(private val context: Context, private val imagesLinks: List<Attachment>) : BaseAdapter() {
    override fun getCount(): Int {
        return imagesLinks.size
    }

    override fun getItem(position: Int): Any {
        return imagesLinks[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val imageView: ImageView
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = ImageView(context)
            imageView.layoutParams = ViewGroup.LayoutParams(200, 200)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setPadding(8, 8, 8, 8)
        } else {
            imageView = convertView as ImageView
        }
        Log.d("ImageAdapter","Активность")
        PicassoNotSafe.get(context).load(Client.BASE_URL + imagesLinks[position].link).error(R.drawable.ic_error).into(imageView)
        return imageView
    }
}