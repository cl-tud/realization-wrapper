# Realization Wrapper Branches

This project is maintained in three Git branches:

1) `Naive`: Naive implementation (nested loops, checking each possible consequence explicitly).

The next ones use the OWL API `InferredOntologyGenerator` to compute the chosen consequences. We are interested in class and role assertions, so we use: `InferredClassAssertionAxiomGenerator` and `InferredPropertyAssertionGenerator`.

2) `Pellet`: Only uses Pellet.
3) `HermitJFact`: Uses any of the reasoners: `HermiT`, `JFact`, `ELK` or `StructuralReasoner`. The choice of the reasoner has to be provided as a commandline argument. Note that `ELK` and `StructuralReasoner` do not compute the required class and role assertions, hence for our use only `HermiT` and `JFact` are useful.

# Running
Either get the relevant JAR from the releases section or build it from the source (see below.)

1,2) Naive + Pellet, run:
```
java -jar realization-wrapper-*.jar <input.owl> <output.ttl>
```
> **Note (Pellet only):** Pellet uses an older Guice/CGLIB version that requires an extra JVM flag on Java 16+:
> ```
> java --add-opens java.base/java.lang=ALL-UNNAMED -jar realization-wrapper-*.jar <input.owl> <output.ttl>
> ```

3) HermitJFact
```
java -jar target/realization-wrapper-1.0.jar <input.owl> <output.ttl> <hermit|jfact|elk|struct>
```

# Usage:

Assume the file [test-cases/test.ttl](test-cases/test.ttl), with the following content:
```
@prefix : <http://example.org#> .
@prefix owl: <http://www.w3.org/2002/07/owl#> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .

:role1 rdf:type owl:ObjectProperty .
:role2 rdf:type owl:ObjectProperty .

:Class1 rdf:type owl:Class .

:i1 rdf:type owl:NamedIndividual .
:i2 rdf:type owl:NamedIndividual .
:x rdf:type owl:NamedIndividual ;
           :role1 :i1 .

[ rdf:type owl:Restriction ;
  owl:onProperty :role1 ;
  owl:hasValue :i1 ;
  rdfs:subClassOf [ rdf:type owl:Restriction ;
                    owl:onProperty :role2 ;
                    owl:hasValue :i2
                  ]
] .

[ rdf:type owl:Restriction ;
  owl:onProperty :role2 ;
  owl:hasValue :i2 ;
  rdfs:subClassOf :Class1
] .

```

We get:

```
<http://example.org#x> rdf:type owl:NamedIndividual ,
                                <http://example.org#Class1> ,
                                owl:Thing ;
                       <http://example.org#role1> <http://example.org#i1> ;
                       <http://example.org#role2> <http://example.org#i2> .
```




# Building
- Check out the relevant branch. 
- Build: `mvn clean package`
- Run with (depending on the choice of the reasoner):
```
java -jar realization-wrapper-*.jar <input.owl> <output.ttl>
```
or 
```
java -jar realization-wrapper-*.jar <input.owl> <output.ttl> <reasoner>
```



**Main (naive)**
- Checkout the `main` branch.
- Build: `mvn clean package`
- Run: `java -jar target/realization-wrapper-1.0.jar <input.owl> <output.ttl>`

**Pellet branch**
- Checkout the `pellet` branch (or use the `realization-wrapper-pellet-only/` folder if you have it unpacked separately).
- Build: `cd realization-wrapper-pellet-only && mvn clean package`
- Run: `java -jar target/realization-wrapper-pellet-1.0.jar <input.owl> <output.ttl>`
- **Java 16+ only:** add `--add-opens java.base/java.lang=ALL-UNNAMED` before `-jar`

**HermitJFact branch**
- Checkout the `HermitJFact` branch (mirrored here under `realization-wrapper/`).
- Build: `cd realization-wrapper && mvn clean package`
- Run: `java -jar target/realization-wrapper-1.0.jar <input.owl> <output.ttl> <hermit|jfact|elk|struct>`
- Note: `elk` and `struct` are available for quick checks, but they cannot fully materialize class and role assertions.
