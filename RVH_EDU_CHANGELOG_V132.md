# RVH Edu V132 — Drawer navigation fix

## Fixed
- Selecting **Class Results** now closes the navigation drawer immediately after loading the results screen.
- Selecting **Class Analytics** now closes the navigation drawer immediately after loading the analytics screen.
- If either destination is blocked by an assessment-policy error, the drawer still closes and the policy-review screen is shown, consistent with the other navigation destinations.

## Version
- Android versionCode: 98
- Android versionName: 1.1.2

## Validation
- Source-level navigation regression checks verify both handlers close the drawer in `finally`, including error paths, and both drawer items still call their corresponding loaders.
- Full Android Gradle build and device verification have not been run in this environment.
