#!/bin/sh

# NAME: tei2text.sh
# AUTHOR: Sophie Arnoult
# DATE: 08 January 2020
#
# Options:
#  - i: (compulsory) input TEI file or directory; input files must have a '.xml' extension
#  - d: output directory for extracted text files
#  - p: paginate one file per page
#  - n: extract notes after their containing text
#  - c: complete paragraphs interrupted by page breaks
#  - m: module (missives or chronicles); defaults to chronicles
#
# --------------------------------------------------------------------

usage() {
  echo "Usage: $0 [ -i input ] [ -d out_dir ] [ -p paginate ] \
	[ -n extract_notes ] [ -c complete_paragraphs ] \
        [ -s split_notes ] [ -m module (chronicles|missives)]" 1>&2
  exit 1
}

module="chronicles"
while getopts ":i:d:psncm:" opt; do
  case "$opt" in
    i)
      input=$OPTARG ;;
    d)
      outdir=$OPTARG ;;
    p)
      paginate=1 ;;
    s)
      split_notes=1 ;;
    n)
      extract_notes=1 ;;
    c)
      complete_paragraphs=1 ;;
    m)
      module=$OPTARG ;;
    *)
      usage ;;
  esac
done
shift $((OPTIND - 1))

if [[ -z ${input} ]]; then
  2&> echo "Please specify an input file or directory (-i)"
  exit 1 
fi
args="-i $input "
if [[ ! -z ${outdir} ]]; then
  args="$args-d $outdir "
  [[ ! -d $outdir ]] && mkdir -p $outdir
fi
if [[ "$paginate" == 1 ]]; then
  args="$args-p "
fi
if [[ "$split_notes" == 1 ]]; then
  args="$args-s "
fi  
if [[ "$extract_notes" == 1 ]]; then
  args="$args-n "
fi  
if [[ "$complete_paragraphs" == 1 ]]; then
  args="$args-c "
fi
java -jar ${module}/target/${module}-1.0-SNAPSHOT-jar-with-dependencies.jar ${args}

