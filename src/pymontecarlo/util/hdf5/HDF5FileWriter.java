package pymontecarlo.util.hdf5;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import edu.ucar.ral.nujan.hdf.HdfException;
import edu.ucar.ral.nujan.hdf.HdfFileWriter;
import edu.ucar.ral.nujan.hdf.HdfGroup;

public class HDF5FileWriter {

    private static final Map<HDF5Type, Integer> typeRegistry = new HashMap<>();

    static {
        typeRegistry.put(HDF5Type.STRING, HdfGroup.DTYPE_STRING_VAR);
        typeRegistry.put(HDF5Type.FLOAT, HdfGroup.DTYPE_FLOAT64);
        typeRegistry.put(HDF5Type.INTEGER, HdfGroup.DTYPE_FIXED32);
        typeRegistry.put(HDF5Type.STRING_FIXED, HdfGroup.DTYPE_STRING_FIX);
    }



    /**
     * Creates a new HDF5 file.
     * 
     * @param path
     *            location of file
     * @return HDF5 file object
     * @throws IOException
     *             if an error occurs
     */
    private static HdfFileWriter create(File path, boolean overwrite)
            throws IOException {
        int optFlag = overwrite ? HdfFileWriter.OPT_ALLOW_OVERWRITE : 0;

        HdfFileWriter hdf5file;
        try {
            hdf5file = new HdfFileWriter(path.toString(), optFlag);
        } catch (HdfException e) {
            throw new IOException(e);
        }

        return hdf5file;
    }



    private static void writeAttributes(HDF5Attributer attributer,
            HdfGroup attributerWriter) throws IOException {
        Entry<String, HDF5Attribute> attrEntry;
        String attrName;
        Object attrValue;
        int attrType;
        for (Iterator<Entry<String, HDF5Attribute>> iter =
                attributer.iterateAttributes(); iter.hasNext();) {
            attrEntry = iter.next();
            attrName = attrEntry.getKey();
            attrValue = attrEntry.getValue().getValue();
            attrType = typeRegistry.get(attrEntry.getValue().getType());

            try {
                attributerWriter.addAttribute(attrName, attrType, 0, attrValue,
                        false);
            } catch (HdfException e) {
                throw new IOException(e);
            }
        }
    }



    private static void writeDatasetsTOC(HDF5Group group, HdfGroup groupWriter,
            Map<HDF5Dataset, HdfGroup> datasetRegistry) throws IOException {
        Entry<String, HDF5Dataset> datasetEntry;
        HDF5Dataset dataset;
        String datasetName;
        int datasetType;
        Object datasetFillValue;
        int[] datasetDims;
        HdfGroup datasetWriter;
        int fieldLength;

        for (Iterator<Entry<String, HDF5Dataset>> iter =
                group.iterateDatasets(); iter.hasNext();) {
            datasetEntry = iter.next();
            datasetName = datasetEntry.getKey();
            dataset = datasetEntry.getValue();
            datasetType = typeRegistry.get(dataset.getType());
            datasetFillValue = dataset.getType().getFillValue();
            datasetDims = dataset.getDimensions();
            fieldLength = dataset.getFieldLength();

            try {
                datasetWriter =
                        groupWriter.addVariable(datasetName, datasetType,
                                fieldLength, datasetDims, datasetDims, 
                                datasetFillValue, 9);
            } catch (HdfException e) {
                throw new IOException(e);
            }

            writeAttributes(dataset, datasetWriter);

            datasetRegistry.put(datasetEntry.getValue(), datasetWriter);
        }
    }



    private static void writeDatasetsData(
            Map<HDF5Dataset, HdfGroup> datasetRegistry) throws IOException {
        Object datasetData;
        int[] datasetDims;
        HdfGroup datasetWriter;
        int[] startIxs;
        for (Entry<HDF5Dataset, HdfGroup> entry : datasetRegistry.entrySet()) {
            datasetData = entry.getKey().getData();
            datasetDims = entry.getKey().getDimensions();
            datasetWriter = entry.getValue();

            startIxs = datasetDims.clone();
            Arrays.fill(startIxs, 0);

            try {
                datasetWriter.writeData(startIxs, datasetData, false);
            } catch (HdfException e) {
                throw new IOException(e);
            }
        }
    }



    private static void writeGroup(HDF5Group group, HdfGroup groupWriter,
            Map<HDF5Dataset, HdfGroup> datasetRegistry) throws IOException {
        writeAttributes(group, groupWriter);
        writeDatasetsTOC(group, groupWriter, datasetRegistry);

        // Process subgroups recursively
        Entry<String, HDF5Group> subgroupEntry;
        String subgroupName;
        HDF5Group subgroup;
        HdfGroup subgroupWriter;

        for (Iterator<Entry<String, HDF5Group>> iter =
                group.iterateSubgroups(); iter.hasNext();) {
            subgroupEntry = iter.next();
            subgroupName = subgroupEntry.getKey();
            subgroup = subgroupEntry.getValue();

            try {
                subgroupWriter = groupWriter.addGroup(subgroupName);
            } catch (HdfException e) {
                throw new IOException(e);
            }

            writeGroup(subgroup, subgroupWriter, datasetRegistry);
        }
    }



    public static void write(HDF5Group root, File path, boolean overwrite)
            throws IOException {
        HdfFileWriter writer = create(path, overwrite);

        Map<HDF5Dataset, HdfGroup> datasetRegistry = new HashMap<>();

        // Define group, attribute and datasets' table of content
        writeGroup(root, writer.getRootGroup(), datasetRegistry);

        // End define
        try {
            writer.endDefine();
        } catch (HdfException e) {
            throw new IOException(e);
        }

        // Write datasets' data
        writeDatasetsData(datasetRegistry);

        // Close
        try {
            writer.close();
        } catch (HdfException e) {
            throw new IOException(e);
        }
    }
}
