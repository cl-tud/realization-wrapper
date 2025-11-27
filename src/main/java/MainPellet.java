import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredPropertyAssertionGenerator;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class MainPellet {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: java -jar realization-wrapper-pellet.jar input.owl output.ttl");
            System.exit(1);
        }

        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = man.getOWLDataFactory();
        OWLOntology base = man.loadOntologyFromOntologyDocument(inputFile);

        OWLReasoner reasoner = new PelletReasonerFactory().createReasoner(base);
        reasoner.precomputeInferences(
                InferenceType.CLASS_ASSERTIONS,
                InferenceType.OBJECT_PROPERTY_ASSERTIONS,
                InferenceType.DATA_PROPERTY_ASSERTIONS
        );

        OWLOntology mat = man.createOntology();
        List<InferredAxiomGenerator<? extends OWLAxiom>> gens = Arrays.asList(
                new InferredClassAssertionAxiomGenerator(),
                new InferredPropertyAssertionGenerator()
        );
        InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner, gens);
        iog.fillOntology(df, mat);

        man.saveOntology(mat, new TurtleDocumentFormat(), IRI.create(outputFile));
        System.out.println("Saved inferred axioms to: " + outputFile.getAbsolutePath());
    }
}
