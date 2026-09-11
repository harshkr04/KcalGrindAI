package com.lumina.nutrition.feature.logging.voice

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.lumina.nutrition.core.designsystem.LuminaBackground
import com.lumina.nutrition.core.designsystem.LuminaError
import com.lumina.nutrition.core.designsystem.LuminaOnPrimary
import com.lumina.nutrition.core.designsystem.LuminaOnSurface
import com.lumina.nutrition.core.designsystem.LuminaOnSurfaceVariant
import com.lumina.nutrition.core.designsystem.LuminaPrimary
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainerHigh
import com.lumina.nutrition.core.designsystem.LuminaSurfaceContainerLowest

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceInputScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReview: () -> Unit,
    onNavigateToManualSearch: () -> Unit,
    viewModel: VoiceInputViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
    }

    // SpeechRecognizer setup
    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else null
    }

    DisposableEffect(speechRecognizer) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                viewModel.setListening(true)
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                viewModel.setListening(false)
            }
            override fun onError(error: Int) {
                viewModel.setListening(false)
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    viewModel.onTranscriptReceived(matches[0])
                }
                viewModel.setListening(false)
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    viewModel.onTranscriptReceived(matches[0])
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }

        speechRecognizer?.setRecognitionListener(listener)

        onDispose {
            speechRecognizer?.destroy()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (uiState.isListening) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LuminaBackground)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onNavigateBack),
                shape = CircleShape,
                color = LuminaSurfaceContainerHigh
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "←", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LuminaOnSurface)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Voice Logging",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = LuminaOnSurface
                    )
                )
                Text(
                    text = "Speak naturally, e.g. \"2 eggs and whole wheat toast\"",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = LuminaOnSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Central Animated Microphone Circle
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulsing outer halo
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(pulseScale)
                    .background(LuminaPrimary.copy(alpha = if (uiState.isListening) 0.25f else 0.10f), CircleShape)
            )

            // Main mic button
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .clickable {
                        if (!hasMicPermission) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            if (uiState.isListening) {
                                speechRecognizer?.stopListening()
                                viewModel.setListening(false)
                            } else {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                                }
                                speechRecognizer?.startListening(intent)
                                viewModel.setListening(true)
                            }
                        }
                    },
                shape = CircleShape,
                color = if (uiState.isListening) LuminaPrimary else LuminaSurfaceContainerHigh
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (uiState.isListening) "⏹" else "🎙️",
                        fontSize = 32.sp,
                        color = if (uiState.isListening) LuminaOnPrimary else LuminaOnSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (uiState.isListening) "Listening... Speak now" else "Tap microphone to speak",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = if (uiState.isListening) LuminaPrimary else LuminaOnSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Real-time Transcript Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = LuminaSurfaceContainerLowest
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Transcript",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = LuminaOnSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = uiState.transcript,
                    onValueChange = viewModel::onTranscriptReceived,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Your spoken transcript will appear here...",
                            color = LuminaOnSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Simulated Voice Chips (for emulator testing without virtual microphone)
        Text(
            text = "Or tap to simulate spoken voice:",
            style = MaterialTheme.typography.labelSmall.copy(
                color = LuminaOnSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "2 scrambled eggs, avocado toast, and black coffee",
                "Greek yogurt with blueberries and almonds",
                "Grilled chicken bowl with brown rice and salsa"
            ).forEach { sample ->
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.simulateTranscript(sample) },
                    shape = RoundedCornerShape(16.dp),
                    color = LuminaSurfaceContainerHigh
                ) {
                    Text(
                        text = sample,
                        color = LuminaOnSurface,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Offline / Error Banner
        if (uiState.isNetworkError) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = LuminaError.copy(alpha = 0.12f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "AI logging needs a connection",
                        color = LuminaError,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Unable to reach the AI server. Try manual search instead.",
                        color = LuminaOnSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onNavigateToManualSearch,
                        colors = ButtonDefaults.buttonColors(containerColor = LuminaPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Search Manual Database", color = LuminaOnPrimary, fontSize = 13.sp)
                    }
                }
            }
        }

        // Action CTA
        Button(
            onClick = { viewModel.analyzeTranscript(onNavigateToReview) },
            enabled = uiState.transcript.isNotBlank() && !uiState.isAnalyzing,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LuminaPrimary,
                disabledContainerColor = LuminaPrimary.copy(alpha = 0.4f)
            )
        ) {
            if (uiState.isAnalyzing) {
                CircularProgressIndicator(
                    color = LuminaOnPrimary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Analyzing Spoken Meal...", color = LuminaOnPrimary, fontWeight = FontWeight.Bold)
            } else {
                Text("Review & Confirm Food ✨", color = LuminaOnPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
