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

properties_file='fixcheck-props.properties'
cat <<EOF > $properties_file
test-classes-path=$test_classes_path
test-class=$target_test
test-methods=$target_test_methods
test-classes-src=$target_test_dir
test-failure-trace-log=$original_failure_log
inputs-class=$inputs_class
number-of-prefixes=100
assertion-generator=$assertion_generation
EOF
echo 'properties file: '$properties_file

java -cp $full_cp org.imdea.fixcheck.FixCheck -p $properties_file | tee log.out
