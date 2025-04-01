package com.example.adyen.checkout.googlepay.ui.checkout

import androidx.activity.compose.LocalActivity
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.components.compose.AdyenComponent
import com.adyen.checkout.components.compose.get
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.googlepay.GooglePayComponent

@Composable
fun CheckoutScreen(
    viewModel: CheckoutViewModel,
) {
    val state by viewModel.checkoutState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val checkoutUIState = state.checkoutUIState) {
            is CheckoutUIState.LoadingSpinner -> LoadingSpinner()
            is CheckoutUIState.StatusText -> StatusText(checkoutUIState.textResId)
            is CheckoutUIState.GooglePayComponent -> GooglePayComponent(checkoutUIState.googlePayComponentData)
        }

        state.handleAction?.let {
            HandleAction(
                action = it.action,
                googlePayComponentData = it.googlePayComponentData,
                onFinished = viewModel::actionHandled
            )
        }
    }
}

@Composable
fun LoadingSpinner() {
    CircularProgressIndicator(
        modifier = Modifier.width(48.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        trackColor = MaterialTheme.colorScheme.secondary,
    )
}

@Composable
fun StatusText(@StringRes textResId: Int) {
    Text(text = stringResource(id = textResId))
}

@Composable
fun GooglePayComponent(googlePayComponentData: GooglePayComponentData) {
    val googlePayComponent = getGooglePayComponent(googlePayComponentData)
    AdyenComponent(googlePayComponent)
}

@Composable
private fun HandleAction(
    action: Action,
    googlePayComponentData: GooglePayComponentData,
    onFinished: () -> Unit,
) {
    val googlePayComponent = getGooglePayComponent(googlePayComponentData)
    val activity = LocalActivity.current ?: return
    googlePayComponent.handleAction(action, activity)
    onFinished()
}

@Composable
private fun getGooglePayComponent(googlePayComponentData: GooglePayComponentData): GooglePayComponent {
    return with(googlePayComponentData) {
        GooglePayComponent.PROVIDER.get(
            checkoutSession = checkoutSession,
            paymentMethod = paymentMethod,
            checkoutConfiguration = checkoutConfiguration,
            componentCallback = componentCallback,
            key = key,
        )
    }
}
