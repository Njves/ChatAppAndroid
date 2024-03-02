package com.example.androiduni

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter

class ImageViewPagerAdapter(private val context: Context, private val imageUrls: List<String>) :
    PagerAdapter() {

    override fun getCount(): Int {
        return imageUrls.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_image, container, false)
        val imageView = view.findViewById<ImageView>(R.id.imagePreview)
        PicassoNotSafe(context).get().load(Client.BASE_URL + imageUrls[position]).error(R.drawable.ic_error).placeholder(R.drawable.placeholder).centerCrop().fit().into(imageView)
        container.addView(view)
        return view
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}