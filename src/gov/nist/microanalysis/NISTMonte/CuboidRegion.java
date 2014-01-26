package gov.nist.microanalysis.NISTMonte;

import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.Material;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.Region;
import gov.nist.microanalysis.NISTMonte.MonteCarloSS.RegionBase;
import gov.nist.microanalysis.Utility.Math2;

public class CuboidRegion extends IndexedRegion {

    private static class Cuboid extends IndexedRegion {

        public final double x0;

        public final double x1;

        public final double y0;

        public final double y1;

        public final double z0;

        public final double z1;



        public Cuboid(Region parent, IMaterialScatterModel msm, double[] dims,
                double[] point, int index) {
            super(parent, msm, MultiPlaneShape.createBlock(dims, point, 0.0,
                    0.0, 0.0), index);

            x0 = point[0] - dims[0] / 2.0;
            x1 = point[0] + dims[0] / 2.0;
            y0 = point[1] - dims[1] / 2.0;
            y1 = point[1] + dims[1] / 2.0;
            z0 = point[2] - dims[2] / 2.0;
            z1 = point[2] + dims[2] / 2.0;
        }



        @Override
        public RegionBase findEndOfStep(double[] pos0, double[] pos1) {
            return mParent.findEndOfStep(pos0, pos1);
        }



        @Override
        protected RegionBase containingSubRegion(double[] pos) {
            return mParent.containingSubRegion(pos);
        }

    }

    protected final Cuboid[][][] cuboids;

    protected final double dx, dy, dz;

    protected final double x0, y0, z0;

    protected final double x1, y1, z1;

    protected final int nx, ny, nz;



