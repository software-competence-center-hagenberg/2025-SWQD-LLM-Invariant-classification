package invariant.evaluator.apispec;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PathTreeNode {

    private final PathNode pathNode;

    private final PathTreeNode parent;

    private final List<PathTreeNode> children = new LinkedList<>();

    public PathTreeNode(PathNode pathNode, PathTreeNode parent) {
        this.pathNode = pathNode;
        this.parent = parent;
        if(this.parent != null){
            this.parent.addChild(this);
        }
    }

    private void addChild(PathTreeNode child){
        this.children.add(child);
    }

    public PathNode getPathNode() {
        return pathNode;
    }

    public PathTreeNode getParent() {
        return parent;
    }

    public List<PathTreeNode> getChildren() {
        return children;
    }

    public static PathTreeNode getMatchingNode(Collection<PathTreeNode> nodes, PathNode matches){
        for (PathTreeNode node : nodes) {
            if(node.pathNode.getPropertyName().equals(matches.getPropertyName())){
                return node;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return toString("");
    }

    private String toString(String indent) {
        StringBuilder s = new StringBuilder(indent + pathNode.getPropertyName());
        for (PathTreeNode child : children) {
            s.append("\n");
            s.append(child.toString(indent + "\t"));
        }

        return s.toString();
    }

    public String toYaml(String currentIndent, String yamlIndentIncrement){
        String yaml;
        Schema<?> parentSchema = null;
        if(parent != null){
            parentSchema = parent.pathNode.getSchema();
        }
        if(parentSchema instanceof ArraySchema){
            yaml = currentIndent + "items:\n";
        } else {
            yaml = currentIndent + "properties:\n";
        }
        yaml += currentIndent + yamlIndentIncrement + pathNode.getPropertyName() + ":\n";

        Schema<?> schema = pathNode.getSchema();
        if(schema != null) {
            String type = schema.getType();
            if(type != null) {
                yaml += currentIndent + yamlIndentIncrement + yamlIndentIncrement + "type: " + type + "\n";
            }
            String description = schema.getDescription();
            if(description != null) {
                yaml += currentIndent + yamlIndentIncrement + yamlIndentIncrement + "description: " + description.indent((currentIndent + yamlIndentIncrement + yamlIndentIncrement).length()).trim() + "\n";
            }
            String pattern = schema.getPattern();
            if(pattern != null) {
                yaml += currentIndent + yamlIndentIncrement + yamlIndentIncrement + "pattern: " + pattern + "\n";
            }
            Object example = schema.getExample();
            if(example != null) {
                yaml += currentIndent + yamlIndentIncrement + yamlIndentIncrement + "example: " + example + "\n";
            }

            Integer minLength = schema.getMinLength();
            if(minLength != null) {
                yaml += currentIndent + yamlIndentIncrement + yamlIndentIncrement + "minLength: " + minLength + "\n";
            }
            Integer maxLength = schema.getMaxLength();
            if(maxLength != null) {
                yaml += currentIndent + yamlIndentIncrement + yamlIndentIncrement + "maxLength: " + maxLength + "\n";
            }

            List<?> enums = schema.getEnum();
            if(enums != null) {
                yaml += currentIndent + yamlIndentIncrement + yamlIndentIncrement + "enum: " + enums + "\n";
            }
        }

        // Print more details for leaf nodes.
        if(children.isEmpty()){
            if(schema instanceof ArraySchema){
                Schema<?> itemSchema = schema.getItems();
                if(itemSchema != null) {
                    yaml += currentIndent + yamlIndentIncrement + yamlIndentIncrement + "items:\n";
                    String type = itemSchema.getType();
                    if(type != null) {
                        yaml += currentIndent + yamlIndentIncrement + yamlIndentIncrement + yamlIndentIncrement + "type: " + type + "\n";
                        if(type.equals("object")){
                            yaml += currentIndent + yamlIndentIncrement + yamlIndentIncrement + yamlIndentIncrement + "properties:\n";
                            Map<String, Schema> itemProps = itemSchema.getProperties();
                            for (Map.Entry<String, Schema> entry : itemProps.entrySet()) {
                                yaml += currentIndent + yamlIndentIncrement + yamlIndentIncrement + yamlIndentIncrement + yamlIndentIncrement + entry.getKey() + ":\n";
                                String description = entry.getValue().getDescription();
                                if(description != null) {
                                    yaml += currentIndent + yamlIndentIncrement + yamlIndentIncrement + yamlIndentIncrement + yamlIndentIncrement + yamlIndentIncrement + "description: " + description.indent((currentIndent + yamlIndentIncrement + yamlIndentIncrement + yamlIndentIncrement + yamlIndentIncrement + yamlIndentIncrement).length()).trim() + "\n";
                                }
                            }
                        }
                    }
                    String description = itemSchema.getDescription();
                    if(description != null) {
                        yaml += currentIndent + yamlIndentIncrement + yamlIndentIncrement + yamlIndentIncrement + "description: " + description.indent((currentIndent + yamlIndentIncrement + yamlIndentIncrement + yamlIndentIncrement).length()).trim() + "\n";
                    }
                    Integer minLength = itemSchema.getMinLength();
                    if(minLength != null) {
                        yaml += currentIndent + yamlIndentIncrement + yamlIndentIncrement + yamlIndentIncrement + "minLength: " + minLength + "\n";
                    }
                    Integer maxLength = itemSchema.getMaxLength();
                    if(maxLength != null) {
                        yaml += currentIndent + yamlIndentIncrement + yamlIndentIncrement + yamlIndentIncrement + "maxLength: " + maxLength + "\n";
                    }
                    String pattern = itemSchema.getPattern();
                    if(pattern != null) {
                        yaml += currentIndent + yamlIndentIncrement + yamlIndentIncrement + yamlIndentIncrement + "pattern: " + pattern + "\n";
                    }
                }
            }
        }

        for (PathTreeNode child : children) {
            yaml += child.toYaml(currentIndent + yamlIndentIncrement + yamlIndentIncrement, yamlIndentIncrement);
        }

        return yaml;
    }
}
