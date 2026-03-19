package com.ai.spam.classifier.controller;

import com.ai.spam.classifier.dto.ClassificationResult;
import com.ai.spam.classifier.dto.ClassifyRequest;
import com.ai.spam.classifier.dto.EvaluationResult;
import com.ai.spam.classifier.service.ModelEvaluator;
import com.ai.spam.classifier.service.SpamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/v1/api/spam")
@Tag(
        name = "Spam Detector",
        description = "Naive Bayes email classifier powered by OpenNLP"
)
public class SpamController {

    private final SpamService spamService;
    private final ModelEvaluator modelEvaluator;


    public SpamController(SpamService spamService, ModelEvaluator modelEvaluator) {
        this.spamService = spamService;
        this.modelEvaluator = modelEvaluator;
    }

    @Operation(
            summary = "Classify an email as spam or ham",
            description = "Scores each word against the trained model and returns category + confidence",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Classification result",
                            content = @Content(schema = @Schema(implementation = ClassificationResult.class))
                    )
            }
    )
    @PostMapping("/classify")
    public ClassificationResult classify(
            @RequestBody ClassifyRequest request) {
        return spamService.classify(request.text());
    }

    @Operation(
            summary = "Evaluate model accuracy",
            description = "Runs 80/20 train/test split and returns accuracy, precision, recall, F1"
    )
    @GetMapping("/metrics")
    public EvaluationResult metrics() throws IOException {
        return modelEvaluator.evaluate();
    }

}
