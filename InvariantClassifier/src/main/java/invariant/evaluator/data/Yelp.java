package invariant.evaluator.data;

public class Yelp extends AgoraDataSet {

    public static final String OPEN_API_SPECIFICATION = "agora_data/apiSpecifications/Yelp/swagger.yaml";

    public static final String SYSTEM_NAME = "Yelp";

    public static final String PATH = "/businesses/search";

    public static final String OPERATION_NAME = "getBusinesses";

    public static final String RESPONSE_CODE = "200";



    public static final AgoraDataSet YELP_GET_BUSINESSES_50_ORIGINAL = new Yelp(50, true);
    public static final AgoraDataSet YELP_GET_BUSINESSES_50_MODIFIED = new Yelp(50, false);

    public static final AgoraDataSet YELP_GET_BUSINESSES_100_ORIGINAL = new Yelp(100, true);
    public static final AgoraDataSet YELP_GET_BUSINESSES_100_MODIFIED = new Yelp(100, false);
    public static final AgoraDataSet YELP_GET_BUSINESSES_500_ORIGINAL = new Yelp(500, true);
    public static final AgoraDataSet YELP_GET_BUSINESSES_500_MODIFIED = new Yelp(500, false);
    public static final AgoraDataSet YELP_GET_BUSINESSES_1000_ORIGINAL = new Yelp(1000, true);
    public static final AgoraDataSet YELP_GET_BUSINESSES_1000_MODIFIED = new Yelp(1000, false);
    public static final AgoraDataSet YELP_GET_BUSINESSES_10000_ORIGINAL = new Yelp(10000, true);
    public static final AgoraDataSet YELP_GET_BUSINESSES_10000_MODIFIED = new Yelp(10000, false);

    public static final AgoraDataSet[] ALL_DATA_SETS = new AgoraDataSet[]{
            YELP_GET_BUSINESSES_50_ORIGINAL,
            YELP_GET_BUSINESSES_50_MODIFIED,
            YELP_GET_BUSINESSES_100_ORIGINAL,
            YELP_GET_BUSINESSES_100_MODIFIED,
            YELP_GET_BUSINESSES_500_ORIGINAL,
            YELP_GET_BUSINESSES_500_MODIFIED,
            YELP_GET_BUSINESSES_1000_ORIGINAL,
            YELP_GET_BUSINESSES_1000_MODIFIED,
            YELP_GET_BUSINESSES_10000_ORIGINAL,
            YELP_GET_BUSINESSES_10000_MODIFIED
    };

    public static final MergedDataSet YELP_GET_BUSINESSES = new MergedDataSet(
            ALL_DATA_SETS
    );

    public static final UniqueInvariantAgoraDataSet YELP_GET_BUSINESSES_UNIQUE = new UniqueInvariantAgoraDataSet(
            ALL_DATA_SETS
    );

    public static final UniqueInvariantAgoraDataSet UNIQUE_YELP_GET_BUSINESSES = new UniqueInvariantAgoraDataSet(OPEN_API_SPECIFICATION, SYSTEM_NAME, PATH, OPERATION_NAME, HttpMethod.GET, RESPONSE_CODE);

    public Yelp(long numberOfApiCalls, boolean original) {
        super(OPEN_API_SPECIFICATION, SYSTEM_NAME, PATH, OPERATION_NAME, HttpMethod.GET, RESPONSE_CODE, numberOfApiCalls, original);
    }
}
