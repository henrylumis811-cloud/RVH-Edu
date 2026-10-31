from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
source = (ROOT / "app/src/main/java/com/henrylumis/rvhgrader/core/repository/LearnerRepository.kt").read_text()
start = source.index('fun findByAdmissionNumber(')
end = source.index('    /** Find possible matches', start)
method = source[start:end]
assert 'normalizeAdmission(admissionNumber)' in method
assert 'school_admission_number' in method
assert 'normalized_admission_number = ?' in method
assert 'getById(c.getString(0), c.getString(1))' in method
assert 'admission_number = ? COLLATE NOCASE' not in method
print("Canonical admission-number lookup checks passed")
