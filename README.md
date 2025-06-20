# Realization Wrapper

A simple wrapper for HermiT's OWL API capable of completing the data graph and providing all class and object property assertions given an ontology.

## Usage:
After obtaining the jar file, either from the Releases section or by building from sources, execute:
```
java -jar realization-wrapper.jar <INPUT ONTOLOGY PATH> <OUTPUT-ONTOLOGY PATH>
```
for example, assume the file [test-cases/test.ttl](test-cases/test.ttl), with the following content:
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
Running `java -jar realization-reasoner.jar test-cases/test.ttl test-out.ttl` will produce an output `test-out.ttl` file containing the expected conclusions, i.e.
```
<http://example.org#x> rdf:type owl:NamedIndividual ,
                                <http://example.org#Class1> ,
                                owl:Thing ;
                       <http://example.org#role1> <http://example.org#i1> ;
                       <http://example.org#role2> <http://example.org#i2> .
```

## Building:
Run:

```
mvn clean package
```
in the cloned repository directory. A fat jar file will be created in the [target](target) directory.