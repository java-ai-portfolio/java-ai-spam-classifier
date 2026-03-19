package com.ai.spam.classifier.service;

import com.ai.spam.classifier.dto.EvaluationResult;
import opennlp.tools.doccat.*;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class ModelEvaluator {


    public EvaluationResult evaluate() throws IOException {

        // ── Step 1: Load all lines from txt ──────────────
        ArrayList<String> trainingData = readData();

        if (trainingData.size() < 10) {
            throw new IllegalStateException(
                    "Not enough training data. Need at least 10 lines, found: "
                            + trainingData.size() + ". Add more data to training-data"
            );
        }

        // ── Step 2: Shuffle for fair split ───────────────
        Collections.shuffle(trainingData, new Random(42)); // seed=42 reproducible

        // ── Step 3: 80/20 split ───────────────────────────
        int splitIndex  = (int)(trainingData.size() * 0.80);
        List<String> trainLines = trainingData.subList(0, splitIndex);
        List<String> testLines  = trainingData.subList(splitIndex, trainingData.size());

        long startTime = System.currentTimeMillis();
        DocumentCategorizerME trainedModel = trainModel(trainLines);
        long trainingTimeMs = System.currentTimeMillis() - startTime;

        // ── Step 5: Evaluate on 20% ───────────────────────
        int truePositive  = 0; // predicted spam,  actually spam
        int falsePositive = 0; // predicted spam,  actually ham
        int falseNegative = 0; // predicted ham,   actually spam
        int correct       = 0;


        for (String line : testLines) {
            // Parse "spam\tsome email text here"
            String[] parts = line.split("\t", 2);
            if (parts.length < 2) continue;

            String actualLabel    = parts[0].trim().toLowerCase();
            String text           = parts[1].trim();
            String[] tokens       = text.split("\\s+");

            double[] outcomes     = trainedModel.categorize(tokens);
            String predictedLabel = trainedModel.getBestCategory(outcomes);

            boolean isCorrect = predictedLabel.equals(actualLabel);
            if (isCorrect) correct++;

            // Precision/Recall counters (spam = positive class)
            if (predictedLabel.equals("spam") && actualLabel.equals("spam")) truePositive++;
            if (predictedLabel.equals("spam") && actualLabel.equals("ham"))  falsePositive++;
            if (predictedLabel.equals("ham")  && actualLabel.equals("spam")) falseNegative++;
        }

        // ── Step 6: Calculate metrics ─────────────────────
        double accuracy  = (double) correct / testLines.size();

        // Precision: of all emails flagged spam, how many were really spam?
        double precision = truePositive + falsePositive == 0 ? 0 :
                (double) truePositive / (truePositive + falsePositive);

        // Recall: of all real spam emails, how many did we catch?
        double recall    = truePositive + falseNegative == 0 ? 0 :
                (double) truePositive / (truePositive + falseNegative);

        // F1: harmonic mean of precision and recall
        double f1Score   = precision + recall == 0 ? 0 :
                2 * (precision * recall) / (precision + recall);

        return new EvaluationResult(
                round(accuracy),
                round(precision),
                round(recall),
                round(f1Score),
                trainLines.size(),
                testLines.size(),
                correct,
                trainingTimeMs
        );

    }



    private DocumentCategorizerME trainModel(List<String> data) throws IOException{

        String joined = String.join("\n", data);
        InputStreamFactory dataIn = () -> new ByteArrayInputStream(joined.getBytes(UTF_8));

        try (ObjectStream<String> lineStream =
                     new PlainTextByLineStream(dataIn, UTF_8);
             ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream)) {

            TrainingParameters params = TrainingParameters.defaultParams();
            params.put(TrainingParameters.ITERATIONS_PARAM, 100);
            params.put(TrainingParameters.CUTOFF_PARAM, 1);

            DoccatModel model = DocumentCategorizerME.train(
                    "en", sampleStream, params, new DoccatFactory()
            );
            return new DocumentCategorizerME(model);
        }
    }

    private ArrayList<String> readData() throws IOException {
        try (InputStream io = getClass().getResourceAsStream("/SMSSpamCollection.txt")) {
            if (io == null) {
                throw new FileNotFoundException(
                        "training-data not found in classpath"
                );
            }
            try (BufferedReader reader = new BufferedReader( new InputStreamReader(io, UTF_8)) ){

                return new ArrayList<>( reader.lines()
                        .map(String::trim)
                        .filter(lines -> !lines.isEmpty())
                        .filter(lines->lines.contains("\t"))
                        .toList());
            }

        }
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

}
