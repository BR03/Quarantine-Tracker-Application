package com.learn.quarantinetrack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            val intent = Intent(this,hccRegistration::class.java)
            startActivity(intent)
        }
        button4.setOnClickListener {
            val intent = Intent(this,hccLogin::class.java)
            startActivity(intent)
        }

    }
}
