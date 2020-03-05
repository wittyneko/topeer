package com.wittyneko.topeer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.wittyneko.template.ui.MainViewModule
import com.wittyneko.template.ui.mainKodeinModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.Copy
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.android.retainedKodein
import org.kodein.di.generic.instance
import org.kodein.di.generic.kcontext
import java.net.*


class MainActivity : AppCompatActivity(), KodeinAware {

    val navHostFragment by lazy { findNavController(R.id.nav_host_fragment) }

    protected val parentKodein by closestKodein()

    override val kodeinContext = kcontext(this)

    override val kodein: Kodein by retainedKodein {
        extend(parentKodein, copy = Copy.All)
        import(mainKodeinModule)
    }
    val viewModule: MainViewModule by instance()

    override fun onSupportNavigateUp(): Boolean = navHostFragment.navigateUp()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
