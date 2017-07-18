#!/bin/bash
# iterate through each directory and run tests
if [[ $# -eq 0 ]]; then
	#need to specify a directory name
	echo Error, please specify a test result directory name.
	exit 1
fi
cd test_results/ 
mkdir -p $1 #make the test directory
cd ../tests/
for suite in */; do #for every test suite...
	cd $suite #open that suite folder
	for dir in */; do #for every test
		base="${dir:0:-1}" #test name (subtract trailing backslash)
		infile="$base.in" #test input file
		cd $dir #open that test folder
		#current code doesn't handle transaction files... replace the second line with the first line once implemented
		#../../../ticket.exe ../../../_generic/user_accounts.acc ../../../_generic/ticket_list.tkt ../../../test_results/$1/$base.trans < $infile > "../../../test_results/$1/$base.out"
		../../../ticket.exe ../../../_generic/user_accounts.acc ../../../_generic/ticket_list.tkt < $infile > "../../../test_results/$1/$base.out"
		cd ../
	done
	cd ../
done