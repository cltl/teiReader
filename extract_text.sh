#!/bin/sh

# NAME: extract_text.sh
# AUTHOR: Sophie Arnoult
# DATE: 16 Dec 2019
#
# Arguments:
#  - input TEI file or directory; input files must have a '.xml' extension
# Options:
#  - d: output directory for extracted text files
#  - p: write one file per page
#
# Example usages are provided below
# --------------------------------------------------------------------


# Usage 1: reads from file or from directory, outputs each file in the current directory (one output file per input file)
java -jar target/chronicles-reader-1.0-SNAPSHOT-jar-with-dependencies.jar -i $1

# Usage 2: reads from file or from directory,
# outputs each file in the output directory specified by the second argument to this script
# java -jar target/chronicles-reader-1.0-SNAPSHOT-jar-with-dependencies.jar -i $1 -d $2

# Usage 3: reads from file or from directory,
# the output directory is specified by the second argument to this script,
# and output files are printed one file per page, in a shared directory
# java -jar target/chronicles-reader-1.0-SNAPSHOT-jar-with-dependencies.jar -i $1 -d $2 -p
