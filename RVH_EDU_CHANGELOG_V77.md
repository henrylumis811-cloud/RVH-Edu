# RVH Edu V77 — Learner profile and academic history UI

## Changes
- Redesigned learner profile identity header, academic overview metrics, subject insights, and enrolment cards.
- Incomplete assessments no longer display a partial average or division as if it were a final result; missing subjects remain visible.
- Redesigned academic-history directory with search by learner name, class, stream, or academic year.
- Added compact directory summaries, readable learner cards, enrolment summaries, and explicit empty/no-match states.
- Kept navigation callbacks, data models, and grading calculations unchanged.

## Validation
- Learner/history UI source regression checks: 12/12 passed.
- Existing learner/settings checks: 11/11 passed.
- Existing mark-entry checks: 12/12 passed.
- Existing results/analytics checks: 14/14 passed.
- Python tools compile successfully.
- Full Android Gradle compilation and physical-device visual validation remain outstanding; this archive does not contain a Gradle wrapper.
