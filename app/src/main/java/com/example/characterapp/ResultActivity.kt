package com.example.characterapp

import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_result.*

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val msg = intent.getStringExtra("msg");
        var resource = R.drawable.ready
        when(msg){
            "BTS RM"-> resource = R.drawable.bts_rm
            "창모"-> resource = R.drawable.chang
            "옥택연"->resource = R.drawable.ock
            "BTS 뷔"->resource = R.drawable.bts_v
            "BTS 슈가"->resource = R.drawable.bts_suga
            "BTS 정국"->resource = R.drawable.bts_junguk
            "BTS 제이홉"->resource = R.drawable.bts_jhob
            "BTS 지민"->resource = R.drawable.bts_jimin
            "BTS 진"->resource = R.drawable.bts_jin
            "강다니엘"->resource = R.drawable.daniel
            "권지용"->resource = R.drawable.g_dragon
            "권혁수"->resource = R.drawable.heyucksoo
            "김민석"->resource = R.drawable.kimminsuck
            "남궁민"->resource = R.drawable.namgoong
            "남주혁"->resource = R.drawable.namjoo
            "박명수"->resource = R.drawable.parkmeyong
            "박서준"->resource = R.drawable.parkseo
            "박재범"->resource = R.drawable.jpark
            "비와이"->resource = R.drawable.by
            "샤이니 민호"->resource = R.drawable.minho
            "샤이니 온유"->resource = R.drawable.onyoo
            "샤이니 키"->resource = R.drawable.key
            "송강호"->resource = R.drawable.songang
            "이병헌"->resource = R.drawable.leebyeong
            "이승기"->resource = R.drawable.leeseungki
            "유재석"->resource = R.drawable.yoojaesuk
            "유해진"->resource = R.drawable.yoohaejin
            "이정재"->resource = R.drawable.leejung
        }
        result_image.setImageResource(resource)
        result_txt.text = "${msg}"

    }
}