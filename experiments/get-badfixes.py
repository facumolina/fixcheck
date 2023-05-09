import csv
import os
import json

# This script gets the badfixes from the dataset and saves them to a file
# to be later processed before passing it to the FixCheck script

# Config variables
FIXCHECK = os.getenv('FIXCHECK')
BF4J_DATASET = os.getenv('BF4J_DATASET')

output_file = 'experiments/badfixes-to-analyze.csv'

subjects_folder = BF4J_DATASET+"/data/"
subjects = os.listdir(subjects_folder)

# Get the badfixes
all_bad_fixes = sorted(os.listdir(subjects_folder))
all_jsons_files = [subjects_folder + bad_fix + '/bad-fix.json' for bad_fix in all_bad_fixes]
all_bad_fixes_json = [json.load(open(bad_fix_json_file)) for bad_fix_json_file in all_jsons_files]

headers = ['id', 'project', 'main_dep', 'tests_classes', 'test_src_dir', 'target_test', 'target_test_methods', 'target_class', 'input_class']

bad_fixes_rows = []
for bad_fix_json in all_bad_fixes_json:
    bad_fix_name = bad_fix_json['name']
    bad_fix_data = bad_fix_json['bad-fix']
    if "test_class" in bad_fix_data:
        print(f'Adding fix with test class: {bad_fix_name}')
        bad_fix_project = bad_fix_json['project']
        bad_fix_main_dep = ''
        bad_fix_tests_classes = ''
        bad_fix_test_src_dir = ''
        if 'build' in bad_fix_json:
            if 'test_classes_dir' in bad_fix_json['build']:
                bad_fix_tests_classes = bad_fix_json['build']['test_classes_dir']
            if  'test_classes_sources' in bad_fix_json['build']:
                bad_fix_test_src_dir = bad_fix_json['build']['test_classes_sources']
        bad_fix_target_test = bad_fix_data['test_class']
        bad_fix_target_class = ''
        bad_fix_input_class = ''
        bad_fix_target_methods = ''
        bad_fix_row = [bad_fix_name, bad_fix_project, bad_fix_main_dep, bad_fix_tests_classes, bad_fix_test_src_dir, bad_fix_target_test, bad_fix_target_methods,bad_fix_target_class, bad_fix_input_class]
        bad_fixes_rows.append(bad_fix_row)
    else:
        print(f'Skipping bad fix without test class: {bad_fix_name}')

# Write to csv
with open(output_file, 'w') as csvfile:
    csvwriter = csv.writer(csvfile)
    csvwriter.writerow(headers)
    csvwriter.writerows(bad_fixes_rows)