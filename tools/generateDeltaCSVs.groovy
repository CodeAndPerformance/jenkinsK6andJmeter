def config = load "tools/config.groovy"

def threshold = (config.transactionChangeThreshold ?: 20) as double

if (!fileExists("previous/report/statistics.json")) {
    echo "No previous report/statistics.json found; skipping per-transaction CSV generation."
    return
}

def current = readJSON file: "report/statistics.json"
def previous = readJSON file: "previous/report/statistics.json"

def improved = []
def degraded = []

previous.keySet().each { name ->
    if (name == 'Total' || name == 'overall') { return }
    def prevRec = previous[name]
    def curRec = current[name]
    if (!prevRec || !curRec) { return }
    def b = prevRec.meanResTime as Double
    if (!b || b == 0) { return }
    def c = curRec.meanResTime as Double
    def chg = ((c - b) / b) * 100.0
    def row = [
        transaction: name,
        baseline: b,
        current: c,
        change: chg
    ]
    if (chg >= threshold) { degraded << row }
    if (chg <= -threshold) { improved << row }
}

def sortByAbs = { a, b -> Math.abs(b.change) <=> Math.abs(a.change) }
improved.sort(sortByAbs)
degraded.sort(sortByAbs)

String toCsv(List rows) {
    def sb = new StringBuilder()
    sb.append('transaction,baseline_ms,current_ms,change_pct\r\n')
    rows.each { r ->
        def txn = '"' + (r.transaction as String).replace('"','""') + '"'
        def b = String.format('%.2f', r.baseline)
        def c = String.format('%.2f', r.current)
        def p = String.format('%.2f', r.change)
        sb.append(String.join(',', [txn, b, c, p])).append("\r\n")
    }
    return sb.toString()
}

writeFile file: 'metrics/improved.csv', text: toCsv(improved)
writeFile file: 'metrics/degraded.csv', text: toCsv(degraded)

echo "Per-transaction changes: improved=${improved.size()} degraded=${degraded.size()} (threshold ${threshold}%)"