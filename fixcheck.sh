#!/bin/bash

subject_cp=$1
test_classes_path=$2
target_test=$3
target_test_methods=$4
target_test_dir=$5
target_class=$6
inputs_class=$7
original_failure_log=$8
assertion_generation=$9
full_cp='build/libs/fixcheck-all-1.0.0.jar:'$subject_cp

java -cp $full_cp org.imdea.fixcheck.FixCheck -tp $test_classes_path -tc $target_test -tm $target_test_methods -ts $target_test_dir -i $inputs_class -tf $original_failure_log -np 100 -ag $assertion_generation | tee log.out
