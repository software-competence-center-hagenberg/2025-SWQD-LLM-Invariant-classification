package invariant.evaluator.data;

import at.sfischer.utils.table.ArrayValueTable;
import at.sfischer.utils.table.ValueTable;
import at.sfischer.utils.table.ValueTableRow;
import at.sfischer.utils.table.visitor.ValueTableRowVisitor;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;

import java.io.File;

/**
 * Merges all invariant data for one end point.
 * Important do not mix data from different end points with this.
 */
public class UniqueInvariantAgoraDataSet extends AbstractAgoraDataSet {

    private ValueTable completeTable = null;

    private final OpenAPI openApiSpecification;

    private final String systemName;

    private final String path;

    private final String operationName;

    private final HttpMethod httpMethod;

    private final String responseCode;

    /**
     * To read existing unique invariant files in "allInvariants.csv".
     *
     * @param openApiSpecification
     * @param systemName
     * @param path
     * @param operationName
     * @param httpMethod
     * @param responseCode
     */
    public UniqueInvariantAgoraDataSet(String openApiSpecification, String systemName, String path, String operationName, HttpMethod httpMethod, String responseCode) {
        this.openApiSpecification = new OpenAPIParser().readLocation(openApiSpecification, null, null).getOpenAPI();
        this.systemName = systemName;
        this.path = path;
        this.operationName = operationName;
        this.httpMethod = httpMethod;
        this.responseCode = responseCode;

        File invariantFile = getInvariantFile();
        this.completeTable = AgoraDataSet.readInvariantData(invariantFile);
    }

    /**
     * To create a data set only containing the unique invariants in the other data sets of the same end point, i.e., removing all the duplicates.
     *
     * @param dataSets
     */
    public UniqueInvariantAgoraDataSet(AbstractAgoraDataSet... dataSets) {
        if(dataSets.length == 0){
            throw new IllegalArgumentException("You need to pass at least one data set for an end point.");
        }

        this.openApiSpecification = dataSets[0].getOpenAPISpecification();
        this.systemName = dataSets[0].getSystemName();
        this.path = dataSets[0].getPath();
        this.operationName = dataSets[0].getOperation();
        this.httpMethod = dataSets[0].getHttpMethod();
        this.responseCode = dataSets[0].getResponseCode();

        for (AbstractAgoraDataSet dataSet : dataSets) {
            if(!this.systemName.equals(dataSet.getSystemName()) ||
                    !this.path.equals(dataSet.getPath())){
                throw new IllegalArgumentException("The data sets need to be for the same system and the same path.");
            }
        }

        final int[] newRowIndex = {0};
        for (AbstractAgoraDataSet dataSet : dataSets) {
            ValueTable tab = dataSet.getInvariantData();
            if(completeTable == null){
                completeTable = new ArrayValueTable(tab.cols(), tab.rows());
            }

            tab.visitRows(new ValueTableRowVisitor() {
                @Override
                public void visit(ValueTableRow row) {
                    if(!tableContainsRow(completeTable, row)){
                        addRowToTable(completeTable, row, newRowIndex[0]);
                        newRowIndex[0]++;
                    }
                }
            });
        }

        if(completeTable != null){
            completeTable.trim();

            ValueTable table = new ArrayValueTable(completeTable.cols() + 1, completeTable.rows());
            newRowIndex[0] = 0;
            completeTable.visitRows(new ValueTableRowVisitor() {
                @Override
                public void visit(ValueTableRow row) {
                    table.addValue("ID", newRowIndex[0], newRowIndex[0]);
                    addRowToTable(table, row, newRowIndex[0]);
                    newRowIndex[0]++;
                }
            });
            completeTable = table;
        }
    }

    public File getInvariantFile(){
        File invariantsBaseDir = new File(AgoraDataSet.INVARIANTS_BASE_DIR);
        File systemDir = new File(invariantsBaseDir, this.systemName);
        File endPointDir = new File(systemDir, this.operationName);
        return new File(endPointDir, "allInvariants.csv");
    }

    static boolean tableContainsRow(ValueTable tab, ValueTableRow row){
        final boolean[] found = {false};
        tab.visitRows(new ValueTableRowVisitor() {
            @Override
            public void visit(ValueTableRow tabRow) {
                if(found[0]) {
                    return;
                }
                if(row.cols() != tabRow.cols()) {
                    return;
                }

                // TODO Should also compare the xHeaders for a full implementation. For this use case it should work fine without.

                if(equalRows(row, tabRow)){
                    found[0] = true;
                }

            }
        });

        return found[0];
    }

    static boolean equalRows(ValueTableRow row1, ValueTableRow row2){
        for (int i = 0; i < row1.cols(); i++) {
            String header = row1.getXHeader(i);
            try {
                Object val1 = row1.getValue(header);
                Object val2 = row2.getValue(header);
                if (val1 == val2) {
                    continue;
                }
                if (val1 == null || val2 == null) {
                    return false;
                }
                if (!val1.equals(val2)) {
                    return false;
                }
            } catch (IllegalArgumentException e){
                return false;
            }
        }

        return true;
    }

    static void addRowToTable(ValueTable tab, ValueTableRow row, int newRowIndex){
        final int columns = row.cols();
        for (int i = 0; i < columns; i++) {
            tab.addValue(row.getXHeader(i), newRowIndex, row.getValue(i));
        }
    }

    @Override
    public ValueTable getInvariantData() {
        return this.completeTable;
    }

    @Override
    public OpenAPI getOpenAPISpecification() {
        return openApiSpecification;
    }

    @Override
    public String getSystemName() {
        return systemName;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getOperation() {
        return operationName;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    @Override
    public String getResponseCode() {
        return responseCode;
    }
}
