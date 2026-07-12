import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def stats = new JsonSlurper().parseText(readFile('report/content/statistics.json'))

//def stats = readJSON file: "report/content/statistics.json"

def total = stats["Total"]

def metrics = [

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

]



writeFile(
        file: 'metrics/currentMetrics.json',
        text: JsonOutput.prettyPrint(JsonOutput.toJson(metrics))
)

echo "========== Current Metrics =========="
echo JsonOutput.prettyPrint(JsonOutput.toJson(metrics))