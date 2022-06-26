package pers.vaccae.mvidemo.ui.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import pers.vaccae.mvidemo.R
import kotlin.reflect.KClass

class DemoActivity : AppCompatActivity() {

    private val btnhing_angle :Button by lazy { findViewById(R.id.hing_angle_demo) }
    private val btnwindowmanager : Button by lazy { findViewById(R.id.windonmanager_demo) }
    private val btnfold :Button by lazy { findViewById(R.id.fold_demo) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)

        btnhing_angle.setOnClickListener {
            openActivity(MainActivity::class)
        }

        btnwindowmanager.setOnClickListener { openActivity(WindowInfoTrackerActivity::class) }

        btnfold.setOnClickListener { openActivity(FoldActivity::class) }
    }

    private fun <T : Any> openActivity(cls: KClass<T>){
        val intent = Intent(this, cls.java)
        startActivity(intent)
    }
}