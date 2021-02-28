package com.example.characterapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_result.*

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val msg = intent.getStringExtra("msg");
        if(msg=="선풍기"){
            result_image.setImageResource(R.drawable.coolltool)
        }
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()

    }
}