package invariant.evaluator;

import at.sfischer.utils.table.ArrayValueTable;
import at.sfischer.utils.table.ValueTable;
import at.sfischer.utils.table.ValueTableRow;
import at.sfischer.utils.table.visitor.ValueTableRowVisitor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import invariant.evaluator.data.*;
import invariant.evaluator.openai.GPTModel;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DataAnalysis {

    public static class AggregatedResults{
        private int tp, tn, fp, fn;

        public AggregatedResults(int tp, int tn, int fp, int fn) {
            this.tp = tp;
            this.tn = tn;
            this.fp = fp;
            this.fn = fn;
        }
    }

    public static class AggregatedResultsDetailed{
        private List<Integer> tp, tn, fp, fn;

        public AggregatedResultsDetailed() {
            this.tp = new LinkedList<>();
            this.tn = new LinkedList<>();
            this.fp = new LinkedList<>();
            this.fn = new LinkedList<>();
        }
    }

    public static class InvariantResult{
        private final int id;
        private final String invariantType;
        private final String classification;
        private final String gptVerdict;
        private final float confidence;
        private final long promptTokens;
        private final long completionTokens;
        private final long totalTokens;
        private final long maxNumberOfOperations;

        public InvariantResult(int id, String invariantType, String classification, String gptVerdict, float confidence, long promptTokens, long completionTokens, long totalTokens, long maxNumberOfOperations) {
            this.id = id;
            this.invariantType = invariantType;
            this.classification = classification;
            this.gptVerdict = gptVerdict;
            this.confidence = confidence;
            this.promptTokens = promptTokens;
            this.completionTokens = completionTokens;
            this.totalTokens = totalTokens;
            this.maxNumberOfOperations = maxNumberOfOperations;
        }
    }

    public interface InvariantResultVisitor{
        void visitResult(InvariantResult result);
    }

    public enum InvariantCategory {
        FORMAT("Specific formats"),
        STRING_COMP("String comparisons"),
        ARITHMETIC_COMP("Arithmetic comparisons"),
        VALUE("Specific values"),
        ARRAY("Array properties");

        private final String categoryName;

        InvariantCategory(String categoryName) {
            this.categoryName = categoryName;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public static InvariantCategory getCategory(String invariantType){
            switch (invariantType){
                case "daikon.inv.unary.string.FixedLengthString":
                case "daikon.inv.unary.string.IsNumeric":
                case "daikon.inv.unary.string.IsUrl":
                case "daikon.inv.unary.string.dates.IsDateYYYYMMDD":
                case "daikon.inv.unary.string.dates.IsTimestampYYYYMMHHThhmmssmm":
                case "daikon.inv.unary.stringsequence.SequenceFixedLengthString":
                case "daikon.inv.unary.stringsequence.SequenceStringElementsAreUrl":
                    return FORMAT;
                case "daikon.inv.binary.twoString.StdString$SubString":
                case "daikon.inv.binary.twoString.StringEqual":
                    return STRING_COMP;
                case "daikon.inv.binary.twoScalar.FloatGreaterThan":
                case "daikon.inv.binary.twoScalar.FloatLessThan":
                case "daikon.inv.binary.twoScalar.FloatNonEqual":
                case "daikon.inv.binary.twoScalar.IntEqual":
                case "daikon.inv.binary.twoScalar.IntGreaterEqual":
                case "daikon.inv.binary.twoScalar.IntGreaterThan":
                case "daikon.inv.binary.twoScalar.IntLessEqual":
                case "daikon.inv.binary.twoScalar.IntLessThan":
                case "daikon.inv.binary.twoScalar.IntNonEqual":
                case "daikon.inv.binary.twoScalar.NumericInt$Divides":
                case "daikon.inv.binary.twoScalar.NumericInt$Square":
                case "daikon.inv.unary.scalar.NonZero":
                case "daikon.inv.unary.scalar.NonZeroFloat":
                    return ARITHMETIC_COMP;
                case "daikon.inv.unary.scalar.LowerBound":
                case "daikon.inv.unary.scalar.LowerBoundFloat":
                case "daikon.inv.unary.scalar.OneOfFloat":
                case "daikon.inv.unary.scalar.OneOfScalar":
                case "daikon.inv.unary.sequence.OneOfSequence":
                case "daikon.inv.unary.string.OneOfString":
                case "daikon.inv.unary.stringsequence.EltOneOfString":
                case "daikon.inv.unary.stringsequence.OneOfStringSequence":
                    return VALUE;
                case "daikon.inv.binary.sequenceScalar.SeqIntEqual":
                case "daikon.inv.binary.sequenceString.MemberString":
                case "daikon.inv.binary.twoSequence.SuperSet":
                case "daikon.inv.unary.sequence.EltNonZero":
                    return ARRAY;
                default:
                    return null;
            }
        }
    }

    public static final AbstractAgoraDataSet[] DATA_SETS = new AbstractAgoraDataSet[]{
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
    };

    public static final AbstractAgoraDataSet DATA_SET = AmadeusHotel.UNIQUE_AMADEUS_HOTEL_OFFERS;
//    public static final AbstractAgoraDataSet DATA_SET = GitHubCreateRepo.UNIQUE_GITHUB_CREATE_REPO;
//    public static final AbstractAgoraDataSet DATA_SET = GitHubGetRepo.UNIQUE_GITHUB_GET_REPO;
//    public static final AbstractAgoraDataSet DATA_SET = Marvel.UNIQUE_MARVEL_GET_COMICS;
//    public static final AbstractAgoraDataSet DATA_SET = OMDbByIdOrTitle.UNIQUE_OMDB_ID_TITLE;
//    public static final AbstractAgoraDataSet DATA_SET = OMDbBySearch.UNIQUE_OMDB_SEARCH;
//    public static final AbstractAgoraDataSet DATA_SET = SpotifyCreatePlaylist.UNIQUE_SPOTIFY_CREATE_PLAYLIST;
//    public static final AbstractAgoraDataSet DATA_SET = SpotifyGetTracks.UNIQUE_SPOTIFY_GET_TRACKS;
//    public static final AbstractAgoraDataSet DATA_SET = SpotifyGetArtists.UNIQUE_SPOTIFY_GET_ARTISTS;
//    public static final AbstractAgoraDataSet DATA_SET = Yelp.UNIQUE_YELP_GET_BUSINESSES;
//    public static final AbstractAgoraDataSet DATA_SET = Youtube.UNIQUE_YOUTUBE_GET_VIDEOS;

    public static final GPTModel MODEL = GPTModel.GPT_3_5;
//    public static final GPTModel MODEL = GPTModel.GPT_4o_mini;
//    public static final GPTModel MODEL = GPTModel.GPT_4o;

    public static void main(String[] args) throws IOException {

        File resultsDir = new File(Main1.RESULTS_DIR);
        String model = MODEL.getName();

        Map<String, AggregatedResults> perInvariantAggregatedResults = new HashMap<>();
        Map<String, List<InvariantResult>> invariantResults = new HashMap<>();

        { // Just for single system operation analysis.
//            String systemName = DATA_SET.getSystemName();
//            String operation = DATA_SET.getOperation();
//            computeResults(resultsDir, systemName, operation, model, perInvariantAggregatedResults, new LinkedList<>());
//            System.exit(0);
        }


        ValueTable perOperationAggregation = new ArrayValueTable();

        int tp = 0, tn = 0, fp = 0, fn = 0;

        int i = 0;
        for (AbstractAgoraDataSet dataSet : DATA_SETS) {
            String systemName = dataSet.getSystemName();
            String operation = dataSet.getOperation();

            List<InvariantResult> results = invariantResults.computeIfAbsent(systemName + "_" + operation, k -> new LinkedList<>());
            AggregatedResults aggregatedResults = computeResults(resultsDir, systemName, operation, model, perInvariantAggregatedResults, results);

            perOperationAggregation.addValue("System", i , systemName);
            perOperationAggregation.addValue("Operation", i , operation);
            perOperationAggregation.addValue("API-Operation", i , systemName + "-" + operation);
            perOperationAggregation.addValue("GPT_Model", i , MODEL.getSimpleName());

            perOperationAggregation.addValue("TP", i , aggregatedResults.tp);
            perOperationAggregation.addValue("TN", i , aggregatedResults.tn);
            perOperationAggregation.addValue("FP", i , aggregatedResults.fp);
            perOperationAggregation.addValue("FN", i , aggregatedResults.fn);
            perOperationAggregation.addValue("count", i , aggregatedResults.tp + aggregatedResults.tn + aggregatedResults.fp + aggregatedResults.fn);

            perOperationAggregation.addValue("accuracy", i , computeAccuracy(aggregatedResults.tp, aggregatedResults.tn, aggregatedResults.fp, aggregatedResults.fn));
            perOperationAggregation.addValue("precision", i , computePrecision(aggregatedResults.tp, aggregatedResults.fp));
            perOperationAggregation.addValue("recall", i , computeRecall(aggregatedResults.tp, aggregatedResults.fn));
            perOperationAggregation.addValue("f1score", i , computeF1Score(aggregatedResults.tp, aggregatedResults.fp, aggregatedResults.fn));

            tp += aggregatedResults.tp;
            tn += aggregatedResults.tn;
            fp += aggregatedResults.fp;
            fn += aggregatedResults.fn;

            i++;
        }

        System.out.println("\n----------------------------------------------");
        System.out.println("TP: " + tp);
        System.out.println("TN: " + tn);
        System.out.println("FP: " + fp);
        System.out.println("FN: " + fn);
        System.out.println("accuracy: " + computeAccuracy(tp, tn, fp, fn));
        System.out.println("precision: " + computePrecision(tp, fp));
        System.out.println("recall: " + computeRecall(tp, fn));
        System.out.println("f1score: " + computeF1Score(tp, fp, fn));
        System.out.println("TPR: " + computeTPR(tp, fn));
        System.out.println("FPR: " + computeFPR(tn, fp));
        System.out.println("all: " + (tp + tn + fp + fn));

        perOperationAggregation.printCSV(new File(resultsDir, model + ".csv"), ";");
        perOperationAggregation.print();

        i = 0;
        Map<InvariantCategory, AggregatedResults> perInvariantCategoryAggregatedResults = new LinkedHashMap<>();
        ValueTable perInvariantTypeAggregation = new ArrayValueTable(8, perInvariantAggregatedResults.size());
        for (Map.Entry<String, AggregatedResults> entry : perInvariantAggregatedResults.entrySet()) {

            InvariantCategory category = InvariantCategory.getCategory(entry.getKey());
            AggregatedResults categoryAggregation = perInvariantCategoryAggregatedResults.computeIfAbsent(category, k -> new AggregatedResults(0,0,0,0));

            perInvariantTypeAggregation.addValue("invariantType", i, entry.getKey());
            perInvariantTypeAggregation.addValue("GPT_Model", i , MODEL.getSimpleName());

            AggregatedResults aggregatedResults = entry.getValue();

            categoryAggregation.tp += aggregatedResults.tp;
            categoryAggregation.tn += aggregatedResults.tn;
            categoryAggregation.fp += aggregatedResults.fp;
            categoryAggregation.fn += aggregatedResults.fn;

            perInvariantTypeAggregation.addValue("TP", i , aggregatedResults.tp);
            perInvariantTypeAggregation.addValue("TN", i , aggregatedResults.tn);
            perInvariantTypeAggregation.addValue("FP", i , aggregatedResults.fp);
            perInvariantTypeAggregation.addValue("FN", i , aggregatedResults.fn);
            perInvariantTypeAggregation.addValue("count", i , aggregatedResults.tp + aggregatedResults.tn + aggregatedResults.fp + aggregatedResults.fn);

            perInvariantTypeAggregation.addValue("accuracy", i , computeAccuracy(aggregatedResults.tp, aggregatedResults.tn, aggregatedResults.fp, aggregatedResults.fn));
            perInvariantTypeAggregation.addValue("precision", i , computePrecision(aggregatedResults.tp, aggregatedResults.fp));
            perInvariantTypeAggregation.addValue("recall", i , computeRecall(aggregatedResults.tp, aggregatedResults.fn));
            perInvariantTypeAggregation.addValue("f1score", i , computeF1Score(aggregatedResults.tp, aggregatedResults.fp, aggregatedResults.fn));

            i++;
        }

        perInvariantTypeAggregation.printCSV(new File(resultsDir, model + "_InvariantTypes.csv"), ";");

        perInvariantTypeAggregation.addValue("invariantType", i, "SUM");
        perInvariantTypeAggregation.addValue("TP", i , tp);
        perInvariantTypeAggregation.addValue("TN", i , tn);
        perInvariantTypeAggregation.addValue("FP", i , fp);
        perInvariantTypeAggregation.addValue("FN", i , fn);
        perInvariantTypeAggregation.addValue("count", i , tp + tn + fp + fn);

        perInvariantTypeAggregation.addValue("accuracy", i , computeAccuracy(tp, tn, fp, fn));
        perInvariantTypeAggregation.addValue("precision", i , computePrecision(tp, fp));
        perInvariantTypeAggregation.addValue("recall", i , computeRecall(tp, fn));
        perInvariantTypeAggregation.addValue("f1score", i , computeF1Score(tp, fp, fn));

        perInvariantTypeAggregation.print();

        ValueTable perInvariantCategoryAggregation = new ArrayValueTable(8, perInvariantCategoryAggregatedResults.size());
        i = 0;
        for (Map.Entry<InvariantCategory, AggregatedResults> entry : perInvariantCategoryAggregatedResults.entrySet()) {
            perInvariantCategoryAggregation.addValue("invariantCategory", i, entry.getKey().getCategoryName());
            perInvariantCategoryAggregation.addValue("GPT_Model", i , MODEL.getSimpleName());

            AggregatedResults aggregatedResults = entry.getValue();

            perInvariantCategoryAggregation.addValue("TP", i , aggregatedResults.tp);
            perInvariantCategoryAggregation.addValue("TN", i , aggregatedResults.tn);
            perInvariantCategoryAggregation.addValue("FP", i , aggregatedResults.fp);
            perInvariantCategoryAggregation.addValue("FN", i , aggregatedResults.fn);
            perInvariantCategoryAggregation.addValue("count", i , aggregatedResults.tp + aggregatedResults.tn + aggregatedResults.fp + aggregatedResults.fn);

            perInvariantCategoryAggregation.addValue("accuracy", i , computeAccuracy(aggregatedResults.tp, aggregatedResults.tn, aggregatedResults.fp, aggregatedResults.fn));
            perInvariantCategoryAggregation.addValue("precision", i , computePrecision(aggregatedResults.tp, aggregatedResults.fp));
            perInvariantCategoryAggregation.addValue("recall", i , computeRecall(aggregatedResults.tp, aggregatedResults.fn));
            perInvariantCategoryAggregation.addValue("f1score", i , computeF1Score(aggregatedResults.tp, aggregatedResults.fp, aggregatedResults.fn));

            i++;
        }

        perInvariantCategoryAggregation.printCSV(new File(resultsDir, model + "_InvariantCategories.csv"), ";");
        perInvariantCategoryAggregation.print();

        // All invariant data in one big table.
        ValueTable allInvariantData = new ArrayValueTable(11, 1731);
        Map<String, Map<String, AggregatedResultsDetailed>> systemToInvariantTypeAggregation = new HashMap<>();
        Map<InvariantCategory, Map<String, AggregatedResultsDetailed>> systemToInvariantCategoryAggregation = new HashMap<>();
        i = 0;
        for (Map.Entry<String, List<InvariantResult>> entry : invariantResults.entrySet()) {
            String systemOperation = entry.getKey();
            for (InvariantResult result : entry.getValue()) {

                InvariantCategory category = InvariantCategory.getCategory(result.invariantType);

                allInvariantData.addValue("systemOperation", i, systemOperation);
                allInvariantData.addValue("ID", i, result.id);
                allInvariantData.addValue("invariantType", i, result.invariantType);
                allInvariantData.addValue("invariantCategory", i, category.getCategoryName());

                allInvariantData.addValue("systemOperation-InvariantType", i, systemOperation + "-" + result.invariantType);
                allInvariantData.addValue("systemOperation-InvariantCategory", i, systemOperation + "-" + category.getCategoryName());

                allInvariantData.addValue("classification", i, result.classification);
                allInvariantData.addValue("GPT_verdict", i, result.gptVerdict);

                Map<String, AggregatedResultsDetailed> invariantTypeAggregation = systemToInvariantTypeAggregation.computeIfAbsent(result.invariantType, k -> new HashMap<>());
                Map<String, AggregatedResultsDetailed> invariantCategoryAggregation = systemToInvariantCategoryAggregation.computeIfAbsent(category, k -> new HashMap<>());

                AggregatedResultsDetailed itAggregation = invariantTypeAggregation.computeIfAbsent(systemOperation, k -> new AggregatedResultsDetailed());
                AggregatedResultsDetailed icAggregation = invariantCategoryAggregation.computeIfAbsent(systemOperation, k -> new AggregatedResultsDetailed());

                if(result.classification.equals("TP") && result.gptVerdict.equals("TP")){
                    allInvariantData.addValue("result", i, "TP");
                    itAggregation.tp.add(result.id);
                    icAggregation.tp.add(result.id);
                } else if(result.classification.equals("FP") && result.gptVerdict.equals("TP")){
                    allInvariantData.addValue("result", i, "FP");
                    itAggregation.fp.add(result.id);
                    icAggregation.fp.add(result.id);
                } else if(result.classification.equals("TP") && result.gptVerdict.equals("FP")){
                    allInvariantData.addValue("result", i, "FN");
                    itAggregation.fn.add(result.id);
                    icAggregation.fn.add(result.id);
                } else if(result.classification.equals("FP") && result.gptVerdict.equals("FP")){
                    allInvariantData.addValue("result", i, "TN");
                    itAggregation.tn.add(result.id);
                    icAggregation.tn.add(result.id);
                } else if(result.gptVerdict.equals("INCONCLUSIVE")){
                    allInvariantData.addValue("result", i, "INCONCLUSIVE");
                }

                if(result.confidence >= 0){
                    allInvariantData.addValue("confidence", i, result.confidence);
                }
                allInvariantData.addValue("promptTokens", i, result.promptTokens);
                allInvariantData.addValue("completionTokens", i, result.completionTokens);
                allInvariantData.addValue("totalTokens", i, result.totalTokens);
                if(result.maxNumberOfOperations > 0){
                    allInvariantData.addValue("maxNumberOfOperations", i, result.maxNumberOfOperations);
                }

                allInvariantData.addValue("GPT_Model", i , MODEL.getSimpleName());

                i++;
            }
        }
        allInvariantData.printCSV(new File(resultsDir, model + "_allInvariants.csv"), ";");

        ValueTable systemToInvariantCategoryTable = new ArrayValueTable(8, 55);
        i = 0;
        for (Map.Entry<InvariantCategory, Map<String, AggregatedResultsDetailed>> entry : systemToInvariantCategoryAggregation.entrySet()) {
            InvariantCategory category = entry.getKey();

            for (Map.Entry<String, AggregatedResultsDetailed> resultsEntry : entry.getValue().entrySet()) {
                systemToInvariantCategoryTable.addValue("invariantCategory", i, category.getCategoryName());
                systemToInvariantCategoryTable.addValue("systemOperation", i, resultsEntry.getKey());
                systemToInvariantCategoryTable.addValue("GPT_Model", i , MODEL.getSimpleName());

                AggregatedResultsDetailed aggregatedResults = resultsEntry.getValue();

                systemToInvariantCategoryTable.addValue("TP", i , aggregatedResults.tp.size());
                systemToInvariantCategoryTable.addValue("TN", i , aggregatedResults.tn.size());
                systemToInvariantCategoryTable.addValue("FP", i , aggregatedResults.fp.size());
                systemToInvariantCategoryTable.addValue("FN", i , aggregatedResults.fn.size());
                systemToInvariantCategoryTable.addValue("count", i , aggregatedResults.tp.size() + aggregatedResults.tn.size() + aggregatedResults.fp.size() + aggregatedResults.fn.size());

                systemToInvariantCategoryTable.addValue("accuracy", i , computeAccuracy(aggregatedResults.tp.size(), aggregatedResults.tn.size(), aggregatedResults.fp.size(), aggregatedResults.fn.size()));
                systemToInvariantCategoryTable.addValue("precision", i , computePrecision(aggregatedResults.tp.size(), aggregatedResults.fp.size()));
                systemToInvariantCategoryTable.addValue("recall", i , computeRecall(aggregatedResults.tp.size(), aggregatedResults.fn.size()));
                systemToInvariantCategoryTable.addValue("f1score", i , computeF1Score(aggregatedResults.tp.size(), aggregatedResults.fp.size(), aggregatedResults.fn.size()));

                systemToInvariantCategoryTable.addValue("TP_IDs", i , aggregatedResults.tp.toString());
                systemToInvariantCategoryTable.addValue("TN_IDs", i , aggregatedResults.tn.toString());
                systemToInvariantCategoryTable.addValue("FP_IDs", i , aggregatedResults.fp.toString());
                systemToInvariantCategoryTable.addValue("FN_IDs", i , aggregatedResults.fn.toString());

                i++;
            }
        }
        systemToInvariantCategoryTable = systemToInvariantCategoryTable.trim();
        systemToInvariantCategoryTable.printCSV(new File(resultsDir, model + "_systemToInvariantCategoryTable.csv"), ";");


        ValueTable systemToInvariantTypeTable = new ArrayValueTable(8, 55);
        i = 0;
        for (Map.Entry<String, Map<String, AggregatedResultsDetailed>> entry : systemToInvariantTypeAggregation.entrySet()) {
            String type = entry.getKey();

            for (Map.Entry<String, AggregatedResultsDetailed> resultsEntry : entry.getValue().entrySet()) {
                systemToInvariantTypeTable.addValue("invariantType", i, type);
                systemToInvariantTypeTable.addValue("invariantCategory", i, InvariantCategory.getCategory(type).getCategoryName());
                systemToInvariantTypeTable.addValue("systemOperation", i, resultsEntry.getKey());
                systemToInvariantTypeTable.addValue("GPT_Model", i , MODEL.getSimpleName());

                AggregatedResultsDetailed aggregatedResults = resultsEntry.getValue();

                systemToInvariantTypeTable.addValue("TP", i , aggregatedResults.tp.size());
                systemToInvariantTypeTable.addValue("TN", i , aggregatedResults.tn.size());
                systemToInvariantTypeTable.addValue("FP", i , aggregatedResults.fp.size());
                systemToInvariantTypeTable.addValue("FN", i , aggregatedResults.fn.size());
                systemToInvariantTypeTable.addValue("count", i , aggregatedResults.tp.size() + aggregatedResults.tn.size() + aggregatedResults.fp.size() + aggregatedResults.fn.size());

                systemToInvariantTypeTable.addValue("accuracy", i , computeAccuracy(aggregatedResults.tp.size(), aggregatedResults.tn.size(), aggregatedResults.fp.size(), aggregatedResults.fn.size()));
                systemToInvariantTypeTable.addValue("precision", i , computePrecision(aggregatedResults.tp.size(), aggregatedResults.fp.size()));
                systemToInvariantTypeTable.addValue("recall", i , computeRecall(aggregatedResults.tp.size(), aggregatedResults.fn.size()));
                systemToInvariantTypeTable.addValue("f1score", i , computeF1Score(aggregatedResults.tp.size(), aggregatedResults.fp.size(), aggregatedResults.fn.size()));

                systemToInvariantTypeTable.addValue("TP_IDs", i , aggregatedResults.tp.toString());
                systemToInvariantTypeTable.addValue("TN_IDs", i , aggregatedResults.tn.toString());
                systemToInvariantTypeTable.addValue("FP_IDs", i , aggregatedResults.fp.toString());
                systemToInvariantTypeTable.addValue("FN_IDs", i , aggregatedResults.fn.toString());

                i++;
            }
        }
        systemToInvariantTypeTable = systemToInvariantTypeTable.trim();
        systemToInvariantTypeTable.printCSV(new File(resultsDir, model + "_systemToInvariantTypeTable.csv"), ";");


        ValueTable rocData = new ArrayValueTable(3, 0);
        i = 0;
        // Compute ROC curve over all invariants
        for(double d = 1; d >= 0; d = d - 0.01) {
            tp = 0;
            tn = 0;
            fp = 0;
            fn = 0;
            for (Map.Entry<String, List<InvariantResult>> entry : invariantResults.entrySet()) {
                for (InvariantResult result : entry.getValue()) {
                    if (result.classification.equals("TP") && result.gptVerdict.equals("TP")) {
                        if(result.confidence >= d){
                            tp++;
                        } else {
                            fn++;
                        }
                    } else if (result.classification.equals("FP") && result.gptVerdict.equals("TP")) {
                        if(result.confidence >= d){
                            fp++;
                        } else {
                            tn++;
                        }
                    } else if (result.classification.equals("TP") && result.gptVerdict.equals("FP")) {
                        fn++;
                    } else if (result.classification.equals("FP") && result.gptVerdict.equals("FP")) {
                        tn++;
                    }
                }
            }

            double tpr = computeTPR(tp, fn);
            double fpr = computeFPR(tn, fp);

            rocData.addValue("confidenceThreshold", i, d);
            rocData.addValue("tpr", i, tpr);
            rocData.addValue("fpr", i, fpr);
            rocData.addValue("accuracy", i , computeAccuracy(tp, tn, fp, fn));
            rocData.addValue("GPT_Model", i , MODEL.getSimpleName());

            i++;
        }

        rocData.printCSV(new File(resultsDir, model + "_roc.csv"), ";");
    }

    public static AggregatedResults computeResults(File resultsDir, String systemName, String operation, String model, final Map<String, AggregatedResults> perInvariantAggregatedResults, final List<InvariantResult> results) throws IOException {
        File systemDir = new File(resultsDir, systemName);
        File operationDir = new File(systemDir, operation);
        File modelDir = new File(operationDir, model);

        File resFile = new File(modelDir, "results.csv");
        if(!resFile.exists()) {
            File[] files = modelDir.listFiles((dir, name) -> {
                if(!isNumeric(name)){
                    return false;
                }

                return new File(dir, name).isDirectory();
            });
            // Sort invariant results by id.
            Arrays.sort(files, (o1, o2) -> {
                int i1 = Integer.parseInt(o1.getName());
                int i2 = Integer.parseInt(o2.getName());
                return i1 - i2;
            });

            ValueTable resultsTable = new ArrayValueTable(5, files.length);
            int nextRow = 0;
            for (File resDir : files) {
                InvariantResult result = parseJson(new File(resDir, "result.json"));

                resultsTable.addValue("ID", nextRow, result.id);
                resultsTable.addValue("invariantType", nextRow, result.invariantType);
                resultsTable.addValue("classification", nextRow, result.classification);
                resultsTable.addValue("GPT_verdict", nextRow, result.gptVerdict);
                if(result.confidence >= 0){
                    resultsTable.addValue("confidence", nextRow, result.confidence);
                }
                resultsTable.addValue("promptTokens", nextRow, result.promptTokens);
                resultsTable.addValue("completionTokens", nextRow, result.completionTokens);
                resultsTable.addValue("totalTokens", nextRow, result.totalTokens);
                if(result.maxNumberOfOperations > 0){
                    resultsTable.addValue("maxNumberOfOperations", nextRow, result.maxNumberOfOperations);
                }

                nextRow++;
            }

            resultsTable.printCSV(resFile, ";");
        }


        AtomicInteger tp = new AtomicInteger();
        AtomicInteger fp = new AtomicInteger();
        AtomicInteger fn = new AtomicInteger();
        AtomicInteger tn = new AtomicInteger();

        List<Integer> fpIds = new LinkedList<>();
        List<Integer> fnIds = new LinkedList<>();

        List<Integer> inconclusive = new LinkedList<>();

        readResultsCSV(resFile, result -> {
            results.add(result);

            // Recreate json result files from table.
//            File idDir = new File(modelDir, String.valueOf(result.id));
//            File resultFile = new File(idDir, "result.json");
//            String json = Main.jsonResultString(result.id, result.invariantType, result.classification, result.gptVerdict, result.promptTokens, result.completionTokens, result.totalTokens);
//            try {
//                PrintStream resultStream = new PrintStream(resultFile);
//                resultStream.println(json);
//                resultStream.flush();
//                resultStream.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            AggregatedResults aggregatedResults = perInvariantAggregatedResults.computeIfAbsent(result.invariantType, k -> new AggregatedResults(0,0,0,0));
            if(result.classification.equals("TP") && result.gptVerdict.equals("TP")){
                tp.getAndIncrement();
                aggregatedResults.tp++;
            } else if(result.classification.equals("FP") && result.gptVerdict.equals("TP")){
                fp.getAndIncrement();
                fpIds.add(result.id);
                aggregatedResults.fp++;
            } else if(result.classification.equals("TP") && result.gptVerdict.equals("FP")){
                fn.getAndIncrement();
                fnIds.add(result.id);
                aggregatedResults.fn++;
            } else if(result.classification.equals("FP") && result.gptVerdict.equals("FP")){
                tn.getAndIncrement();
                aggregatedResults.tn++;
            } else if(result.gptVerdict.equals("INCONCLUSIVE")){
                inconclusive.add(result.id);
            }
        });

        File summaryFile = new File(modelDir, "summary.txt");
        PrintStream summaryOut = new PrintStream(summaryFile);


        System.out.println("--------------------------------------------");

        System.out.println("System: " + systemName);
        System.out.println("Operation: " + operation);

        System.out.println("TP: " + tp.get());
        System.out.println("FP: " + fp.get());
        System.out.println("FN: " + fn.get());
        System.out.println("TN: " + tn.get());

        System.out.println("P: " + (tp.get() + fn.get()));
        System.out.println("N: " + (tn.get() + fp.get()));
        System.out.println("PP: " + (tp.get() + fp.get()));
        System.out.println("PN: " + (tn.get() + fn.get()));
        System.out.println("All: " + (tp.get() + fp.get() + tn.get() + fn.get() + inconclusive.size()));

        summaryOut.println("TP: " + tp.get());
        summaryOut.println("FP: " + fp.get());
        summaryOut.println("FN: " + fn.get());
        summaryOut.println("TN: " + tn.get());
        summaryOut.println();
        summaryOut.println("P: " + (tp.get() + fn.get()));
        summaryOut.println("N: " + (tn.get() + fp.get()));
        summaryOut.println("PP: " + (tp.get() + fp.get()));
        summaryOut.println("PN: " + (tn.get() + fn.get()));
        summaryOut.println("All: " + (tp.get() + fp.get() + tn.get() + fn.get() + inconclusive.size()));
        summaryOut.println();

        double accuracy = computeAccuracy(tp.get(), tn.get(), fp.get(), fn.get());
        double precision = computePrecision(tp.get(), fp.get());
        double recall = computeRecall(tp.get(), fn.get());
        double f1score = computeF1Score(tp.get(), fp.get(), fn.get());

        System.out.println("accuracy: " + accuracy);
        System.out.println("precision: " + precision);
        System.out.println("recall: " + recall);
        System.out.println("f1score: " + f1score);

        System.out.println("FPs: " + fpIds);
        System.out.println("FNs: " + fnIds);
        System.out.println("INCONCLUSIVE: " + inconclusive);

        summaryOut.println("accuracy: " + accuracy);
        summaryOut.println("precision: " + precision);
        summaryOut.println("recall: " + recall);
        summaryOut.println();
        summaryOut.println("FPs: " + fpIds);
        summaryOut.println("FNs: " + fnIds);
        summaryOut.println("INCONCLUSIVE: " + inconclusive);

        summaryOut.flush();
        summaryOut.close();

        return new AggregatedResults(tp.get(), tn.get(), fp.get(), fn.get());
    }

    public static double computeAccuracy(int tp, int tn, int fp, int fn){
        return (double)(tp + tn) / (double)(tp + tn + fp + fn);
    }

    public static double computePrecision(int tp, int fp){
        return (double)(tp) / (double)(tp + fp);
    }

    public static double computeRecall(int tp, int fn){
        return (double)(tp) / (double)(tp + fn);
    }

    public static double computeF1Score(int tp, int fp, int fn){
        return (double)(2 * tp) / (double)((2 * tp) + fp + fn);
    }

    public static double computeTPR(int tp, int fn){
        return computeRecall(tp, fn);
    }

    public static double computeFPR(int tn, int fp){
        return (double)(fp) / (double)(fp + tn);
    }

    public static InvariantResult parseJson(File resultFile){
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(resultFile);

            int id = node.get("id").asInt();
            String invariantType = node.get("invariantType").asText();
            String classification = node.get("classification").asText();
            String gptVerdict = node.get("GPT_verdict").asText();
            float confidence;
            if(node.has("confidence")){
                confidence =  (float)node.get("confidence").asDouble();
            } else {
                confidence = -1;
            }
            long promptTokens = node.get("promptTokens").asLong();
            long completionTokens = node.get("completionTokens").asLong();
            long totalTokens = node.get("totalTokens").asLong();
            long maxNumberOfOperations;
            if(node.has("maxNumberOfOperations")){
                maxNumberOfOperations =  node.get("maxNumberOfOperations").asLong();
            } else {
                maxNumberOfOperations = -1;
            }

            return new InvariantResult(id, invariantType, classification, gptVerdict, confidence, promptTokens, completionTokens, totalTokens, maxNumberOfOperations);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void readResultsCSV(File resFile, InvariantResultVisitor visitor){
        try {
            ValueTable results = ValueTable.parseCSV(resFile, ";", true, false);
            results.visitRows(new ValueTableRowVisitor() {
                @Override
                public void visit(ValueTableRow row) {
                    int id = (int) ((double)row.getValue("ID"));
                    String invariantType = row.getValue("invariantType");
                    String classification = row.getValue("classification");
                    String gptVerdict = row.getValue("GPT_verdict");
                    float confidence;
                    try {
                        confidence = (float) ((double)row.getValue("confidence"));
                    } catch (Exception e){
                        confidence = -1;
                    }
                    long promptTokens = (long) ((double)row.getValue("promptTokens"));
                    long completionTokens = (long) ((double)row.getValue("completionTokens"));
                    long totalTokens = (long) ((double)row.getValue("totalTokens"));
                    long maxNumberOfOperations;
                    try {
                        maxNumberOfOperations = (long) ((double)row.getValue("maxNumberOfOperations"));
                    } catch (Exception e){
                        maxNumberOfOperations = -1;
                    }

                    visitor.visitResult(new InvariantResult(id, invariantType, classification, gptVerdict, confidence, promptTokens, completionTokens, totalTokens, maxNumberOfOperations));
                }
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
