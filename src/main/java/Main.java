import org.semanticweb.HermiT.ReasonerFactory;

// import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;


import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.util.*;

import java.io.File;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: java -jar realization-wrapper.jar input.owl output.ttl");
            System.exit(1);
        }

        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);

        // Load ontology
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(inputFile);
        OWLDataFactory dataFactory = manager.getOWLDataFactory();

        // Setup reasoner
        OWLReasonerFactory reasonerFactory = new ReasonerFactory();
        // OWLReasonerFactory reasonerFactory = new FaCTPlusPlusReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
        reasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS, InferenceType.OBJECT_PROPERTY_ASSERTIONS);

        // Create new ontology for inferred axioms
        OWLOntology inferredOntology = manager.createOntology();

        // Add inferred class assertions using generator
        List<InferredAxiomGenerator<? extends OWLAxiom>> generators = java.util.Arrays.asList(new InferredClassAssertionAxiomGenerator());
    
        InferredOntologyGenerator inferredOntologyGenerator = new InferredOntologyGenerator(reasoner, generators);
        inferredOntologyGenerator.fillOntology(dataFactory, inferredOntology);

        // Manually add inferred object property assertions
        Set<OWLNamedIndividual> individuals = ontology.getIndividualsInSignature();
        Set<OWLObjectProperty> properties = ontology.getObjectPropertiesInSignature();

        for (OWLNamedIndividual subject : individuals) {
            for (OWLObjectProperty property : properties) {
                NodeSet<OWLNamedIndividual> values = reasoner.getObjectPropertyValues(subject, property);
                for (OWLNamedIndividual object : values.getFlattened()) {
                    OWLObjectPropertyAssertionAxiom ax = dataFactory.getOWLObjectPropertyAssertionAxiom(
                        property, subject, object
                    );
                    manager.addAxiom(inferredOntology, ax);
                }
            }
        }

        // Save output as Turtle
        manager.saveOntology(inferredOntology, new TurtleDocumentFormat(), IRI.create(outputFile.toURI()));

        reasoner.dispose();
        System.out.println("Saved inferred axioms to: " + outputFile.getAbsolutePath());
    }
}
