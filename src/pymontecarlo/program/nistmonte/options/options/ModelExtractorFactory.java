package pymontecarlo.program.nistmonte.options.options;

import gov.nist.microanalysis.EPQLibrary.AbsoluteIonizationCrossSection;
import gov.nist.microanalysis.EPQLibrary.AlgorithmClass;
import gov.nist.microanalysis.EPQLibrary.BetheElectronEnergyLoss;
import gov.nist.microanalysis.EPQLibrary.CzyzewskiMottScatteringAngle;
import gov.nist.microanalysis.EPQLibrary.IonizationCrossSection;
import gov.nist.microanalysis.EPQLibrary.MassAbsorptionCoefficient;
import gov.nist.microanalysis.EPQLibrary.MeanIonizationPotential;
import gov.nist.microanalysis.EPQLibrary.NISTMottScatteringAngle;
import gov.nist.microanalysis.EPQLibrary.ProportionalIonizationCrossSection;
import gov.nist.microanalysis.EPQLibrary.RandomizedScatterFactory;
import gov.nist.microanalysis.EPQLibrary.ScreenedRutherfordScatteringAngle;
import gov.nist.microanalysis.EPQLibrary.Strategy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jdom2.Element;

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

    public static final class NISTModel {

        public final Class<? extends AlgorithmClass> type;

        public final AlgorithmClass algorithm;



        public NISTModel(Class<? extends AlgorithmClass> type,
                AlgorithmClass algorithm) {
            if (type == null)
                throw new NullPointerException("type == null");
            this.type = type;

            if (algorithm == null)
                throw new NullPointerException("algorithm == null");
            this.algorithm = algorithm;
        }
    }

    private static final Map<Model, NISTModel> map = new HashMap<>();



    public static void register(Model model, NISTModel nistModel) {
        if (map.put(model, nistModel) != null)
            throw new IllegalArgumentException("Model (" + model
                    + ") already registered");
    }

    static {
        // Elastic cross section
        register(new Model("elastic cross section",
                "Mott by interpolation (Czyzewski)"), new NISTModel(
                RandomizedScatterFactory.class,
                CzyzewskiMottScatteringAngle.Factory));
        register(new Model("elastic cross section",
                "Rutherford"), new NISTModel(
                RandomizedScatterFactory.class,
                ScreenedRutherfordScatteringAngle.Factory));
        register(new Model("elastic cross section",
                "ELSEPA"), new NISTModel(
                RandomizedScatterFactory.class,
                NISTMottScatteringAngle.Factory));

        // Ionization cross section
        register(new Model("ionization cross section",
                "Pouchou 1986"), new NISTModel(
                IonizationCrossSection.class,
                ProportionalIonizationCrossSection.Pouchou86));
        register(new Model("ionization cross section",
                "Dijkstra and Heijliger 1998 (PROZA96)"), new NISTModel(
                IonizationCrossSection.class,
                ProportionalIonizationCrossSection.Proza96));
        register(new Model("ionization cross section",
                "Casnati 1982"), new NISTModel(
                IonizationCrossSection.class,
                AbsoluteIonizationCrossSection.Casnati82));
        register(new Model("ionization cross section",
                "Bote and Salvat 2008"), new NISTModel(
                IonizationCrossSection.class,
                AbsoluteIonizationCrossSection.BoteSalvat2008));

        // Ionization potential
        register(new Model("ionization potential",
                "Berger & Seltzer 1964"), new NISTModel(
                MeanIonizationPotential.class,
                MeanIonizationPotential.Berger64));
        register(new Model("ionization potential",
                "Berger & Seltzer 1983"), new NISTModel(
                MeanIonizationPotential.class,
                MeanIonizationPotential.Berger83));
        register(new Model("ionization potential",
                "Berger & Seltzer 1983 (CITZAF)"), new NISTModel(
                MeanIonizationPotential.class,
                MeanIonizationPotential.BergerAndSeltzerCITZAF));
        register(new Model("ionization potential",
                "Zeller 1975"), new NISTModel(
                MeanIonizationPotential.class,
                MeanIonizationPotential.Zeller75));
        register(new Model("ionization potential",
                "Duncumb & DeCasa 1969"), new NISTModel(
                MeanIonizationPotential.class,
                MeanIonizationPotential.Duncumb69));
        register(new Model("ionization potential",
                "Heinrich & Yakowitz 1970"), new NISTModel(
                MeanIonizationPotential.class,
                MeanIonizationPotential.Heinrich70));
        register(new Model("ionization potential",
                "Springer 1967"), new NISTModel(
                MeanIonizationPotential.class,
                MeanIonizationPotential.Springer67));
        register(new Model("ionization potential",
                "Wilson 1941"), new NISTModel(
                MeanIonizationPotential.class,
                MeanIonizationPotential.Wilson41));
        register(new Model("ionization potential",
                "Bloch 1933"), new NISTModel(
                MeanIonizationPotential.class,
                MeanIonizationPotential.Bloch33));
        register(new Model("ionization potential",
                "Sternheimer 1964"), new NISTModel(
                MeanIonizationPotential.class,
                MeanIonizationPotential.Sternheimer64));

        // Energy loss
        register(new Model("energy loss",
                "Bethe 1930"), new NISTModel(
                BetheElectronEnergyLoss.class,
                BetheElectronEnergyLoss.Bethe1930Strict));
        register(new Model("energy loss",
                "Modified Bethe 1930"), new NISTModel(
                BetheElectronEnergyLoss.class,
                BetheElectronEnergyLoss.Bethe1930));
        register(new Model("energy loss",
                "Joy and Luo 1989"), new NISTModel(
                BetheElectronEnergyLoss.class,
                BetheElectronEnergyLoss.JoyLuo1989));

        // Mass absorption coefficient
        register(new Model("mass absorption coefficient",
                "Ruste 1979"), new NISTModel(
                MassAbsorptionCoefficient.class,
                MassAbsorptionCoefficient.Ruste79));
        register(new Model("mass absorption coefficient",
                "Pouchou and Pichoir 1991"), new NISTModel(
                MassAbsorptionCoefficient.class,
                MassAbsorptionCoefficient.Pouchou1991));
        register(new Model("mass absorption coefficient",
                "Pouchou and Pichoir 1988"), new NISTModel(
                MassAbsorptionCoefficient.class,
                MassAbsorptionCoefficient.PouchouPichoir88));
        register(new Model("mass absorption coefficient",
                "Henke 1982"), new NISTModel(
                MassAbsorptionCoefficient.class,
                MassAbsorptionCoefficient.Henke82));
        register(new Model("mass absorption coefficient",
                "Henke 1993"), new NISTModel(
                MassAbsorptionCoefficient.class,
                MassAbsorptionCoefficient.Henke1993));
        register(new Model("mass absorption coefficient",
                "Bastin and Heijligers 1985, 1988, 1989"), new NISTModel(
                MassAbsorptionCoefficient.class,
                MassAbsorptionCoefficient.BastinHeijligers89));
        register(new Model("mass absorption coefficient",
                "Heinrich IXCOM 11 (DTSA)"), new NISTModel(
                MassAbsorptionCoefficient.class,
                MassAbsorptionCoefficient.HeinrichDtsa));
        register(new Model("mass absorption coefficient",
                "Heinrich IXCOM 11"), new NISTModel(
                MassAbsorptionCoefficient.class,
                MassAbsorptionCoefficient.Heinrich86));
        register(new Model("mass absorption coefficient",
                "NIST-Chantler 2005"), new NISTModel(
                MassAbsorptionCoefficient.class,
                MassAbsorptionCoefficient.Chantler2005));
        register(new Model("mass absorption coefficient",
                "DTSA CitZAF"), new NISTModel(
                MassAbsorptionCoefficient.class,
                MassAbsorptionCoefficient.DTSA_CitZAF));
        register(new Model("mass absorption coefficient",
                "No MAC"), new NISTModel(
                MassAbsorptionCoefficient.class,
                MassAbsorptionCoefficient.Null));

        // Fluorescence
        register(new Model("fluorescence", "no fluorescence"), new NISTModel(
                FluorescenceMC.class, FluorescenceMC.Null));
        register(new Model("fluorescence", "fluorescence"), new NISTModel(
                FluorescenceMC.class, FluorescenceMC.Fluorescence));
        register(new Model("fluorescence", "fluorescence with Compton"),
                new NISTModel(FluorescenceMC.class,
                        FluorescenceMC.FluorescenceCompton));
    }

    protected static class ModelExtractorImpl implements ModelExtractor {

        @Override
        public Strategy extract(Element modelElement) throws IOException {
            String type = modelElement.getAttributeValue("type");
            String name = modelElement.getAttributeValue("name");

            Model model = new Model(type, name);
            NISTModel nistModel = map.get(model);

            if (nistModel == null)
                throw new IOException("Model (" + model + ") not found");

            Strategy st = new Strategy();
            st.addAlgorithm(nistModel.type, nistModel.algorithm);
            return st;
        }

    }

    public static final ModelExtractor ALL = new ModelExtractorImpl();
}