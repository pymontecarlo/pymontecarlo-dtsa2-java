package pymontecarlo.util.hdf5;

public class HDF5Dataset extends HDF5Attributer {

    private final HDF5Type type;

    private final Object data;

    private final int[] dims;

    private final int fieldLength;



    public HDF5Dataset(HDF5Type type, Object data, int[] dims) {
        super();
        this.type = type;
        this.data = data;
        this.dims = dims.clone();

        int fieldLength = 0;
        if (data instanceof String[][]) {
            String[][] arr = (String[][]) data;
            fieldLength = Integer.MIN_VALUE;
            for (int i = 0; i < arr.length; i++) {
                for (int j = 0; j < arr[0].length; j++) {
                    fieldLength = Math.max(fieldLength, arr[i][j].length());
                }
            }
        }
        this.fieldLength = fieldLength;
    }



    public HDF5Type getType() {
        return type;
    }



    public Object getData() {
        return data;
    }



    public int[] getDimensions() {
        return dims.clone();
    }



    public int getFieldLength() {
        return fieldLength;
    }
}
