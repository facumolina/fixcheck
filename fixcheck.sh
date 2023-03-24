#!/bin/bash

#subject_cp='build/libs/fixcheck-all-1.0.0.jar:/Users/facundo.molina/research/software/bad-fixes-dataset/tmp/jackson-databind-118/badfix/jackson-databind/target/jackson-databind-2.1.2-SNAPSHOT-jar-with-dependencies.jar:/Users/facundo.molina/research/software/bad-fixes-dataset/tmp/jackson-databind-118/badfix/jackson-databind/target/test-classes'
#target_test='com.fasterxml.jackson.databind.jsontype.TestExternalIdForAnalysis'
#target_class='com.fasterxml.jackson.databind.ObjectMapper'
#inputs_class='java.util.Date'

subject_cp=$1
test_classes_path=$2
target_test=$3
target_test_src=$4
target_class=$5
inputs_class=$6
full_cp='build/libs/fixcheck-all-1.0.0.jar:'$subject_cp

java -cp $full_cp org.imdea.fixcheck.FixCheck $test_classes_path $target_test $target_test_src $target_class $inputs_class 2