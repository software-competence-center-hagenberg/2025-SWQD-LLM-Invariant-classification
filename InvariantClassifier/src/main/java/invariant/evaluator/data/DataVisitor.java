package invariant.evaluator.data;

import io.swagger.v3.oas.models.OpenAPI;

import java.util.List;

public interface DataVisitor {
    void visitData(int id, String systemName, String operation, OpenAPI openAPI, String path, HttpMethod method, String responseCode, String invariant, String invariantType, String invariantDescription, List<String> inputVariables, List<String> outputVariables, String outputVariablesPath, String classification);
}
