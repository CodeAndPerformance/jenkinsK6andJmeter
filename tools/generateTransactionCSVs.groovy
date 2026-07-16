def cfg = fileExists('tools/config.groovy') ? load('tools/config.groovy') : [:]

def threshold = (cfg?.transactionChangeThreshold ?: 20) as double

if (!fileExists('report/statistics.json')) {
    echo 'Per-transaction CSVs: current report/statistics.json not found; skipping.'
    return
}

if (!fileExists('previous/report/statistics.json')) {
    echo 'Per-transaction CSVs: no previous report/statistics.json; skipping.'
    return
}

def curr = readJSON file: 'report/statistics.json'
def prev = readJSON file: 'previous/report/statistics.json'

def improvedLines = ['transaction,baseline_ms,current_ms,change_pct']
def degradedLines = ['transaction,baseline_ms,current_ms,change_pct']

int improved = 0; int degraded = 0; int considered = 0

curr.each { k, v ->
    if (k == 'Total' || k == 'Transactions') { return }
    if (!prev.containsKey(k)) { return }
    def base = (prev[k]?.meanResTime ?: 0) as double
    def nowv = (v?.meanResTime ?: 0) as double
    if (base <= 0) { return }
    considered++
    def change = ((nowv - base) / base) * 100
    def row = [k, String.format('%.2f', base), String.format('%.2f', nowv), String.format('%.2f', change)].join(',')
    if (change >= threshold) { degradedLines << row; degraded++ }
    else if (change <= -threshold) { improvedLines << row; improved++ }
}

// Ensure metrics folder exists
if (!fileExists('metrics')) { bat 'mkdir metrics' }

writeFile file: 'metrics/degraded.csv', text: degradedLines.join("\r\n") + "\r\n"
writeFile file: 'metrics/improved.csv', text: improvedLines.join("\r\n") + "\r\n"

echo "Per-transaction summary: considered=${considered}, degraded=${degraded}, improved=${improved} (threshold=${threshold}%)"