package invariant.evaluator.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class InvariantKinds {

    private static final String KINDS_FILE = "invariantKinds.txt";

    private static final Map<String, String> KINDS_DESCRIPTION = new HashMap<>();

    public static String getInvariantDescription(String invariantKind){
        if(KINDS_DESCRIPTION.isEmpty()) {
            URL resource = InvariantKinds.class.getClassLoader().getResource(KINDS_FILE);
            try {
                File f = new File(resource.toURI());
                BufferedReader in = new BufferedReader(new FileReader(f));
                String line = in.readLine();
                while (line != null) {
                    String[] split = line.split(": ");
                    KINDS_DESCRIPTION.put(split[0], split[1]);
                    line = in.readLine();
                }
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        return KINDS_DESCRIPTION.get(invariantKind);
    }

}
