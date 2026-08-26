package com.mentricstudios.cidquest.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mentricstudios.cidquest.ui.theme.AccentOrange
import com.mentricstudios.cidquest.ui.theme.AccentTeal
import com.mentricstudios.cidquest.ui.theme.BackgroundBottom
import com.mentricstudios.cidquest.ui.theme.BackgroundTop
import com.mentricstudios.cidquest.ui.theme.CardLocked
import com.mentricstudios.cidquest.ui.theme.TextPrimary
import com.mentricstudios.cidquest.ui.theme.TextSecondary
import com.mentricstudios.cidquest.util.bounceClick
import java.util.Calendar

private val MONTHS = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)

private const val MIN_AGE = 13

private fun isLeapYear(year: Int) = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

private fun daysInMonth(monthIndex: Int, year: Int): Int = when (monthIndex) {
    1 -> if (isLeapYear(year)) 29 else 28
    3, 5, 8, 10 -> 30
    else -> 31
}

/** True if the birthdate is on/before (today - MIN_AGE years) — i.e. old enough. */
private fun isOldEnough(monthIndex: Int, day: Int, year: Int): Boolean {
    val today = Calendar.getInstance()
    val birth = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, monthIndex)
        set(Calendar.DAY_OF_MONTH, day)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val cutoff = Calendar.getInstance().apply {
        add(Calendar.YEAR, -MIN_AGE)
    }
    return !birth.after(cutoff) && !birth.after(today)
}

@Composable
fun AgeGateScreen(onConfirmed: () -> Unit) {
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }

    var monthIndex by remember { mutableIntStateOf(0) }
    var day by remember { mutableIntStateOf(1) }
    var year by remember { mutableIntStateOf(currentYear - 18) }
    var showTooYoungError by remember { mutableStateOf(false) }
    var showLegalDoc by remember { mutableStateOf(false) }

    var monthMenu by remember { mutableStateOf(false) }
    var dayMenu by remember { mutableStateOf(false) }
    var yearMenu by remember { mutableStateOf(false) }

    var cardVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { cardVisible = true }

    // Whenever month or year changes, clamp the selected day down if it's no
    // longer valid (e.g. day 30 was selected, then the month changed to Feb).
    LaunchedEffect(monthIndex, year) {
        val maxDay = daysInMonth(monthIndex, year)
        if (day > maxDay) day = maxDay
    }

    val confirmInteraction = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom))),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = cardVisible,
            enter = fadeIn(tween(260)) + scaleIn(
                initialScale = 0.9f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = 280f)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(BackgroundBottom)
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(AccentTeal.copy(alpha = 0.5f), AccentOrange.copy(alpha = 0.35f))
                        ),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(AccentTeal.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = AccentTeal,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Text(
                    text = "Confirm your age to continue",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 14.dp)
                )
                Text(
                    text = "Enter your birthdate",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PickerField(
                        label = MONTHS[monthIndex],
                        expanded = monthMenu,
                        onOpen = { monthMenu = true },
                        onDismiss = { monthMenu = false }
                    ) {
                        MONTHS.forEachIndexed { index, m ->
                            DropdownMenuItem(text = { Text(m) }, onClick = {
                                monthIndex = index
                                monthMenu = false
                            })
                        }
                    }

                    PickerField(
                        label = day.toString(),
                        expanded = dayMenu,
                        onOpen = { dayMenu = true },
                        onDismiss = { dayMenu = false }
                    ) {
                        (1..daysInMonth(monthIndex, year)).forEach { d ->
                            DropdownMenuItem(text = { Text(d.toString()) }, onClick = {
                                day = d
                                dayMenu = false
                            })
                        }
                    }

                    PickerField(
                        label = year.toString(),
                        expanded = yearMenu,
                        onOpen = { yearMenu = true },
                        onDismiss = { yearMenu = false }
                    ) {
                        // A plain DropdownMenu composes every item inside it
                        // eagerly — building all 101 years up front on every
                        // open was the actual cause of the noticeable lag
                        // here. A LazyColumn inside the menu only composes
                        // what's on screen, same as any other long list.
                        LazyColumn(modifier = Modifier.heightIn(max = 260.dp)) {
                            items(currentYear - (currentYear - 100) + 1) { offset ->
                                val y = currentYear - offset
                                DropdownMenuItem(text = { Text(y.toString()) }, onClick = {
                                    year = y
                                    yearMenu = false
                                })
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 26.dp)
                        .height(52.dp)
                        .bounceClick(confirmInteraction)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(listOf(AccentTeal, AccentTeal.copy(alpha = 0.7f)))
                        )
                        .clickable(interactionSource = confirmInteraction, indication = null) {
                            if (isOldEnough(monthIndex, day, year)) {
                                showTooYoungError = false
                                onConfirmed()
                            } else {
                                showTooYoungError = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = BackgroundTop,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "  CONFIRM",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = BackgroundTop
                        )
                    }
                }

                AnimatedVisibility(visible = showTooYoungError) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ErrorOutline,
                            contentDescription = null,
                            tint = AccentOrange,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "  You must be at least $MIN_AGE years old to use this app.",
                            color = AccentOrange,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = "Terms of Service",
                        color = AccentTeal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { showLegalDoc = true }
                    )
                    Text(text = "  |  ", color = TextSecondary, fontSize = 12.sp)
                    Text(
                        text = "Privacy Policy",
                        color = AccentTeal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { showLegalDoc = true }
                    )
                }
            }
        }

        if (showLegalDoc) {
            LegalDocNotice(onDismiss = { showLegalDoc = false })
        }
    }
}

@Composable
private fun PickerField(
    label: String,
    expanded: Boolean,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    menuContent: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box {
        Row(
            modifier = Modifier
                .bounceClick(interactionSource, pressedScale = 0.95f, playSound = false)
                .clip(RoundedCornerShape(12.dp))
                .background(CardLocked)
                .border(
                    width = 1.dp,
                    color = if (expanded) AccentTeal.copy(alpha = 0.6f) else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(interactionSource = interactionSource, indication = null) { onOpen() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
            menuContent()
        }
    }
}
