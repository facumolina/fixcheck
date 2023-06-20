import csv
import os
import json
import re

# This script gets the badfixes from the defects repairing and saves them to a file
# to be later processed before passing it to the FixCheck script

def atoi(text):
    return int(text) if text.isdigit() else text

def natural_keys(text):
    '''
    alist.sort(key=natural_keys) sorts in human order
    http://nedbatchelder.com/blog/200712/human_sorting.html
    (See Toothy's implementation in the comments)
    '''
    return [ atoi(c) for c in re.split(r'(\d+)', text) ]

DEFECTS_REPAIRING = os.getenv('DEFECT_REPAIRING_DATASET')
patches_folder = DEFECTS_REPAIRING+"/tool/patches/INFO"
patches_file = 'experiments/patches-dr.csv'
patches = os.listdir(patches_folder)
patches.sort(key=natural_keys)

headers = ['id','tool','correctness','project','bug','base_dir','main_dep','tests_build','tests_src_dir','target_test','target_test_methods','target_class','input_class']

patches_rows = []
for patch in patches:
    if patch.endswith('json'):
        patch_json = json.load(open(patches_folder+'/'+patch))
        patch_id = patch_json['ID']
        patch_tool = patch_json['tool']
        patch_correctness = patch_json['correctness']
        patch_bug_id = patch_json['bug_id']
        patch_project = patch_json['project']
        print(f'Adding patch: {patch_id} {patch_tool}')
        patch_base_dir = patch_project+patch_bug_id+'b'
        patch_main_dep = ''
        patch_tests_build = ''
        patch_tests_src_dir = ''
        patch_target_test = ''
        patch_target_methods = ''
        patch_target_class = ''
        patch_input_class = ''
        patch_row = [patch_id,patch_tool,patch_correctness,patch_project,patch_bug_id,patch_base_dir,patch_main_dep,patch_tests_build,patch_tests_src_dir,patch_target_test,patch_target_methods,patch_target_class,patch_input_class]
        patches_rows.append(patch_row)

# Write to csv
with open(patches_file, 'w') as csvfile:
    csvwriter = csv.writer(csvfile)
    csvwriter.writerow(headers)
    csvwriter.writerows(patches_rows)