package com.henrylumis.rvhgrader.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.henrylumis.rvhgrader.model.SchoolClass
import com.henrylumis.rvhgrader.model.Term
import com.henrylumis.rvhgrader.model.TermSection
import java.util.Calendar

/** Owns the shared navigation and academic workspace selection state for the app shell. */
class AppNavigationState(
    initialYear: Int = Calendar.getInstance().get(Calendar.YEAR)
) {
    val currentScreen: MutableState<Screen> = mutableStateOf(Screen.HOME)
    val selectedClass: MutableState<SchoolClass> = mutableStateOf(SchoolClass.P1)
    val selectedTerm: MutableState<Term> = mutableStateOf(Term.TERM1)
    val selectedSection: MutableState<TermSection> = mutableStateOf(TermSection.BEG)
    val academicYear: MutableState<Int> = mutableStateOf(initialYear)

    fun navigateTo(screen: Screen) { currentScreen.value = screen }
    fun selectClass(value: SchoolClass) { selectedClass.value = value }
    fun selectTerm(value: Term) { selectedTerm.value = value }
    fun selectSection(value: TermSection) { selectedSection.value = value }
    fun selectAcademicYear(value: Int) { require(value in 1900..2200); academicYear.value = value }
}
