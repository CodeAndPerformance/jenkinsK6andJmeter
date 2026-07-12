pipeline {

    agent any

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

        string(
            name: 'SCRIPT_NAME',
            defaultValue: '',
            description: 'Script name only (Login.jmx or login.js)'
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
		stage('Compare With Previous Build') {

			steps {

				script {

					load "tools/compareMetrics.groovy"

				}

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
			when {
				expression { params.TOOL == 'JMeter' }
			}

			steps {
				script {
					load "tools/extractJMeterMetrics.groovy"
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

			}

}

		stage('Compare Metrics') {

			steps {

				script {

					load "tools/compareMetrics.groovy"

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
							 
			archiveArtifacts artifacts: 'metrics/currentMetrics.json'
        }
    }
}