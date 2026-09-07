package com.lmt.global.base.common

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.ScrollView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.annotation.CallSuper
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.NestedScrollView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import com.lmt.global.base.helper.firebase.RemoteConfigManagement
import com.lmt.global.base.helper.preferences.AppSharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.Locale

typealias OnPerformBackPressed = () -> Unit

abstract class IActivity<VB : ViewDataBinding, VM : IViewModel<*>> : AppCompatActivity() {

    protected val appSharedPreferences by inject<AppSharedPreferences>()
    protected val configManagement by inject<RemoteConfigManagement>()

    protected val viewModel: VM by provideViewModel()
    protected abstract fun provideViewModel(): Lazy<VM>

    @LayoutRes
    protected abstract fun provideLayout(): Int
    protected val viewBinding: VB by lazy {
        DataBindingUtil.setContentView<VB>(this@IActivity, provideLayout())
    }

    private var onPerformBackPressed: OnPerformBackPressed? = null
    private var imeInsetBottom = 0
    private var insetRoot: View? = null
    private var focusChangeListener: ViewTreeObserver.OnGlobalFocusChangeListener? = null
    private var onBackPressedCallback = object : OnBackPressedCallback(enabled = true) {
        override fun handleOnBackPressed() {
            onPerformBackPressed?.invoke()
        }
    }

    protected val TAG: String
        get() = this::class.java.simpleName

