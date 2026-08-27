package com.mentricstudios.cidquest.notifications

/**
 * Copy for the two daily re-engagement notifications.
 *
 * Each line leans on a well-known, legitimate mobile-game retention technique
 * (curiosity gap, loss aversion on a streak, the Zeigarnik "unfinished task"
 * pull, variable reward) — the same tools Duolingo, Candy Crush, etc. use.
 * On purpose, none of these invent facts the app can't back up (no fake
 * "your friend just passed you" style claims, since there's no real friends
 * or leaderboard data behind it) — the goal is genuinely engaging copy, not
 * deceptive copy.
 *
 * Two separate pools (morning vs. evening) so the two daily notifications
 * don't feel like the same nudge twice, and a rotating cursor (see
 * [com.mentricstudios.cidquest.util.NotificationPrefs]) walks forward through
 * each pool so the *same* slot doesn't repeat itself day after day either.
 */
data class ReminderMessage(val title: String, val body: String)

object ReminderMessages {

    // Late-morning / midday slot — framed around discovery and fresh
    // content, meant to catch someone during a natural phone-check break.
    val MORNING = listOf(
        ReminderMessage("Something's different today", "We hid a twist in today's maze. See if you can spot it. 🌀"),
        ReminderMessage("Your streak is still standing", "Don't let today be the day it falls. One quick maze keeps it going."),
        ReminderMessage("So close to unlocking it", "A new skin is just a few stars away — today could be the day."),
        ReminderMessage("Beat yesterday?", "Your best time is sitting right there, daring you to top it."),
        ReminderMessage("New levels just opened up", "A fresh set of mazes is waiting whenever you're ready."),
        ReminderMessage("You left this unfinished", "Pick up right where you left off — it's a quick finish from here."),
        ReminderMessage("Quick maze break?", "Two minutes is all it takes to clear one more level.")
    )

    // Evening slot — framed around wrapping the day / not losing progress,
    // since loss-aversion framing is most effective close to a natural
    // deadline (midnight streak reset).
    val EVENING = listOf(
        ReminderMessage("Midnight is coming", "Your streak resets soon — one maze keeps it alive tonight."),
        ReminderMessage("One more before bed?", "A quick maze is a nice way to close out the day."),
        ReminderMessage("1 level away", "You're one level away from unlocking something new."),
        ReminderMessage("Tonight's shop update", "Come see what's new before you turn in."),
        ReminderMessage("End the day with a win", "One more maze, one more star on the board."),
        ReminderMessage("Your record's still beatable", "One more try tonight and it might not survive."),
        ReminderMessage("Don't break the chain", "A few minutes now keeps your streak going into tomorrow.")
    )
}
