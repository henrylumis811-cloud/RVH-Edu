package com.henrylumis.rvhgrader.core.model

/** A migration-time warning where legacy data cannot prove two same-named records are one learner. */
data class LegacyIdentityReview(
    val id: String,
    val normalizedName: String,
    val learnerId: String,
    val relatedLearnerId: String,
    val reason: String,
    val resolved: Boolean = false
)
