package invariant.evaluator.data;

import at.sfischer.utils.table.ValueTable;
import at.sfischer.utils.table.ValueTableRow;
import at.sfischer.utils.table.visitor.ValueTableRowVisitor;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractAgoraDataSet implements DataSet {

    public abstract ValueTable getInvariantData();

    public abstract OpenAPI getOpenAPISpecification();

    public abstract String getSystemName();

    public abstract String getPath();

    public abstract String getOperation();

    public abstract HttpMethod getHttpMethod();

    public abstract String getResponseCode();

    @Override
    public void visitData(DataVisitor visitor) {
        ValueTable invariantData = getInvariantData();
        invariantData.visitRows(new ValueTableRowVisitor() {
            @Override
            public void visit(ValueTableRow row) {
                String invariant = row.getValue("invariant");
                String invariantType = row.getValue("invariantType");
                String invariantDescription = InvariantKinds.getInvariantDescription(invariantType);

                // pptname of the invariant data contains the path for the return variables after 200. Add this to identify the correct path.
                String pptname = row.getValue("pptname");
                String pptReturnPrefix = pptname.substring(pptname.indexOf("&" + getResponseCode()) + 4, pptname.indexOf('('));
                String outputVarsPrefix = null;
                if(!pptReturnPrefix.isEmpty()){
                    while(pptReturnPrefix.startsWith("&")){
                        pptReturnPrefix = pptReturnPrefix.substring(1);
                    }
                    if(!pptReturnPrefix.startsWith(".")){
                        String[] split = pptReturnPrefix.split("&");
                        outputVarsPrefix = String.join(".", split);
                    } else {
                        outputVarsPrefix = "RESPONSE_BODY";
                    }
                }

                String variables = row.getValue("variables");
                List<String> splitVariables = getVariables(variables);

                splitVariables = cleanVariableNames(splitVariables);
                List<String> inputVariables = new LinkedList<>();
                splitVariables.forEach(var -> {
                    if(var.startsWith("input.")){
                        inputVariables.add(var.substring("input.".length()));
                    }
                });
                List<String> outputVariables = new LinkedList<>();
                String finalOutputVarsPrefix = outputVarsPrefix;
                splitVariables.forEach(var -> {
                    if(var.startsWith("return.")){
                        if(finalOutputVarsPrefix != null){
                            outputVariables.add(finalOutputVarsPrefix + "." + var.substring("return.".length()));
                        } else {
                            outputVariables.add(var.substring("return.".length()));
                        }
                    }
                });
                // Sanity check if any variables are missing.
                if(splitVariables.size() != inputVariables.size() + outputVariables.size()){
                    Set<String> missing = new HashSet<>(splitVariables);
                    for (String variable : inputVariables) {
                        missing.remove("input." + variable);
                    }
                    for (String variable : outputVariables) {
                        missing.remove("return." + variable);
                    }
                    throw new RuntimeException("Could not identify variables: " + missing);
                }

                double tp = row.getValue("tp");
                String classification = tp != 0 ? "TP" : "FP";

                double id;
                try {
                    id = row.getValue("ID");
                } catch (Exception e) {
                    id = -1;
                }

                String outputVariablesPath = outputVarsPrefix == null || outputVarsPrefix.startsWith("RESPONSE_BODY") ? "" : outputVarsPrefix;

                visitor.visitData((int)id, getSystemName(), getOperation(), getOpenAPISpecification(), getPath(), getHttpMethod(), getResponseCode(), invariant, invariantType, invariantDescription, inputVariables, outputVariables, outputVariablesPath, classification);
            }
        });
    }

    static List<String> cleanVariableNames(List<String> variables){
        AtomicBoolean replace = new AtomicBoolean(true);
        variables = new LinkedList<>(variables);

        while(replace.get()) {
            List<String> toAdd = new LinkedList<>();
            replace.set(false);
            variables.replaceAll(s -> {
                if (s.startsWith("size(") && s.endsWith(")")) {
                    replace.set(true);
                    return s.substring("size(".length(), s.length() - 1);
                }

                if(s.endsWith("[..]")){
                    replace.set(true);
                    return s.substring(0, s.length() - 4);
                }

                if(s.endsWith("-1")){
                    replace.set(true);
                    return s.substring(0, s.length() - 2);
                }

                if(s.endsWith("]")){
                    int index = s.lastIndexOf('[');
                    if(index > 0){
                        toAdd.add(s.substring(index + 1, s.length() - 1));
                        return s.substring(0, index);
                    }
                }

                return s;
            });

            if(!toAdd.isEmpty()){
                variables.addAll(toAdd);
                replace.set(true);
            }
        }

        return variables;
    }

    public static List<String> getVariables(String variablesValue){
        variablesValue = variablesValue.substring(1, variablesValue.length() - 1);
        return List.of(variablesValue.split(", "));
    }
}
