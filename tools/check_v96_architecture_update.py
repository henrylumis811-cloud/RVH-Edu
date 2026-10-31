from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def require(condition, message):
    if not condition:
        raise AssertionError(message)

build = (ROOT / 'app/build.gradle.kts').read_text()
workflow = (ROOT / '.github/workflows/android-build.yml').read_text()
update_guide = (ROOT / 'ANDROID_UPDATE_INSTALL_GUIDE.md').read_text()
architecture = (ROOT / 'ARCHITECTURE_DECISIONS_MULTI_SCHOOL.md').read_text()

def main():
    require('versionCode = 96' in build and 'versionName = "1.1.0"' in build, 'version must advance')
    require('applicationId = "com.henrylumis.rvhgrader"' in build, 'application ID must stay stable')
    for env_name in ('RVH_CI_KEYSTORE_PATH', 'RVH_CI_STORE_PASSWORD', 'RVH_CI_KEY_ALIAS', 'RVH_CI_KEY_PASSWORD'):
        require(env_name in build, f'missing Gradle signing input {env_name}')
    for secret in ('RVH_DEBUG_KEYSTORE_BASE64', 'RVH_DEBUG_STORE_PASSWORD', 'RVH_DEBUG_KEY_ALIAS', 'RVH_DEBUG_KEY_PASSWORD'):
        require(secret in workflow, f'missing CI signing secret {secret}')
    require('pull_request' in workflow and 'actions/upload-artifact@v4' in workflow, 'CI build or artifact upload missing')
    require('same signing certificate' in update_guide and 'Uninstalling can erase local app data' in update_guide, 'update safety guidance missing')
    for term in ('TeacherSchoolMembership', 'school_id', 'transaction', 'learner transfer', 'multi-device synchronization'):
        require(term.lower() in architecture.lower(), f'multi-school architecture requirement missing: {term}')
    print('V96 architecture/update checks: 12/12 passed')

if __name__ == '__main__':
    main()
