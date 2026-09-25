package com.example.util

object StringsHelper {
    enum class Language {
        BN, EN
    }

    fun get(lang: Language, en: String, bn: String): String {
        return if (lang == Language.BN) bn else en
    }

    // Common football translations
    fun goal(lang: Language) = get(lang, "Goal", "গোল")
    fun corners(lang: Language) = get(lang, "Corners", "কর্নার")
    fun yellowCard(lang: Language) = get(lang, "Yellow Card", "হলুদ কার্ড")
    fun redCard(lang: Language) = get(lang, "Red Card", "লাল কার্ড")
    fun btts(lang: Language) = get(lang, "BTTS %", "বিটিটিএস %")
    fun lineup(lang: Language) = get(lang, "Lineup", "লাইনআপ")
    fun doubleChance(lang: Language) = get(lang, "Double Chance", "ডাবল চান্স")
    fun live(lang: Language) = get(lang, "LIVE", "লাইভ")
    fun upcoming(lang: Language) = get(lang, "Upcoming", "আসন্ন")
    fun finished(lang: Language) = get(lang, "Finished", "শেষ হয়েছে")
    fun autoUpdate(lang: Language) = get(lang, "Auto Update", "প্রতিদিন অটো আপডেট")
    fun playerStats(lang: Language) = get(lang, "Player Stats", "খেলোয়াড় পরিসংখ্যান")
    fun withdrawBkash(lang: Language) = get(lang, "bKash Withdraw", "বিকাশ উইথড্র")
    fun balance(lang: Language) = get(lang, "Coin Balance", "কয়েন ব্যালেন্স")
    fun history(lang: Language) = get(lang, "History", "হিস্ট্রি")
    fun settings(lang: Language) = get(lang, "Settings", "সেটিংস")
    fun matches(lang: Language) = get(lang, "Matches", "ম্যাচসমূহ")
    fun rewards(lang: Language) = get(lang, "Rewards & bKash", "পুরস্কার ও বিকাশ")
    fun dailyCheckIn(lang: Language) = get(lang, "Daily Check-in", "দৈনিক বোনাস")
    fun watchAdEarn(lang: Language) = get(lang, "Watch Ad (+50 Coins)", "বিজ্ঞাপন দেখুন (+৫০ কয়েন)")
    fun predictWin(lang: Language) = get(lang, "Match Predictions", "ম্যাচ প্রেডিকশন")
    fun possession(lang: Language) = get(lang, "Possession", "বল পজেশন")
    fun shots(lang: Language) = get(lang, "Total Shots", "মোট শট")
    fun shotsOnTarget(lang: Language) = get(lang, "Shots on Target", "অন-টার্গেট শট")
    fun fouls(lang: Language) = get(lang, "Fouls", "ফাউল")
    fun offsides(lang: Language) = get(lang, "Offsides", "অফসাইড")
    fun saves(lang: Language) = get(lang, "Saves", "সেভ")
}
