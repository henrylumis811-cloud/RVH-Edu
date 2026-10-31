# RVH Edu V78 — PIN gate and entry-screen polish

## Changes
- Reworked the first-launch PIN setup and returning-user unlock screen around the shared RVH Edu visual system.
- Replaced generic “mainframe” branding with explicit RVH Edu identity and offline school-management context.
- Improved vertical scrolling and IME padding so the setup/unlock form can remain reachable on smaller screens when the keyboard is open.
- Standardised numeric PIN fields, helper text, error presentation, card geometry, and primary action touch target.
- Preserved first-launch PIN creation, 4–8 digit validation, confirmation, hashed credential storage, legacy PIN migration, offline operation, and the Hellen dedication.

## Validation
- PIN UI source regression checks: 10/10 passed.
- All repository Python regression scripts passed.
- Python tool compilation passed.
- ZIP integrity verified; project files are at archive root.
- Full Android Gradle compilation and physical-device visual validation remain outstanding.
