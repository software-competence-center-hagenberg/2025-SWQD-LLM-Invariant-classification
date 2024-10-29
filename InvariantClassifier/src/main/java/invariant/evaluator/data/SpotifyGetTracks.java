package invariant.evaluator.data;

public class SpotifyGetTracks extends AgoraDataSet {

    public static final String OPEN_API_SPECIFICATION = "agora_data/apiSpecifications/Spotify/getAlbumTracks/swagger_albumTracks.yaml";

    public static final String SYSTEM_NAME = "Spotify";

    public static final String PATH = "/albums/{id}/tracks";

    public static final String OPERATION_NAME = "getAlbumTracks";

    public static final String RESPONSE_CODE = "200";



    public static final AgoraDataSet SPOTIFY_GET_TRACKS_50_ORIGINAL = new SpotifyGetTracks(50, true);
    public static final AgoraDataSet SPOTIFY_GET_TRACKS_50_MODIFIED = new SpotifyGetTracks(50, false);

    public static final AgoraDataSet SPOTIFY_GET_TRACKS_100_ORIGINAL = new SpotifyGetTracks(100, true);
    public static final AgoraDataSet SPOTIFY_GET_TRACKS_100_MODIFIED = new SpotifyGetTracks(100, false);
    public static final AgoraDataSet SPOTIFY_GET_TRACKS_500_ORIGINAL = new SpotifyGetTracks(500, true);
    public static final AgoraDataSet SPOTIFY_GET_TRACKS_500_MODIFIED = new SpotifyGetTracks(500, false);
    public static final AgoraDataSet SPOTIFY_GET_TRACKS_1000_ORIGINAL = new SpotifyGetTracks(1000, true);
    public static final AgoraDataSet SPOTIFY_GET_TRACKS_1000_MODIFIED = new SpotifyGetTracks(1000, false);
    public static final AgoraDataSet SPOTIFY_GET_TRACKS_10000_ORIGINAL = new SpotifyGetTracks(10000, true);
    public static final AgoraDataSet SPOTIFY_GET_TRACKS_10000_MODIFIED = new SpotifyGetTracks(10000, false);

    public static final AgoraDataSet[] ALL_DATA_SETS = new AgoraDataSet[]{
            SPOTIFY_GET_TRACKS_50_ORIGINAL,
            SPOTIFY_GET_TRACKS_50_MODIFIED,
            SPOTIFY_GET_TRACKS_100_ORIGINAL,
            SPOTIFY_GET_TRACKS_100_MODIFIED,
            SPOTIFY_GET_TRACKS_500_ORIGINAL,
            SPOTIFY_GET_TRACKS_500_MODIFIED,
            SPOTIFY_GET_TRACKS_1000_ORIGINAL,
            SPOTIFY_GET_TRACKS_1000_MODIFIED,
            SPOTIFY_GET_TRACKS_10000_ORIGINAL,
            SPOTIFY_GET_TRACKS_10000_MODIFIED
    };

    public static final MergedDataSet SPOTIFY_GET_TRACKS = new MergedDataSet(
            ALL_DATA_SETS
    );

    public static final UniqueInvariantAgoraDataSet SPOTIFY_GET_TRACKS_UNIQUE = new UniqueInvariantAgoraDataSet(
            ALL_DATA_SETS
    );

    public static final UniqueInvariantAgoraDataSet UNIQUE_SPOTIFY_GET_TRACKS = new UniqueInvariantAgoraDataSet(OPEN_API_SPECIFICATION, SYSTEM_NAME, PATH, OPERATION_NAME, HttpMethod.GET, RESPONSE_CODE);

    public SpotifyGetTracks(long numberOfApiCalls, boolean original) {
        super(OPEN_API_SPECIFICATION, SYSTEM_NAME, PATH, OPERATION_NAME, HttpMethod.GET, RESPONSE_CODE, numberOfApiCalls, original);
    }
}
