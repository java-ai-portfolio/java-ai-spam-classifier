# Java AI — Spam Classifier

A production-grade Naive Bayes spam classifier built with **Java 25**, **Spring Boot 4**, and **Apache OpenNLP**.
Trained on **5,574 real SMS messages** at startup — no external ML service required.

![Java](https://img.shields.io/badge/Java-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-green)
![Accuracy](https://img.shields.io/badge/Accuracy-98%25-brightgreen)
![License](https://img.shields.io/badge/License-MIT-blue)

---

## Model Performance

| Metric | Score | Meaning |
|---|---|---|
| Accuracy | **98.0%** | 1,093 out of 1,115 test emails correct |
| Precision | **94.7%** | When flagged spam — correct 94.7% of the time |
| Recall | **91.0%** | Catches 91% of all real spam emails |
| F1 Score | **92.8%** | Balanced score — neither metric is hiding |
| Training set | **4,459** emails | 80% of SMS Spam Collection dataset |
| Test set | **1,115** emails | 20% held out — never seen during training |
| Training time | **~1.8 seconds** | Trains fresh on every startup |

> Dataset: [SMS Spam Collection](https://archive.ics.uci.edu/dataset/228/sms+spam+collection) — 5,574 real labeled messages

---

## Prerequisites

| Tool | Version |
|---|---|
| Java | 25 (preview features required) |
| Gradle | 8.x (via wrapper) |
| Spring Boot | 4.0.1 |

---

## Build and Run
```bash
# Clone
git clone git@github.com:java-ai-portfolio/java-ai-spam-classifier.git
cd java-ai-spam-classifier

# Run
./gradlew bootRun          # macOS / Linux
gradlew.bat bootRun        # Windows
```

On startup you will see:
```
SpamService: model trained and ready.
```

### Build a jar
```bash
./gradlew bootJar
java --enable-preview -jar build/libs/spam-detector-0.0.1.jar
```

---

## API Endpoints

### POST /v1/api/spam/classify
Classify an email as spam or ham.
```bash
curl -X POST http://localhost:8080/v1/api/spam/classify \
  -H "Content-Type: application/json" \
  -d '{"text": "Congratulations! You win a free iPhone. Click now!"}'
```
```json
{
  "category": "spam",
  "confidence": 0.9421,
  "spam": true
}
```

### GET /v1/api/spam/metrics
Run 80/20 evaluation and return live model metrics.
```bash
curl http://localhost:8080/v1/api/spam/metrics
```
```json
{
  "accuracy": 0.98,
  "precision": 0.947,
  "recall": 0.91,
  "f1Score": 0.928,
  "totalTrained": 4459,
  "totalTested": 1115,
  "correctPredictions": 1093,
  "trainingTimeMs": 1810,
  "productionReady": true
}
```

### Error Responses
All endpoints return structured errors — no raw stack traces.
```bash
# Empty body → 400
curl -X POST http://localhost:8080/v1/api/spam/classify \
  -H "Content-Type: application/json" \
  -d '{}'
```
```json
{
  "error": "Text cannot be empty",
  "status": 400,
  "path": "/v1/api/spam/classify",
  "timestamp": "2025-03-19T05:30:00"
}
```

| Scenario | Status | Error message |
|---|---|---|
| Missing / malformed JSON | 400 | Request body is missing or malformed JSON |
| Empty or blank text | 400 | Text cannot be empty |
| Text under 3 characters | 400 | Text too short — minimum 3 characters required |
| Text over 10,000 characters | 400 | Text too long — maximum 10,000 characters allowed |
| Unexpected server error | 500 | Something went wrong: ... |

---

## Project Structure
```
spam-detector/
├── src/main/java/com/ai/spam/classifier/
│   ├── SpamDetectorApp.java          # entry point
│   ├── controller/
│   │   └── SpamController.java       # REST endpoints
│   ├── service/
│   │   ├── SpamService.java          # OpenNLP training + classify()
│   │   └── ModelEvaluator.java       # 80/20 split evaluation
│   ├── dto/
│   │   ├── ClassifyRequest.java      # request record
│   │   ├── ClassificationResult.java # response record
│   │   └── EvaluationResult.java     # metrics response record
│   │   └── ErrorResponse.java        # error response record
│   └── exception/
│       └── GlobalExceptionHandler.java # @RestControllerAdvice
├── src/main/resources/
│   ├── application.properties
│   └── training-data.txt             # 5,574 labeled SMS messages
└── src/test/
    └── SpamDetectorAppTest.java
```

---

## Training Data Format

Located at `src/main/resources/training-data.txt`.
Tab-separated, one labeled sample per line:
```
spam	Congratulations! You win a free iPhone. Click now!
ham	Hi, please find the attached report for review.
```

Model trains fresh on every startup. Add more lines to improve accuracy.
**Supported labels:** `spam` and `ham`

---

## Swagger UI

Interactive API docs — test endpoints directly in browser.

| URL | Description |
|---|---|
| `http://localhost:8080/swagger-ui.html` | Swagger UI |
| `http://localhost:8080/v3/api-docs` | Raw OpenAPI JSON |

---

## Spring Boot Actuator

| URL | Description |
|---|---|
| `http://localhost:8080/actuator/health` | App health + disk space |
| `http://localhost:8080/actuator/info` | App name, version |
| `http://localhost:8080/actuator/metrics` | JVM memory, CPU, request counts |
| `http://localhost:8080/actuator/mappings` | All registered URL mappings |

> Restrict actuator endpoints before production deployment via
> `management.endpoints.web.exposure.include` in `application.properties`

---

## How It Works
```
Email text arrives
       ↓
Split into tokens (words)
       ↓
Each word votes with its training frequency
  spam score += log P(word | spam)
  ham score  += log P(word | ham)
       ↓
Add prior probability (base rate of spam in dataset)
       ↓
Higher log score wins → spam or ham
       ↓
Softmax converts log score to confidence 0.0 – 1.0
```

Log is used instead of raw probability multiplication to prevent
floating point underflow across hundreds of words.

---

## Part of the java-ai-portfolio

This is **Project 1 of 10** in the Java AI learning portfolio.

| Project | Description | Status |
|---|---|---|
| **java-ai-spam-classifier** | Naive Bayes + Spring Boot REST | ✅ Week 1–2 |
| java-ai-house-prices | Linear Regression + Smile ML | 🔜 Week 3 |
| java-ai-churn-api | Random Forest + Docker | 🔜 Week 4 |
| java-ai-digit-cnn | CNN + DL4J | 🔜 Week 8 |
| java-ai-sentiment | LSTM + Word2Vec | 🔜 Week 13 |
| java-ai-rag-qa | RAG + Spring AI + LangChain4j | 🔜 Week 18 |

---

## Contributing

1. Fork and create a branch from `main`
2. Add labeled samples to `training-data.txt` if changing classifier behaviour
3. Run `./gradlew test` — all tests must pass
4. Open a pull request with clear description

Commit messages follow [Conventional Commits](https://www.conventionalcommits.org):
`feat:` `fix:` `chore:` `docs:` `test:`