package invariant.evaluator.apispec;

import io.swagger.v3.oas.models.media.Schema;

public class PathNode {

    private final String propertyName;

    private final Schema<?> schema;

    public PathNode(String propertyName, Schema<?> schema) {
        this.propertyName = propertyName;
        this.schema = schema;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Schema<?> getSchema() {
        return schema;
    }

    @Override
    public String toString() {
        return "(" + propertyName + ": " + schema.getClass() + ")";
    }
}
