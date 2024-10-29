package invariant.evaluator.data;

public class SpotifyGetArtists extends AgoraDataSet {

    public static final String OPEN_API_SPECIFICATION = "agora_data/apiSpecifications/Spotify/getArtistAlbums/swagger_artistAlbums.yaml";

    public static final String SYSTEM_NAME = "Spotify";

    public static final String PATH = "/artists/{id}/albums";

    public static final String OPERATION_NAME = "getArtistAlbums";

    public static final String RESPONSE_CODE = "200";



    public static final AgoraDataSet SPOTIFY_GET_ARTISTS_50_ORIGINAL = new SpotifyGetArtists(50, true);
    public static final AgoraDataSet SPOTIFY_GET_ARTISTS_50_MODIFIED = new SpotifyGetArtists(50, false);

    public static final AgoraDataSet SPOTIFY_GET_ARTISTS_100_ORIGINAL = new SpotifyGetArtists(100, true);
    public static final AgoraDataSet SPOTIFY_GET_ARTISTS_100_MODIFIED = new SpotifyGetArtists(100, false);
    public static final AgoraDataSet SPOTIFY_GET_ARTISTS_500_ORIGINAL = new SpotifyGetArtists(500, true);
    public static final AgoraDataSet SPOTIFY_GET_ARTISTS_500_MODIFIED = new SpotifyGetArtists(500, false);
    public static final AgoraDataSet SPOTIFY_GET_ARTISTS_1000_ORIGINAL = new SpotifyGetArtists(1000, true);
    public static final AgoraDataSet SPOTIFY_GET_ARTISTS_1000_MODIFIED = new SpotifyGetArtists(1000, false);
    public static final AgoraDataSet SPOTIFY_GET_ARTISTS_10000_ORIGINAL = new SpotifyGetArtists(10000, true);
    public static final AgoraDataSet SPOTIFY_GET_ARTISTS_10000_MODIFIED = new SpotifyGetArtists(10000, false);

    public static final AgoraDataSet[] ALL_DATA_SETS = new AgoraDataSet[]{
            SPOTIFY_GET_ARTISTS_50_ORIGINAL,
            SPOTIFY_GET_ARTISTS_50_MODIFIED,
            SPOTIFY_GET_ARTISTS_100_ORIGINAL,
            SPOTIFY_GET_ARTISTS_100_MODIFIED,
            SPOTIFY_GET_ARTISTS_500_ORIGINAL,
            SPOTIFY_GET_ARTISTS_500_MODIFIED,
            SPOTIFY_GET_ARTISTS_1000_ORIGINAL,
            SPOTIFY_GET_ARTISTS_1000_MODIFIED,
            SPOTIFY_GET_ARTISTS_10000_ORIGINAL,
            SPOTIFY_GET_ARTISTS_10000_MODIFIED
    };

    public static final MergedDataSet SPOTIFY_GET_ARTISTS = new MergedDataSet(
            ALL_DATA_SETS
    );

    public static final UniqueInvariantAgoraDataSet SPOTIFY_GET_ARTISTS_UNIQUE = new UniqueInvariantAgoraDataSet(
            ALL_DATA_SETS
    );

    public static final UniqueInvariantAgoraDataSet UNIQUE_SPOTIFY_GET_ARTISTS = new UniqueInvariantAgoraDataSet(OPEN_API_SPECIFICATION, SYSTEM_NAME, PATH, OPERATION_NAME, HttpMethod.GET, RESPONSE_CODE);

    public SpotifyGetArtists(long numberOfApiCalls, boolean original) {
        super(OPEN_API_SPECIFICATION, SYSTEM_NAME, PATH, OPERATION_NAME, HttpMethod.GET, RESPONSE_CODE, numberOfApiCalls, original);
    }
}
