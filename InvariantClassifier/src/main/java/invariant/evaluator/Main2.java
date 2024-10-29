package invariant.evaluator;

import at.sfischer.utils.table.ArrayValueTable;
import at.sfischer.utils.table.ValueTable;
import at.sfischer.utils.table.ValueTableRow;
import at.sfischer.utils.table.visitor.FilterValueTableRowVisitor;
import invariant.evaluator.apispec.APISpecFilter;
import invariant.evaluator.data.*;
import invariant.evaluator.data.testdata.JSONManager;
import invariant.evaluator.data.testdata.TestCase;
import invariant.evaluator.openai.GPTModel;
import invariant.evaluator.openai.LLM;
import invariant.evaluator.openai.LLMClassifierWithConfidence;
import invariant.evaluator.openai.PromptGenerator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main2 {

    public static final MergedDataSet COMPLETE_AGORA_DATA = new MergedDataSet(
            AmadeusHotel.AMADEUS_HOTEL_OFFERS,
            GitHubCreateRepo.GITHUB_CREATE_REPO,
            GitHubGetRepo.GITHUB_GET_REPO,
            Marvel.MARVEL_GET_COMICS,
            OMDbByIdOrTitle.OMDB_ID_TITLE,
            OMDbBySearch.OMDB_SEARCH,
            SpotifyCreatePlaylist.SPOTIFY_CREATE_PLAYLIST,
            SpotifyGetTracks.SPOTIFY_GET_TRACKS,
            SpotifyGetArtists.SPOTIFY_GET_ARTISTS,
            Yelp.YELP_GET_BUSINESSES,
            Youtube.YOUTUBE_GET_VIDEOS
    );

    public static final MergedDataSet UNIQUE_COMPLETE_AGORA_DATA = new MergedDataSet(
            AmadeusHotel.UNIQUE_AMADEUS_HOTEL_OFFERS,
            GitHubCreateRepo.UNIQUE_GITHUB_CREATE_REPO,
            GitHubGetRepo.UNIQUE_GITHUB_GET_REPO,
            Marvel.UNIQUE_MARVEL_GET_COMICS,
            OMDbByIdOrTitle.UNIQUE_OMDB_ID_TITLE,
            OMDbBySearch.UNIQUE_OMDB_SEARCH,
            SpotifyCreatePlaylist.UNIQUE_SPOTIFY_CREATE_PLAYLIST,
            SpotifyGetTracks.UNIQUE_SPOTIFY_GET_TRACKS,
            SpotifyGetArtists.UNIQUE_SPOTIFY_GET_ARTISTS,
            Yelp.UNIQUE_YELP_GET_BUSINESSES,
            Youtube.UNIQUE_YOUTUBE_GET_VIDEOS
    );

//    public static final DataSet DATA_SET = UNIQUE_COMPLETE_AGORA_DATA;

    public static final DataSet DATA_SET = AmadeusHotel.UNIQUE_AMADEUS_HOTEL_OFFERS; public static final AgoraDataSet[] ALL_DATA_SETS = AmadeusHotel.ALL_DATA_SETS;
//    public static final DataSet DATA_SET = GitHubCreateRepo.UNIQUE_GITHUB_CREATE_REPO; public static final AgoraDataSet[] ALL_DATA_SETS = GitHubCreateRepo.ALL_DATA_SETS;
//    public static final DataSet DATA_SET = GitHubGetRepo.UNIQUE_GITHUB_GET_REPO; public static final AgoraDataSet[] ALL_DATA_SETS = GitHubGetRepo.ALL_DATA_SETS;
//    public static final DataSet DATA_SET = Marvel.UNIQUE_MARVEL_GET_COMICS; public static final AgoraDataSet[] ALL_DATA_SETS = Marvel.ALL_DATA_SETS;
//    public static final DataSet DATA_SET = OMDbByIdOrTitle.UNIQUE_OMDB_ID_TITLE; public static final AgoraDataSet[] ALL_DATA_SETS = OMDbByIdOrTitle.ALL_DATA_SETS;
//    public static final DataSet DATA_SET = OMDbBySearch.UNIQUE_OMDB_SEARCH; public static final AgoraDataSet[] ALL_DATA_SETS = OMDbBySearch.ALL_DATA_SETS;
//    public static final DataSet DATA_SET = SpotifyCreatePlaylist.UNIQUE_SPOTIFY_CREATE_PLAYLIST; public static final AgoraDataSet[] ALL_DATA_SETS = SpotifyCreatePlaylist.ALL_DATA_SETS;
//    public static final DataSet DATA_SET = SpotifyGetTracks.UNIQUE_SPOTIFY_GET_TRACKS; public static final AgoraDataSet[] ALL_DATA_SETS = SpotifyGetTracks.ALL_DATA_SETS;
//    public static final DataSet DATA_SET = SpotifyGetArtists.UNIQUE_SPOTIFY_GET_ARTISTS; public static final AgoraDataSet[] ALL_DATA_SETS = SpotifyGetArtists.ALL_DATA_SETS;
//    public static final DataSet DATA_SET = Yelp.UNIQUE_YELP_GET_BUSINESSES; public static final AgoraDataSet[] ALL_DATA_SETS = Yelp.ALL_DATA_SETS;
//    public static final DataSet DATA_SET = Youtube.UNIQUE_YOUTUBE_GET_VIDEOS; public static final AgoraDataSet[] ALL_DATA_SETS = Youtube.ALL_DATA_SETS;

    public static final LLM MODEL = GPTModel.GPT_3_5;
//    public static final LLM MODEL = GPTModel.GPT_4o_mini;
//    public static final LLM MODEL = GPTModel.GPT_4o;

    public static final String YAML_INDENT = "  ";


    public static final String RESULTS_DIR = "results_improved_prompt";


    public static void main(String[] args) throws Exception {

        Map<Long, List<TestCase>> testCasesPerNumberOfCalls = new HashMap<>();
        for (AgoraDataSet agoraDataSet : ALL_DATA_SETS) {
            if(testCasesPerNumberOfCalls.containsKey(agoraDataSet.getNumberOfApiCalls())){
                continue;
            }

            List<TestCase> testCases = AgoraDataSet.parseCallData(agoraDataSet.getSystemName(), agoraDataSet.getOperation(), agoraDataSet.getNumberOfApiCalls());
            testCasesPerNumberOfCalls.put(agoraDataSet.getNumberOfApiCalls(), testCases);
        }

        File resultsDir = new File(RESULTS_DIR);
        Map<String, Map<String, ValueTable>> results = new HashMap<>();

        if(DATA_SET instanceof AbstractAgoraDataSet){
            System.out.println("Number of entries: " + ((AbstractAgoraDataSet) DATA_SET).getInvariantData().rows());
        }

        AtomicInteger invariantCount = new AtomicInteger();
        DATA_SET.visitData((id, systemName, operation, openAPI, path, method, responseCode, invariant, invariantType, invariantDescription, inputVariables, outputVariables, outputVariablesPath, classification) -> {
            File systemDir = new File(resultsDir, systemName);
            File operationDir = new File(systemDir, operation);
            File modelDir = new File(operationDir, MODEL.getName());
            File idDir = new File(modelDir, String.valueOf(id));
            if(idDir.exists()){
                return;
            }

            String yamlString = APISpecFilter.extractRelevantSwagger(openAPI, path, method, responseCode, inputVariables, outputVariables, YAML_INDENT);

            // Parse the input data for the operation and find examples for the variables in the constraint.
            // We can only use the data that AGORA also had to determine the invariant, so we can only use data from the set that found the invariant, other set would possibly have counter examples, which would have led them to not be detected.
            Set<Long> callNumbersWithInvariant = new HashSet<>();
            for (AgoraDataSet agoraDataSet : ALL_DATA_SETS) {
                long numberOfOperations = agoraDataSet.getNumberOfApiCalls();
                boolean containsInvariants = agoraDataSet.containsInvariant(invariant);
                if(containsInvariants){
                    callNumbersWithInvariant.add(numberOfOperations);
                }
            }
            List<String> examples = new LinkedList<>();
            long maxNumberOfOperations = 0;
            for (Long numberOfOperations : callNumbersWithInvariant) {
                if(numberOfOperations > maxNumberOfOperations){
                    maxNumberOfOperations = numberOfOperations;
                }
                List<TestCase> testCases = testCasesPerNumberOfCalls.get(numberOfOperations);
                // Extract the examples.
                examples.addAll(getExamples(testCases, inputVariables, outputVariables));
            }

            String prompt = PromptGenerator.generatePrompt(yamlString, invariant, invariantType, invariantDescription, outputVariablesPath, examples, maxNumberOfOperations);

            String additionalInstructions = """
                    We tried from 50 up to 10000 calls for the operation to find different invariants.
                    If we have tried more calls and found more examples of the invariant holding it should be regarded more likely that the invariant is a "true-positive".
                    If there is an invariant on a string variable, for which we have many examples or calls, that might make sense and the specification does not clearly contradict the invariant, it should be regarded as a likely "true-positive".
                    If the invariant restricts the value of a variable and the specification explicitly mentions other values the variable can take on, it is most likely a "false-positive".
                    You should also consider the semantics of the variables in the context of the invariant. For instance, if we compare two variables where it makes no semantic sense, the invariant is most likely a "false-positive".
                    """;

            LLMClassifierWithConfidence.InvariantClassificationResult response = LLMClassifierWithConfidence.classifyInvariant(MODEL, 0.2, prompt, additionalInstructions);

//            System.out.println("------------------------------");
//            System.out.println("Invariant: " + systemName + " / " + operation + " - " + id);
//            System.out.println("------------ " + classification + " --------------");
//            System.out.println(prompt);
//            System.out.println("------------ " + classification + " --------------");
//
//            System.out.println(response);


            idDir.mkdirs();
            // Store prompt in a file.
            File promptFile = new File(idDir, "prompt.txt");
            try {
                PrintStream promptStream = new PrintStream(promptFile);
                promptStream.println(prompt);
                promptStream.flush();
                promptStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Store response reason in a markdown file.
            File reasonFile = new File(idDir, "response.md");
            try {
                PrintStream promptStream = new PrintStream(reasonFile);
                promptStream.println(response.getReason());
                promptStream.flush();
                promptStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            File resultFile = new File(idDir, "result.json");
            String json = jsonResultString(id, invariantType, classification, response.getVerdict().name(), response.getConfidence(), response.getPromptTokens(), response.getCompletionTokens(), response.getTotalTokens(), maxNumberOfOperations);
            try {
                PrintStream resultStream = new PrintStream(resultFile);
                resultStream.println(json);
                resultStream.flush();
                resultStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Store data (id, invariant type, classification, verdict, tokens used) in a table and print to csv.
            Map<String, ValueTable> operationResults = results.computeIfAbsent(systemName, k -> new HashMap<>());
            ValueTable resultsTable = operationResults.computeIfAbsent(operation, k -> new ArrayValueTable(5, 0));
            int nextRow = resultsTable.rows();
            resultsTable.addValue("ID", nextRow, id);
            resultsTable.addValue("invariantType", nextRow, invariantType);
            resultsTable.addValue("classification", nextRow, classification);
            resultsTable.addValue("GPT_verdict", nextRow, response.getVerdict().name());
            resultsTable.addValue("confidence", nextRow, response.getConfidence());
            resultsTable.addValue("promptTokens", nextRow, response.getPromptTokens());
            resultsTable.addValue("completionTokens", nextRow, response.getCompletionTokens());
            resultsTable.addValue("totalTokens", nextRow, response.getTotalTokens());
            resultsTable.addValue("maxNumberOfOperations", nextRow, maxNumberOfOperations);


            invariantCount.getAndIncrement();
            System.out.println(invariantCount.get() + " #" + id + " : " + classification + " vs. " + response.getVerdict() + " (" + response.getConfidence() * 100.0 + "%)");
        });

        for (Map.Entry<String, Map<String, ValueTable>> entry1 : results.entrySet()) {
            String systemName = entry1.getKey();
            for (Map.Entry<String, ValueTable> entry2 : entry1.getValue().entrySet()) {
                String operation = entry2.getKey();
                ValueTable resultsTab = entry2.getValue();

                File systemDir = new File(resultsDir, systemName);
                File operationDir = new File(systemDir, operation);
                File modelDir = new File(operationDir, MODEL.getName());
                File resFile = new File(modelDir, "results.csv");

                try {
                    resultsTab = resultsTab.filter(new FilterValueTableRowVisitor() {
                        @Override
                        public boolean filter(ValueTableRow row) {
                            try{
                                return row.getValue("ID") != null;
                            } catch (IllegalArgumentException e){
                                return false;
                            }
                        }
                    });
                    resultsTab.printCSV(resFile, ";");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Number of unique invariants: " + invariantCount.get());
    }

    public static String jsonResultString(int id, String invariantType, String classification, String gptVerdict, float confidence, long promptTokens, long completionTokens, long totalTokens, long maxNumberOfOperations){
        String json = """
                    {
                        "id":%d,
                        "invariantType":"%s",
                        "classification":"%s",
                        "GPT_verdict":"%s",
                        "confidence":%f,
                        "promptTokens":%d,
                        "completionTokens":%d,
                        "totalTokens":%d,
                        "maxNumberOfOperations":%d
                    }
                    """;

        return String.format(json, id, invariantType, classification, gptVerdict, confidence, promptTokens, completionTokens, totalTokens, maxNumberOfOperations);
    }

    public static List<String> getExamples(List<TestCase> testCases, List<String> inputVariables, List<String> outputVariables){
        List<String> examples = new LinkedList<>();
        String[] commonPath = getCommonPath(outputVariables);
        List<String> adjustedOutputVariables = new LinkedList<>();
        String common = String.join(".", commonPath);
        if(!common.isEmpty()){
            common += ".";
        }
        for (String outputVariable : outputVariables) {
            adjustedOutputVariables.add(outputVariable.substring(common.length()));
        }

        for (TestCase testCase : testCases) {
            String example = getInputExample(testCase, inputVariables);
            if(!inputVariables.isEmpty() && example == null){
                continue;
            }

            List<String> out = getOutputExample(testCase, commonPath, adjustedOutputVariables);
            if(!outputVariables.isEmpty() && out == null){
                continue;
            }

            if(example == null){
                examples.addAll(out);
            } else {
                Set<String> newExamples = new HashSet<>();
                for (String s : out) {
                    newExamples.add(example + s);
                }
                examples.addAll(newExamples);
            }
        }

        return examples;
    }

    static String getInputExample(TestCase testCase, List<String> inputVariables){
        if(inputVariables == null || inputVariables.isEmpty()){
            return null;
        }

        StringBuilder example = new StringBuilder();
        for (String inputVariable : inputVariables) {
            String value = getInputExample(testCase, inputVariable);
            if(value == null){
                return null;
            }

            example.append("input.").append(inputVariable).append("=").append(value).append("; ");
        }

        return example.toString();
    }

    static String getInputExample(TestCase testCase, String inputVariable){
        String value = testCase.getQueryParameters().get(inputVariable);
        if(value != null){
            return value;
        }

        value = testCase.getPathParameters().get(inputVariable);
        if(value != null){
            return value;
        }

        value = testCase.getFormParameters().get(inputVariable);
        if(value != null){
            return value;
        }

        // Parse body parameter.
        String body = testCase.getBodyParameter();
        if(body == null || body.isEmpty()){
            return null;
        }

        String[] path = inputVariable.split("\\.");
        if(JSONManager.isStringJsonArray(body)){
            JSONArray jsonArray = JSONManager.stringToJsonArray(body);
            List<String> examples = getExample(jsonArray, path, 0);
            if(examples != null){
                return examples.get(0);
            }
        } else {
            JSONObject json = JSONManager.stringToJsonObject(body);
            List<String> examples = getExample(json, path, 0);
            if(examples != null){
                return examples.get(0);
            }
        }

        return null;
    }

    static List<String> getOutputExample(TestCase testCase, String[] commonPath, List<String> outputVariables){
        if(outputVariables == null || outputVariables.isEmpty()){
            return null;
        }

        String common = String.join(".", commonPath);
        if(!common.isEmpty()){
            common += ".";
        }

        Object commonObject = getOutputObject(testCase, commonPath);
        return getOutputExample(commonObject, common, outputVariables);
    }

    static String[] getCommonPath(List<String> outputVariables){
        if(outputVariables.size() <= 1){
            return new String[0];
        }

        String[] commonPath = outputVariables.get(0).split("\\.");
        for (int i = 1; i < outputVariables.size(); i++) {
            String[] path = outputVariables.get(i).split("\\.");
            commonPath = greatestCommonPrefix(commonPath, path);
        }

        return commonPath;
    }

    public static String[] greatestCommonPrefix(String[] a, String[] b) {
        int minLength = Math.min(a.length, b.length);
        for (int i = 0; i < minLength; i++) {
            if (!a[i].equals(b[i])) {
                return Arrays.copyOfRange(a, 0, i);
            }
        }
        return Arrays.copyOfRange(a,0, minLength);
    }

    static Object getOutputObject(TestCase testCase, String[] path){
        String responseBody = testCase.getResponseBody();
        if(JSONManager.isStringJsonArray(responseBody)){
            JSONArray jsonArray = JSONManager.stringToJsonArray(responseBody);
            return getOutputObject(jsonArray, path, 0);
        } else {
            JSONObject json = JSONManager.stringToJsonObject(responseBody);
            return getOutputObject(json, path, 0);
        }
    }

    static Object getOutputObject(JSONObject json, String[] path, int index){
        if(index >= path.length){
            return json;
        }

        Object val = json.get(path[index]);
        if(val instanceof JSONObject){
            return getOutputObject((JSONObject) val, path, index + 1);
        }
        if(val instanceof JSONArray){
            return getOutputObject((JSONArray) val, path, index + 1);
        }

        return json;
    }

    static Object getOutputObject(JSONArray jsonArray, String[] path, int index){
        if(index >= path.length){
            return jsonArray;
        }

        JSONArray result = new JSONArray();
        for (Object o : jsonArray) {
            if(o instanceof JSONObject){
                result.add(getOutputObject((JSONObject)o, path, index));
                continue;
            }

            result.add(o);
        }

        return result;
    }

    static List<String> getOutputExample(Object value, String variablePrefix, List<String> outputVariables){
        if(value instanceof JSONArray){
            List<String> examples = new LinkedList<>();
            for (Object o : ((JSONArray) value)) {
                List<String> ex = getOutputExample(o, variablePrefix, outputVariables);
                if(ex != null){
                    examples.addAll(ex);
                }
            }
            return examples;
        }

        if(value instanceof JSONObject){
            List<String> examples = new LinkedList<>();
            for (String outputVariable : outputVariables) {
                String[] path = outputVariable.split("\\.");
                List<String> ex = getExample((JSONObject)value, path, 0);
                if(ex == null || ex.isEmpty()){
                    return null;
                }

                if(examples.isEmpty()){
                    for (String s : ex) {
                        examples.add("return." + variablePrefix + outputVariable + "=" + s);
                    }
                } else {
                    if(examples.size() != ex.size()){
                        return null;
                    }

                    List<String> newExamples = new LinkedList<>();
                    for (int i = 0; i < examples.size(); i++) {
                        newExamples.add(examples.get(i) + "; " + "return." + variablePrefix + outputVariable + "=" + ex.get(i));
                    }
                    examples = newExamples;
                }
            }

            return examples;
        }

        return null;
    }

    static List<String> getExample(JSONObject json, String[] path, int index){
        if(index >= path.length){
            return Collections.singletonList(json.toJSONString());
        }

        Object val = json.get(path[index]);
        if(val == null){
            return null;
        }
        if(val instanceof JSONObject){
            return getExample((JSONObject) val, path, index + 1);
        }
        if(val instanceof JSONArray){
            return getExample((JSONArray) val, path, index + 1);
        }
        if(val instanceof String){
            return Collections.singletonList((String)val);
        }
        if(val instanceof Boolean){
            return Collections.singletonList(String.valueOf(((Boolean)val).booleanValue()));
        }
        if(val instanceof Long){
            return Collections.singletonList(String.valueOf(((Long)val).longValue()));
        }
        if(val instanceof Integer){
            return Collections.singletonList(String.valueOf(((Integer)val).intValue()));
        }
        if(val instanceof Float){
            return Collections.singletonList(String.valueOf(((Float)val).floatValue()));
        }
        if(val instanceof Double){
            return Collections.singletonList(String.valueOf(((Double)val).doubleValue()));
        }
        if(val instanceof Character){
            return Collections.singletonList(String.valueOf(((Character)val).charValue()));
        }

        return Collections.singletonList(val.toString());
    }

    static List<String> getExample(JSONArray jsonArray, String[] path, int index){
        if(jsonArray.isEmpty()){
            return null;
        }
        if(index >= path.length){
            return Collections.singletonList(jsonArray.toJSONString());
        }

        List<String> examples = new LinkedList<>();
        boolean nestedObjects = false;
        for (Object o : jsonArray) {
            if(o instanceof JSONObject){
                nestedObjects = true;
                List<String> ex = getExample((JSONObject) o, path, index);
                if(ex == null){
                    continue;
                }

                examples.addAll(ex);
                continue;
            }

            break;
        }
        if(nestedObjects){
            if(examples.isEmpty()){
                return null;
            } else {
                return examples;
            }
        }

        examples.add(jsonArray.toJSONString());
        return examples;
    }
}