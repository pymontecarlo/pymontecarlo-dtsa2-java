package pymontecarlo.util.hdf5;

public enum HDF5Type {
    INTEGER(0), FLOAT(0.0), STRING(null), STRING_FIXED(null);
    
    private final Object fillValue;
    
    HDF5Type(Object fillValue) {
        this.fillValue = fillValue;
    }
    
    public Object getFillValue() {
        return fillValue;
    }
}