    override fun attachBaseContext(newBase: Context) {
        var isoLanguage = appSharedPreferences.currentLanguage
        if (isoLanguage.isEmpty()) {
            isoLanguage = Resources.getSystem().configuration.locales[0].language
        }
        val newLocale = Locale(isoLanguage.toString())
        Locale.setDefault(newLocale)
        val configuration = newBase.resources.configuration
        configuration.setLocale(newLocale)
        val newContext = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(ContextWrapper(newContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setupInit()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Draw edge-to-edge consistently on every Android version. Normal screens receive
        // their real status-bar/cutout inset below; full-screen screens opt out of that inset.
        enableConfigEdgeSystemBar(
            isFitsSystemWindows = false,
            isLight = usesDarkStatusBarIcons()
        )
        registerBackPressedDispatcher()
        viewBinding.lifecycleOwner = this@IActivity
        applySafeInsets()

        initViews(savedInstanceState)
        initObservers()
        initListeners()
    }

    protected fun enableConfigEdgeSystemBar(
        isFitsSystemWindows: Boolean = true,
        isLight: Boolean = true
    ) {
        lifecycleScope.launch(Dispatchers.Main) {
            WindowCompat.setDecorFitsSystemWindows(window, isFitsSystemWindows)
            window.decorView.post {
                WindowInsetsControllerCompat(window, window.decorView).let { controller ->
                    controller.isAppearanceLightStatusBars = isLight
                    controller.isAppearanceLightNavigationBars = isLight
                    controller.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
        }
    }

    protected open fun isHideSystemBars(): Boolean = false
    protected open fun usesDarkStatusBarIcons(): Boolean = true

    private fun applySafeInsets() {
        if (isHideSystemBars()) return

        val root = viewBinding.root
        val initialPaddingLeft = root.paddingLeft
        val initialPaddingTop = root.paddingTop
        val initialPaddingRight = root.paddingRight
        val initialPaddingBottom = root.paddingBottom

        insetRoot = root
        focusChangeListener = ViewTreeObserver.OnGlobalFocusChangeListener { _, newFocus ->
            keepFocusedInputAboveKeyboard(newFocus, root, imeInsetBottom)
        }.also(root.viewTreeObserver::addOnGlobalFocusChangeListener)

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val safeTop = insets.getInsets(
                WindowInsetsCompat.Type.statusBars() or
                    WindowInsetsCompat.Type.displayCutout()
            ).top
            val isImeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeBottom = if (isImeVisible) {
                insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            } else {
                0
            }
            imeInsetBottom = imeBottom
            view.setPadding(
                initialPaddingLeft,
                initialPaddingTop + safeTop,
                initialPaddingRight,
                initialPaddingBottom + imeBottom
            )
            keepFocusedInputAboveKeyboard(currentFocus, root, imeBottom)
            insets
        }
        ViewCompat.requestApplyInsets(root)
    }

    private fun keepFocusedInputAboveKeyboard(
        focusedView: View?,
        root: View,
        imeBottom: Int
    ) {
        if (focusedView !is EditText || imeBottom <= 0) return

        root.post {
            if (!focusedView.isAttachedToWindow || !focusedView.hasFocus()) return@post

            val rootLocation = IntArray(2)
            val inputLocation = IntArray(2)
            root.getLocationOnScreen(rootLocation)
            focusedView.getLocationOnScreen(inputLocation)

            val gapAboveKeyboard = (KEYBOARD_INPUT_GAP_DP * resources.displayMetrics.density).toInt()
            val visibleBottom = rootLocation[1] + root.height - imeBottom - gapAboveKeyboard
            val inputBottom = inputLocation[1] + focusedView.height
            val overlap = inputBottom - visibleBottom
            if (overlap <= 0) return@post

            var ancestor: View? = focusedView.parent as? View
            while (ancestor != null) {
                when (ancestor) {
                    is ScrollView -> {
                        ancestor.smoothScrollBy(0, overlap)
                        return@post
                    }
                    is NestedScrollView -> {
                        ancestor.smoothScrollBy(0, overlap)
                        return@post
                    }
                }
                ancestor = ancestor.parent as? View
            }
        }
    }

    protected fun applyStatusBarStyle(@ColorRes color: Int, darkIcons: Boolean) {
        @Suppress("DEPRECATION")
        window.statusBarColor = ContextCompat.getColor(this, color)
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = darkIcons
    }
    protected fun setupApplyWindowInsetListener(block: (insets: WindowInsetsCompat) -> Unit) {
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insetCompact ->
            block(insetCompact)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun registerBackPressedDispatcher() {
        onBackPressedDispatcher.addCallback(this@IActivity, onBackPressedCallback)
        onBackPressedCallback.isEnabled = onHandleBackPressed()
    }

    @CallSuper
    protected open fun onHandleBackPressed(onBackPressed: OnPerformBackPressed? = null): Boolean {
        this.onPerformBackPressed = onBackPressed
        return onBackPressed != null
    }

    protected open fun setupInit() = Unit

    protected abstract fun initViews(savedInstanceState: Bundle?)

    @CallSuper
    protected open fun initObservers() {
        viewModel
    }

    protected open fun initListeners() = Unit

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        var isoLanguage = appSharedPreferences.currentLanguage
        if (isoLanguage.isEmpty()) {
            isoLanguage = Resources.getSystem().configuration.locales[0].language
        }
        val newLocale = Locale(isoLanguage)
        Locale.setDefault(newLocale)
        newConfig.setLocale(newLocale)
    }

    protected fun setupToolbar(
        toolbar: Toolbar?,
        @StringRes title: Int? = null,
        @DrawableRes icon: Int? = null,
        onNavigationClicked: (() -> Unit)? = null
    ) {
        toolbar ?: return
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayShowTitleEnabled(title != null)
            title?.let { supportActionBar?.setTitle(title) }

            it.setDisplayHomeAsUpEnabled(icon != null)
            icon?.let { supportActionBar?.setHomeAsUpIndicator(icon) }
        }
        toolbar.setNavigationOnClickListener {
            onNavigationClicked?.invoke() ?: onBackPressedDispatcher.onBackPressed()
        }
    }

    @CallSuper
    override fun onStart() {
        postHideSystemBar()
        super.onStart()
    }

    override fun onDestroy() {
        val root = insetRoot
        val listener = focusChangeListener
        if (root != null && listener != null && root.viewTreeObserver.isAlive) {
            root.viewTreeObserver.removeOnGlobalFocusChangeListener(listener)
        }
        focusChangeListener = null
        insetRoot = null
        super.onDestroy()
    }

    protected fun postHideSystemBar() {
        window.decorView.postDelayed({
            WindowInsetsControllerCompat(window, window.decorView).let { controller ->
                if (isHideSystemBars()) {
                    controller.hide(WindowInsetsCompat.Type.systemBars())
                } else {
                    controller.show(WindowInsetsCompat.Type.statusBars())
                    controller.hide(WindowInsetsCompat.Type.navigationBars())
                }
            }
        }, 50L)
    }

    private companion object {
        const val KEYBOARD_INPUT_GAP_DP = 12
    }
}
