package invariant.evaluator.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public interface LLM {

    String getIdentifier();

    String getName();

    String getApiKey();

    Retrofit getRetroFit(OkHttpClient client, ObjectMapper mapper);
}
