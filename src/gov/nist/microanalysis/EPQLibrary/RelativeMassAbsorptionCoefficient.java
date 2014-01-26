package gov.nist.microanalysis.EPQLibrary;

/**
 * Decreases or increases an existing table of mass absorption coefficient by a
 * certain percentage.
 * 
 * @author Philippe T. Pinard
 */
public class RelativeMassAbsorptionCoefficient extends
        MassAbsorptionCoefficient {

    /** Base table of mass absorption coefficient. */
    private final MassAbsorptionCoefficient baseMAC;

    /** Variation factor on the MAC. */
    private final double variationFactor;



    /**
     * Creates a new <code>UncertainMassAbsorptionCoefficient</code>.
     * 
     * @param baseMAC
     *            base table of mass absorption coefficient
     * @param variationFactor
     *            relative variation (between -1.0 and 1.0) to increase/decrease
     *            from the base MAC
     */
    public RelativeMassAbsorptionCoefficient(
            MassAbsorptionCoefficient baseMAC, double variationFactor) {
        super(baseMAC.getName() + " with " + variationFactor * 100.0
                + "% unc", baseMAC.getReference());

        if (variationFactor < -1.0 || variationFactor > 1.0)
            throw new IllegalArgumentException(
                    "Relative uncertainty must be between [-1.0, 1.0]: "
                            + variationFactor);

        this.baseMAC = baseMAC;
        this.variationFactor = variationFactor;
    }



    @Override
    public boolean isAvailable(Element el, double energy) {
        return baseMAC.isAvailable(el, energy);
    }



    @Override
    public double compute(Element el, double energy) {
        return baseMAC.compute(el, energy) * (1.0 + variationFactor);
    }

}
