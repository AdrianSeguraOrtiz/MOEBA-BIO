package moeba.fitnessfunction;

import java.util.ArrayList;

public abstract class GlobalFitnessFunction extends FitnessFunction {

    public GlobalFitnessFunction(double[][] data, Class<?>[] types) {
        super(data, types);
    }

    @Override
    public double run(ArrayList<ArrayList<Integer>[]> biclusters) {
        return super.func.run(deleteSmallOrEmptyBiclusters(biclusters));
    }
    
}
