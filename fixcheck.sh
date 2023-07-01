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

java -cp $full_cp org.imdea.fixcheck.FixCheck $test_classes_path $target_test $target_test_methods $target_test_dir $target_class $inputs_class $original_failure_log 10 $assertion_generation | tee log.out
