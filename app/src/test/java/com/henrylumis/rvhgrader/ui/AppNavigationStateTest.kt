package com.henrylumis.rvhgrader.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNavigationStateTest {
    @Test
    fun backReturnsToActualPreviousRouteInOrder() {
        val navigation = AppNavigationState(initialYear = 2026)

        navigation.navigateTo(Screen.LEARNERS)
        navigation.navigateTo(Screen.LEARNER_PROFILE)
        navigation.navigateTo(Screen.HISTORY)

        assertEquals(Screen.HISTORY, navigation.currentScreen.value)
        assertTrue(navigation.navigateBack())
        assertEquals(Screen.LEARNER_PROFILE, navigation.currentScreen.value)
        assertTrue(navigation.navigateBack())
        assertEquals(Screen.LEARNERS, navigation.currentScreen.value)
        assertTrue(navigation.navigateBack())
        assertEquals(Screen.HOME, navigation.currentScreen.value)
        assertFalse(navigation.navigateBack())
    }

    @Test
    fun selectingCurrentRouteDoesNotCreateDuplicateHistory() {
        val navigation = AppNavigationState(initialYear = 2026)
        navigation.navigateTo(Screen.LEARNERS)
        val before = navigation.backStackSnapshot()

        navigation.navigateTo(Screen.LEARNERS)

        assertEquals(before, navigation.backStackSnapshot())
        assertEquals(Screen.LEARNERS, navigation.currentScreen.value)
    }

    @Test
    fun historyIsBoundedAndNewestRoutesAreKept() {
        val navigation = AppNavigationState(initialYear = 2026)
        repeat(150) { index ->
            navigation.navigateTo(if (index % 2 == 0) Screen.LEARNERS else Screen.DASHBOARD)
        }

        assertEquals(100, navigation.backStackSnapshot().size)
        assertTrue(navigation.canNavigateBack)
    }

    @Test
    fun clearBackStackMakesBackUnavailable() {
        val navigation = AppNavigationState(initialYear = 2026)
        navigation.navigateTo(Screen.SETTINGS)
        navigation.clearBackStack()

        assertFalse(navigation.canNavigateBack)
        assertFalse(navigation.navigateBack())
        assertEquals(Screen.SETTINGS, navigation.currentScreen.value)
    }
}
