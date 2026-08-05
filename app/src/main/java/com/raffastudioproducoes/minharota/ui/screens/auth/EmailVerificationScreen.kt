package com.raffastudioproducoes.minharota.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raffastudioproducoes.minharota.R
import kotlinx.coroutines.delay

@Composable
fun EmailVerificationScreen(
    onMainAuthorized: () -> Unit,
    onProfileCompletionRequired: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: EmailAuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    var cooldownSeconds by remember { mutableLongStateOf(0L) }
    val isLoading = authState is AuthState.Loading

    LaunchedEffect(Unit) {
        authViewModel.startEmailVerification()
    }
    LaunchedEffect(authState) {
        when (authState) {
            AuthState.Authenticated -> onMainAuthorized()
            AuthState.ProfileCompletionRequired -> onProfileCompletionRequired()
            else -> Unit
        }
    }
    LaunchedEffect(authState, isLoading) {
        do {
            cooldownSeconds = authViewModel.cooldownRemainingSeconds()
            if (cooldownSeconds > 0L) delay(1_000L)
        } while (cooldownSeconds > 0L)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.auth_verify_email_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.auth_verify_email_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        AuthErrorMessage(authState)
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = authViewModel::checkEmailVerification,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) CircularProgressIndicator()
            else Text(stringResource(R.string.auth_verify_email_check))
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = authViewModel::resendVerificationEmail,
            enabled = authViewModel.canResendVerification && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (cooldownSeconds > 0L) {
                    stringResource(R.string.auth_verify_email_resend_cooldown, cooldownSeconds)
                } else {
                    stringResource(R.string.auth_verify_email_resend)
                }
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.auth_verify_email_rate_limit_note),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        TextButton(
            onClick = {
                authViewModel.logout()
                onLogout()
            },
            enabled = true
        ) {
            Text(stringResource(R.string.auth_verify_email_change_account))
        }
    }
}
