package invariant.evaluator.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.client.OpenAiApi;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.completion.chat.ChatCompletionRequest.ChatCompletionRequestFunctionCall;
import com.theokanning.openai.model.Model;
import com.theokanning.openai.service.FunctionExecutor;
import com.theokanning.openai.service.OpenAiService;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LLMClassifierWithConfidence {

    public static final String SYSTEM_MESSAGE = """
            You are an assistant that evaluates if a given invariant is correct for a REST API.
            Your task is to classify the invariant as "true-positive" (i.e., correct: holds for every valid request on the API) or "false-positive" (i.e., incorrect: will not hold for every valid request on the API) and provide your reasoning for the decision and your confidence how sure you are about the classification.
            """;

    public enum InvariantVerdict {
        TP, FP, INCONCLUSIVE;

        public static InvariantVerdict fromString(String verdictString){
            if(verdictString == null){
                return InvariantVerdict.INCONCLUSIVE;
            }

            return switch(verdictString){
                case "true-positive" -> InvariantVerdict.TP;
                case "false-positive" -> InvariantVerdict.FP;
                default -> InvariantVerdict.INCONCLUSIVE;
            };
        }
    }

    public static class InvariantClassificationResult {
        private final String reason;
        private final InvariantVerdict verdict;
        private float confidence;
        private final long promptTokens;
        private final long completionTokens;
        private final long totalTokens;

        public InvariantClassificationResult(String reason, InvariantVerdict verdict, float confidence, long promptTokens, long completionTokens, long totalTokens) {
            this.reason = reason;
            this.verdict = verdict;
            this.confidence = confidence;
            this.promptTokens = promptTokens;
            this.completionTokens = completionTokens;
            this.totalTokens = totalTokens;
        }

        public String getReason() {
            return reason;
        }

        public InvariantVerdict getVerdict() {
            return verdict;
        }

        public float getConfidence() {
            return confidence;
        }

        public long getPromptTokens() {
            return promptTokens;
        }

        public long getCompletionTokens() {
            return completionTokens;
        }

        public long getTotalTokens() {
            return totalTokens;
        }

        @Override
        public String toString() {
            return "InvariantClassificationResult{" +
                    "reason='" + reason + '\'' +
                    ", verdict=" + verdict +
                    ", confidence=" + confidence +
                    ", promptTokens=" + promptTokens +
                    ", completionTokens=" + completionTokens +
                    ", totalTokens=" + totalTokens +
                    '}';
        }
    }


    public static class Invariant {
        @JsonPropertyDescription("Contains the complete reasoning about the decision in markdown including sections \"Swagger Definition Breakdown\", \"Invariant\", \"Analysis\", \"Conclusion\"")
        @JsonProperty(required = true)
        private String reason;
        @JsonPropertyDescription("The verdict on the invariant, can be \"true-positive\" or \"false-positive\"")
        @JsonProperty(required = true)
        private String verdict;

        @JsonPropertyDescription("The confidence about the verdict, as a number between 0 and 1 (0 very low confidence, 1 very high confidence).")
        @JsonProperty(required = true)
        private float confidence;
    }

    public static class InvariantResponse {
        private String reason;
        private String verdict;
        private float confidence;

        public InvariantResponse(String reason, String verdict, float confidence) {
            this.reason = reason;
            this.verdict = verdict;
            this.confidence = confidence;
        }
    }

    public static List<Model> getAvailableModels(String token, LLM model){
        ObjectMapper mapper = OpenAiService.defaultObjectMapper();
        OkHttpClient client = OpenAiService.defaultClient(token, Duration.ofMinutes(2))
                .newBuilder()
                .build();
        Retrofit retrofit = model.getRetroFit(client, mapper);

        OpenAiApi api = retrofit.create(OpenAiApi.class);
        OpenAiService service = new OpenAiService(api);

        return service.listModels();
    }

    public static InvariantClassificationResult classifyInvariant(LLM model, double temperature, String prompt){
        return classifyInvariant(model, temperature, prompt, null);
    }

    public static InvariantClassificationResult classifyInvariant(LLM model, double temperature, String prompt, String additionalInstructions){
        ObjectMapper mapper = OpenAiService.defaultObjectMapper();
        OkHttpClient client = OpenAiService.defaultClient(model.getApiKey(), Duration.ofMinutes(20))
                .newBuilder()
                .build();
        Retrofit retrofit = model.getRetroFit(client, mapper);

        OpenAiApi api = retrofit.create(OpenAiApi.class);
        OpenAiService service = new OpenAiService(api);

        FunctionExecutor functionExecutor = new FunctionExecutor(Collections.singletonList(ChatFunction.builder()
                .name("classify_invariant")
                .description("Classify the invariant for a REST API as \"true-positive\" or \"false-positive\"")
                .executor(Invariant.class, i -> new InvariantResponse(i.reason, i.verdict, i.confidence))
                .build()));

        String systemMessageString = SYSTEM_MESSAGE;
        if(additionalInstructions != null && !additionalInstructions.isEmpty()){
            systemMessageString += "\n" + additionalInstructions;
        }

        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessageString);
        messages.add(systemMessage);

        ChatMessage firstMsg = new ChatMessage(ChatMessageRole.USER.value(), prompt);
        messages.add(firstMsg);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model(model.getIdentifier())
                .messages(messages)
                .functions(functionExecutor.getFunctions())
                .functionCall(ChatCompletionRequestFunctionCall.of("auto"))
                .n(1)
                .temperature(temperature)
//                .maxTokens(100)
//                .logitBias(new HashMap<>())
                .build();

        ChatCompletionResult result = service.createChatCompletion(chatCompletionRequest);
        long promptTokens = result.getUsage().getPromptTokens();
        long completionTokens = result.getUsage().getCompletionTokens();
        long totalTokens = result.getUsage().getTotalTokens();

        ChatMessage response = result.getChoices().get(0).getMessage();
        ChatFunctionCall functionCall = response.getFunctionCall();
        if (functionCall != null) {
            JsonNode arguments = functionCall.getArguments();
            try {
                Invariant inv = mapper.treeToValue(arguments, Invariant.class);
                return new InvariantClassificationResult(inv.reason, InvariantVerdict.fromString(inv.verdict), inv.confidence, promptTokens, completionTokens, totalTokens);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return new InvariantClassificationResult(response.getContent(), InvariantVerdict.INCONCLUSIVE, 0, promptTokens, completionTokens, totalTokens);
    }
}
