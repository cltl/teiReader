#!/bin/sh

# NAME: extract_text.sh
# AUTHOR: Sophie Arnoult
# DATE: 16 Dec 2019
#
# Provides example usages for the extraction of text for the chronicles
# or missives. 
# 
# This script is set up to read missives.
#
# Arguments:
#  1- input TEI file or directory; input files must have a '.xml' extension
#  2- (optionally) an output directory
#
# --------------------------------------------------------------------

# module can be 'chronicles' or 'missives'
module=chronicles

# Usage 1: reads from file or from directory, outputs each file in 
# the current directory (one output file per input file)
bash scripts/tei2text.sh -m $module -i $1

# Usage 2: reads from file or from directory,
# outputs each file in the output directory specified by the second argument to this script
# bash scripts/tei2text.sh -m $module -i $1 -d $2 

# Usage 3: reads from file or from directory,
# the output directory is specified by the second argument to this script,
# and output files are printed one file per page, in a shared directory
# bash scripts/tei2text.sh -m $module -i $1 -d $2 -p

# Usage 4: as Usage 3, but notes are placed after their containing text,
# rather than being kept inline.
# bash scripts/tei2text.sh -m $module -i $1 -d $2 -p -n

# Usage 5: as Usage 2, but paragraphs interrupted by page breaks are completed
# bash scripts/tei2text.sh -m $module -i $1 -d $2 -c

# Usage 6: as Usage 5, but notes are written to separate files
# bash scripts/tei2text.sh -m $module -i $1 -d $2 -s -c
