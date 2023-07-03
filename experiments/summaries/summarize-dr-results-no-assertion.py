import os
import pandas as pd
import json
results_dir = "fixcheck-output/defects-repairing"
DEFECT_REPAIRING_DATASET = os.getenv('DEFECT_REPAIRING_DATASET')

# Loop over all folders in results_dir
# Create empty dataframe with columns test_class,input_prefixes,inputs_class,target_class,prefixes_gen_time,assertions_gen_time,prefixes_running_time,output_prefixes,passing_prefixes,crashing_prefixes,assertion_failing_prefixes
correct_patches_reports = pd.DataFrame(columns=['project','test_class','input_prefixes','inputs_class','target_class','prefixes_gen_time','assertions_gen_time','prefixes_running_time','output_prefixes','passing_prefixes','crashing_prefixes','assertion_failing_prefixes','max_score'])
incorrect_patches_reports = pd.DataFrame(columns=['project','test_class','input_prefixes','inputs_class','target_class','prefixes_gen_time','assertions_gen_time','prefixes_running_time','output_prefixes','passing_prefixes','crashing_prefixes','assertion_failing_prefixes','max_score'])
no_report = []
patches_with_score_greater_than_90 = []
for subject_id in os.listdir(results_dir):
    if subject_id == 'correct_patches_reports.csv' or subject_id == 'incorrect_patches_reports.csv':
        continue
    base_folder = os.path.join(subject_id,"no-assertion")
    report_csv = os.path.join(results_dir, base_folder, 'report.csv')
    subject_config_json = os.path.join(DEFECT_REPAIRING_DATASET, f'tool/patches/INFO/{subject_id}.json')
    # Load json
    with open(subject_config_json) as f:
        patch_json = json.load(f)
    print(f'Processing report: {report_csv}')
    print(f'Correctness: {patch_json["correctness"]}')
    if not os.path.exists(report_csv):
        no_report.append(subject_id)
        continue
    project = patch_json["project"]
    score_file = os.path.join(results_dir, base_folder, 'scores-failing-tests.csv')
    score_df = pd.read_csv(score_file)
    max_score = score_df['score'].max()
    report_df = pd.read_csv(report_csv)
    report_df['project'] = project
    report_df['max_score'] = max_score
    print(f'Max score: {max_score}')
    if max_score >= 0.90:
        patches_with_score_greater_than_90.append(subject_id)
    if patch_json["correctness"] == "Correct":
        correct_patches_reports = pd.concat([correct_patches_reports, report_df], ignore_index=True)
    else:
        incorrect_patches_reports = pd.concat([incorrect_patches_reports, report_df], ignore_index=True)


def print_results_for_project(patches,project):
    proj_rows = patches[patches['project'] == project]
    proj_passing_rows = proj_rows[(proj_rows['passing_prefixes'] > 0)].count()
    passing_tests_sum = proj_rows['passing_prefixes'].sum()
    proj_failing_rows = proj_rows[((proj_rows['crashing_prefixes'] > 0) | (proj_rows['assertion_failing_prefixes'] > 0))].count()
    failing_tests_sum = proj_rows['crashing_prefixes'].sum() + proj_rows['assertion_failing_prefixes'].sum()
    scores_greater_thatn_90 = proj_rows[proj_rows['max_score'] >= 0.90].count()
    print(f'{project}')
    print(f'patches: {proj_rows.shape[0]}')
    print(f'with passing tests: {proj_passing_rows[0]}')
    print(f'passing tests: {passing_tests_sum}')
    print(f'with failing tests: {proj_failing_rows[0]}')
    print(f'failing tests: {failing_tests_sum}')
    print(f'scores > 0.90: {scores_greater_thatn_90[0]}')

# Save correct_patches_reports
correct_patches_reports.to_csv(results_dir+'/correct_patches_reports.csv', index=False)
incorrect_patches_reports.to_csv(results_dir+'/incorrect_patches_reports.csv', index=False)


print('----------------------------------')
print(f'Correct patches: {correct_patches_reports.shape[0]}')
print_results_for_project(correct_patches_reports,'Chart')
print_results_for_project(correct_patches_reports,'Lang')
print_results_for_project(correct_patches_reports,'Math')
print_results_for_project(correct_patches_reports,'Time')

print()
print('----------------------------------')
print(f'Incorrect patches: {incorrect_patches_reports.shape[0]}')
print_results_for_project(incorrect_patches_reports,'Chart')
print_results_for_project(incorrect_patches_reports,'Lang')
print_results_for_project(incorrect_patches_reports,'Math')
print_results_for_project(incorrect_patches_reports,'Time')

print()
print('----------------------------------')
print(f'No report for: {no_report}')
print(f'Patches with score > 0.90: {patches_with_score_greater_than_90}')
