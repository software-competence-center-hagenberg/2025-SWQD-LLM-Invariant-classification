package invariant.evaluator.data;

public class GitHubGetRepo extends AgoraDataSet {

    public static final String OPEN_API_SPECIFICATION = "agora_data/apiSpecifications/GitHub/getOrganizationRepositories/swagger_getOrganizationRepositories.yaml";

    public static final String SYSTEM_NAME = "GitHub";

    public static final String PATH = "/orgs/{org}/repos";

    public static final String OPERATION_NAME = "getOrganizationRepositories";

    public static final String RESPONSE_CODE = "200";



    public static final AgoraDataSet GITHUB_GET_REPO_50_ORIGINAL = new GitHubGetRepo(50, true);
    public static final AgoraDataSet GITHUB_GET_REPO_50_MODIFIED = new GitHubGetRepo(50, false);

    public static final AgoraDataSet GITHUB_GET_REPO_100_ORIGINAL = new GitHubGetRepo(100, true);
    public static final AgoraDataSet GITHUB_GET_REPO_100_MODIFIED = new GitHubGetRepo(100, false);
    public static final AgoraDataSet GITHUB_GET_REPO_500_ORIGINAL = new GitHubGetRepo(500, true);
    public static final AgoraDataSet GITHUB_GET_REPO_500_MODIFIED = new GitHubGetRepo(500, false);
    public static final AgoraDataSet GITHUB_GET_REPO_1000_ORIGINAL = new GitHubGetRepo(1000, true);
    public static final AgoraDataSet GITHUB_GET_REPO_1000_MODIFIED = new GitHubGetRepo(1000, false);
    public static final AgoraDataSet GITHUB_GET_REPO_10000_ORIGINAL = new GitHubGetRepo(10000, true);
    public static final AgoraDataSet GITHUB_GET_REPO_10000_MODIFIED = new GitHubGetRepo(10000, false);

    public static final AgoraDataSet[] ALL_DATA_SETS = new AgoraDataSet[]{
            GITHUB_GET_REPO_50_ORIGINAL,
            GITHUB_GET_REPO_50_MODIFIED,
            GITHUB_GET_REPO_100_ORIGINAL,
            GITHUB_GET_REPO_100_MODIFIED,
            GITHUB_GET_REPO_500_ORIGINAL,
            GITHUB_GET_REPO_500_MODIFIED,
            GITHUB_GET_REPO_1000_ORIGINAL,
            GITHUB_GET_REPO_1000_MODIFIED,
            GITHUB_GET_REPO_10000_ORIGINAL,
            GITHUB_GET_REPO_10000_MODIFIED
    };

    public static final MergedDataSet GITHUB_GET_REPO = new MergedDataSet(
            ALL_DATA_SETS
    );

    public static final UniqueInvariantAgoraDataSet GITHUB_GET_REPO_UNIQUE = new UniqueInvariantAgoraDataSet(
            ALL_DATA_SETS
    );

    public static final UniqueInvariantAgoraDataSet UNIQUE_GITHUB_GET_REPO = new UniqueInvariantAgoraDataSet(OPEN_API_SPECIFICATION, SYSTEM_NAME, PATH, OPERATION_NAME, HttpMethod.GET, RESPONSE_CODE);

    public GitHubGetRepo(long numberOfApiCalls, boolean original) {
        super(OPEN_API_SPECIFICATION, SYSTEM_NAME, PATH, OPERATION_NAME, HttpMethod.GET, RESPONSE_CODE, numberOfApiCalls, original);
    }
}
