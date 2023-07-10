import os
import pandas as pd
import json
results_dir = "fixcheck-output/bf4j"
BF4J_DATASET = os.getenv('BF4J_DATASET')

# Loop over all folders in results_dir
# Create empty dataframe with columns test_class,input_prefixes,inputs_class,target_class,prefixes_gen_time,assertions_gen_time,prefixes_running_time,output_prefixes,passing_prefixes,crashing_prefixes,assertion_failing_prefixes
incorrect_patches_reports = pd.DataFrame(columns=['project','test_class','input_prefixes','inputs_class','target_class','prefixes_gen_time','assertions_gen_time','prefixes_running_time','output_prefixes','passing_prefixes','crashing_prefixes','assertion_failing_prefixes','max_score'])
no_report = []
patches_with_score_greater = []
score_threshold = 0.80
for subject_id in os.listdir(results_dir):
    if subject_id.endswith('.csv'):
        continue
    base_folder = os.path.join(subject_id,"previous-assertion")
    report_csv = os.path.join(results_dir, base_folder, 'report.csv')
    subject_config_json = os.path.join(BF4J_DATASET, f'data/{subject_id}/bad-fix.json')
    # Load json
    with open(subject_config_json) as f:
        patch_json = json.load(f)
    print(f'Processing report: {report_csv}')
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
    incorrect_patches_reports = pd.concat([incorrect_patches_reports, report_df], ignore_index=True)


def print_results_for_all_projects(patches,row_latex):
    proj_passing_rows = patches[(patches['passing_prefixes'] > 0)].count()
    passing_tests_sum = patches['passing_prefixes'].sum()
    proj_failing_rows = patches[((patches['crashing_prefixes'] > 0) | (patches['assertion_failing_prefixes'] > 0))].count()
    failing_tests_sum = patches['crashing_prefixes'].sum() + patches['assertion_failing_prefixes'].sum()
    scores_greater_than_threshold = patches[patches['max_score'] >= score_threshold].count()
    print(f'patches: {patches.shape[0]}')
    print(f'with passing tests: {proj_passing_rows[0]}')
    print(f'passing tests: {passing_tests_sum}')
    print(f'with failing tests: {proj_failing_rows[0]}')
    print(f'failing tests: {failing_tests_sum}')
    print(f'scores > {score_threshold}: {scores_greater_than_threshold[0]}')
    row_latex.append(patches.shape[0])
    row_latex.append(proj_passing_rows[0])
    row_latex.append(passing_tests_sum)
    row_latex.append(proj_failing_rows[0])
    row_latex.append(failing_tests_sum)
    row_latex.append(scores_greater_than_threshold[0])

# Save correct_patches_reports
incorrect_patches_reports.to_csv(results_dir+'/incorrect_patches_reports-previous-assertion.csv', index=False)

row_latex = ['bf4j']

print()
print('----------------------------------')
print(f'Incorrect patches: {incorrect_patches_reports.shape[0]}')
print_results_for_all_projects(incorrect_patches_reports, row_latex)

print()
print('----------------------------------')
print(f'No report for: {no_report}')
print(f'Patches with score > {score_threshold}: {patches_with_score_greater}')
print()

print('----------------------------------')
print('Latex table')
# Print latex rows for chart with each element in the list interleaved with &
print(' & '.join([str(elem) for elem in row_latex]) + ' \\\\')
