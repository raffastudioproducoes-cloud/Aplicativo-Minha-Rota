package com.raffastudioproducoes.minharota.ui.screens.auth

import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.rounded.LockClock
import androidx.compose.material.icons.rounded.Person
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
import com.raffastudioproducoes.minharota.ui.theme.VerdeNeon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    onEmailVerificationRequired: () -> Unit = {},
    authViewModel: EmailAuthViewModel = viewModel(),
    profileCompletionOnly: Boolean = false
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()
    val isLoading = authState is AuthState.Loading
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.White else Color(0xFF1F2937)

    LaunchedEffect(profileCompletionOnly) {
        if (profileCompletionOnly) authViewModel.startProfileCompletion()
    }

    LaunchedEffect(authState) {
        if (authState.allowsNavigation()) onRegisterSuccess()
        if (authState is AuthState.EmailVerificationRequired) onEmailVerificationRequired()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (profileCompletionOnly) R.string.auth_complete_profile_title
                            else R.string.auth_register_title
                        ),
                        color = textColor
                    )
                },
                navigationIcon = {
                    if (!profileCompletionOnly) {
                        IconButton(onClick = onNavigateBack, enabled = !isLoading) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.auth_back),
                                tint = textColor
                            )
                        }
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(
                    if (profileCompletionOnly) R.string.auth_complete_profile_heading
                    else R.string.auth_join_us
                ),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                text = stringResource(
                    if (profileCompletionOnly) R.string.auth_complete_profile_subtitle
                    else R.string.auth_register_subtitle
                ),
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.Start).padding(bottom = 24.dp)
            )

            AuthTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.auth_full_name),
                enabled = !isLoading,
                leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                keyboardType = KeyboardType.Text,
                textColor = textColor
            )
            if (!profileCompletionOnly) {
                Spacer(modifier = Modifier.height(12.dp))
                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = stringResource(R.string.auth_email),
                    enabled = !isLoading,
                    leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                    keyboardType = KeyboardType.Email,
                    textColor = textColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                AuthTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = stringResource(R.string.auth_password),
                    enabled = !isLoading,
                    leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                contentDescription = stringResource(R.string.auth_toggle_password_visibility)
                            )
                        }
                    },
                    keyboardType = KeyboardType.Password,
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    textColor = textColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                AuthTextField(
                    value = confirmation,
                    onValueChange = { confirmation = it },
                    label = stringResource(R.string.auth_confirm_password),
                    enabled = !isLoading,
                    leadingIcon = { Icon(Icons.Rounded.LockClock, contentDescription = null) },
                    keyboardType = KeyboardType.Password,
                    visualTransformation = PasswordVisualTransformation(),
                    textColor = textColor
                )
            }

            AuthErrorMessage(authState)
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (profileCompletionOnly) authViewModel.completeProfile(name)
                    else authViewModel.register(name, email, password, confirmation)
                },
                enabled = authViewModel.isSubmitEnabled &&
                    ((profileCompletionOnly && name.isNotBlank()) || authViewModel.isProfileRecoveryPending ||
                        (name.isNotBlank() && email.isNotBlank() && password.isNotBlank() &&
                            confirmation.isNotBlank())),
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
                        stringResource(
                            if (profileCompletionOnly) R.string.auth_complete_profile_action
                            else R.string.auth_register
                        ),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean,
    leadingIcon: @Composable () -> Unit,
    keyboardType: KeyboardType,
    textColor: Color,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VerdeNeon,
            unfocusedTextColor = textColor,
            focusedTextColor = textColor
        )
    )
}
