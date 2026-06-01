package com.example.capstone_frontend.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

enum class TopNotificationType {
    SUCCESS,
    INFO,
    ERROR
}

data class TopNotificationUiState(
    val title: String,
    val message: String,
    val type: TopNotificationType = TopNotificationType.INFO,
    val actionLabel: String? = null
)

@Composable
fun TopFloatingNotification(
    visible: Boolean,
    notification: TopNotificationUiState?,
    modifier: Modifier = Modifier,
    autoDismissMillis: Long = 3500L,
    onDismiss: () -> Unit,
    onActionClick: (() -> Unit)? = null
) {
    if (visible && notification != null) {
        LaunchedEffect(notification.title, notification.message) {
            delay(autoDismissMillis)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible && notification != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(durationMillis = 250)
        ) + fadeOut(animationSpec = tween(durationMillis = 250)),
        modifier = modifier
    ) {
        notification?.let { data ->
            val icon = when (data.type) {
                TopNotificationType.SUCCESS -> Icons.Default.CheckCircle
                TopNotificationType.INFO -> Icons.Default.Info
                TopNotificationType.ERROR -> Icons.Default.Error
            }

            val iconColor = when (data.type) {
                TopNotificationType.SUCCESS -> Color(0xFF1FA971)
                TopNotificationType.INFO -> Color(0xFF2F80ED)
                TopNotificationType.ERROR -> Color(0xFFD64545)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF20222A)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(iconColor.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = data.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = data.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFECECEC)
                            )
                        }
                    }

                    if (!data.actionLabel.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))

                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.10f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = data.actionLabel,
                                color = Color(0xFFFF4D6D),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable {
                                    onActionClick?.invoke()
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}