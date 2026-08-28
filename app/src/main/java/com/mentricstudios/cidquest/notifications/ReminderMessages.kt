package com.mentricstudios.cidquest.notifications

/**
 * Copy for the two daily re-engagement notifications.
 *
 * Hinglish, street-banter tone on purpose — like a friend roasting you into
 * opening the app, not a corporate "come back!" push. Still leans on the
 * same underlying ideas as before (curiosity, the Zeigarnik "unfinished
 * task" pull, "your record's still standing") and still doesn't invent
 * facts the app can't back up — no fake streaks/stars/shop, none of that
 * exists in this build — just said the way a friend would actually say it,
 * not a marketing team.
 *
 * Two separate pools (morning vs. evening) so the two daily notifications
 * don't feel like the same nudge twice, and a rotating cursor (see
 * [com.mentricstudios.cidquest.util.NotificationPrefs]) walks forward through
 * each pool so the *same* slot doesn't repeat itself day after day either.
 */
data class ReminderMessage(val title: String, val body: String)

object ReminderMessages {

    // Late-morning / midday slot — a friend nudging you during a phone
    // scroll break.
    val MORNING = listOf(
        ReminderMessage("Oye!", "Din bhar phone chalayega ya ek Choduu CID bhi crack karega? 😭😏"),
        ReminderMessage("Sun bay ", "Ek level toh banta hai, itna bhi busy nahi hai tu 😂😂😂😂"),
        ReminderMessage("Choduu CID bula rahi hai", "ACP abhi bhi tera peecha kar raha hai, aaja jaldi 🏃💨"),
        ReminderMessage("Best time tootne wala hai", "Bas ek try aur — pichli baar se fast bhaag sakta hai 🔥"),
        ReminderMessage("Naye levels aa gaye", "Dekh toh le bhai, warna scene miss kar dega 👀"),
        ReminderMessage("Adhoora chhoda tha na?", "Wahi se shuru kar, khatam kar ke aaram se baith"),
        ReminderMessage("2 minute nikaal ACP ka lya", "Utna time toh waise bhi reels mein ud jaata hai 😅")
    )

    // Evening slot — winding-down-the-day guilt trip, friend-roast style.
    val EVENING = listOf(
        ReminderMessage("Ku bay", "Aaj game nahi khelni? Choduu CID tere talash mein hai 😂😭"),
        ReminderMessage("Raat ho gayi aur tu ACP ko bhool gya😭😭", "Sone se pehle ek maze toh banta hai boss 🌙"),
        ReminderMessage("1 level door hai", "Bas thoda sa aur bacha hai, phir aaram se so jaana"),
        ReminderMessage("ACP abhi bhi dhoond raha hai", "Kahan chhup gaya BSRKA, wapas toh aaja 🫦👮"),
        ReminderMessage("Record abhi tak khada hai", "Aaj todega ya kal pe taalega? 👀"),
        ReminderMessage("Phone rakh, game khol", "2 minute ka kaam hai, itna toh banta hai ACP ka lya😭"),
        ReminderMessage("Baby ko base pasand hai ACP ko Desh pasand hai ik mere ghar wale jisko clash pasand ha 👧😴")
    )
}
