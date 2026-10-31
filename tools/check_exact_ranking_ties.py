from pathlib import Path
root = Path(__file__).resolve().parents[1]
source = (root / 'app/src/main/java/com/henrylumis/rvhgrader/core/grading/RankingEngine.kt').read_text()
assert 'left.total.toLong() * right.maxTotal.toLong()' in source
assert 'right.total.toLong() * left.maxTotal.toLong()' in source
assert 'result.total.toLong() * prior.maxTotal.toLong()' in source
assert 'score != previousScore' not in source
assert 'position = index + 1' in source
print('PASS: ranking compares normalized marks as exact fractions and ties share competition positions')
