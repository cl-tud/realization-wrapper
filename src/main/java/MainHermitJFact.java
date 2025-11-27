import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator;
import org.semanticweb.owlapi.util.InferredPropertyAssertionGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import uk.ac.manchester.cs.jfact.JFactFactory;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;



import java.io.File;
import java.util.Arrays;
import java.util.List;

public class MainHermitJFact {
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("Usage: java -jar realization-wrapper.jar input.owl output.ttl [hermit | jfact | elk | struct]");
            System.exit(1);
        }

        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = man.getOWLDataFactory();

        // load your TBox + ABox
        OWLOntology base = man.loadOntologyFromOntologyDocument(inputFile);

        String r = args[2].toLowerCase();
        OWLReasonerFactory rf;

        switch (r) {
            case "hermit":
                rf = new ReasonerFactory();
                break;

            case "jfact":
                rf = new JFactFactory();
                break;

            case "elk":
                rf = new ElkReasonerFactory();
                break;

            case "struct":
                rf = new StructuralReasonerFactory();
                break;

            default:
                throw new IllegalArgumentException("Unknown reasoner: " + r);
        }
        OWLReasoner reasoner = rf.createReasoner(base);

        // turn on precomputation (optional but recommended)
        reasoner.precomputeInferences(
                InferenceType.CLASS_HIERARCHY,
                InferenceType.CLASS_ASSERTIONS,
                InferenceType.OBJECT_PROPERTY_ASSERTIONS,
                InferenceType.DATA_PROPERTY_ASSERTIONS
        );

        // materialisation target
        OWLOntology mat = man.createOntology();

        // configure what you want to materialise
        List<InferredAxiomGenerator<? extends OWLAxiom>> gens =
                Arrays.<InferredAxiomGenerator<? extends OWLAxiom>>asList(
                        new InferredClassAssertionAxiomGenerator(),
                        new InferredPropertyAssertionGenerator()
        );

        // run the generator
        InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner, gens);
        iog.fillOntology(df, mat);

        // export the ontology as turtle
        man.saveOntology(mat, new TurtleDocumentFormat(), IRI.create(outputFile));
        System.out.println("Saved inferred axioms to: " + outputFile.getAbsolutePath());
    }
}
