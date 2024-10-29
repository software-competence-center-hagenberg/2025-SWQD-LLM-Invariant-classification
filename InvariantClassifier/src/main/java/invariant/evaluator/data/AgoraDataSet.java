package invariant.evaluator.data;

import at.sfischer.utils.table.ValueTable;
import at.sfischer.utils.table.ValueTableRow;
import at.sfischer.utils.table.visitor.FilterValueTableRowVisitor;
import at.sfischer.utils.table.visitor.ValueTableRowVisitor;
import invariant.evaluator.data.testdata.CSVManager;
import invariant.evaluator.data.testdata.TestCase;
import invariant.evaluator.data.testdata.TestCaseFileManager;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public abstract class AgoraDataSet extends AbstractAgoraDataSet {

    public static final String INVARIANTS_BASE_DIR = "agora_data/invariants";

    public static final String CALL_DATA_BASE_DIR = "agora_data/dataSets";

    public static final String INV_FILE_PREFIX = "invariants_";

    public static final String INV_FILE_ORG_POSTFIX = "_original.csv";
    public static final String INV_FILE_MOD_POSTFIX = "_modified.csv";

    private final String openApiSpecification;

    private final String systemName;

    private final String path;

    private final String operationName;

    private final HttpMethod httpMethod;

    private final String responseCode;

    private final long numberOfApiCalls;

    private final boolean original;

    public AgoraDataSet(String openApiSpecification, String systemName, String path, String operationName, HttpMethod httpMethod, String responseCode, long numberOfApiCalls, boolean original) {
        this.openApiSpecification = openApiSpecification;
        this.systemName = systemName;
        this.path = path;
        this.operationName = operationName;
        this.httpMethod = httpMethod;
        this.responseCode = responseCode;
        this.numberOfApiCalls = numberOfApiCalls;
        this.original = original;
    }

    @Override
    public OpenAPI getOpenAPISpecification() {
        SwaggerParseResult result = new OpenAPIParser().readLocation(this.openApiSpecification, null, null);
        return result.getOpenAPI();
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public String getSystemName() {
        return this.systemName;
    }

    @Override
    public String getOperation() {
        return this.operationName;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return this.httpMethod;
    }

    @Override
    public String getResponseCode() {
        return responseCode;
    }

    public long getNumberOfApiCalls() {
        return numberOfApiCalls;
    }

    public boolean containsInvariant(String invariant){
        final boolean[] results = {false};
        ValueTable tab = getInvariantData();
        tab.visitRows(new ValueTableRowVisitor() {
            @Override
            public void visit(ValueTableRow row) {
                String inv = row.getValue("invariant");
                if(invariant.equals(inv)){
                    results[0] = true;
                }
            }
        });

        return results[0];
    }

    @Override
    public ValueTable getInvariantData(){
        File invariantFile = getInvariantFile();
        return readInvariantData(invariantFile);
    }

    public File getInvariantFile(){
        File invariantsBaseDir = new File(INVARIANTS_BASE_DIR);
        File systemDir = new File(invariantsBaseDir, this.systemName);
        File endPointDir = new File(systemDir, this.operationName);
        File callsDir = new File(endPointDir, this.numberOfApiCalls + "");

        if(this.original){
            if(this.numberOfApiCalls == 10000){
                return new File(callsDir, INV_FILE_PREFIX + "10K" + INV_FILE_ORG_POSTFIX);
            } else {
                return new File(callsDir, INV_FILE_PREFIX + this.numberOfApiCalls + INV_FILE_ORG_POSTFIX);
            }
        } else {
            if(this.numberOfApiCalls == 10000){
                return new File(callsDir, INV_FILE_PREFIX + "10K" + INV_FILE_MOD_POSTFIX);
            } else {
                return new File(callsDir, INV_FILE_PREFIX + this.numberOfApiCalls + INV_FILE_MOD_POSTFIX);
            }
        }
    }

    public static ValueTable readInvariantData(File invariantFile) {
        try {
            ValueTable invData = ValueTable.parseCSV(invariantFile, ";", true, false, false);

            invData = invData.filter(new FilterValueTableRowVisitor() {
                @Override
                public boolean filter(ValueTableRow row) {
                    double tp = row.getValue("tp");
                    double fp = row.getValue("fp");
//                    double bug = row.getValue("bug");
                    return tp != 0 || fp != 0;// || bug != 0;
                }
            });

            return invData;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<TestCase> parseCallData(String system, String operation, long numberOfApiCalls){
        try {
            File callFile = AgoraDataSet.getCallDataFile(system, operation, numberOfApiCalls);
            List<TestCase> testCases = new LinkedList<>();
            BufferedReader in = new BufferedReader(new FileReader(callFile));
            String line = in.readLine();
            TestCaseFileManager testCaseFileManager = new TestCaseFileManager(line);
            while((line = in.readLine()) != null){
                TestCase testCase = testCaseFileManager.getTestCase(CSVManager.getCSVRecord(line));
                testCases.add(testCase);
            }

            return testCases;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static File getCallDataFile(String system, String operation, long numberOfApiCalls){
        String callCount;
        if(numberOfApiCalls == 10000){
            callCount = "10K";
        } else {
            callCount = String.valueOf(numberOfApiCalls);
        }

        File callDataBaseDir = new File(CALL_DATA_BASE_DIR);
        File dataFile = new File(callDataBaseDir, system + File.separator + operation + File.separator + system + "_" + operation + "_" + callCount + ".csv");
        if(dataFile.exists()){
            return dataFile;
        }

        dataFile = new File(callDataBaseDir, system + File.separator + operation + File.separator + system + "_" + callCount + ".csv");
        if(dataFile.exists()){
            return dataFile;
        }

        dataFile = new File(callDataBaseDir, system + File.separator + system + "_" + operation + "_" + callCount + ".csv");
        if(dataFile.exists()){
            return dataFile;
        }

        dataFile = new File(callDataBaseDir, system + File.separator + system + operation + "_" + callCount + ".csv");
        if(dataFile.exists()){
            return dataFile;
        }

        dataFile = new File(callDataBaseDir, system + File.separator + system + "_" + callCount + ".csv");
        if(dataFile.exists()){
            return dataFile;
        }

        return null;
    }
}
