#!/bin/sh

# NAME: extract_text.sh
# AUTHOR: Sophie Arnoult
# DATE: 16 Dec 2019
#
# Arguments:
#  - input TEI file; the file should have a '.xml' extension
#  - output directory for extracted text files
# --------------------------------------------------------------------

java -jar target/chronicles-reader-1.0-SNAPSHOT-jar-with-dependencies.jar $1 $2 
