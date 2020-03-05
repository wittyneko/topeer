package com.wittyneko.topeer

import androidx.fragment.app.Fragment
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.kcontext

abstract class BaseFragment : Fragment(), KodeinAware {

    protected val parentKodein by closestKodein()

    override val kodeinContext = kcontext<Fragment>(this)
}