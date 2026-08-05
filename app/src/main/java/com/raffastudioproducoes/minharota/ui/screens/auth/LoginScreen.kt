package com.raffastudioproducoes.minharota.ui.screens.auth

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raffastudioproducoes.minharota.R
import com.raffastudioproducoes.minharota.repository.auth.AuthError
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateBack: () -> Unit,
    onLoginSuccess: () -> Unit,
    onProfileCompletionRequired: () -> Unit = {},
    onEmailVerificationRequired: () -> Unit = {},
    authViewModel: EmailAuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()
    val isLoading = authState is AuthState.Loading
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)

    LaunchedEffect(authState) {
        if (authState.allowsNavigation()) onLoginSuccess()
        if (authState is AuthState.ProfileCompletionRequired) onProfileCompletionRequired()
        if (authState is AuthState.EmailVerificationRequired) onEmailVerificationRequired()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.auth_login_title), color = textColor) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, enabled = !isLoading) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.auth_back),
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = stringResource(R.string.auth_welcome_back),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                text = stringResource(R.string.auth_login_subtitle),
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.Start).padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                enabled = !isLoading,
                label = { Text(stringResource(R.string.auth_email)) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdeNeon,
                    unfocusedTextColor = textColor,
                    focusedTextColor = textColor
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                enabled = !isLoading,
                label = { Text(stringResource(R.string.auth_password)) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                            contentDescription = stringResource(R.string.auth_toggle_password_visibility)
                        )
                    }
                },
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdeNeon,
                    unfocusedTextColor = textColor,
                    focusedTextColor = textColor
                )
            )

            AuthErrorMessage(authState)
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { authViewModel.login(email, password) },
                enabled = authViewModel.isSubmitEnabled &&
                    email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeNeon,
                    contentColor = Color.Black
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text(
                        stringResource(R.string.auth_sign_in),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
internal fun AuthErrorMessage(authState: AuthState) {
    val error = (authState as? AuthState.Error)?.error ?: return
    val message = when (error) {
        AuthError.REQUIRED_FIELDS -> R.string.auth_error_required_fields
        AuthError.INVALID_EMAIL -> R.string.auth_error_invalid_email
        AuthError.WEAK_PASSWORD -> R.string.auth_error_weak_password
        AuthError.PASSWORDS_DO_NOT_MATCH -> R.string.auth_error_passwords_do_not_match
        AuthError.INVALID_CREDENTIALS -> R.string.auth_error_invalid_credentials
        AuthError.TOO_MANY_REQUESTS -> R.string.auth_error_too_many_requests
        AuthError.NETWORK -> R.string.auth_error_network
        AuthError.PROFILE_UPDATE_FAILED -> R.string.auth_error_profile_update
        AuthError.EMAIL_VERIFICATION_FAILED -> R.string.auth_error_email_verification
        AuthError.SESSION_CHANGED -> R.string.auth_error_session_changed
        AuthError.REQUEST_FAILED -> R.string.auth_error_request_failed
        AuthError.UNKNOWN -> R.string.auth_error_unknown
    }
    Text(
        text = stringResource(message),
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
    )
}
