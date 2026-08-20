pipeline {
    agent any

    environment {
        DOCKER_IMAGE    = 'nn-app'
        CONTAINER_NAME = 'nn-app'
        NETWORK        = 'niuniu-account_nn-network'
        MYSQL_PASSWORD = 'NiuniuRoot@2026!'
    }

    stages {
        stage('Maven Build') {
            steps {
                script {
                    docker.image('maven:3.9-eclipse-temurin-17').inside('-v jenkins-maven-cache:/root/.m2') {
                        sh 'mvn -s settings.xml clean package -DskipTests -Pprod'
                    }
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${DOCKER_IMAGE}:latest ."
            }
        }

        stage('Deploy') {
            steps {
                sh "docker stop ${CONTAINER_NAME} || true"
                sh "docker rm ${CONTAINER_NAME} || true"
                sh """
                    docker run -d \
                        --name ${CONTAINER_NAME} \
                        --network ${NETWORK} \
                        -p 8081:8081 \
                        -e SPRING_PROFILES_ACTIVE=prod \
                        -e "SPRING_DATASOURCE_URL=jdbc:mysql://nn-mysql:3306/niuniu_account?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4" \
                        -e SPRING_DATASOURCE_USERNAME=root \
                        -e SPRING_DATASOURCE_PASSWORD=${MYSQL_PASSWORD} \
                        -e TZ=Asia/Shanghai \
                        --restart always \
                        ${DOCKER_IMAGE}:latest
                """
            }
        }

        stage('Health Check') {
            steps {
                script {
                    def maxRetries = 10
                    def retryCount = 0
                    def healthy = false

                    while (retryCount < maxRetries && !healthy) {
                        try {
                            sh "curl -f http://localhost:8081/actuator/health"
                            healthy = true
                            echo "应用启动成功"
                        } catch (Exception e) {
                            retryCount++
                            echo "等待应用启动... (${retryCount}/${maxRetries})"
                            sleep(time: 5, unit: 'SECONDS')
                        }
                    }

                    if (!healthy) {
                        error "应用启动失败，健康检查未通过"
                    }
                }
            }
        }
    }

    post {
        success {
            echo "部署成功！"
        }
        failure {
            echo "部署失败，请检查日志"
        }
    }
}
