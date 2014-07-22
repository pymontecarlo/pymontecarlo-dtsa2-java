package pymontecarlo.fileformat.options;

import gov.nist.microanalysis.EPQLibrary.AbsoluteIonizationCrossSection;
import gov.nist.microanalysis.EPQLibrary.AlgorithmClass;
import gov.nist.microanalysis.EPQLibrary.BetheElectronEnergyLoss;
import gov.nist.microanalysis.EPQLibrary.CzyzewskiMottScatteringAngle;
import gov.nist.microanalysis.EPQLibrary.EPQException;
import gov.nist.microanalysis.EPQLibrary.IonizationCrossSection;
import gov.nist.microanalysis.EPQLibrary.MassAbsorptionCoefficient;
import gov.nist.microanalysis.EPQLibrary.MeanIonizationPotential;
import gov.nist.microanalysis.EPQLibrary.NISTMottScatteringAngle;
import gov.nist.microanalysis.EPQLibrary.ProportionalIonizationCrossSection;
import gov.nist.microanalysis.EPQLibrary.RandomizedScatterFactory;
import gov.nist.microanalysis.EPQLibrary.ScreenedRutherfordScatteringAngle;
import gov.nist.microanalysis.EPQLibrary.Strategy;
import gov.nist.microanalysis.EPQLibrary.ToSI;
import gov.nist.microanalysis.EPQLibrary.XRayTransition;
import static gov.nist.microanalysis.EPQLibrary.MassAbsorptionCoefficient.UserSpecifiedCoefficient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jdom2.DataConversionException;
import org.jdom2.Element;

import pymontecarlo.fileformat.Extractor;
import pymontecarlo.fileformat.ExtractorManager;
import pymontecarlo.program.nistmonte.options.model.FluorescenceMC;

/**
 * Factory of model extractors.
 * 
 * @author ppinard
 */
public class ModelExtractorFactory {

    public static final class Model {

        public final String name;

        public final String type;



        public Model(String type, String name) {
            if (type == null)
                throw new NullPointerException("type == null");
            this.type = type;

            if (name == null)
                throw new NullPointerException("name == null");
            this.name = name;
        }



        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }



        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;

            Model other = (Model) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            if (type == null) {
                if (other.type != null)
                    return false;
            } else if (!type.equals(other.type))
                return false;

