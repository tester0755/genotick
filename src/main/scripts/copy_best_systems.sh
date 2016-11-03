###############################################################
# Usage:
# ./copy_best_systems.sh dir_with_population min_weight destination_dir
###############################################################

#!/bin/bash

set -e

export dir=$1
if [ "$dir" == "" ]; then
	echo "Give directory as 1st param"
	exit 1
fi

export min_weight=$2
if [ "$min_weight" == "" ]; then
	echo "Give min weight as 2nd param"
	exit 2
fi

export new_dir=$3
if [ "$new_dir" == "" ]; then
	echo "Give new directory as 3rd param"
	exit 3
fi
if [ ! -d $new_dir ]; then
	mkdir $new_dir
fi

export genotick=`ls Genotick*.jar`
if [ "$genotick" == "" ]; then
	echo "Genotick jar not found in current dir"
	exit 4
fi
echo "Starting..."

java -jar $genotick showPopulation=$dir | grep -v name | cut -d',' -f1,2 | sed -e 's/,/ /g' | sed -e 's/-//g' |
while read name weight;
do
export int_weight=`echo $weight | cut -d'.' -f1`
if (( $int_weight >= $min_weight )); then
	cp -v $dir/$name.prg $new_dir
fi
done
echo "Done!"

