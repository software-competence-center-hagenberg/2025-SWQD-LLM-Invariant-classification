package invariant.evaluator.apispec;

import invariant.evaluator.data.HttpMethod;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class APISpecFilter {

    public static String extractRelevantSwagger(OpenAPI apiSpec, String path, HttpMethod method, String responseCode, List<String> inputVariables, List<String> outputVariables, String yamlIndent){
        String yamlString = "paths:\n";
        yamlString += yamlIndent + "\"" + path + "\"\n";
        yamlString += yamlIndent + getOperationString(method) + ":\n";


        PathItem pathItem = apiSpec.getPaths().get(path);
        if(pathItem == null){
            throw new RuntimeException("Could not find path: " + path);
        }

        Operation op = getOperation(pathItem, method);

        String summary = op.getSummary();
        if(summary != null){
            yamlString += yamlIndent + yamlIndent + "summary: " + summary + "\n";
        }
        String description = op.getDescription();
        if(description != null){
            yamlString += yamlIndent + yamlIndent + "description: " + description.indent((yamlIndent + yamlIndent).length()).trim() + "\n";
        }

        // -----------------------------------------------------------------------
        // Parameters.
        List<Parameter> parameters = op.getParameters();
        Map<String, Parameter> inputs = new LinkedHashMap<>();
        for (String inputVariable : inputVariables) {
            String[] split = inputVariable.split("\\.");
            String parameterName = split[0];
            for (Parameter parameter : parameters) {
                parameter = resolveParameter(apiSpec, parameter);
                String paraName = parameter.getName();
                if(paraName.equals(parameterName)){
                    inputs.put(inputVariable, parameter);
                    break;
                }
            }
        }

        yamlString += yamlIndent + yamlIndent + "parameters:\n";

        for (Map.Entry<String, Parameter> entry : inputs.entrySet()) {
            Parameter parameter = entry.getValue();
            yamlString += yamlIndent + yamlIndent + yamlIndent + "- name: " + parameter.getName() + "\n";
            String in = parameter.getIn();
            if(in != null){
                yamlString += yamlIndent + yamlIndent + yamlIndent + yamlIndent + "in: " + in + "\n";
            }
            String description1 = parameter.getDescription();
            if(description1 != null){
                yamlString += yamlIndent + yamlIndent + yamlIndent + yamlIndent + "description: " + description1.indent((yamlIndent + yamlIndent + yamlIndent + yamlIndent).length()).trim() + "\n";
            }
            Boolean required = parameter.getRequired();
            if(required != null){
                yamlString += yamlIndent + yamlIndent + yamlIndent + yamlIndent + "required: " + required + "\n";
            }
            Object example = parameter.getExample();
            if(example != null){
                yamlString += yamlIndent + yamlIndent + yamlIndent + yamlIndent + "example: " + example + "\n";
            }

            Schema<?> parameterSchema = parameter.getSchema();
            String pattern = parameterSchema.getPattern();
            if(pattern != null){
                yamlString += yamlIndent + yamlIndent + yamlIndent + yamlIndent + "pattern: " + pattern + "\n";
            }
            String type = parameterSchema.getType();
            if(type != null) {
                yamlString += yamlIndent + yamlIndent + yamlIndent + yamlIndent + "type: " + type + "\n";
                if (type.equals("array")) {
                    Integer minLength = parameterSchema.getMinLength();
                    if (minLength != null) {
                        yamlString += yamlIndent + yamlIndent + yamlIndent + yamlIndent + "minLength: " + minLength + "\n";
                    }
                    Integer maxLength = parameterSchema.getMaxLength();
                    if (maxLength != null) {
                        yamlString += yamlIndent + yamlIndent + yamlIndent + yamlIndent + "maxLength: " + maxLength + "\n";
                    }
                }
            }

            String format = parameterSchema.getFormat();
            if(format != null) {
                yamlString += yamlIndent + yamlIndent + yamlIndent + yamlIndent + "format: " + format + "\n";
            }

            BigDecimal minimum = parameterSchema.getMinimum();
            if(minimum != null){
                yamlString += yamlIndent + yamlIndent + yamlIndent + yamlIndent + "minimum: " + minimum + "\n";
            }
            BigDecimal maximum = parameterSchema.getMaximum();
            if(maximum != null){
                yamlString += yamlIndent + yamlIndent + yamlIndent + yamlIndent + "maxLength: " + maximum + "\n";
            }


        }


        // -----------------------------------------------------------------------
        // RequestBody.
        RequestBody requestBody = op.getRequestBody();
        if(requestBody != null) {
            Content requestContent = requestBody.getContent();
            if (requestContent.keySet().size() != 1) {
                throw new RuntimeException("Unclear which media type to use: " + requestContent.keySet());
            }
            String requestMediaTypeKey = requestContent.keySet().iterator().next();
            MediaType requestMediaType = requestContent.get(requestMediaTypeKey);
            Schema<?> requestSchema = requestMediaType.getSchema();
            requestSchema = resolveSchema(apiSpec, requestSchema);

            String requestSchemaType = requestSchema.getType();
            String requestSchemaDescription = requestSchema.getDescription();

            yamlString += yamlIndent + yamlIndent + "requestBody:\n";

            yamlString += yamlIndent + yamlIndent + yamlIndent + yamlIndent + "schema:\n";
            if (requestSchemaType != null) {
                yamlString += yamlIndent + yamlIndent + yamlIndent + yamlIndent + yamlIndent + "type: " + requestSchemaType + "\n";
            }
            if (requestSchemaDescription != null) {
                yamlString += yamlIndent + yamlIndent + yamlIndent + yamlIndent + yamlIndent + "description: " + requestSchemaDescription.indent((yamlIndent + yamlIndent + yamlIndent + yamlIndent + yamlIndent).length()).trim() + "\n";
            }

            List<String> nonParameterInputVariables = new LinkedList<>(inputVariables);
            nonParameterInputVariables.removeAll(inputs.keySet());
            List<PathTreeNode> requestPathTree = getPathsToVariables(apiSpec, requestSchema, nonParameterInputVariables);
            if (requestPathTree != null) {
                for (PathTreeNode pathTreeNode : requestPathTree) {
                    yamlString += pathTreeNode.toYaml(yamlIndent + yamlIndent + yamlIndent + yamlIndent + yamlIndent, yamlIndent);
                }
            }
        }

        // -----------------------------------------------------------------------
        // Response.
        ApiResponses responses = op.getResponses();
        ApiResponse response = responses.get(responseCode); // 200, 201

        yamlString += yamlIndent + yamlIndent + "responses:\n";
        yamlString += yamlIndent + yamlIndent + yamlIndent + "'" + responseCode + "':\n";

        String responseDescription = op.getDescription();
        if(responseDescription != null){
            yamlString += yamlIndent + yamlIndent + yamlIndent + yamlIndent + "description: " + responseDescription.indent((yamlIndent + yamlIndent + yamlIndent + yamlIndent).length()).trim() + "\n";
        }

        Content responseContent = response.getContent();
        if(responseContent.keySet().size() != 1){
            throw new RuntimeException("Unclear which media type to use: " + responseContent.keySet());
        }
        String mediaTypeKey = responseContent.keySet().iterator().next();
        MediaType mediaType = responseContent.get(mediaTypeKey);
        Schema<?> responseSchema = mediaType.getSchema();
        responseSchema = resolveSchema(apiSpec,  responseSchema);

        String responseSchemaType = responseSchema.getType();
        String responseSchemaDescription = responseSchema.getDescription();

        yamlString += yamlIndent + yamlIndent + yamlIndent + yamlIndent + "schema:\n";
        if(responseSchemaType != null){
            yamlString += yamlIndent + yamlIndent + yamlIndent + yamlIndent + yamlIndent + "type: " + responseSchemaType + "\n";
        }
        if(responseSchemaDescription != null){
            yamlString += yamlIndent + yamlIndent + yamlIndent + yamlIndent + yamlIndent + "description: " + responseSchemaDescription.indent((yamlIndent + yamlIndent + yamlIndent + yamlIndent + yamlIndent).length()).trim() + "\n";
        }

        List<PathTreeNode> pathTree = getPathsToVariables(apiSpec, responseSchema, outputVariables);
        if(pathTree != null) {
            for (PathTreeNode pathTreeNode : pathTree) {
                yamlString += pathTreeNode.toYaml(yamlIndent + yamlIndent + yamlIndent + yamlIndent + yamlIndent, yamlIndent);
            }
        }

        return yamlString;
    }

    static Operation getOperation(PathItem pathItem, HttpMethod method){
        return switch (method) {
            case GET -> pathItem.getGet();
            case POST -> pathItem.getPost();
            case PATCH -> pathItem.getPatch();
            case PUT -> pathItem.getPut();
            case DELETE -> pathItem.getDelete();
        };
    }

    static String getOperationString(HttpMethod method){
        return switch (method) {
            case GET -> "get";
            case POST -> "post";
            case PATCH -> "patch";
            case PUT -> "put";
            case DELETE -> "delete";
        };
    }

    static Parameter resolveParameter(OpenAPI apiSpec, Parameter parameter){
        if(parameter.get$ref() != null){
            return geParameterFromRef(apiSpec, parameter.get$ref());
        }

        return parameter;
    }

    static Parameter geParameterFromRef(OpenAPI apiSpec, String ref){
        return apiSpec.getComponents().getParameters().get(ref.substring(ref.lastIndexOf('/') + 1));
    }

    static Schema<?> resolveSchema(OpenAPI apiSpec, Schema<?> schema){
        if(schema.get$ref() != null){
            return getSchemaFromRef(apiSpec, schema.get$ref());
        }

        return schema;
    }

    static Schema<?> getSchemaFromRef(OpenAPI apiSpec, String ref){
        return apiSpec.getComponents().getSchemas().get(ref.substring(ref.lastIndexOf('/') + 1));
    }

    static List<PathTreeNode> getPathsToVariables(OpenAPI apiSpec, Schema<?> schema, List<String> variables){
        List<PathTreeNode> pathTree = null;
        for (String variable : variables) {
            List<PathNode> shortestPath = shortestPathToVariable(apiSpec, variable.split("\\."), schema);

            if(shortestPath == null && variable.startsWith("RESPONSE_BODY")){
                shortestPath = new LinkedList<>();
                shortestPath.add(new PathNode(variable.replaceFirst("RESPONSE_BODY\\.", ""), schema));
            }

            if(shortestPath == null){
                // TODO Fix these.
                System.err.println("NO SHORTEST PATH FOUND: " + variable);
                continue;
            }


            // Find path overlaps so we do not print the same properties multiple times.
            if(pathTree == null){
                pathTree = new LinkedList<>();
                PathTreeNode parent = null;
                for (PathNode pathNode : shortestPath) {
                    if(parent == null){
                        parent = new PathTreeNode(pathNode,null);
                        pathTree.add(parent);

                        continue;
                    }

                    parent = new PathTreeNode(pathNode, parent);
                }

                continue;
            }

            PathTreeNode parent = null;
            for (PathNode pathNode : shortestPath) {
                if(parent == null){
                    PathTreeNode match = PathTreeNode.getMatchingNode(pathTree, pathNode);
                    if(match != null){
                        parent = match;
                    } else {
                        parent = new PathTreeNode(pathNode, null);
                        pathTree.add(parent);
                    }

                    continue;
                }

                PathTreeNode match = PathTreeNode.getMatchingNode(parent.getChildren(), pathNode);
                if(match != null){
                    parent = match;
                } else {
                    parent = new PathTreeNode(pathNode, parent);
                }
            }
        }

        return pathTree;
    }

    static List<PathNode> shortestPathToVariable(OpenAPI apiSpec, String[] variable, Schema<?> schema){
        List<List<PathNode>> paths = pathsToVariable(apiSpec, variable, schema, "");

//        if(paths.size() != 1){
//            System.err.println("Multiple paths found for: " + String.join(".", variable));
//            paths.forEach(System.err::println);
//        }

        List<PathNode> shortest = null;
        for (List<PathNode> p : paths) {
            if(!p.isEmpty() &&
                    (shortest == null
                            || p.size() < shortest.size()
                    )){
                shortest = p;
            }
        }

        return shortest;
    }

    static List<List<PathNode>> pathsToVariable(OpenAPI apiSpec, String[] variable, Schema<?> schema, String debugIndent){
        List<List<PathNode>> paths = new LinkedList<>();

        if(schema instanceof ArraySchema){
            Schema<?> itemSchema = resolveSchema(apiSpec, schema.getItems());
            return pathsToVariable(apiSpec, variable, itemSchema, debugIndent + "\t");
        }


        Map<String, Schema> properties = schema.getProperties();
        if(properties == null){
            return paths;
        }
        for (Map.Entry<String, Schema> entry : properties.entrySet()) {
            Schema<?> resolvedSchema = resolveSchema(apiSpec, entry.getValue());
            if(entry.getKey().equals(variable[variable.length - 1])){
                List<PathNode> path = new LinkedList<>();

                // To make sure the item schema of the leaf variables contain all information.
                if(resolvedSchema instanceof ArraySchema){
                    resolvedSchema.setItems(resolveSchema(apiSpec, resolvedSchema.getItems()));
                }

                path.add(0, new PathNode(entry.getKey(), resolvedSchema));
                paths.add(path);
            }

            List<List<PathNode>> childPaths = pathsToVariable(apiSpec, variable, resolvedSchema, debugIndent + "\t");
            for (List<PathNode> childPath : childPaths) {
                if(childPath.size() >= variable.length){
                    childPath.add(0, new PathNode(entry.getKey(), resolvedSchema));
                    paths.add(childPath);
                    continue;
                }

                if(variable[variable.length - childPath.size() - 1].equals(entry.getKey())){
                    childPath.add(0, new PathNode(entry.getKey(), resolvedSchema));
                    paths.add(childPath);
                }
            }
        }

        return paths;
    }
}
