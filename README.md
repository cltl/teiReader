# TEI text extraction 

This repository provides code for the extraction of text from TEI files for the *Chronicles* and *Clariah WP6* projects. 

## Quick start

Download the content of this repository, and run:

>   mvn clean package


This will generate code for binding TEI XML elements to Java objects, as well as an executable jar for text extraction for both the `chronicles` and `missives` projects. 

Binding was tested with `xjc` version 2.3.1 under Java 10, and `xjc` version 2.2.8 under Java 8.

The executable jar file takes as arguments:

* [-i]: input file or directory; 
* [-d] (optional): output directory; 
* [-p] (optional): pagination flag; this will create one output text file per page in the input file; 
* [-n] (optional): extracts notes and places them after the element they appear in; defaults to keeping notes inline in the text.
* [-s] (optional): splits running text and notes into separate (paired) documents
* [-c] (optional): completes paragraphs interrupted by page breaks (removing hyphens and merging paragraph parts)

The `./extract_text.sh` script is set to call the `missives` jar, and extract one text file per input file into an output directory 'out-dir', without pagination or note extraction:

>   bash extract_text.sh [path-to-TEI-input-file] [out-dir]
  

## XML-Java binding and code generation

The code relies on [Jaxb](https://javaee.github.io/jaxb-v2/) to map the TEI XML representation to java objects. The classes for these objects are generated at compilation with the [maven-jaxb-plugin](https://github.com/highsource/maven-jaxb2-plugin). The code can be generated (without `jar` packaging) with:

>   mvn clean compile

Code binding relies on a XSD schema for the input TEI files, and on a bindings file. The schema and bindings are located in `./src/main/resources/` for each of the `chronicles` and `missives` modules.

## Binding schema 

### Chronicles
The XSD file was obtained from the [teixlite.rng](https://tei-c.org/Vault/P4/xml/custom/schema/relaxng/teixlite.rng) schema provided by the TEI Consortium, converting it to XSD with [trang](https://relaxng.org/jclark/trang.html).

This schema is not exactly the same as the [DBNL schema](https://www.dbnl.org/xml/dtd/teixlite.dtd) used for the chronicles. This notably means that `cf` sections are not extracted for now.  

### Missives
We are using [tei_lite.xsd](https://tei-c.org/release/xml/tei/custom/schema/xsd/tei_lite.xsd).


## Formatting 
### Chronicles
The following formatting decisions have been taken when extracting the text from TEI-XML to plain text:

- Table and Lg elements are marked with a [TABLE] or [<type>] tag, e.g., [POEM];
- Margin notes are marked by square brackets;
- Foot notes are referred to by their number in the text, and are placed either inline or after the text element containing them;
- Paragraphs that are interrupted by page breaks are regrouped (hyphens marking line breaks are removed)
- interpGrp elements are ignored, whereas they may be useful (they provide dates);
- Line breaks have been added to increase readability (after headings, before tables, etc.)

### Missives
Formatting is minimal:

- Line-break elements are ignored
- 'fw' elements are ignored
