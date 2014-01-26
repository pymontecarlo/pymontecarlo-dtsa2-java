package pymontecarlo.util.hdf5;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class HDF5Group extends HDF5Attributer {

    public static HDF5Group createRoot() {
        return new HDF5Group();
    }

    private final Map<String, HDF5Group> subgroups;

    private final Map<String, HDF5Dataset> datasets;



    private HDF5Group() {
        super();
        subgroups = new HashMap<>();
        datasets = new HashMap<>();
    }



    public HDF5Group createSubgroup(String name) {
        HDF5Group group = new HDF5Group();
        if (subgroups.put(name, group) != null)
            throw new IllegalArgumentException("Subgroup already exists: "
                    + name);
        return group;
    }



    public HDF5Group requireSubgroup(String name) {
        HDF5Group group = subgroups.get(name);
        if (group == null)
            group = createSubgroup(name);
        return group;
    }



    public HDF5Dataset createDataset(String name, int[][] data) {
        int[] dims = { data.length, data[0].length };
        HDF5Dataset dataset = new HDF5Dataset(HDF5Type.INTEGER, data, dims);
        if (datasets.put(name, dataset) != null)
            throw new IllegalArgumentException("Dataset already exists: "
                    + name);
        return dataset;
    }



    public HDF5Dataset createDataset(String name, double[][] data) {
        int[] dims = { data.length, data[0].length };
        HDF5Dataset dataset = new HDF5Dataset(HDF5Type.FLOAT, data, dims);
        if (datasets.put(name, dataset) != null)
            throw new IllegalArgumentException("Dataset already exists: "
                    + name);
        return dataset;
    }



    public HDF5Dataset createDataset(String name, String[][] data) {
        int[] dims = { data.length, data[0].length };
        HDF5Dataset dataset =
                new HDF5Dataset(HDF5Type.STRING_FIXED, data, dims);
        if (datasets.put(name, dataset) != null)
            throw new IllegalArgumentException("Dataset already exists: "
                    + name);
        return dataset;
    }



    public HDF5Dataset createDataset(String name, double[][][] data) {
        int[] dims = { data.length, data[0].length, data[0][0].length };
        HDF5Dataset dataset = new HDF5Dataset(HDF5Type.FLOAT, data, dims);
        if (datasets.put(name, dataset) != null)
            throw new IllegalArgumentException("Dataset already exists: "
                    + name);
        return dataset;
    }



    public Iterator<Entry<String, HDF5Group>> iterateSubgroups() {
        return subgroups.entrySet().iterator();
    }



    public Iterator<Entry<String, HDF5Dataset>> iterateDatasets() {
        return datasets.entrySet().iterator();
    }
}
