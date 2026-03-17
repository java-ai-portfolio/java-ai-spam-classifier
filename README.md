# spam-classifier

A Naive Bayes email spam classifier built with Spring Boot 4, OpenNLP, and Java 25.
Trained at startup from a plain text file — no external ML service required.

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 25 |
| Gradle | 8.x (via wrapper) |
| Spring Boot | 4.0.1 |

> Java 25 preview features are required. Make sure `JAVA_HOME` points to a JDK 25 installation.

---

## Build and run

```bash
# Clone the repo
git clone https://github.com/your-org/spam-detector.git
cd spam-detector

# Run (wrapper downloads the correct Gradle version automatically)
./gradlew bootRun         # macOS / Linux
gradlew.bat bootRun       # Windows
```

On startup you will see:

```
SpamService: model trained and stable.
```

This confirms OpenNLP has trained and the classifier is ready.

### Build a jar

```bash
./gradlew bootJar
java --enable-preview -jar build/libs/spam-detector-0.0.1.jar
```

---

## Project structure

```
spam-detector/
├── gradle/
│   ├── libs.versions.toml          # centralised dependency versions
│   └── wrapper/
│       └── gradle-wrapper.properties
├── src/
│   ├── main/
│   │   ├── java/com/ai/spam/classifier/
│   │   │   ├── SpamDetectorApp.java     # entry point
│   │   │   ├── SpamService.java         # OpenNLP training + classify()
│   │   │   └── SpamController.java      # REST endpoints
│   │   └── resources/
│   │       ├── application.properties
│   │       └── training-data.txt        # labeled spam/ham samples
│   └── test/
│       └── java/com/ai/spam/classifier/
│           └── SpamDetectorAppTest.java
├── .gitattributes
├── .gitignore
├── build.gradle
├── gradle.properties
├── gradlew
├── gradlew.bat
└── settings.gradle
```

---

## Training data

Located at `src/main/resources/training-data.txt`.
One labeled sample per line, category and text separated by a tab:

```
spam	Congratulations! You win a free iPhone. Click now!
ham	Hi, please find the attached report for review.
```

The model trains fresh on every startup. To improve accuracy, add more labeled
lines to this file — the more examples, the better the word frequency counts.

**Supported labels:** `spam` and `ham` (ham = legitimate email).

---

## API endpoints

### Classify an email

```bash
curl -X POST http://localhost:8080/api/spam/classify \
  -H "Content-Type: application/json" \
  -d '{"text": "Congratulations! You win a free iPhone. Click now!"}'
```

Response:

```json
{
  "category": "spam",
  "confidence": 0.9421,
  "spam": true
}
```

### Health check

```bash
curl http://localhost:8080/v1/api/spam/health
```

---

## Swagger UI

Interactive API documentation — try endpoints directly in the browser.

| URL | Description |
|-----|-------------|
| `http://localhost:8080/swagger-ui.html` | Swagger UI |
| `http://localhost:8080/v3/api-docs` | Raw OpenAPI JSON spec |

---

## Actuator

Spring Boot Actuator endpoints for monitoring.

| URL | Description |
|-----|-------------|
| `http://localhost:8080/actuator/health` | App health + disk space |
| `http://localhost:8080/actuator/info` | App name, version, description |
| `http://localhost:8080/actuator/metrics` | JVM memory, CPU, request counts |
| `http://localhost:8080/actuator/mappings` | All registered URL mappings |

> All actuator endpoints are exposed in the default config. Restrict them before deploying to production by editing `management.endpoints.web.exposure.include` in `application.properties`.

---

## Contributing

1. Fork the repo and create a branch from `main`
2. Add labeled samples to `training-data.txt` if changing classifier behaviour
3. Run `./gradlew test` — all tests must pass
4. Open a pull request with a clear description of what changed and why

Commit messages follow [Conventional Commits](https://www.conventionalcommits.org):
`feat:` `fix:` `chore:` `docs:` `test:`