    public CuboidRegion(double x0, double x1, int nx,
            double y0, double y1, int ny,
            double z0, double z1, int nz) throws EPQException {
        super(null, new BasicMaterialModel(Material.Null),
                MultiPlaneShape.createSubstrate(Math2.Z_AXIS, Math2.ORIGIN_3D), 0);

        if (x1 <= x0)
            throw new IllegalArgumentException("x1 must be greater than x0");
        if (nx < 1)
            throw new IllegalArgumentException(
                    "nx must be greater or equal to 1");
        this.x0 = x0;
        this.x1 = x1;
        this.nx = nx;
        dx = (x1 - x0) / nx;

        if (y1 <= y0)
            throw new IllegalArgumentException("y1 must be greater than y0");
        if (ny < 1)
            throw new IllegalArgumentException(
                    "ny must be greater or equal to 1");
        this.y0 = y0;
        this.y1 = y1;
        this.ny = ny;
        dy = (y1 - y0) / ny;

        if (z1 <= z0)
            throw new IllegalArgumentException("z1 must be greater than z0");
        if (nz < 1)
            throw new IllegalArgumentException(
                    "nz must be greater or equal to 1");
        this.z0 = z0;
        this.z1 = z1;
        this.nz = nz;
        dz = (z1 - z0) / nz;

        cuboids = new Cuboid[nx][ny][nz];

        Cuboid cuboid;
        double[] point;
        int index = 1;
        double[] dims = new double[] { dx, dy, dz };
        IMaterialScatterModel msm = new BasicMaterialModel(Material.Null);
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                for (int k = 0; k < nz; k++) {
                    point =
                            new double[] { x0 + (i + 0.5) * dx,
                                    y0 + (j + 0.5) * dy, z0 + (k + 0.5) * dz };
                    cuboid = new Cuboid(this, msm, dims, point, index);

                    cuboids[i][j][k] = cuboid;
                    this.mSubRegions.add(cuboid);

                    index++;
                }
            }
        }
    }



    public CuboidRegion(double x0, double x1, int nx,
            double y0, double y1, int ny,
            double z0, int nz) throws EPQException {
        this(x0, x1, nx, y0, y1, ny, z0, 0.0, nz);
    }



    public void updateCuboidMaterial(int i, int j, int k,
            IMaterialScatterModel model) {
        cuboids[i][j][k].mScatterModel = model;
    }



    public void updateCuboidMaterial(int i, int j, int k, Material material)
            throws EPQException {
        updateCuboidMaterial(i, j, k, new BasicMaterialModel(material));
    }



    public void updateCuboidMaterial(IMaterialScatterModel model) {
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                for (int k = 0; k < nz; k++) {
                    updateCuboidMaterial(i, j, k, model);
                }
            }
        }
    }



    public void updateCuboidMaterial(Material material) throws EPQException {
        updateCuboidMaterial(new BasicMaterialModel(material));
    }



    public void updateSurroundingMaterial(IMaterialScatterModel model) {
        mScatterModel = model;
    }



    public void updateSurroundingMaterial(Material material)
            throws EPQException
    {
        updateSurroundingMaterial(new BasicMaterialModel(material));
    }



    @Override
    protected RegionBase containingSubRegion(double[] pos) {
        int[] indices = getCuboidIndices(pos);
        int i = indices[0];
        int j = indices[1];
        int k = indices[2];

        if (i < 0 || i >= nx)
            return this;
        if (j < 0 || j >= ny)
            return this;
        if (k < 0)
            return this;
        if (k >= nz)
            return null; // Vacuum (BSE)

        return cuboids[i][j][k];
    }



    private int[] getCuboidIndices(double[] pos) {
        int i = (int) Math.floor((pos[0] - x0) / dx);
        int j = (int) Math.floor((pos[1] - y0) / dy);
        int k = (int) Math.floor((pos[2] - z0) / dz);

        return new int[] { i, j, k };
    }



    @Override
    public RegionBase findEndOfStep(double[] pos0, double[] pos1) {
        RegionBase pos0Region = containingSubRegion(pos0);
        RegionBase pos1Region = containingSubRegion(pos1);

        if (pos0Region == pos1Region)
            return pos0Region;

        if (pos0Region instanceof Cuboid) {
            Cuboid cuboid = (Cuboid) pos0Region;

            double v[] = Math2.minus(pos1, pos0);

            double x = v[0] > 0 ? cuboid.x1 : cuboid.x0;
            double y = v[1] > 0 ? cuboid.y1 : cuboid.y0;
            double z = v[2] > 0 ? cuboid.z1 : cuboid.z0;

            // Find intersecting point
            double t0 = v[0] != 0 ? (x - pos0[0]) / v[0] : 1;
            double t1 = v[1] != 0 ? (y - pos0[1]) / v[1] : 1;
            double t2 = v[2] != 0 ? (z - pos0[2]) / v[2] : 1;

            double t = Math.min(t0, Math.min(t1, t2));

            double[] posi = Math2.plus(pos0, Math2.multiply(t, v));

            // Update pos1
            pos1[0] = posi[0];
            pos1[1] = posi[1];
            pos1[2] = posi[2];

            // Find next region
            int[] indices = getCuboidIndices(posi);
            int i = indices[0];
            int j = indices[1];
            int k = indices[2];

            if (v[0] < 0)
                i--;
            if (v[1] < 0)
                j--;
            if (v[2] < 0)
                k--;

            if (i < 0 || i >= nx)
                return this;
            if (j < 0 || j >= ny)
                return this;
            if (k < 0)
                return this;
            if (k >= nz)
                return null; // Vacuum (BSE)

            return cuboids[i][j][k];
        } else if (pos1Region instanceof Cuboid) {
            double v[] = Math2.minus(pos1, pos0);

            double x = v[0] > 0 ? x1 : x0;
            double y = v[1] > 0 ? y1 : y0;
            double z = v[2] > 0 ? z1 : z0;

            // Find intersecting point
            double t0 = v[0] != 0 ? (x - pos0[0]) / v[0] : 1;
            double t1 = v[1] != 0 ? (y - pos0[1]) / v[1] : 1;
            double t2 = v[2] != 0 ? (z - pos0[2]) / v[2] : 1;

            double t = Math.min(t0, Math.min(t1, t2));

            double[] posi = Math2.plus(pos0, Math2.multiply(t, v));

            // Update pos1
            pos1[0] = posi[0];
            pos1[1] = posi[1];
            pos1[2] = posi[2];

            // Find next region
            int[] indices = getCuboidIndices(posi);
            int i = indices[0];
            int j = indices[1];
            int k = indices[2];

            if (v[0] < 0)
                i--;
            if (v[1] < 0)
                j--;
            if (v[2] < 0)
                k--;

            if (i < 0 || i >= nx)
                return this;
            if (j < 0 || j >= ny)
                return this;
            if (k < 0)
                return this;
            if (k >= nz)
                return null; // Vacuum (BSE)

            return cuboids[i][j][k];
        } else {
            // Check for exiting to vacuum
            double v[] = Math2.minus(pos1, pos0);
            double t = (z0 - pos0[2]) / v[2];

            double[] posi = Math2.plus(pos0, Math2.multiply(t, v));

            // Update pos1
            pos1[0] = posi[0];
            pos1[1] = posi[1];
            pos1[2] = posi[2];

            return this;
        }
    }

}
