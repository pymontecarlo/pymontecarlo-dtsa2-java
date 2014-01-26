package pymontecarlo.util.hdf5;

public class HDF5Attribute {

    private final HDF5Type type;
    
    private final Object value;
    
    
    public HDF5Attribute(HDF5Type type, Object value) {
        this.type = type;
        this.value = value;
    }


    public HDF5Type getType() {
        return type;
    }



    public Object getValue() {
        return value;
    }
}
