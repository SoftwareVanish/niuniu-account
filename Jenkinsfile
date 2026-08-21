pipeline {
    agent any

    environment {
        // 镜像名自动取 application.yml 中的 spring.application.name
        DOCKER_IMAGE    = """${sh(
            script: "grep -A1 '^  application:' niuniu_account_api/src/main/resources/application.yml | grep 'name:' | awk '{print \$2}'",
            returnStdout: true
        ).trim()}"""
        CONTAINER_NAME = 'nn-app'
        NETWORK        = 'niuniu-account_nn-network'
        MYSQL_PASSWORD = 'NiuniuRoot@2026!'
    }

    stages {
        stage('Maven Build') {
            steps {
                script {
                    docker.image('maven:3.9-eclipse-temurin-21').inside('-v jenkins-maven-cache:/root/.m2') {
                        sh 'mvn -s settings.xml clean package -DskipTests'
                    }
                }
            }
        }

        stage('Docker Build & Deploy') {
            steps {
                sh "docker build -t ${DOCKER_IMAGE}:latest ."
                sh "docker stop ${CONTAINER_NAME} || true"
                sh "docker rm ${CONTAINER_NAME} || true"
                sh """
                    docker run -d \
                        --name ${CONTAINER_NAME} \
                        --network ${NETWORK} \
                        -p 9527:9527 \
                        -v /data/java/niuniu-account:/app/logs \
                        -e LOG_PATH=/app/logs \
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
    }
}
