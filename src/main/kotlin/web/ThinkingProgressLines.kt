package web

/**
 * Status lines shown while an agent or Job Matching request is in flight.
 * Each line stays on screen for two seconds; the last line holds until the reply lands.
 */
object ThinkingProgressLines {
    val lines = listOf(
        "Thinking",
        "Reading the brief",
        "Checking published experience",
        "Opening the knowledge pack",
        "Looking for URLs to scrape",
        "Pulling page content",
        "Weighing the fit",
        "Drafting the first pass",
        "Tightening the structure",
        "Checking the numbers",
        "Cutting the fluff",
        "Laying out the table",
        "Making final touches",
        "Polishing the reply",
        "Almost there",
    )
}
