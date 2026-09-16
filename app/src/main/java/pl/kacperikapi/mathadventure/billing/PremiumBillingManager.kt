package pl.kacperikapi.mathadventure.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.kacperikapi.mathadventure.data.PremiumAccess

enum class BillingIssue {
    NONE,
    PLAY_UNAVAILABLE,
    PRODUCT_NOT_CONFIGURED,
    PURCHASE_CANCELLED,
    PURCHASE_PENDING,
    PURCHASE_ERROR
}

data class PremiumBillingState(
    val connected: Boolean = false,
    val premiumUnlocked: Boolean = false,
    val purchasePending: Boolean = false,
    val productAvailable: Boolean = false,
    val priceText: String? = null,
    val issue: BillingIssue = BillingIssue.NONE
)

class PremiumBillingManager(context: Context) : PurchasesUpdatedListener {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(
        PremiumBillingState(
            premiumUnlocked = prefs.getBoolean(KEY_PREMIUM_CACHE, false)
        )
    )
    val state: StateFlow<PremiumBillingState> = _state.asStateFlow()

    private var productDetails: ProductDetails? = null
    private var offerToken: String? = null

    private val billingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    init {
        connect()
    }

    fun connect() {
        if (billingClient.isReady) {
            refresh()
            return
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _state.value = _state.value.copy(connected = true, issue = BillingIssue.NONE)
                    refresh()
                } else {
                    _state.value = _state.value.copy(
                        connected = false,
                        issue = BillingIssue.PLAY_UNAVAILABLE
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                _state.value = _state.value.copy(connected = false)
            }
        })
    }

    fun refresh() {
        if (!billingClient.isReady) {
            connect()
            return
        }
        queryProductDetails()
        queryOwnedPurchases()
    }

    fun restorePurchases() {
        queryOwnedPurchases()
    }

    private fun queryProductDetails() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PremiumAccess.PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsResult ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                _state.value = _state.value.copy(
                    productAvailable = false,
                    issue = BillingIssue.PRODUCT_NOT_CONFIGURED
                )
                return@queryProductDetailsAsync
            }

            val details = productDetailsResult.productDetailsList
                .firstOrNull { it.productId == PremiumAccess.PRODUCT_ID }
            productDetails = details

            val selectedOffer = details?.oneTimePurchaseOfferDetailsList?.firstOrNull()
                ?: details?.oneTimePurchaseOfferDetails
            offerToken = selectedOffer?.offerToken

            _state.value = _state.value.copy(
                productAvailable = details != null,
                priceText = selectedOffer?.formattedPrice,
                issue = if (details == null) BillingIssue.PRODUCT_NOT_CONFIGURED else BillingIssue.NONE
            )
        }
    }

    private fun queryOwnedPurchases() {
        if (!billingClient.isReady) {
            connect()
            return
        }
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                processPurchases(purchases)
            } else {
                _state.value = _state.value.copy(issue = BillingIssue.PURCHASE_ERROR)
            }
        }
    }

    fun launchPurchase(activity: Activity) {
        val details = productDetails
        if (!billingClient.isReady) {
            connect()
            _state.value = _state.value.copy(issue = BillingIssue.PLAY_UNAVAILABLE)
            return
        }
        if (details == null) {
            queryProductDetails()
            _state.value = _state.value.copy(issue = BillingIssue.PRODUCT_NOT_CONFIGURED)
            return
        }

        val productParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
        offerToken?.takeIf { it.isNotBlank() }?.let(productParamsBuilder::setOfferToken)

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParamsBuilder.build()))
            .build()

        val result = billingClient.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _state.value = _state.value.copy(issue = BillingIssue.PURCHASE_ERROR)
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> processPurchases(purchases.orEmpty())
            BillingClient.BillingResponseCode.USER_CANCELED ->
                _state.value = _state.value.copy(issue = BillingIssue.PURCHASE_CANCELLED)
            else -> _state.value = _state.value.copy(issue = BillingIssue.PURCHASE_ERROR)
        }
    }

    private fun processPurchases(purchases: List<Purchase>) {
        val matching = purchases.filter { PremiumAccess.PRODUCT_ID in it.products }
        val purchased = matching.firstOrNull { it.purchaseState == Purchase.PurchaseState.PURCHASED }
        val pending = matching.any { it.purchaseState == Purchase.PurchaseState.PENDING }

        if (purchased != null) {
            setPremiumUnlocked(true)
            if (!purchased.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchased.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(params) { result ->
                    if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                        _state.value = _state.value.copy(issue = BillingIssue.PURCHASE_ERROR)
                    }
                }
            }
        } else if (!pending) {
            setPremiumUnlocked(false)
        }

        _state.value = _state.value.copy(
            purchasePending = pending,
            issue = if (pending) BillingIssue.PURCHASE_PENDING else _state.value.issue
        )
    }

    private fun setPremiumUnlocked(value: Boolean) {
        prefs.edit().putBoolean(KEY_PREMIUM_CACHE, value).apply()
        _state.value = _state.value.copy(
            premiumUnlocked = value,
            purchasePending = false,
            issue = BillingIssue.NONE
        )
    }

    fun close() {
        if (billingClient.isReady) billingClient.endConnection()
    }

    private companion object {
        const val PREFS_NAME = "premium_billing"
        const val KEY_PREMIUM_CACHE = "premium_unlocked_verified"
    }
}
