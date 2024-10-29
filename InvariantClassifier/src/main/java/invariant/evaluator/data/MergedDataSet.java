package invariant.evaluator.data;

import java.util.List;

public class MergedDataSet implements DataSet {
    private final List<DataSet> dataSets;

    public MergedDataSet(DataSet... dataSets) {
        this.dataSets = List.of(dataSets);
    }

    @Override
    public void visitData(DataVisitor visitor) {
        dataSets.forEach(dataSet -> dataSet.visitData(visitor));
    }
}
