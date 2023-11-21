package cn.jailedbird.core.common.base.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseVBFragment<T : ViewBinding> : Fragment() {

    companion object {
        // Using debug for navigation fragment's lifecycle
        private const val LIFECYCLE_TAG = "FragmentLifecycle"
    }

    @Suppress("PrivatePropertyName")
    private val TAG by lazy {
        this::class.java.name
    }
    private var _binding: T? = null
    open val binding
        get() = _binding!!

    abstract val inflate: (LayoutInflater, ViewGroup?, Boolean) -> T


    @CallSuper
    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(LIFECYCLE_TAG, "onAttach: ${TAG}@${this.hashCode()}")
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initOnCreate()
        Log.d(LIFECYCLE_TAG, "onCreate: ${TAG}@${this.hashCode()}")
    }

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        Log.d(LIFECYCLE_TAG, "onCreateView: ${TAG}@${this.hashCode()}")
        _binding = inflate.invoke(inflater, container, false)
        return binding.root
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(LIFECYCLE_TAG, "onViewCreated: ${TAG}@${this.hashCode()}")
        initView()
        initEvent()
        initObserve()
        initOther()
    }

    open fun initOnCreate() {

    }

    /**
     * Log.d the fragment lifecycle only for debug in app
     * */
    @CallSuper
    override fun onStart() {
        super.onStart()
        Log.d(LIFECYCLE_TAG, "onStart: ${TAG}@${this.hashCode()}")
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        Log.d(LIFECYCLE_TAG, "onResume: ${TAG}@${this.hashCode()}")
    }

    @CallSuper
    override fun onPause() {
        super.onPause()
        Log.d(LIFECYCLE_TAG, "onPause: ${TAG}@${this.hashCode()}")
    }

    @CallSuper
    override fun onStop() {
        super.onStop()
        Log.d(LIFECYCLE_TAG, "onStop: ${TAG}@${this.hashCode()}")
    }

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(LIFECYCLE_TAG, "onDestroyView: ${TAG}@${this.hashCode()}")
        // Avoid binding leak!!!
        _binding = null
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        Log.d(LIFECYCLE_TAG, "onDestroy: ${TAG}@${this.hashCode()}")
    }

    @CallSuper
    override fun onDetach() {
        super.onDetach()
        Log.d(LIFECYCLE_TAG, "onDetach: ${TAG}@${this.hashCode()}")
    }

    /**
     * init view such us set the [View.GONE]
     * */
    protected open fun initView() {}

    /**
     * init event such as button click listener
     */
    protected open fun initEvent() {}

    /**
     * init observer such as the observe of livedata(or flow) element in ViewModel
     */
    protected open fun initObserve() {}

    /**
     * init other
     * */
    protected open fun initOther() {}
}