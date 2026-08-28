package br.com.amandaluz.cielotickets.ui.binding

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class FragmentViewBindingDelegate<T : ViewBinding>(
    private val fragment: Fragment,
    private val bind: (View) -> T,
) : ReadOnlyProperty<Fragment, T>, DefaultLifecycleObserver {
    private var binding: T? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        binding?.let { return it }
        val viewLifecycleOwner = fragment.viewLifecycleOwner
        return bind(fragment.requireView()).also { createdBinding ->
            binding = createdBinding
            viewLifecycleOwner.lifecycle.addObserver(this)
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        binding = null
        owner.lifecycle.removeObserver(this)
    }
}

fun <T : ViewBinding> Fragment.viewBinding(
    bind: (View) -> T,
): FragmentViewBindingDelegate<T> =
    FragmentViewBindingDelegate(this, bind)

