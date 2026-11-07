# RVH Edu V57 — Duplicate review in learner creation and editing

- Connected the existing duplicate matcher to the learner add and edit workflows.
- Before saving, the teacher is shown likely matches, match reasons, and a distinction between strong identity warnings and name-based review suggestions.
- The teacher can go back to revise the details or explicitly confirm that the proposed learner is different.
- Editing excludes the current learner ID from candidate matching.
- The review remains non-destructive: it never merges, archives, or changes a candidate record.
- This is a source-level change; a full Android build and physical-device test have not been verified.
