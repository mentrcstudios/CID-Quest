package com.mentricstudios.cidquest.notifications

/**
 * Copy for the two daily re-engagement notifications.
 *
 * Each line leans on a well-known, legitimate mobile-game retention technique
 * (curiosity gap, the Zeigarnik "unfinished task" pull, variable reward) —
 * the same tools Duolingo, Candy Crush, etc. use. On purpose, none of these
 * invent facts the app can't back up (no fake "your friend just passed you"
 * style claims, since there's no real friends or leaderboard data behind
 * it, and none reference streaks, stars, or a shop — none of those exist in
 * this build) — the goal is genuinely engaging copy, not deceptive copy.
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
        ReminderMessage("Ready for a quick win?", "A maze is waiting whenever you've got two minutes."),
        ReminderMessage("Can you clear it faster?", "Your best time on an early level is begging to be broken."),
        ReminderMessage("Beat yesterday?", "Your best time is sitting right there, daring you to top it."),
        ReminderMessage("New levels just opened up", "A fresh set of mazes is waiting whenever you're ready."),
        ReminderMessage("You left this unfinished", "Pick up right where you left off — it's a quick finish from here."),
        ReminderMessage("Quick maze break?", "Two minutes is all it takes to clear one more level.")
    )

    // Evening slot — framed around wrapping the day, since a natural "day's
    // winding down" moment is a good nudge point without needing a fake
    // deadline to manufacture urgency.
    val EVENING = listOf(
        ReminderMessage("Wind down with a maze", "A quick level before bed beats scrolling one more time."),
        ReminderMessage("One more before bed?", "A quick maze is a nice way to close out the day."),
        ReminderMessage("1 level away", "You're one level away from unlocking something new."),
        ReminderMessage("Haven't tried the later levels?", "The maze gets trickier the further you go — see how far you get."),
        ReminderMessage("End the day with a clean run", "One more maze, one more shot at your best moves yet."),
        ReminderMessage("Your record's still beatable", "One more try tonight and it might not survive."),
        ReminderMessage("Two minutes tonight?", "That's about all it takes to clear one more maze.")
    )
}
