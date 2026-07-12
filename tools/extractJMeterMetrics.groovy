def stats = readJSON file: "report/content/statistics.json"

def total = stats["Total"]

writeJSON(
        file: "metrics/currentMetrics.json",
        json: [
                tool        : "JMeter",
                script      : params.SCRIPT_NAME,
                sampleCount : total.sampleCount,
                errorCount  : total.errorCount,
                errorRate   : total.errorPct,
                average     : total.meanResTime,
                median      : total.medianResTime,
                p90         : total.pct1ResTime,
                p95         : total.pct2ResTime,
                p99         : total.pct3ResTime,
                throughput  : total.throughput
        ],
        pretty: 4
)

echo "Metrics extracted successfully."