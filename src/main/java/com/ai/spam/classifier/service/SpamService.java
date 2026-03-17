package com.ai.spam.classifier.service;

import com.ai.spam.classifier.dto.ClassificationResult;
import jakarta.annotation.PostConstruct;
import opennlp.tools.doccat.*;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class SpamService {

    private DocumentCategorizerME categorizer;

    @PostConstruct
    public void init() throws IOException {
        InputStream trainingStream = getClass()
                .getResourceAsStream("/training-data.txt");

        if (trainingStream == null) {
            throw new FileNotFoundException(
                    "training-data.txt not found on classpath. " +
                            "Check src/main/resources/"
            );
        }

        InputStreamFactory dataIn = () -> trainingStream;

        try (ObjectStream<String> lineStream =
                     new PlainTextByLineStream(dataIn, StandardCharsets.UTF_8);
             ObjectStream<DocumentSample> sampleStream =
                     new DocumentSampleStream(lineStream)) {

            TrainingParameters params = TrainingParameters.defaultParams();
            params.put(TrainingParameters.ITERATIONS_PARAM, 100);
            params.put(TrainingParameters.CUTOFF_PARAM, 1);

            DoccatModel model = DocumentCategorizerME.train(
                    "en", sampleStream, params, new DoccatFactory()
            );

            this.categorizer = new DocumentCategorizerME(model);
        }

        System.out.println("SpamService: model trained and ready.");
    }

    public ClassificationResult classify(String text) {
        double[] outcomes = categorizer.categorize(text.split("\\s+"));
        String category   = categorizer.getBestCategory(outcomes);
        double confidence = outcomes[categorizer.getIndex(category)];

        return new ClassificationResult(category, confidence);
    }


}
