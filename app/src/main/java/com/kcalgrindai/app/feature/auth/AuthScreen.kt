package com.kcalgrindai.app.feature.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.kcalgrindai.app.R
import com.kcalgrindai.app.core.designsystem.components.KcalGrindCoachIcon
import com.kcalgrindai.app.core.designsystem.components.liquidGlass
import com.kcalgrindai.app.core.designsystem.v3Colors

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onNavigateBack: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val v3 = MaterialTheme.v3Colors

    // Attempt to resolve Web Client ID from resources without hardcoding
    val webClientId = remember(context) {
        try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) context.getString(resId).takeIf { it.isNotBlank() } else null
        } catch (_: Exception) {
            null
        }
    }

    // Google Sign-In Activity Result Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_CANCELED) {
            viewModel.onGoogleSignInCancelled()
            return@rememberLauncherForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (!idToken.isNullOrBlank()) {
                viewModel.onGoogleSignInSuccess(idToken)
            } else {
                viewModel.onGoogleSignInFailure("Google authentication failed: ID token was null.")
            }
        } catch (e: ApiException) {
            if (e.statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                viewModel.onGoogleSignInCancelled()
            } else {
                val userFriendlyMessage = when (e.statusCode) {
                    CommonStatusCodes.DEVELOPER_ERROR ->
                        "Google Sign-In configuration error (Code 10): Ensure SHA-1 fingerprint and Web Client ID are registered in Firebase Console."
                    CommonStatusCodes.NETWORK_ERROR ->
                        "Network error during Google Sign-In. Please check your internet connection."
                    else ->
                        "Google Sign-In failed (Code ${e.statusCode}): ${e.localizedMessage ?: "Unknown error"}"
                }
                viewModel.onGoogleSignInFailure(userFriendlyMessage)
            }
        } catch (e: Exception) {
            viewModel.onGoogleSignInFailure(e.localizedMessage ?: "Unexpected error during Google Sign-In.")
        }
    }

    LaunchedEffect(uiState.authSuccess) {
        if (uiState.authSuccess) {
            onAuthSuccess()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = v3.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top Header: App Identity & V3 Liquid Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(v3.surfaceElevated)
                            .border(1.dp, v3.subtleBorder, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        KcalGrindCoachIcon(
                            size = 20.dp,
                            tint = v3.emeraldAccent,
                            strokeWidthDp = 2.dp
                        )
                    }

                    Text(
                        text = "Kcal Grind AI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = v3.textPrimary,
                        letterSpacing = 0.5.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = v3.surface,
                    border = BorderStroke(1.dp, v3.subtleBorder.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(v3.emeraldAccent)
                        )
                        Text(
                            text = "V3 LIQUID",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = v3.emeraldAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Sproutling Mascot Floating Chamber
            Box(
                modifier = Modifier.size(136.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer Radial Glow Circle
                Box(
                    modifier = Modifier
                        .size(136.dp)
                        .clip(CircleShape)
                        .background(v3.emeraldAccent.copy(alpha = 0.12f))
                )

                // Glass Ring Chamber
                Box(
                    modifier = Modifier
                        .size(116.dp)
                        .liquidGlass(RoundedCornerShape(32.dp))
                        .background(v3.surfaceElevated.copy(alpha = 0.85f))
                        .border(1.dp, v3.subtleBorder, RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_sproutling_waving),
                        contentDescription = "Sproutling mascot",
                        modifier = Modifier.size(72.dp)
                    )

                    // Sproutling Tag Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = v3.surface,
                        border = BorderStroke(1.dp, v3.emeraldAccent.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "SPROUTLING AI",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = v3.emeraldAccent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Value Proposition Headline
            Text(
                text = "Welcome to your\nAI Nutrition Journey",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = v3.textPrimary,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Track calories, balance macros, and reach your goals with your personal AI coach.",
                style = MaterialTheme.typography.bodyMedium,
                color = v3.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Micro Feature Badges Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = v3.surface.copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, v3.subtleBorder.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = v3.emeraldAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "AI Meal Scan",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = v3.textSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = v3.surface.copy(alpha = 0.8f),
                    border = BorderStroke(1.dp, v3.subtleBorder.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = null,
                            tint = v3.emeraldAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Android Glance",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = v3.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Error Banner (if error message exists)
            AnimatedVisibility(visible = uiState.errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = viewModel::dismissError,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss error",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // ================================================================
            // Primary Action: Continue with Google Button
            // ================================================================
            Button(
                onClick = {
                    if (uiState.isLoading) return@Button
                    if (webClientId.isNullOrBlank()) {
                        viewModel.onGoogleSignInFailure(
                            "Google Sign-In configuration required: OAuth Web Client ID is missing in google-services.json. Please add Web Client ID in Firebase Console."
                        )
                    } else {
                        viewModel.onGoogleSignInStarted()
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(webClientId)
                            .requestEmail()
                            .build()
                        val client = GoogleSignIn.getClient(context, gso)
                        googleSignInLauncher.launch(client.signInIntent)
                    }
                },
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF101211),
                    disabledContainerColor = Color.White.copy(alpha = 0.6f),
                    disabledContentColor = Color(0xFF101211).copy(alpha = 0.6f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterStart),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = v3.strongEmerald,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Continue with Google",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF101211)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Divider: "or"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = v3.subtleBorder.copy(alpha = 0.8f)
                )
                Text(
                    text = "or",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = v3.textMuted,
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = v3.subtleBorder.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Secondary Action: Sign in with Email (Toggles expandable email form)
            Surface(
                onClick = viewModel::toggleEmailExpanded,
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(24.dp),
                color = v3.surfaceElevated.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, v3.subtleBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = v3.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (uiState.isEmailExpanded) "Hide Email Form" else "Sign in with Email",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = v3.textPrimary
                    )
                }
            }

            // Expandable Email Form
            AnimatedVisibility(
                visible = uiState.isEmailExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = v3.surface),
                    border = BorderStroke(1.dp, v3.subtleBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Sign In / Create Account Tab Selector
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = v3.surfaceElevated,
                            border = BorderStroke(1.dp, v3.subtleBorder.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(if (!uiState.isSignUpMode) v3.surface else Color.Transparent)
                                        .clickable { if (uiState.isSignUpMode) viewModel.toggleMode() }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Sign In",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (!uiState.isSignUpMode) FontWeight.Bold else FontWeight.Normal,
                                        color = if (!uiState.isSignUpMode) v3.emeraldAccent else v3.textSecondary
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(if (uiState.isSignUpMode) v3.surface else Color.Transparent)
                                        .clickable { if (!uiState.isSignUpMode) viewModel.toggleMode() }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Create Account",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (uiState.isSignUpMode) FontWeight.Bold else FontWeight.Normal,
                                        color = if (uiState.isSignUpMode) v3.emeraldAccent else v3.textSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Email Field
                        Text(
                            text = "Email Address",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = v3.textSecondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = viewModel::onEmailChanged,
                            placeholder = { Text("you@example.com", color = v3.textMuted) },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = v3.emeraldAccent, modifier = Modifier.size(18.dp))
                            },
                            isError = uiState.emailError != null,
                            singleLine = true,
                            enabled = !uiState.isLoading,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = v3.textPrimary,
                                unfocusedTextColor = v3.textPrimary,
                                focusedContainerColor = v3.surfaceElevated,
                                unfocusedContainerColor = v3.surfaceElevated,
                                focusedBorderColor = v3.emeraldAccent,
                                unfocusedBorderColor = v3.subtleBorder,
                                errorBorderColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        AnimatedVisibility(visible = uiState.emailError != null) {
                            Text(
                                text = uiState.emailError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password Field
                        Text(
                            text = "Password",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = v3.textSecondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = uiState.password,
                            onValueChange = viewModel::onPasswordChanged,
                            placeholder = { Text("At least 6 characters", color = v3.textMuted) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = v3.emeraldAccent, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                IconButton(onClick = viewModel::togglePasswordVisibility) {
                                    Text(
                                        text = if (uiState.isPasswordVisible) "Hide" else "Show",
                                        color = v3.emeraldAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            isError = uiState.passwordError != null,
                            singleLine = true,
                            enabled = !uiState.isLoading,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = if (uiState.isSignUpMode) ImeAction.Next else ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel.submit()
                                }
                            ),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = v3.textPrimary,
                                unfocusedTextColor = v3.textPrimary,
                                focusedContainerColor = v3.surfaceElevated,
                                unfocusedContainerColor = v3.surfaceElevated,
                                focusedBorderColor = v3.emeraldAccent,
                                unfocusedBorderColor = v3.subtleBorder,
                                errorBorderColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        AnimatedVisibility(visible = uiState.passwordError != null) {
                            Text(
                                text = uiState.passwordError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }

                        // Confirm Password (Sign-up only)
                        AnimatedVisibility(visible = uiState.isSignUpMode) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Confirm Password",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = v3.textSecondary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                OutlinedTextField(
                                    value = uiState.confirmPassword,
                                    onValueChange = viewModel::onConfirmPasswordChanged,
                                    placeholder = { Text("Re-enter password", color = v3.textMuted) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = v3.emeraldAccent, modifier = Modifier.size(18.dp))
                                    },
                                    visualTransformation = PasswordVisualTransformation(),
                                    isError = uiState.confirmPasswordError != null,
                                    singleLine = true,
                                    enabled = !uiState.isLoading,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            focusManager.clearFocus()
                                            viewModel.submit()
                                        }
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = v3.textPrimary,
                                        unfocusedTextColor = v3.textPrimary,
                                        focusedContainerColor = v3.surfaceElevated,
                                        unfocusedContainerColor = v3.surfaceElevated,
                                        focusedBorderColor = v3.emeraldAccent,
                                        unfocusedBorderColor = v3.subtleBorder,
                                        errorBorderColor = MaterialTheme.colorScheme.error
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                AnimatedVisibility(visible = uiState.confirmPasswordError != null) {
                                    Text(
                                        text = uiState.confirmPasswordError ?: "",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.submit()
                            },
                            enabled = !uiState.isLoading,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = v3.emeraldAccent,
                                contentColor = Color(0xFF101211)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    color = Color(0xFF101211),
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = if (uiState.isSignUpMode) "Create Account" else "Sign In",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ================================================================
            // Tertiary Action: Continue as Guest Card
            // ================================================================
            Surface(
                onClick = viewModel::continueAsGuest,
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(20.dp),
                color = v3.surface.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, v3.subtleBorder.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(v3.surfaceElevated)
                                .border(1.dp, v3.subtleBorder, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = v3.emeraldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Continue as Guest",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = v3.textPrimary
                                )

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = v3.emeraldAccent.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "OFFLINE",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = v3.emeraldAccent,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Start tracking offline • No account required",
                                style = MaterialTheme.typography.bodySmall,
                                color = v3.textSecondary
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = v3.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer Policy Links
            Text(
                text = "By signing in, you agree to our Terms of Service and Privacy Policy.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = v3.textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
