#!/bin/bash
# iterate through each directory and run tests
if [[ $# -eq 0 ]]; then
	#need to specify a directory name
	echo Error, please specify a test result directory name.
	exit 1
fi
cd tests/
for suite in */; do #for every test suite...
	cd $suite #open that suite folder
	for dir in */; do #for every test
		base="${dir:0:-1}" #test name (subtract trailing backslash)
		file="$base.out" #test input file
		cd $dir #open that test folder
		diff $file "../../../test_results/$1/$file"
		cd ../
	done
	cd ../
done