return [

        averageIncreaseThreshold : 10,      // %
        p90IncreaseThreshold     : 15,      // %
        p95IncreaseThreshold     : 20,
        errorRateThreshold       : 1,       // %
        throughputDecrease       : 10,      // %

        // Per-transaction CSV threshold (% change on meanResTime)
        transactionChangeThreshold: 20,

        failOnRegression         : true,
        markBuildUnstable        : false

]