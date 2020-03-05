package com.wittyneko.template.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import org.kodein.di.Kodein
import org.kodein.di.android.x.AndroidLifecycleScope
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.scoped
import org.kodein.di.generic.singleton

const val MAIN_MOUDLUE_TAG = "MAIN_MOUDLE_TAG"

val mainKodeinModule = Kodein.Module(MAIN_MOUDLUE_TAG) {

    bind() from scoped<Fragment>(AndroidLifecycleScope).singleton {
        ViewModelProviders.of(context.requireActivity(), ViewModelFactory {
            MainViewModule(instance(), instance())
        }).get<MainViewModule>()
    }

    bind() from scoped<FragmentActivity>(AndroidLifecycleScope).singleton {
        ViewModelProviders.of(context, ViewModelFactory {
            MainViewModule(instance(), instance())
        }).get<MainViewModule>()
    }
}


class ViewModelFactory(
    var create: (() -> ViewModel)? = null
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        create?.invoke() as? T ?: super.create(modelClass)
}