package invariant.evaluator.data;

public class OMDbByIdOrTitle extends AgoraDataSet {

    public static final String OPEN_API_SPECIFICATION = "agora_data/apiSpecifications/OMDb/ByIdOrTitle/swagger_byIdOrTitle.yaml";

    public static final String SYSTEM_NAME = "OMDb";

    public static final String PATH = "/";

    public static final String OPERATION_NAME = "ByIdOrTitle";

    public static final String RESPONSE_CODE = "200";



    public static final AgoraDataSet OMDB_ID_TITLE_50_ORIGINAL = new OMDbByIdOrTitle(50, true);
    public static final AgoraDataSet OMDB_ID_TITLE_50_MODIFIED = new OMDbByIdOrTitle(50, false);

    public static final AgoraDataSet OMDB_ID_TITLE_100_ORIGINAL = new OMDbByIdOrTitle(100, true);
    public static final AgoraDataSet OMDB_ID_TITLE_100_MODIFIED = new OMDbByIdOrTitle(100, false);
    public static final AgoraDataSet OMDB_ID_TITLE_500_ORIGINAL = new OMDbByIdOrTitle(500, true);
    public static final AgoraDataSet OMDB_ID_TITLE_500_MODIFIED = new OMDbByIdOrTitle(500, false);
    public static final AgoraDataSet OMDB_ID_TITLE_1000_ORIGINAL = new OMDbByIdOrTitle(1000, true);
    public static final AgoraDataSet OMDB_ID_TITLE_1000_MODIFIED = new OMDbByIdOrTitle(1000, false);
    public static final AgoraDataSet OMDB_ID_TITLE_10000_ORIGINAL = new OMDbByIdOrTitle(10000, true);
    public static final AgoraDataSet OMDB_ID_TITLE_10000_MODIFIED = new OMDbByIdOrTitle(10000, false);

    public static final AgoraDataSet[] ALL_DATA_SETS = new AgoraDataSet[]{
            OMDB_ID_TITLE_50_ORIGINAL,
            OMDB_ID_TITLE_50_MODIFIED,
            OMDB_ID_TITLE_100_ORIGINAL,
            OMDB_ID_TITLE_100_MODIFIED,
            OMDB_ID_TITLE_500_ORIGINAL,
            OMDB_ID_TITLE_500_MODIFIED,
            OMDB_ID_TITLE_1000_ORIGINAL,
            OMDB_ID_TITLE_1000_MODIFIED,
            OMDB_ID_TITLE_10000_ORIGINAL,
            OMDB_ID_TITLE_10000_MODIFIED
    };

    public static final MergedDataSet OMDB_ID_TITLE = new MergedDataSet(
            ALL_DATA_SETS
    );

    public static final UniqueInvariantAgoraDataSet OMDB_ID_TITLE_UNIQUE = new UniqueInvariantAgoraDataSet(
            ALL_DATA_SETS
    );

    public static final UniqueInvariantAgoraDataSet UNIQUE_OMDB_ID_TITLE = new UniqueInvariantAgoraDataSet(OPEN_API_SPECIFICATION, SYSTEM_NAME, PATH, OPERATION_NAME, HttpMethod.GET, RESPONSE_CODE);

    public OMDbByIdOrTitle(long numberOfApiCalls, boolean original) {
        super(OPEN_API_SPECIFICATION, SYSTEM_NAME, PATH, OPERATION_NAME, HttpMethod.GET, RESPONSE_CODE, numberOfApiCalls, original);
    }
}
