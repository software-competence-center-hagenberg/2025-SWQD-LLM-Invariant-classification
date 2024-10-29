package invariant.evaluator.data;

public class SpotifyCreatePlaylist extends AgoraDataSet {

    public static final String OPEN_API_SPECIFICATION = "agora_data/apiSpecifications/Spotify/createPlaylist/swagger_createPlaylist.yaml";

    public static final String SYSTEM_NAME = "Spotify";

    public static final String PATH = "/users/{user_id}/playlists";

    public static final String OPERATION_NAME = "createPlaylist";

    public static final String RESPONSE_CODE = "201";



    public static final AgoraDataSet SPOTIFY_CREATE_PLAYLIST_50_ORIGINAL = new SpotifyCreatePlaylist(50, true);
    public static final AgoraDataSet SPOTIFY_CREATE_PLAYLIST_50_MODIFIED = new SpotifyCreatePlaylist(50, false);

    public static final AgoraDataSet SPOTIFY_CREATE_PLAYLIST_100_ORIGINAL = new SpotifyCreatePlaylist(100, true);
    public static final AgoraDataSet SPOTIFY_CREATE_PLAYLIST_100_MODIFIED = new SpotifyCreatePlaylist(100, false);
    public static final AgoraDataSet SPOTIFY_CREATE_PLAYLIST_500_ORIGINAL = new SpotifyCreatePlaylist(500, true);
    public static final AgoraDataSet SPOTIFY_CREATE_PLAYLIST_500_MODIFIED = new SpotifyCreatePlaylist(500, false);
    public static final AgoraDataSet SPOTIFY_CREATE_PLAYLIST_1000_ORIGINAL = new SpotifyCreatePlaylist(1000, true);
    public static final AgoraDataSet SPOTIFY_CREATE_PLAYLIST_1000_MODIFIED = new SpotifyCreatePlaylist(1000, false);
    public static final AgoraDataSet SPOTIFY_CREATE_PLAYLIST_10000_ORIGINAL = new SpotifyCreatePlaylist(10000, true);
    public static final AgoraDataSet SPOTIFY_CREATE_PLAYLIST_10000_MODIFIED = new SpotifyCreatePlaylist(10000, false);

    public static final AgoraDataSet[] ALL_DATA_SETS = new AgoraDataSet[]{
            SPOTIFY_CREATE_PLAYLIST_50_ORIGINAL,
            SPOTIFY_CREATE_PLAYLIST_50_MODIFIED,
            SPOTIFY_CREATE_PLAYLIST_100_ORIGINAL,
            SPOTIFY_CREATE_PLAYLIST_100_MODIFIED,
            SPOTIFY_CREATE_PLAYLIST_500_ORIGINAL,
            SPOTIFY_CREATE_PLAYLIST_500_MODIFIED,
            SPOTIFY_CREATE_PLAYLIST_1000_ORIGINAL,
            SPOTIFY_CREATE_PLAYLIST_1000_MODIFIED,
            SPOTIFY_CREATE_PLAYLIST_10000_ORIGINAL,
            SPOTIFY_CREATE_PLAYLIST_10000_MODIFIED
    };

    public static final MergedDataSet SPOTIFY_CREATE_PLAYLIST = new MergedDataSet(
            ALL_DATA_SETS
    );

    public static final UniqueInvariantAgoraDataSet SPOTIFY_CREATE_PLAYLIST_UNIQUE = new UniqueInvariantAgoraDataSet(
            ALL_DATA_SETS
    );

    public static final UniqueInvariantAgoraDataSet UNIQUE_SPOTIFY_CREATE_PLAYLIST = new UniqueInvariantAgoraDataSet(OPEN_API_SPECIFICATION, SYSTEM_NAME, PATH, OPERATION_NAME, HttpMethod.POST, RESPONSE_CODE);

    public SpotifyCreatePlaylist(long numberOfApiCalls, boolean original) {
        super(OPEN_API_SPECIFICATION, SYSTEM_NAME, PATH, OPERATION_NAME, HttpMethod.POST, RESPONSE_CODE, numberOfApiCalls, original);
    }
}
