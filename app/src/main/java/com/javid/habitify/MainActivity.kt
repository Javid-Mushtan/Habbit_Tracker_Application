package com.javid.habitify

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.javid.habitify.fragments.HomeFragment
import com.javid.habitify.fragments.LSbutton
import com.javid.habitify.fragments.LoginFragment
import com.javid.habitify.fragments.SignupFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        /*supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, LSbutton())
            .commit()*/

//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragment_container, LoginFragment.newInstance())
//            .commit()

//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragment_container, SignupFragment.newInstance())
//            .commit()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment.newInstance())
            .commit()
    }
}
