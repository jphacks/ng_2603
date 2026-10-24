package com.example.myapplication.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.myapplication.domain.model.AvatarExpression
import com.example.myapplication.domain.model.DisappointmentLevel
import com.example.myapplication.ui.disappointment.components.ParentAvatarView
import com.example.myapplication.ui.theme.AkiColdGlanceBg
import com.example.myapplication.ui.theme.MyApplicationTheme

/**
 * 画面最前面介入サービス (SYSTEM_ALERT_WINDOW)
 * 制限時間超過時に強制描画し、操作を物理的に邪魔する
 */
class DisappointmentOverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    // Compose State
    private var overlayAlphaState by mutableFloatStateOf(0.7f)
    private var quoteState by mutableStateOf("………………。")
    private var subQuoteState by mutableStateOf("（無言の圧を感じてください）")
    private var expressionState by mutableStateOf(AvatarExpression.COLD_STARE)

    companion object {
        const val ACTION_SHOW = "ACTION_SHOW_OVERLAY"
        const val ACTION_HIDE = "ACTION_HIDE_OVERLAY"
        const val EXTRA_ALPHA = "EXTRA_ALPHA"
        const val EXTRA_BLOCK_TOUCH = "EXTRA_BLOCK_TOUCH"
        const val EXTRA_QUOTE = "EXTRA_QUOTE"
        const val EXTRA_SUB_QUOTE = "EXTRA_SUB_QUOTE"

        fun show(
            context: Context,
            alpha: Float,
            blockTouch: Boolean,
            quote: String,
            subQuote: String
        ) {
            val intent = Intent(context, DisappointmentOverlayService::class.java).apply {
                action = ACTION_SHOW
                putExtra(EXTRA_ALPHA, alpha)
                putExtra(EXTRA_BLOCK_TOUCH, blockTouch)
                putExtra(EXTRA_QUOTE, quote)
                putExtra(EXTRA_SUB_QUOTE, subQuote)
            }
            context.startService(intent)
        }

        fun hide(context: Context) {
            val intent = Intent(context, DisappointmentOverlayService::class.java).apply {
                action = ACTION_HIDE
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW -> {
                val alpha = intent.getFloatExtra(EXTRA_ALPHA, 0.7f)
                val blockTouch = intent.getBooleanExtra(EXTRA_BLOCK_TOUCH, false)
                val quote = intent.getStringExtra(EXTRA_QUOTE) ?: "………………。"
                val subQuote = intent.getStringExtra(EXTRA_SUB_QUOTE) ?: "（無言の圧を感じてください）"

                overlayAlphaState = alpha
                quoteState = quote
                subQuoteState = subQuote
                expressionState = if (alpha > 0.8f) AvatarExpression.COLD_STARE else AvatarExpression.GLARE

                showOrUpdateOverlay(alpha, blockTouch)
            }
            ACTION_HIDE -> {
                removeOverlay()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun showOrUpdateOverlay(alpha: Float, blockTouch: Boolean) {
        val wm = windowManager ?: return

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        var windowFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN

        if (!blockTouch) {
            windowFlags = windowFlags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutType,
            windowFlags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        if (composeView == null) {
            composeView = ComposeView(this).apply {
                setViewTreeLifecycleOwner(this@DisappointmentOverlayService)
                setViewTreeSavedStateRegistryOwner(this@DisappointmentOverlayService)
                setContent {
                    MyApplicationTheme(darkTheme = true) {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(AkiColdGlanceBg.copy(alpha = overlayAlphaState))
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF1D1B26).copy(alpha = 0.92f)
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        ParentAvatarView(
                                            expression = expressionState,
                                            level = DisappointmentLevel.LEVEL_4_COLD_STARE
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Text(
                                            text = quoteState,
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = subQuoteState,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = Color(0xFFCCCCCC)
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            try {
                wm.addView(composeView, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            try {
                wm.updateViewLayout(composeView, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun removeOverlay() {
        composeView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            composeView = null
        }
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        removeOverlay()
        super.onDestroy()
    }
}
