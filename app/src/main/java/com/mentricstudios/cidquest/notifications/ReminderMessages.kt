package com.mentricstudios.cidquest.notifications

/**
 * Copy for the two daily re-engagement notifications.
 *
 * Hinglish, street-banter tone on purpose, like a friend roasting you into
 * opening the app, not a corporate "come back!" push. Still doesn't invent
 * facts the app can't back up, no fake streaks/stars/shop, none of that
 * exists in this build, just said the way a friend would actually say it,
 * not a marketing team.
 *
 * Two separate pools (morning vs. evening) so the two daily notifications
 * don't feel like the same nudge twice, and a rotating cursor (see
 * [com.mentricstudios.cidquest.util.NotificationPrefs]) walks forward through
 * each pool so the same slot doesn't repeat itself day after day either.
 *
 * Emoji below are written as explicit \uXXXX surrogate-pair escapes rather
 * than literal UTF-8 characters in the source, on purpose (an earlier
 * version of this file with literal emoji characters failed to compile
 * with "No value passed for parameter" on one of them, for a reason that
 * couldn't be pinned down after checking encoding, line endings, and
 * quote-pairing all came back clean) — this sidesteps whatever the actual
 * cause was. If you add more lines by hand later and want to type emoji
 * directly instead of computing escapes, that's fine too — just double
 * check the result still has exactly two comma-separated string arguments
 * per ReminderMessage(...) call (that's what broke last time: a title and
 * body accidentally merged into one string with no comma between them).
 */
data class ReminderMessage(val title: String, val body: String)

object ReminderMessages {

    // Late-morning / midday slot: a friend nudging you during a phone
    // scroll break.
    val MORNING = listOf(
        ReminderMessage("Oye!", "Din bhar phone chalayega ya ek Choduu CID bhi crack karega? \uD83D\uDE2D\uD83D\uDE0F"),
        ReminderMessage("Sun bay", "Ek level toh banta hai, itna bhi busy nahi hai tu \uD83D\uDE02\uD83D\uDE02\uD83D\uDE02\uD83D\uDE02"),
        ReminderMessage("Choduu CID bula rahi hai", "ACP abhi bhi tera peecha kar raha hai, aaja jaldi \uD83C\uDFC3\uD83D\uDCA8"),
        ReminderMessage("Best time tootne wala hai", "Bas ek try aur, pichli baar se fast bhaag sakta hai \uD83D\uDD25"),
        ReminderMessage("Naye levels aa gaye", "Dekh toh le bhai, warna scene miss kar dega \uD83D\uDC40"),
        ReminderMessage("Adhoora chhoda tha na?", "Wahi se shuru kar, khatam kar ke aaram se baith"),
        ReminderMessage("2 minute nikaal ACP ka lya", "Utna time toh waise bhi reels mein ud jaata hai \uD83D\uDE05")
    )

    // Evening slot: winding-down-the-day guilt trip, friend-roast style.
    val EVENING = listOf(
        ReminderMessage("Ku bay", "Aaj game nahi khelni? Choduu CID tere talash mein hai \uD83D\uDE02\uD83D\uDE2D"),
        ReminderMessage("Raat ho gayi aur tu ACP ko bhool gya\uD83D\uDE2D\uD83D\uDE2D", "Sone se pehle ek maze toh banta hai boss \uD83C\uDF19"),
        ReminderMessage("1 level door hai", "Bas thoda sa aur bacha hai, phir aaram se so jaana"),
        ReminderMessage("ACP abhi bhi dhoond raha hai", "Kahan chhup gaya BSRKA, wapas toh aaja \uD83E\uDEE6\uD83D\uDC6E"),
        ReminderMessage("Record abhi tak khada hai", "Aaj todega ya kal pe taalega? \uD83D\uDC40"),
        ReminderMessage("Phone rakh, game khol", "2 minute ka kaam hai, itna toh banta hai ACP ka lya\uD83D\uDE2D"),
        ReminderMessage("Baby ko base pasand hai, ACP ko Desh pasand hai", "Ik mere ghar wale jisko clash pasand hai \uD83D\uDC67\uD83D\uDE34")
    )
}
