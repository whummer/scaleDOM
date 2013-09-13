ScaleDOM
========

A lazy-loading DOM implementation for processing huge XML documents.

Usage 
-----

Please refer to the folder "example" for a small sample project.
The class ScaleDomParsingTest illustrates how to dynamically 
enable/disable ScaleDOM parsing using the corresponding system property:

```
System.setProperty(
    "javax.xml.parsers.DocumentBuilderFactory", 
    ScaleDomDocumentBuilderFactory.class.getName()
);
```

To run the sample project, first build and install ScaleDOM using

```
mvn install
```

and then run the tests in the "example" project:

```
cd example
mvn test
```

Project Details 
---------------

For detailed information, please refer to the document doc/ScaleDOM.pdf, 
which also contains a small performance evaluation of ScaleDOM.

Change Log
----------

- 2013-09-13: v1.1
  * allow dynamic switching between ScaleDOM and Xerces using system 
    property "javax.xml.parsers.DocumentBuilderFactory"
  * add isScaleDomEnabled() method to the patched Xerces classes 
    (CoreDocumentImpl, DocumentFragmentImpl, ElementImpl, 
    EntityReferenceImpl, ParentNode) to dynamically detect whether 
    we are in "ScaleDOM mode" or not.
  * add example project to illustrate the use of ScaleDOM
  * source code compatibility with Java 1.6 (removed 1.7 specific code)
- 2013-08-29: v1.0
  * initial release with base functionality

Developers
----------

* Dominik Rauch (e0825084@student.tuwien.ac.at)
* Waldemar Hummer (hummer@infosys.tuwien.ac.at)

License
-------

ScaleDOM is published open-source under the Apache License 2.0.

