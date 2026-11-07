# RVH Edu V141 — Android system Back navigation

- Added a Compose BackHandler so Android's system Back button navigates within the app instead of immediately closing the activity from secondary screens.
- Back closes the navigation drawer first when it is open.
- Learner mark sheets, learner profiles, and archived learners return to the learner list.
- School subjects, grading rules, and feedback/update screens return to Settings.
- Bulk Marks returns to Home; other secondary screens return to Home.
- Preserved normal Android Back-to-exit behaviour on the Home screen.
- Version: 1.2.1 (versionCode 107).

Validation note: source change only; run CI/build and verify on-device before release.
