package invariant.evaluator;

import at.sfischer.utils.table.ArrayValueTable;
import at.sfischer.utils.table.ValueTable;
import at.sfischer.utils.table.ValueTableRow;
import at.sfischer.utils.table.visitor.FilterValueTableRowVisitor;
import invariant.evaluator.apispec.APISpecFilter;
import invariant.evaluator.data.*;
import invariant.evaluator.openai.GPTModel;
import invariant.evaluator.openai.LLM;
import invariant.evaluator.openai.LLMClassifier;
import invariant.evaluator.openai.PromptGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Main1 {

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

    public static final DataSet DATA_SET = AmadeusHotel.UNIQUE_AMADEUS_HOTEL_OFFERS;
//    public static final DataSet DATA_SET = GitHubCreateRepo.UNIQUE_GITHUB_CREATE_REPO;
//    public static final DataSet DATA_SET = GitHubGetRepo.UNIQUE_GITHUB_GET_REPO;
//    public static final DataSet DATA_SET = Marvel.UNIQUE_MARVEL_GET_COMICS;
//    public static final DataSet DATA_SET = OMDbByIdOrTitle.UNIQUE_OMDB_ID_TITLE;
//    public static final DataSet DATA_SET = OMDbBySearch.UNIQUE_OMDB_SEARCH;
//    public static final DataSet DATA_SET = SpotifyCreatePlaylist.UNIQUE_SPOTIFY_CREATE_PLAYLIST;
//    public static final DataSet DATA_SET = SpotifyGetTracks.UNIQUE_SPOTIFY_GET_TRACKS;
//    public static final DataSet DATA_SET = SpotifyGetArtists.UNIQUE_SPOTIFY_GET_ARTISTS;
//    public static final DataSet DATA_SET = Yelp.UNIQUE_YELP_GET_BUSINESSES;
//    public static final DataSet DATA_SET = Youtube.UNIQUE_YOUTUBE_GET_VIDEOS;

    public static final LLM MODEL = GPTModel.GPT_3_5;
//    public static final LLM MODEL = GPTModel.GPT_4o_mini;

    public static final String YAML_INDENT = "  ";


    public static final String RESULTS_DIR = "results";


    public static void main(String[] args) {
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
            String prompt = PromptGenerator.generatePrompt(yamlString, invariant, invariantType, invariantDescription, outputVariablesPath);

//            System.out.println(prompt);
//            if(true){
//                return;
//            }

            LLMClassifier.InvariantClassificationResult response = LLMClassifier.classifyInvariant(MODEL, 0.2, prompt);

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
            String json = jsonResultString(id, invariantType, classification, response.getVerdict().name(), response.getPromptTokens(), response.getCompletionTokens(), response.getTotalTokens());
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
            resultsTable.addValue("promptTokens", nextRow, response.getPromptTokens());
            resultsTable.addValue("completionTokens", nextRow, response.getCompletionTokens());
            resultsTable.addValue("totalTokens", nextRow, response.getTotalTokens());



            invariantCount.getAndIncrement();
            System.out.println(invariantCount.get() + " #" + id + " : " + classification + " vs. " + response.getVerdict());
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

    public static String jsonResultString(int id, String invariantType, String classification, String gptVerdict, long promptTokens, long completionTokens, long totalTokens){
        String json = """
                    {
                        "id":%d,
                        "invariantType":"%s",
                        "classification":"%s",
                        "GPT_verdict":"%s",
                        "promptTokens":%d,
                        "completionTokens":%d,
                        "totalTokens":%d
                    }
                    """;

        return String.format(json, id, invariantType, classification, gptVerdict, promptTokens, completionTokens, totalTokens);
    }
}