import groovy.json.JsonOutput

def config = load "tools/config.groovy"

def current = readJSON file: "metrics/currentMetrics.json"

if(!fileExists("previous/metrics/currentMetrics.json")){

    echo "No previous successful build found."

    return

}

def previous = readJSON file: "previous/metrics/currentMetrics.json"

echo ""
echo "=============================="
echo "Performance Comparison"
echo "=============================="

compareMetric(
        "Average Response Time",
        previous.average,
        current.average,
        config.averageIncreaseThreshold
)

compareMetric(
        "P90",
        previous.p90,
        current.p90,
        config.p90IncreaseThreshold
)

compareMetric(
        "P95",
        previous.p95,
        current.p95,
        config.p95IncreaseThreshold
)

compareError(
        previous.errorRate,
        current.errorRate,
        config.errorRateThreshold
)

compareThroughput(
        previous.throughput,
        current.throughput,
        config.throughputDecrease
)


def compareMetric(name,
                  previous,
                  current,
                  threshold){

    double change=((current-previous)/previous)*100

    echo ""
    echo "${name}"

    echo "Previous : ${previous}"

    echo "Current  : ${current}"

    echo "Change   : ${String.format('%.2f',change)} %"

    if(change>threshold){

        error("${name} increased by ${String.format('%.2f',change)} %")

    }

}


def compareError(previous,
                 current,
                 threshold){

    echo ""

    echo "Error Rate"

    echo "Previous : ${previous}"

    echo "Current  : ${current}"

    if(current>threshold){

        error("Error Rate exceeded threshold")

    }

}


def compareThroughput(previous,
                      current,
                      threshold){

    double decrease=((previous-current)/previous)*100

    echo ""

    echo "Throughput"

    echo "Previous : ${previous}"

    echo "Current  : ${current}"

    echo "Decrease : ${String.format('%.2f',decrease)} %"

    if(decrease>threshold){

        error("Throughput dropped significantly")

    }

}

