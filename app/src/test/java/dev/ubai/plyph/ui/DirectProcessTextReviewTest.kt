package dev.ubai.plyph.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DirectProcessTextReviewTest {
    @Test
    fun enabledSettingReviewsEditableSelection() {
        assertTrue(
            shouldReviewDirectReplacement(
                reviewBeforeReplacement = true,
                readOnly = false,
            ),
        )
    }

    @Test
    fun disabledSettingKeepsAutomaticReplacement() {
        assertFalse(
            shouldReviewDirectReplacement(
                reviewBeforeReplacement = false,
                readOnly = false,
            ),
        )
    }

    @Test
    fun readOnlySelectionKeepsCopyWorkflow() {
        assertFalse(
            shouldReviewDirectReplacement(
                reviewBeforeReplacement = true,
                readOnly = true,
            ),
        )
    }
}
