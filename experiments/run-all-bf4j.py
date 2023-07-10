import sys
import pandas as pd
import subprocess
import os

assertion_generation = sys.argv[1] # One of no-assertion, previous-assertion or llm-assertion

subjects_ids = ["avro-2944","caffeine-220","choco-600","cli-162","cli-164","cli-265","closure-compiler-1114",
"closure-compiler-1857","csv-93","graphhopper-145","graphhopper-172","graphhopper-539","immutables-304",
"io-101","io-744","jackson-databind-118","jackson-databind-735","jackson-databind-1195",
"jxpath-93","jxpath-108","lang-710","mahout-1005","mahout-1574","math-296","math-790",
"math-828","math-1068","math-1208","time4j-140","time4j-577","time4j-728"]

for subject in subjects_ids:
    # Do not run if report exits
        report_file = f'fixcheck-output/bf4j/{subject}/{assertion_generation}/report.csv'
        if os.path.exists(report_file):
            print(f'Report already exists for patch: {subject}')
            continue
        subprocess.run(f'python3 experiments/run-fixcheck-bf4j.py {subject} {assertion_generation}', shell=True)