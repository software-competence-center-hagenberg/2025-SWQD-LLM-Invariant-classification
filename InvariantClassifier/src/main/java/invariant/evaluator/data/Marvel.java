package invariant.evaluator.data;

public class Marvel extends AgoraDataSet {

    public static final String OPEN_API_SPECIFICATION = "agora_data/apiSpecifications/Marvel/getComicById/swagger_getComicById.yaml";

    public static final String SYSTEM_NAME = "Marvel";

    public static final String PATH = "/v1/public/comics/{comicId}";

    public static final String OPERATION_NAME = "getComicById";

    public static final String RESPONSE_CODE = "200";



    public static final AgoraDataSet MARVEL_GET_COMICS_50_ORIGINAL = new Marvel(50, true);
    public static final AgoraDataSet MARVEL_GET_COMICS_50_MODIFIED = new Marvel(50, false);

    public static final AgoraDataSet MARVEL_GET_COMICS_100_ORIGINAL = new Marvel(100, true);
    public static final AgoraDataSet MARVEL_GET_COMICS_100_MODIFIED = new Marvel(100, false);
    public static final AgoraDataSet MARVEL_GET_COMICS_500_ORIGINAL = new Marvel(500, true);
    public static final AgoraDataSet MARVEL_GET_COMICS_500_MODIFIED = new Marvel(500, false);
    public static final AgoraDataSet MARVEL_GET_COMICS_1000_ORIGINAL = new Marvel(1000, true);
    public static final AgoraDataSet MARVEL_GET_COMICS_1000_MODIFIED = new Marvel(1000, false);
    public static final AgoraDataSet MARVEL_GET_COMICS_10000_ORIGINAL = new Marvel(10000, true);
    public static final AgoraDataSet MARVEL_GET_COMICS_10000_MODIFIED = new Marvel(10000, false);

    public static final AgoraDataSet[] ALL_DATA_SETS = new AgoraDataSet[]{
            MARVEL_GET_COMICS_50_ORIGINAL,
            MARVEL_GET_COMICS_50_MODIFIED,
            MARVEL_GET_COMICS_100_ORIGINAL,
            MARVEL_GET_COMICS_100_MODIFIED,
            MARVEL_GET_COMICS_500_ORIGINAL,
            MARVEL_GET_COMICS_500_MODIFIED,
            MARVEL_GET_COMICS_1000_ORIGINAL,
            MARVEL_GET_COMICS_1000_MODIFIED,
            MARVEL_GET_COMICS_10000_ORIGINAL,
            MARVEL_GET_COMICS_10000_MODIFIED
    };
    public static final MergedDataSet MARVEL_GET_COMICS = new MergedDataSet(
            ALL_DATA_SETS
    );

    public static final UniqueInvariantAgoraDataSet MARVEL_GET_COMICS_UNIQUE = new UniqueInvariantAgoraDataSet(
            ALL_DATA_SETS
    );

    public static final UniqueInvariantAgoraDataSet UNIQUE_MARVEL_GET_COMICS = new UniqueInvariantAgoraDataSet(OPEN_API_SPECIFICATION, SYSTEM_NAME, PATH, OPERATION_NAME, HttpMethod.GET, RESPONSE_CODE);

    public Marvel(long numberOfApiCalls, boolean original) {
        super(OPEN_API_SPECIFICATION, SYSTEM_NAME, PATH, OPERATION_NAME, HttpMethod.GET, RESPONSE_CODE, numberOfApiCalls, original);
    }
}
