#!/bin/bash

#subject_cp='build/libs/fixcheck-all-1.0.0.jar:/Users/facundo.molina/research/software/bad-fixes-dataset/tmp/jackson-databind-118/badfix/jackson-databind/target/jackson-databind-2.1.2-SNAPSHOT-jar-with-dependencies.jar:/Users/facundo.molina/research/software/bad-fixes-dataset/tmp/jackson-databind-118/badfix/jackson-databind/target/test-classes'
#target_test='com.fasterxml.jackson.databind.jsontype.TestExternalIdForAnalysis'
#target_class='com.fasterxml.jackson.databind.ObjectMapper'
#inputs_class='java.util.Date'

subject_cp=$1
target_test=$2
target_class=$3
inputs_class=$4
full_cp='build/libs/fixcheck-all-1.0.0.jar:'$subject_cp
java -cp $full_cp org.imdea.fixcheck.FixCheck $full_cp $target_test $target_class $inputs_class 2
