package invariant.evaluator.openai;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PromptGenerator {


    public static String generatePrompt(String yamlString, String invariant, String invariantType, String invariantDescription, String outputVariablesPath){
        String prompt = """
                Is the following invariant for a REST endpoint "true-positive" or "false-positive"?
                
                This is an excerpt of the swagger definition:
                
                %s
                                
                Is the following invariant a true-positive or a false-positive:
                Invariant: %s
                
                Invariant type: %s
                Invariant description: %s
                Fields starting with "input" are request parameters or fields in the request body.
                Fields starting with "return" are fields in the response body.
                """;

        if(outputVariablesPath != null && !outputVariablesPath.isEmpty()){
            prompt += """
                    The return fields refer to the path "%s" in the response body.
                    """;

            return String.format(prompt, yamlString, invariant, invariantType, invariantDescription, outputVariablesPath);
        }

        return String.format(prompt, yamlString, invariant, invariantType, invariantDescription);
    }

    public static String generatePrompt(String yamlString, String invariant, String invariantType, String invariantDescription, String outputVariablesPath, List<String> examples, long maxNumberOfOperations){
        String prompt = generatePrompt(yamlString, invariant, invariantType, invariantDescription, outputVariablesPath);

        String callsString = "We have tried " + maxNumberOfOperations + " calls on this API and did not find a single counter example.\n";

        if(examples.isEmpty() || invariant.endsWith("== null") || invariant.endsWith("!= null")) {
            return prompt + "\n" + callsString;
        }

        Set<String> examplesDistinct = new HashSet<>(examples);
        Set<String> examplesShort = new HashSet<>();
        for (String example : examples) {
            if (example.length() < 200) {
                examplesShort.add(example);
            }
        }
        StringBuilder examplesString = new StringBuilder();
        Iterator<String> i = examplesShort.iterator();
        int ex = 0;
        while (i.hasNext() && ex < 5) {
            String example = i.next();
            examplesString.append(example).append("\n");
            ex++;
        }

        String exampleIntro = "We found " + examples.size() + " examples in the requests data, with " + examplesDistinct.size() + " of them being distinct examples.\n";
        if (!examplesShort.isEmpty()) {
            if (examplesShort.size() == 1) {
                exampleIntro += "Here is an example values of the variables:\n";
            } else if (examplesShort.size() < 5) {
                exampleIntro += "Here are " + examplesShort.size() + " example values of the variables:\n";
            } else {
                exampleIntro += "Here are 5 example values of the variables:\n";
            }
        }

        return prompt + "\n" + callsString + "\n" + exampleIntro + examplesString;
    }

    public static String generateViolationsPrompt(String yamlString, String invariant, String invariantType, String invariantDescription, String outputVariablesPath){
        String prompt = """
                Does the following invariant for a REST endpoint violate the API specification in the swagger definition?
                
                This is an excerpt of the swagger definition:
                
                %s
                                
                Is the following invariant possible according to the specification:
                Invariant: %s
                
                Invariant type: %s
                Invariant description: %s
                Fields starting with "input" are request parameters or fields in the request body.
                Fields starting with "return" are fields in the response body.
                """;

        if(outputVariablesPath != null && !outputVariablesPath.isEmpty()){
            prompt += """
                    The return fields refer to the path "%s" in the response body.
                    """;

            return String.format(prompt, yamlString, invariant, invariantType, invariantDescription, outputVariablesPath);
        }

        return String.format(prompt, yamlString, invariant, invariantType, invariantDescription);
    }
}
