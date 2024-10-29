package invariant.evaluator.data;

public class OMDbBySearch extends AgoraDataSet {

    public static final String OPEN_API_SPECIFICATION = "agora_data/apiSpecifications/OMDb/BySearch/swagger_bySearch.yaml";

    public static final String SYSTEM_NAME = "OMDb";

    public static final String PATH = "/";

    public static final String OPERATION_NAME = "BySearch";

    public static final String RESPONSE_CODE = "200";



    public static final AgoraDataSet OMDB_SEARCH_50_ORIGINAL = new OMDbBySearch(50, true);
    public static final AgoraDataSet OMDB_SEARCH_50_MODIFIED = new OMDbBySearch(50, false);

    public static final AgoraDataSet OMDB_SEARCH_100_ORIGINAL = new OMDbBySearch(100, true);
    public static final AgoraDataSet OMDB_SEARCH_100_MODIFIED = new OMDbBySearch(100, false);
    public static final AgoraDataSet OMDB_SEARCH_500_ORIGINAL = new OMDbBySearch(500, true);
    public static final AgoraDataSet OMDB_SEARCH_500_MODIFIED = new OMDbBySearch(500, false);
    public static final AgoraDataSet OMDB_SEARCH_1000_ORIGINAL = new OMDbBySearch(1000, true);
    public static final AgoraDataSet OMDB_SEARCH_1000_MODIFIED = new OMDbBySearch(1000, false);
    public static final AgoraDataSet OMDB_SEARCH_10000_ORIGINAL = new OMDbBySearch(10000, true);
    public static final AgoraDataSet OMDB_SEARCH_10000_MODIFIED = new OMDbBySearch(10000, false);

    public static final AgoraDataSet[] ALL_DATA_SETS = new AgoraDataSet[]{
            OMDB_SEARCH_50_ORIGINAL,
            OMDB_SEARCH_50_MODIFIED,
            OMDB_SEARCH_100_ORIGINAL,
            OMDB_SEARCH_100_MODIFIED,
            OMDB_SEARCH_500_ORIGINAL,
            OMDB_SEARCH_500_MODIFIED,
            OMDB_SEARCH_1000_ORIGINAL,
            OMDB_SEARCH_1000_MODIFIED,
            OMDB_SEARCH_10000_ORIGINAL,
            OMDB_SEARCH_10000_MODIFIED
    };

    public static final MergedDataSet OMDB_SEARCH = new MergedDataSet(
            ALL_DATA_SETS
    );

    public static final UniqueInvariantAgoraDataSet OMDB_SEARCH_UNIQUE = new UniqueInvariantAgoraDataSet(
            ALL_DATA_SETS
    );

    public static final UniqueInvariantAgoraDataSet UNIQUE_OMDB_SEARCH = new UniqueInvariantAgoraDataSet(OPEN_API_SPECIFICATION, SYSTEM_NAME, PATH, OPERATION_NAME, HttpMethod.GET, RESPONSE_CODE);

    public OMDbBySearch(long numberOfApiCalls, boolean original) {
        super(OPEN_API_SPECIFICATION, SYSTEM_NAME, PATH, OPERATION_NAME, HttpMethod.GET, RESPONSE_CODE, numberOfApiCalls, original);
    }
}
