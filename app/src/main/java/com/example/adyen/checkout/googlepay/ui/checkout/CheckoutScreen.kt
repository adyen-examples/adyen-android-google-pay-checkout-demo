package com.example.adyen.checkout.googlepay.ui.checkout

import android.app.Activity
import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.components.compose.get
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.googlepay.GooglePayComponent
import com.google.pay.button.ButtonTheme
import com.google.pay.button.ButtonType
import com.google.pay.button.PayButton

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
            is CheckoutUIState.GooglePayButton -> GooglePayButton(checkoutUIState.googlePayComponentData)
        }

        state.handleActivityResult?.let {
            HandleActivityResult(
                resultCode = it.resultCode,
                data = it.data,
                googlePayComponentData = it.googlePayComponentData,
                onFinished = viewModel::activityResultHandled
            )
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
fun GooglePayButton(googlePayComponentData: GooglePayComponentData) {
    val googlePayComponent = getGooglePayComponent(googlePayComponentData)
    val activity = LocalContext.current as Activity

    PayButton(
        onClick = { googlePayComponent.startGooglePayScreen(activity, googlePayComponentData.requestCode) },
        allowedPaymentMethods = GooglePayUtils.getAllowedPaymentMethods(),
        theme = ButtonTheme.Dark,
        type = ButtonType.Pay,
    )
}

@Composable
private fun HandleActivityResult(
    resultCode: Int,
    data: Intent?,
    googlePayComponentData: GooglePayComponentData,
    onFinished: () -> Unit,
) {
    val googlePayComponent = getGooglePayComponent(googlePayComponentData)
    googlePayComponent.handleActivityResult(resultCode, data)
    onFinished()
}

@Composable
private fun HandleAction(
    action: Action,
    googlePayComponentData: GooglePayComponentData,
    onFinished: () -> Unit,
) {
    val googlePayComponent = getGooglePayComponent(googlePayComponentData)
    val activity = LocalContext.current as Activity
    googlePayComponent.handleAction(action, activity)
    onFinished()
}

@Composable
private fun getGooglePayComponent(googlePayComponentData: GooglePayComponentData): GooglePayComponent {
    return with(googlePayComponentData) {
        GooglePayComponent.PROVIDER.get(
            checkoutSession = checkoutSession,
            paymentMethod = paymentMethod,
            configuration = googlePayConfiguration,
            componentCallback = componentCallback,
            key = key,
        )
    }
}
