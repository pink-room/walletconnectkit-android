@file:Suppress("PackageDirectoryMismatch") // Because library imports are prettier this way!
package dev.pinkroom.walletconnectkit

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.setPadding
import dev.pinkroom.walletconnectkit.common.canBeIgnored
import dev.pinkroom.walletconnectkit.common.px
import dev.pinkroom.walletconnectkit.common.viewScope
import kotlinx.coroutines.launch
import org.walletconnect.Session

class WalletConnectButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageButton(context, attrs, defStyleAttr), Session.Callback {

    private lateinit var walletConnectKit: WalletConnectKit
    private lateinit var onConnected: (address: String) -> Unit
    private var onDisconnected: (() -> Unit)? = null
    var sessionCallback: Session.Callback? = null

    init {
        initSrc(attrs)
        initPadding(attrs)
        initBackground(attrs)
    }

    fun start(
        walletConnectKit: WalletConnectKit,
        onConnected: (address: String) -> Unit,
        onDisconnected: (() -> Unit)?
    ) {
        this.walletConnectKit = walletConnectKit
        this.onDisconnected = onDisconnected
        this.onConnected = onConnected
        initInputListener(walletConnectKit)
        loadSessionIfStored(walletConnectKit, onConnected)
    }

    fun start(walletConnectKit: WalletConnectKit, onConnected: (address: String) -> Unit) =
        start(walletConnectKit, onConnected, null)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = MeasureSpec.makeMeasureSpec(layoutParams.width, MeasureSpec.EXACTLY)
        var height = MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY)
        if (layoutParams.width == WRAP_CONTENT) {
            width = MeasureSpec.makeMeasureSpec(232.px, MeasureSpec.EXACTLY)
        }
        if (layoutParams.height == WRAP_CONTENT) {
            height = MeasureSpec.makeMeasureSpec(48.px, MeasureSpec.EXACTLY)
        }
        super.onMeasure(width, height)
    }

    override fun onStatus(status: Session.Status) {
        viewScope.launch {
            when (status) {
                is Session.Status.Approved -> onSessionApproved()
                is Session.Status.Connected -> onSessionConnected()
                is Session.Status.Closed -> onSessionDisconnected()
                else -> canBeIgnored
            }
            sessionCallback?.onStatus(status)
        }
    }

    override fun onMethodCall(call: Session.MethodCall) {
        viewScope.launch { sessionCallback?.onMethodCall(call) }
    }

    private fun initSrc(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.src))
        val src = typedArray.getResourceId(0, -1)
        if (src == -1) setImageResource(R.drawable.ic_walletconnect)
        typedArray.recycle()
    }

    private fun initPadding(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.padding))
        val padding = typedArray.getDimensionPixelSize(0, -1)
        if (padding == -1) setPadding(12.px)
        typedArray.recycle()
    }

    private fun initBackground(attrs: AttributeSet?) {
        val typedArray =
            context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.background))
        val drawable = typedArray.getDrawable(0)
        if (drawable == null) {
            setBackgroundResource(R.drawable.button_walletconnect_background)
            scaleType = ScaleType.FIT_CENTER
        }
        typedArray.recycle()
    }

    private fun initInputListener(walletConnectKit: WalletConnectKit) {
        setOnClickListener {
            if (walletConnectKit.isSessionStored) {
                walletConnectKit.removeSession()
            }
            walletConnectKit.createSession(this)
        }
    }

    private fun loadSessionIfStored(
        walletConnectKit: WalletConnectKit,
        onConnected: (address: String) -> Unit
    ) {
        if (walletConnectKit.isSessionStored) {
            walletConnectKit.loadSession(this)
            walletConnectKit.address?.let(onConnected)
        }
    }

    private fun onSessionApproved() {
        walletConnectKit.address?.let(onConnected)
    }

    private fun onSessionConnected() {
        walletConnectKit.address ?: walletConnectKit.requestHandshake()
    }

    private fun onSessionDisconnected() {
        onDisconnected?.invoke()
    }
}