            return true;
        }
    }

    public static final class DTSA2Model {

        public final Class<? extends AlgorithmClass> type;

        public final AlgorithmClass algorithm;



        public DTSA2Model(Class<? extends AlgorithmClass> type,
                AlgorithmClass algorithm) {
            if (type == null)
                throw new NullPointerException("type == null");
            this.type = type;

            if (algorithm == null)
                throw new NullPointerException("algorithm == null");
            this.algorithm = algorithm;
        }
    }

    protected static class RegisteredModelExtractor implements
            ModelExtractor {

        private final Map<Model, DTSA2Model> map;



        public RegisteredModelExtractor() {
            map = new HashMap<>();

            // Elastic cross section
            register(new Model("elastic cross section",
                    "Mott by interpolation (Czyzewski)"), new DTSA2Model(
                    RandomizedScatterFactory.class,
                    CzyzewskiMottScatteringAngle.Factory));
            register(new Model("elastic cross section",
                    "Rutherford"), new DTSA2Model(
                    RandomizedScatterFactory.class,
                    ScreenedRutherfordScatteringAngle.Factory));
            register(new Model("elastic cross section",
                    "ELSEPA"), new DTSA2Model(
                    RandomizedScatterFactory.class,
                    NISTMottScatteringAngle.Factory));

            // Ionization cross section
            register(new Model("ionization cross section",
                    "Pouchou 1986"), new DTSA2Model(
                    IonizationCrossSection.class,
                    ProportionalIonizationCrossSection.Pouchou86));
            register(new Model("ionization cross section",
                    "Dijkstra and Heijliger 1998 (PROZA96)"), new DTSA2Model(
                    IonizationCrossSection.class,
                    ProportionalIonizationCrossSection.Proza96));
            register(new Model("ionization cross section",
                    "Casnati 1982"), new DTSA2Model(
                    IonizationCrossSection.class,
                    AbsoluteIonizationCrossSection.Casnati82));
            register(new Model("ionization cross section",
                    "Bote and Salvat 2008"), new DTSA2Model(
                    IonizationCrossSection.class,
                    AbsoluteIonizationCrossSection.BoteSalvat2008));

            // Ionization potential
            register(new Model("ionization potential",
                    "Berger & Seltzer 1964"), new DTSA2Model(
                    MeanIonizationPotential.class,
                    MeanIonizationPotential.Berger64));
            register(new Model("ionization potential",
                    "Berger & Seltzer 1983"), new DTSA2Model(
                    MeanIonizationPotential.class,
                    MeanIonizationPotential.Berger83));
            register(new Model("ionization potential",
                    "Berger & Seltzer 1983 (CITZAF)"), new DTSA2Model(
                    MeanIonizationPotential.class,
                    MeanIonizationPotential.BergerAndSeltzerCITZAF));
            register(new Model("ionization potential",
                    "Zeller 1975"), new DTSA2Model(
                    MeanIonizationPotential.class,
                    MeanIonizationPotential.Zeller75));
            register(new Model("ionization potential",
                    "Duncumb & DeCasa 1969"), new DTSA2Model(
                    MeanIonizationPotential.class,
                    MeanIonizationPotential.Duncumb69));
            register(new Model("ionization potential",
                    "Heinrich & Yakowitz 1970"), new DTSA2Model(
                    MeanIonizationPotential.class,
                    MeanIonizationPotential.Heinrich70));
            register(new Model("ionization potential",
                    "Springer 1967"), new DTSA2Model(
                    MeanIonizationPotential.class,
                    MeanIonizationPotential.Springer67));
            register(new Model("ionization potential",
                    "Wilson 1941"), new DTSA2Model(
                    MeanIonizationPotential.class,
                    MeanIonizationPotential.Wilson41));
            register(new Model("ionization potential",
                    "Bloch 1933"), new DTSA2Model(
                    MeanIonizationPotential.class,
                    MeanIonizationPotential.Bloch33));
            register(new Model("ionization potential",
                    "Sternheimer 1964"), new DTSA2Model(
                    MeanIonizationPotential.class,
                    MeanIonizationPotential.Sternheimer64));

            // Energy loss
            register(new Model("energy loss",
                    "Bethe 1930"), new DTSA2Model(
                    BetheElectronEnergyLoss.class,
                    BetheElectronEnergyLoss.Bethe1930Strict));
            register(new Model("energy loss",
                    "Modified Bethe 1930"), new DTSA2Model(
                    BetheElectronEnergyLoss.class,
                    BetheElectronEnergyLoss.Bethe1930));
            register(new Model("energy loss",
                    "Joy and Luo 1989"), new DTSA2Model(
                    BetheElectronEnergyLoss.class,
                    BetheElectronEnergyLoss.JoyLuo1989));

            // Mass absorption coefficient
            register(new Model("mass absorption coefficient",
                    "Ruste 1979"), new DTSA2Model(
                    MassAbsorptionCoefficient.class,
                    MassAbsorptionCoefficient.Ruste79));
            register(new Model("mass absorption coefficient",
                    "Pouchou and Pichoir 1991"), new DTSA2Model(
                    MassAbsorptionCoefficient.class,
                    MassAbsorptionCoefficient.Pouchou1991));
            register(new Model("mass absorption coefficient",
                    "Pouchou and Pichoir 1988"), new DTSA2Model(
                    MassAbsorptionCoefficient.class,
                    MassAbsorptionCoefficient.PouchouPichoir88));
            register(new Model("mass absorption coefficient",
                    "Henke 1982"), new DTSA2Model(
                    MassAbsorptionCoefficient.class,
                    MassAbsorptionCoefficient.Henke82));
            register(new Model("mass absorption coefficient",
                    "Henke 1993"), new DTSA2Model(
                    MassAbsorptionCoefficient.class,
                    MassAbsorptionCoefficient.Henke1993));
            register(new Model("mass absorption coefficient",
                    "Bastin and Heijligers 1985, 1988, 1989"), new DTSA2Model(
                    MassAbsorptionCoefficient.class,
                    MassAbsorptionCoefficient.BastinHeijligers89));
            register(new Model("mass absorption coefficient",
                    "Heinrich IXCOM 11 (DTSA)"), new DTSA2Model(
                    MassAbsorptionCoefficient.class,
                    MassAbsorptionCoefficient.HeinrichDtsa));
            register(new Model("mass absorption coefficient",
                    "Heinrich IXCOM 11"), new DTSA2Model(
                    MassAbsorptionCoefficient.class,
                    MassAbsorptionCoefficient.Heinrich86));
            register(new Model("mass absorption coefficient",
                    "NIST-Chantler 2005"), new DTSA2Model(
                    MassAbsorptionCoefficient.class,
                    MassAbsorptionCoefficient.Chantler2005));
            register(new Model("mass absorption coefficient",
                    "DTSA CitZAF"), new DTSA2Model(
                    MassAbsorptionCoefficient.class,
                    MassAbsorptionCoefficient.DTSA_CitZAF));
            register(new Model("mass absorption coefficient",
                    "No MAC"), new DTSA2Model(
                    MassAbsorptionCoefficient.class,
                    MassAbsorptionCoefficient.Null));

            // Fluorescence
            register(new Model("fluorescence", "no fluorescence"),
                    new DTSA2Model(
                            FluorescenceMC.class, FluorescenceMC.Null));
            register(new Model("fluorescence", "fluorescence"), new DTSA2Model(
                    FluorescenceMC.class, FluorescenceMC.Fluorescence));
            register(new Model("fluorescence", "fluorescence with Compton"),
                    new DTSA2Model(FluorescenceMC.class,
                            FluorescenceMC.FluorescenceCompton));
        }



        private void register(Model model, DTSA2Model nistModel) {
            if (map.put(model, nistModel) != null)
                throw new IllegalArgumentException("Model (" + model
                        + ") already registered");
        }



        @Override
        public Strategy extract(Element modelElement) throws IOException {
            String type = modelElement.getAttributeValue("type");
            String name = modelElement.getAttributeValue("name");

            Model model = new Model(type, name);
            DTSA2Model nistModel = map.get(model);

            if (nistModel == null)
                throw new IOException("Model (" + model + ") not found");

            Strategy st = new Strategy();
            st.addAlgorithm(nistModel.type, nistModel.algorithm);
            return st;
        }

    }

    public static final ModelExtractor REGISTERED =
            new RegisteredModelExtractor();

    protected static class UserDefinedMassAbsorptionCoefficientModelExtractor
            implements ModelExtractor {

        @Override
        public Strategy extract(Element modelElement) throws IOException {
            // Read base model
            Element baseElement = modelElement.getChild("model", Extractor.ns);
            ModelExtractor extractor =
                    (ModelExtractor) ExtractorManager.getExtractor(baseElement
                            .getName());
            Strategy baseStrategy = extractor.extract(baseElement);
            MassAbsorptionCoefficient baseMAC =
                    (MassAbsorptionCoefficient) baseStrategy
                            .getAlgorithm(MassAbsorptionCoefficient.class);

            // Read MAC values
            UserSpecifiedCoefficient mac =
                    new UserSpecifiedCoefficient(baseMAC);

            gov.nist.microanalysis.EPQLibrary.Element absorber;
            double energy, macValue;
            int z, src, dest;
            XRayTransition transition;
            for (Element subelement : modelElement.getChildren("mac")) {
                try {
                    absorber =
                            gov.nist.microanalysis.EPQLibrary.Element
                                    .byAtomicNumber(subelement.getAttribute(
                                            "absorber").getIntValue());
                } catch (DataConversionException e) {
                    throw new IOException(e);
                }

                if (subelement.getAttribute("energy") != null) {
                    try {
                        energy =
                                subelement.getAttribute("energy")
                                        .getDoubleValue();
                    } catch (DataConversionException e) {
                        throw new IOException(e);
                    }

                } else {
                    try {
                        z = subelement.getAttribute("z").getIntValue();
                        src = subelement.getAttribute("src").getIntValue() - 1;
                        dest =
                                subelement.getAttribute("dest").getIntValue() - 1;
                    } catch (DataConversionException e) {
                        throw new IOException(e);
                    }

                    transition =
                            new XRayTransition(
                                    gov.nist.microanalysis.EPQLibrary.Element
                                            .byAtomicNumber(z),
                                    src, dest);

                    try {
                        energy = transition.getEnergy_eV();
                    } catch (EPQException e) {
                        throw new IOException(e);
                    }
                }

                macValue = Double.parseDouble(subelement.getText());

                mac.put(absorber, ToSI.eV(energy), macValue);
            }

            Strategy st = new Strategy();
            st.addAlgorithm(MassAbsorptionCoefficient.class, mac);
            return st;
        }
    }

    public static final ModelExtractor USER_DEFINED_MASS_ABSORPTION_COEFFICIENT =
            new UserDefinedMassAbsorptionCoefficientModelExtractor();
}
