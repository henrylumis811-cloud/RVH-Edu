# RVH Edu V153 — Read-only Bulk Mark Loading

Version: 1.3.3  
Build number: 119

## Changes
- Bulk mark loading now looks up an existing assessment instead of creating a draft assessment just to display a learner's marks.
- Learners with no assessment for the selected term and section display blank marks, as expected. A draft is created when the teacher actually saves a mark.
- Retains V152's off-main-thread loading and prior navigation, list, and results-rendering improvements.

## Verification
- Source assertions and ZIP integrity checked.
- Gradle compilation, automated tests, and device performance measurements were not run in this environment.
