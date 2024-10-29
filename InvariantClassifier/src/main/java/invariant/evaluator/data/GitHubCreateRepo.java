package invariant.evaluator.data;

public class GitHubCreateRepo extends AgoraDataSet {

    public static final String OPEN_API_SPECIFICATION = "agora_data/apiSpecifications/GitHub/createAnOrganizationRepository/swagger_createAnOrganizationRepository.yaml";

    public static final String SYSTEM_NAME = "GitHub";

    public static final String PATH = "/orgs/{org}/repos";

    public static final String OPERATION_NAME = "createOrganizationRepository";

    public static final String RESPONSE_CODE = "201";



    public static final AgoraDataSet GITHUB_CREATE_REPO_50_ORIGINAL = new GitHubCreateRepo(50, true);
    public static final AgoraDataSet GITHUB_CREATE_REPO_50_MODIFIED = new GitHubCreateRepo(50, false);

    public static final AgoraDataSet GITHUB_CREATE_REPO_100_ORIGINAL = new GitHubCreateRepo(100, true);
    public static final AgoraDataSet GITHUB_CREATE_REPO_100_MODIFIED = new GitHubCreateRepo(100, false);
    public static final AgoraDataSet GITHUB_CREATE_REPO_500_ORIGINAL = new GitHubCreateRepo(500, true);
    public static final AgoraDataSet GITHUB_CREATE_REPO_500_MODIFIED = new GitHubCreateRepo(500, false);
    public static final AgoraDataSet GITHUB_CREATE_REPO_1000_ORIGINAL = new GitHubCreateRepo(1000, true);
    public static final AgoraDataSet GITHUB_CREATE_REPO_1000_MODIFIED = new GitHubCreateRepo(1000, false);
    public static final AgoraDataSet GITHUB_CREATE_REPO_10000_ORIGINAL = new GitHubCreateRepo(10000, true);
    public static final AgoraDataSet GITHUB_CREATE_REPO_10000_MODIFIED = new GitHubCreateRepo(10000, false);

    public static final AgoraDataSet[] ALL_DATA_SETS = new AgoraDataSet[]{
            GITHUB_CREATE_REPO_50_ORIGINAL,
            GITHUB_CREATE_REPO_50_MODIFIED,
            GITHUB_CREATE_REPO_100_ORIGINAL,
            GITHUB_CREATE_REPO_100_MODIFIED,
            GITHUB_CREATE_REPO_500_ORIGINAL,
            GITHUB_CREATE_REPO_500_MODIFIED,
            GITHUB_CREATE_REPO_1000_ORIGINAL,
            GITHUB_CREATE_REPO_1000_MODIFIED,
            GITHUB_CREATE_REPO_10000_ORIGINAL,
            GITHUB_CREATE_REPO_10000_MODIFIED
    };

    public static final MergedDataSet GITHUB_CREATE_REPO = new MergedDataSet(
            ALL_DATA_SETS
    );

    public static final UniqueInvariantAgoraDataSet GITHUB_CREATE_REPO_UNIQUE = new UniqueInvariantAgoraDataSet(
            ALL_DATA_SETS
    );

    public static final UniqueInvariantAgoraDataSet UNIQUE_GITHUB_CREATE_REPO = new UniqueInvariantAgoraDataSet(OPEN_API_SPECIFICATION, SYSTEM_NAME, PATH, OPERATION_NAME, HttpMethod.POST, RESPONSE_CODE);

    public GitHubCreateRepo(long numberOfApiCalls, boolean original) {
        super(OPEN_API_SPECIFICATION, SYSTEM_NAME, PATH, OPERATION_NAME, HttpMethod.POST, RESPONSE_CODE, numberOfApiCalls, original);
    }
}
