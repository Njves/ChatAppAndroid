package com.example.androiduni

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.androiduni.message.Attachment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.streams.toList


class ImageDetailActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager
    private var imageUrls: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_detail)
        viewPager = findViewById(R.id.viewPager)
        supportActionBar?.title = "Картинки"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        imageUrls = intent.extras?.getString("images")!!
        imageUrls.let {
            val gson = Gson()
            val listType = object : TypeToken<List<Attachment>>() {}.type
            val imageUrls: List<Attachment> = gson.fromJson(it, listType)
            val urls = imageUrls.stream().map {
                it.link
            }.toList()
            val pagerAdapter = ImageViewPagerAdapter(this, urls)
            viewPager.adapter = pagerAdapter
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}