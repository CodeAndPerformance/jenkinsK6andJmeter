pipeline {

    agent {
        label "${params.AGENT}"
    }

    environment {
        JMETER_HOME = 'C:\\apache-jmeter-5.6.3\\apache-jmeter-5.6.3'
        K6_HOME     = 'C:\\Program Files\\k6'
    }

    parameters {

        choice(
            name: 'TOOL',
            choices: ['JMeter', 'K6'],
            description: 'Select Performance Tool'
        )
		
		choice(
            name: 'SCRIPT_NAME',
            choices: ['Jenkins.jmx', 'load.test.js'],
            description: 'Select Performance script'
        )

        string(
            name: 'SCRIPT_NAME',
            defaultValue: '',
            description: 'Script name only (Login.jmx or login.js)'
        )
		
		choice(
            name: 'AGENT',
            choices: [
                'perf-agent-1',
                'perf-agent-2',
                'perf-agent-3'
            ],
            description: 'Select Jenkins Agent'
        )
    }

    stages {

        stage('Checkout') {
			
            steps {
                checkout scm
            }
        }

        stage('Run JMeter') {
			
            when {
                expression { params.TOOL == 'JMeter' }
            }

            steps {

                bat """
                if exist report rmdir /s /q report
                if exist results.jtl del /f /q results.jtl

                "%JMETER_HOME%\\bin\\jmeter.bat" ^
                -n ^
                -t jmeter\\scripts\\${params.SCRIPT_NAME} ^
                -l results.jtl ^
                -e ^
                -o report
                """
            }
        }

        stage('Run K6') {
			
            when {
                expression { params.TOOL == 'K6' }
            }

            steps {

                bat """
                if exist k6\\results rmdir /s /q k6\\results
                mkdir k6\\results

                "%K6_HOME%\\k6.exe" run ^
                --out json=k6\\results\\results.json ^
                k6\\src\\tests\\${params.SCRIPT_NAME}
                """
            }
        }

        stage('Publish JMeter Report') {
			
            when {
                expression { params.TOOL == 'JMeter' }
            }

            steps {

                publishHTML(target: [
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'report',
                    reportFiles: 'index.html',
                    reportName: 'JMeter HTML Report'
                ])
            }
        }
		
		stage('Create Metrics Folder') {
			
			steps {
				bat '''
				if not exist metrics mkdir metrics
				'''
    }
}
		
		stage('Extract Metrics') {
			
			steps {

				script {

					if(params.TOOL == "JMeter") {

						load "tools/extractJMeterMetrics.groovy"

					}

					if(params.TOOL == "K6") {

						load "tools/extractK6Metrics.groovy"

					}

        }

    }

}

		stage('Get Previous Metrics') {
			
			steps {

				copyArtifacts(
					projectName: env.JOB_NAME,
					selector: lastSuccessful(),
					filter: 'metrics/currentMetrics.json',
					target: 'previous',
					optional: true
				)

				// Also fetch previous build's per-transaction stats for comparison
				copyArtifacts(
					projectName: env.JOB_NAME,
					selector: lastSuccessful(),
					filter: 'report/statistics.json',
					target: 'previous',
					optional: true
				)

			}

}

		// Optional validation using sample files, does not fail the build
		stage('Validate Transaction CSV Logic (Dry Run)') {
			steps {
				script {
					try {
						def sample = 'tools/samples/statistics.json'
						def samplePrev = 'tools/samples/previous/statistics.json'
						if (fileExists(sample) && fileExists(samplePrev)) {
							def cfg = fileExists('tools/config.groovy') ? load('tools/config.groovy') : [:]
							def thr = (cfg?.transactionChangeThreshold ?: 20) as double
							def curr = readJSON file: sample
							def prev = readJSON file: samplePrev
							int improved = 0; int degraded = 0
							curr.each { k, v ->
								if (k != 'Total' && prev.containsKey(k)) {
									def base = (prev[k]?.meanResTime ?: 0) as double
									def nowv = (v?.meanResTime ?: 0) as double
									if (base > 0) {
										def ch = ((nowv - base) / base) * 100
										if (ch >= thr) { degraded++ } else if (ch <= -thr) { improved++ }
									}
								}
							}
							echo "Dry run: degraded=${degraded}, improved=${improved} (threshold=${thr}%)"
						} else {
							echo 'Dry run skipped: sample statistics not found.'
						}
					} catch (err) {
						echo "Dry run encountered an error but will not fail the build: ${err}"
					}
				}
			}
		}

		stage('Compare With Previous Build') {
			
			steps {

				script {

					load "tools/compareMetrics.groovy"

				}

			}

}

		stage('Generate Per-Transaction CSVs') {
			when {
				expression { params.TOOL == 'JMeter' }
			}
			steps {
				script {
					load "tools/generateTransactionCSVs.groovy"
				}
			}
		}
    }

    post {
    always {
        

            archiveArtifacts allowEmptyArchive: true,
                             artifacts: 'results.jtl'

            archiveArtifacts allowEmptyArchive: true,
                             artifacts: 'report/**'

            archiveArtifacts allowEmptyArchive: true,
                             artifacts: 'k6/results/**'

            archiveArtifacts allowEmptyArchive: true,
                             artifacts: 'metrics/currentMetrics.json'

            archiveArtifacts allowEmptyArchive: true,
                             artifacts: 'metrics/*.csv'
        
    }
}
}