package com.mentricstudios.cidquest.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.mentricstudios.cidquest.ads.BannerAd
import com.mentricstudios.cidquest.notifications.ReminderScheduler
import com.mentricstudios.cidquest.ui.theme.AccentAmber
import com.mentricstudios.cidquest.ui.theme.AccentGold
import com.mentricstudios.cidquest.ui.theme.BackgroundBottom
import com.mentricstudios.cidquest.ui.theme.BackgroundTop
import com.mentricstudios.cidquest.ui.theme.CardLocked
import com.mentricstudios.cidquest.ui.theme.TextPrimary
import com.mentricstudios.cidquest.ui.theme.TextSecondary
import com.mentricstudios.cidquest.util.CharacterPhoto
import com.mentricstudios.cidquest.util.NotificationPrefs
import com.mentricstudios.cidquest.util.SettingsPrefs
import com.mentricstudios.cidquest.util.bounceClick

private const val SUPPORT_EMAIL = "mentricstudios@gmail.com"
private const val PACKAGE_NAME = "com.mentricstudios.cidquest"

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var onScreenControlsEnabled by remember { mutableStateOf(SettingsPrefs.isOnScreenControlsEnabled(context)) }
    var vibrationEnabled by remember { mutableStateOf(SettingsPrefs.isVibrationEnabled(context)) }
    var soundEnabled by remember { mutableStateOf(SettingsPrefs.isSoundEnabled(context)) }
    var remindersEnabled by remember { mutableStateOf(NotificationPrefs.areRemindersEnabled(context)) }
    var showMoreGamesNotice by remember { mutableStateOf(false) }
    var hasCustomPhoto by remember { mutableStateOf(CharacterPhoto.hasCustomPhoto(context)) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            hasCustomPhoto = CharacterPhoto.saveFromUri(context, uri)
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        // If the player denies the OS prompt, reflect that back into the
        // toggle rather than leaving it stuck "on" with nothing actually
        // scheduled.
        remindersEnabled = granted
        NotificationPrefs.setRemindersEnabled(context, granted)
        ReminderScheduler.scheduleDailyReminders(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundTop)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val backInteraction = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .bounceClick(backInteraction, pressedScale = 0.85f)
                        .clickable(interactionSource = backInteraction, indication = null) { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChevronLeft,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = "SETTINGS",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 28.dp),
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier.padding(top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ToggleSettingButton(
                    icon = Icons.Filled.VolumeUp,
                    label = "SOUND",
                    enabled = soundEnabled,
                    onToggle = {
                        soundEnabled = !soundEnabled
                        SettingsPrefs.setSoundEnabled(context, soundEnabled)
                    }
                )

                ToggleSettingButton(
                    icon = Icons.Filled.GridView,
                    label = "ON-SCREEN CONTROLS",
                    enabled = onScreenControlsEnabled,
                    onToggle = {
                        onScreenControlsEnabled = !onScreenControlsEnabled
                        SettingsPrefs.setOnScreenControlsEnabled(context, onScreenControlsEnabled)
                    }
                )

                ToggleSettingButton(
                    icon = Icons.Filled.Vibration,
                    label = "VIBRATION",
                    enabled = vibrationEnabled,
                    onToggle = {
                        vibrationEnabled = !vibrationEnabled
                        SettingsPrefs.setVibrationEnabled(context, vibrationEnabled)
                    }
                )

                ToggleSettingButton(
                    icon = Icons.Filled.NotificationsActive,
                    label = "PLAY REMINDERS",
                    enabled = remindersEnabled,
                    onToggle = toggleReminders@{
                        val turningOn = !remindersEnabled
                        if (turningOn && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val granted = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                            if (!granted) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                return@toggleReminders
                            }
                        }
                        remindersEnabled = turningOn
                        NotificationPrefs.setRemindersEnabled(context, turningOn)
                        ReminderScheduler.scheduleDailyReminders(context)
                    }
                )

                SettingsButton(
                    icon = Icons.Filled.Face,
                    label = if (hasCustomPhoto) "CHANGE CHARACTER PHOTO" else "SET CHARACTER PHOTO"
                ) {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }

                if (hasCustomPhoto) {
                    SettingsButton(icon = Icons.Filled.Face, label = "RESET TO DEFAULT PHOTO") {
                        CharacterPhoto.clearCustomPhoto(context)
                        hasCustomPhoto = false
                    }
                }

                SettingsButton(icon = Icons.Filled.Email, label = "SUPPORT") {
                    sendSupportEmail(context)
                }

                SettingsButton(icon = Icons.Filled.Apps, label = "MORE GAMES") {
                    showMoreGamesNotice = true
                }

                SettingsButton(icon = Icons.Filled.Star, label = "RATE THE APP!") {
                    openPlayStoreListing(context)
                }
            }

            Spacer(Modifier.height(28.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val contactInteraction = remember { MutableInteractionSource() }
                Text(
                    text = "CONTACT US",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .clickable(interactionSource = contactInteraction, indication = null) {
                            sendSupportEmail(context)
                        }
                        .padding(6.dp)
                )
                Spacer(Modifier.height(4.dp))
            }

            Spacer(Modifier.height(12.dp))
            BannerAd()
        }

        if (showMoreGamesNotice) {
            ComingSoonNotice(onDismiss = { showMoreGamesNotice = false })
        }
    }
}

private fun sendSupportEmail(context: android.content.Context) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
        putExtra(Intent.EXTRA_SUBJECT, "Cid Quest — Support")
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // No email app installed — nothing we can do but fail quietly
        // rather than crash the game over a missing mail client.
    }
}

private fun openPlayStoreListing(context: android.content.Context) {
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$PACKAGE_NAME"))
        )
    } catch (e: ActivityNotFoundException) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$PACKAGE_NAME"))
            )
        } catch (e2: ActivityNotFoundException) {
            // No browser and no Play Store — silently ignore.
        }
    }
}

@Composable
private fun SettingsButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AccentGold.copy(alpha = 0.16f))
            .bounceClick(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(vertical = 18.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = AccentGold, modifier = Modifier.size(18.dp))
        Text(
            text = "  $label",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun ToggleSettingButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AccentGold.copy(alpha = 0.16f))
            .bounceClick(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onToggle)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = AccentGold, modifier = Modifier.size(18.dp))
            Text(
                text = "  $label",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
        }
        Spacer(Modifier.height(6.dp))
        Row {
            Text(
                text = "ON",
                color = if (enabled) AccentAmber else TextSecondary.copy(alpha = 0.5f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(text = "  /  ", color = TextSecondary.copy(alpha = 0.5f), fontSize = 13.sp, fontWeight = FontWeight.Black)
            Text(
                text = "OFF",
                color = if (!enabled) AccentAmber else TextSecondary.copy(alpha = 0.5f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}

/** An honest "not built yet" state
 * instead of a button that silently does nothing when tapped. */
@Composable
private fun ComingSoonNotice(onDismiss: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(200)) + scaleIn(
                initialScale = 0.85f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 320f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(BackgroundBottom)
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("COMING SOON", color = AccentAmber, fontSize = 16.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "We're just getting started — more games from us are on the way!",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
