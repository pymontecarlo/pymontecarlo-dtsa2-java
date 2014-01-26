package pymontecarlo.util.hdf5;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public abstract class HDF5Attributer {

    private final Map<String, HDF5Attribute> attributes;



    public HDF5Attributer() {
        attributes = new HashMap<>();
    }



    public HDF5Attribute setAttribute(String name, String value) {
        HDF5Attribute attr = new HDF5Attribute(HDF5Type.STRING, value);
        attributes.put(name, attr);
        return attr;
    }
    
    public HDF5Attribute setAttribute(String name, String... values) {
        HDF5Attribute attr = new HDF5Attribute(HDF5Type.STRING, values);
        attributes.put(name, attr);
        return attr;
    }



    public HDF5Attribute setAttribute(String name, int value) {
        HDF5Attribute attr = new HDF5Attribute(HDF5Type.INTEGER, value);
        attributes.put(name, attr);
        return attr;
    }



    public HDF5Attribute setAttribute(String name, int... values) {
        HDF5Attribute attr = new HDF5Attribute(HDF5Type.INTEGER, values);
        attributes.put(name, attr);
        return attr;
    }



    public HDF5Attribute setAttribute(String name, double value) {
        HDF5Attribute attr = new HDF5Attribute(HDF5Type.FLOAT, value);
        attributes.put(name, attr);
        return attr;
    }



    public HDF5Attribute setAttribute(String name, double... values) {
        HDF5Attribute attr = new HDF5Attribute(HDF5Type.FLOAT, values);
        attributes.put(name, attr);
        return attr;
    }



    public Iterator<Entry<String, HDF5Attribute>> iterateAttributes() {
        return attributes.entrySet().iterator();
    }
}
