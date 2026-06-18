# CI/CD Setup with GitHub Actions

This guide walks you through creating the automated CI pipeline for this project.

## 1. Create the workflow directory

From the project root, create the required folder structure:

```bash
mkdir -p .github/workflows
```

Add the workflow file
Create .github/workflows/ci.yml and paste the following content:

name: CI Pipeline

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: rootpassword
          MYSQL_DATABASE: url_shortener
        ports:
          - 3306:3306
        options: --health-cmd="mysqladmin ping" --health-interval=10s --health-timeout=5s --health-retries=3

      redis:
        image: redis:7-alpine
        ports:
          - 6379:6379
        options: --health-cmd="redis-cli ping" --health-interval=10s --health-timeout=5s --health-retries=3

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven

      - name: Build and test with Maven
        env:
          SPRING_DATASOURCE_URL: jdbc:mysql://localhost:3306/url_shortener?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
          SPRING_DATASOURCE_USERNAME: root
          SPRING_DATASOURCE_PASSWORD: rootpassword
          SPRING_DATA_REDIS_HOST: localhost
          SPRING_DATA_REDIS_PORT: 6379
        run: mvn -B verify -DskipTests=false

      - name: Build Docker image
        run: docker build -t url-shortener:latest -f docker/Dockerfile .
