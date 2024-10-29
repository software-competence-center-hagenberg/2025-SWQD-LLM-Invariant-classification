package invariant.evaluator.data;

public class Youtube extends AgoraDataSet {

    public static final String OPEN_API_SPECIFICATION = "agora_data/apiSpecifications/YouTube/getVideos/openapi_getVideos.yaml";

    public static final String SYSTEM_NAME = "YouTube";

    public static final String PATH = "/youtube/v3/videos";

    public static final String OPERATION_NAME = "listVideos";

    public static final String RESPONSE_CODE = "200";



    public static final AgoraDataSet YOUTUBE_GET_VIDEOS_50_ORIGINAL = new Youtube(50, true);
    public static final AgoraDataSet YOUTUBE_GET_VIDEOS_50_MODIFIED = new Youtube(50, false);

    public static final AgoraDataSet YOUTUBE_GET_VIDEOS_100_ORIGINAL = new Youtube(100, true);
    public static final AgoraDataSet YOUTUBE_GET_VIDEOS_100_MODIFIED = new Youtube(100, false);
    public static final AgoraDataSet YOUTUBE_GET_VIDEOS_500_ORIGINAL = new Youtube(500, true);
    public static final AgoraDataSet YOUTUBE_GET_VIDEOS_500_MODIFIED = new Youtube(500, false);
    public static final AgoraDataSet YOUTUBE_GET_VIDEOS_1000_ORIGINAL = new Youtube(1000, true);
    public static final AgoraDataSet YOUTUBE_GET_VIDEOS_1000_MODIFIED = new Youtube(1000, false);
    public static final AgoraDataSet YOUTUBE_GET_VIDEOS_10000_ORIGINAL = new Youtube(10000, true);
    public static final AgoraDataSet YOUTUBE_GET_VIDEOS_10000_MODIFIED = new Youtube(10000, false);

    public static final AgoraDataSet[] ALL_DATA_SETS = new AgoraDataSet[]{
            YOUTUBE_GET_VIDEOS_50_ORIGINAL,
            YOUTUBE_GET_VIDEOS_50_MODIFIED,
            YOUTUBE_GET_VIDEOS_100_ORIGINAL,
            YOUTUBE_GET_VIDEOS_100_MODIFIED,
            YOUTUBE_GET_VIDEOS_500_ORIGINAL,
            YOUTUBE_GET_VIDEOS_500_MODIFIED,
            YOUTUBE_GET_VIDEOS_1000_ORIGINAL,
            YOUTUBE_GET_VIDEOS_1000_MODIFIED,
            YOUTUBE_GET_VIDEOS_10000_ORIGINAL,
            YOUTUBE_GET_VIDEOS_10000_MODIFIED
    };

    public static final MergedDataSet YOUTUBE_GET_VIDEOS = new MergedDataSet(
            ALL_DATA_SETS
    );

    public static final UniqueInvariantAgoraDataSet YOUTUBE_GET_VIDEOS_UNIQUE = new UniqueInvariantAgoraDataSet(
            ALL_DATA_SETS
    );

    public static final UniqueInvariantAgoraDataSet UNIQUE_YOUTUBE_GET_VIDEOS = new UniqueInvariantAgoraDataSet(OPEN_API_SPECIFICATION, SYSTEM_NAME, PATH, OPERATION_NAME, HttpMethod.GET, RESPONSE_CODE);

    public Youtube(long numberOfApiCalls, boolean original) {
        super(OPEN_API_SPECIFICATION, SYSTEM_NAME, PATH, OPERATION_NAME, HttpMethod.GET, RESPONSE_CODE, numberOfApiCalls, original);
    }
}
