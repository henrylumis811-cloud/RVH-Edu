package com.henrylumis.rvhgrader.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import com.henrylumis.rvhgrader.model.SchoolClass
import com.henrylumis.rvhgrader.model.Term
import com.henrylumis.rvhgrader.model.TermSection
import java.util.Calendar

/** Owns the shared navigation and academic workspace selection state for the app shell. */
class AppNavigationState(
    initialYear: Int = Calendar.getInstance().get(Calendar.YEAR)
) {
    val currentScreen: MutableState<Screen> = mutableStateOf(Screen.HOME)
    /** Actual route history; the last item is the screen to return to. */
    private val backStack = mutableStateListOf<Screen>()

    /** Read-only diagnostic snapshot, also useful to verify navigation behaviour in tests. */
    fun backStackSnapshot(): List<Screen> = backStack.toList()

    val canNavigateBack: Boolean get() = backStack.isNotEmpty()

    /**
     * Record the real route taken. Re-selecting the current screen is a no-op, and the history
     * is bounded to avoid unbounded growth in long sessions. Repeated destinations are allowed:
     * they can represent legitimate paths such as Learners -> Profile -> Learners.
     */
    fun navigateTo(screen: Screen) {
        val current = currentScreen.value
        if (screen == current) return
        backStack.add(current)
        if (backStack.size > MAX_BACK_STACK_SIZE) backStack.removeAt(0)
        currentScreen.value = screen
    }

    /** Return to the screen actually visited immediately before the current one. */
    fun navigateBack(): Boolean {
        if (backStack.isEmpty()) return false
        currentScreen.value = backStack.removeAt(backStack.lastIndex)
        return true
    }

    /** Use only when a flow is intentionally completed and should not remain in Back history. */
    fun clearBackStack() { backStack.clear() }

    companion object {
        private const val MAX_BACK_STACK_SIZE = 100

        /** Persist route history and the active academic workspace across Activity recreation. */
        val Saver: Saver<AppNavigationState, List<String>> = Saver(
            save = { state ->
                listOf(
                    state.currentScreen.value.name,
                    state.backStack.joinToString(",") { it.name },
                    state.selectedClass.value.name,
                    state.selectedTerm.value.name,
                    state.selectedSection.value.name,
                    state.academicYear.value.toString()
                )
            },
            restore = { saved ->
                if (saved.size < 6) AppNavigationState() else {
                    val state = AppNavigationState(saved[5].toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR))
                    state.currentScreen.value = runCatching { Screen.valueOf(saved[0]) }.getOrDefault(Screen.HOME)
                    state.backStack.clear()
                    saved[1].split(",").filter(String::isNotBlank).takeLast(MAX_BACK_STACK_SIZE).forEach { name ->
                        runCatching { Screen.valueOf(name) }.getOrNull()?.let(state.backStack::add)
                    }
                    state.selectedClass.value = runCatching { SchoolClass.valueOf(saved[2]) }.getOrDefault(SchoolClass.P1)
                    state.selectedTerm.value = runCatching { Term.valueOf(saved[3]) }.getOrDefault(Term.TERM1)
                    state.selectedSection.value = runCatching { TermSection.valueOf(saved[4]) }.getOrDefault(TermSection.BEG)
                    state
                }
            }
        )
    }
    val selectedClass: MutableState<SchoolClass> = mutableStateOf(SchoolClass.P1)
    val selectedTerm: MutableState<Term> = mutableStateOf(Term.TERM1)
    val selectedSection: MutableState<TermSection> = mutableStateOf(TermSection.BEG)
    val academicYear: MutableState<Int> = mutableStateOf(initialYear)

    fun selectClass(value: SchoolClass) { selectedClass.value = value }
    fun selectTerm(value: Term) { selectedTerm.value = value }
    fun selectSection(value: TermSection) { selectedSection.value = value }
    fun selectAcademicYear(value: Int) { require(value in 1900..2200); academicYear.value = value }
}
