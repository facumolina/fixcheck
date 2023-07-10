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
patches_with_score_greater = []
score_threshold = 0.80
for subject_id in os.listdir(results_dir):
    if subject_id.endswith('.csv'):
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
    if max_score >= score_threshold:
        patches_with_score_greater.append(subject_id)
    if patch_json["correctness"] == "Correct":
        correct_patches_reports = pd.concat([correct_patches_reports, report_df], ignore_index=True)
    else:
        incorrect_patches_reports = pd.concat([incorrect_patches_reports, report_df], ignore_index=True)


chart_row_latex = ['chart']
lang_row_latex = ['lang']
math_row_latex = ['math']
time_row_latex = ['time']

def print_results_for_project(patches,project,row_latex):
    proj_rows = patches[patches['project'] == project]
    proj_passing_rows = proj_rows[(proj_rows['passing_prefixes'] > 0)].count()
    passing_tests_sum = proj_rows['passing_prefixes'].sum()
    proj_failing_rows = proj_rows[((proj_rows['crashing_prefixes'] > 0) | (proj_rows['assertion_failing_prefixes'] > 0))].count()
    failing_tests_sum = proj_rows['crashing_prefixes'].sum() + proj_rows['assertion_failing_prefixes'].sum()
    scores_greater_than_threshold = proj_rows[proj_rows['max_score'] >= score_threshold].count()
    print(f'{project}')
    print(f'patches: {proj_rows.shape[0]}')
    print(f'with passing tests: {proj_passing_rows[0]}')
    print(f'passing tests: {passing_tests_sum}')
    print(f'with failing tests: {proj_failing_rows[0]}')
    print(f'failing tests: {failing_tests_sum}')
    print(f'scores > {score_threshold}: {scores_greater_than_threshold[0]}')
    row_latex.append(proj_rows.shape[0])
    row_latex.append(proj_passing_rows[0])
    row_latex.append(passing_tests_sum)
    row_latex.append(proj_failing_rows[0])
    row_latex.append(failing_tests_sum)
    row_latex.append(scores_greater_than_threshold[0])

# Save correct_patches_reports
correct_patches_reports.to_csv(results_dir+'/correct_patches_reports-no-assertion.csv', index=False)
incorrect_patches_reports.to_csv(results_dir+'/incorrect_patches_reports-no-assertion.csv', index=False)


print('----------------------------------')
print(f'Correct patches: {correct_patches_reports.shape[0]}')
print_results_for_project(correct_patches_reports,'Chart', chart_row_latex)
print_results_for_project(correct_patches_reports,'Lang', lang_row_latex)
print_results_for_project(correct_patches_reports,'Math', math_row_latex)
print_results_for_project(correct_patches_reports,'Time', time_row_latex)

print()
print('----------------------------------')
print(f'Incorrect patches: {incorrect_patches_reports.shape[0]}')
print_results_for_project(incorrect_patches_reports,'Chart', chart_row_latex)
print_results_for_project(incorrect_patches_reports,'Lang', lang_row_latex)
print_results_for_project(incorrect_patches_reports,'Math', math_row_latex)
print_results_for_project(incorrect_patches_reports,'Time', time_row_latex)

print()
print('----------------------------------')
print(f'No report for: {no_report}')
print(f'Patches with score > {score_threshold}: {patches_with_score_greater}')
print()

print('----------------------------------')
print('Latex table')
# Print latex rows for chart with each element in the list interleaved with &
print(' & '.join([str(elem) for elem in chart_row_latex]) + ' \\\\')
print(' & '.join([str(elem) for elem in lang_row_latex]) + ' \\\\')
print(' & '.join([str(elem) for elem in math_row_latex]) + ' \\\\')
print(' & '.join([str(elem) for elem in time_row_latex]) + ' \\\\')
