package com.maher.irrigator

import android.os.Bundle
import android.os.StrictMode
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.maher.irrigator.model.kc.KC
import com.maher.irrigator.model.l.L
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.progress_view.*
import kotlinx.android.synthetic.main.toolbar_title.*
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var policy = StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        init()
    }

    private fun init() {
        toolbar_title.text = getString(R.string.app_name)
        val rawL = this.resources.openRawResource(R.raw.l)
        val rdL = BufferedReader(InputStreamReader(rawL))
        val gson = Gson()
        val LData = gson.fromJson<L>(rdL, L::class.java)
        val rawKc = this.resources.openRawResource(R.raw.kc)
        val rdKc = BufferedReader(InputStreamReader(rawKc))
        val KcData = gson.fromJson<KC>(rdKc, KC::class.java)
        list.layoutManager = LinearLayoutManager(this)
        val adapter = HomeAdapter(this)
        list.adapter = adapter
        adapter.setKcData(KcData)
        adapter.setData(LData)
        progress.visibility = View.GONE
    }
}
