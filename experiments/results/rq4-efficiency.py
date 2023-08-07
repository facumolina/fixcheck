import sys
import pandas as pd
import os

results_dir = "fixcheck-output/defects-repairing"
dataset_csv = 'experiments/defect-repairing-subjects.csv'
df = pd.read_csv(dataset_csv)

exec_time = pd.DataFrame(columns=['project','patch_id','fixcheck_time_tg','fixcheck_time_ag','fixcheck_time_te'])

assertion_generation = sys.argv[1]

for target_patch in os.listdir(results_dir):
    if target_patch.endswith('.csv'):
        continue
    subject_data = df[df['id'] == target_patch]
    project = subject_data['project'].values[0]

    base_folder = os.path.join(target_patch,assertion_generation)
    report_csv = os.path.join(results_dir, base_folder, 'report.csv')

    fixcheck_time_tg = 0
    fixcheck_time_ag = 0
    fixcheck_pred = 0

    if os.path.exists(report_csv):
        report = pd.read_csv(report_csv)
        prefixes_gen_time_ms = report['prefixes_gen_time'].sum()
        assertions_gen_time_ms = report['assertions_gen_time'].sum()
        prefix_running_time_ms = report['prefixes_running_time'].sum()

        prefixes_gen_time = prefixes_gen_time_ms // 1000
        assertions_gen_time = assertions_gen_time_ms // 1000
        prefix_running_time = prefix_running_time_ms // 1000

        fixcheck_time_tg = prefixes_gen_time
        fixcheck_time_ag = assertions_gen_time
        fixcheck_time_te = prefix_running_time

    new_row = {'project': project, 'patch_id': target_patch, 'fixcheck_time_tg': fixcheck_time_tg, 'fixcheck_time_ag': fixcheck_time_ag, 'fixcheck_time_te': fixcheck_time_te}
    exec_time = pd.concat([exec_time, pd.DataFrame([new_row])])

bf4j_results_dir = "fixcheck-output/bf4j"
for subject_id in os.listdir(bf4j_results_dir):
    if subject_id.endswith('.csv'):
        continue
    project = 'bf4j'
    base_folder = os.path.join(subject_id,assertion_generation)
    report_csv = os.path.join(bf4j_results_dir, base_folder, 'report.csv')

    fixcheck_time_tg = 0
    fixcheck_time_ag = 0
    fixcheck_pred = 0

    if os.path.exists(report_csv):
        report = pd.read_csv(report_csv)
        prefixes_gen_time_ms = report['prefixes_gen_time'].sum()
        assertions_gen_time_ms = report['assertions_gen_time'].sum()
        prefix_running_time_ms = report['prefixes_running_time'].sum()

        prefixes_gen_time = prefixes_gen_time_ms // 1000
        assertions_gen_time = assertions_gen_time_ms // 1000
        prefix_running_time = prefix_running_time_ms // 1000

        fixcheck_time_tg = prefixes_gen_time
        fixcheck_time_ag = assertions_gen_time
        fixcheck_time_te = prefix_running_time

    new_row = {'project': project, 'patch_id': subject_id, 'fixcheck_time_tg': fixcheck_time_tg, 'fixcheck_time_ag': fixcheck_time_ag, 'fixcheck_time_te': fixcheck_time_te}
    exec_time = pd.concat([exec_time, pd.DataFrame([new_row])])

chart_row_latex = ['chart']
lang_row_latex = ['lang']
math_row_latex = ['math']
time_row_latex = ['time']
bf4j_row_latex = ['bf4j']
total_row_latex = ['total']

def save_results_for_project(results,project,row_latex):
    if project == 'TOTAL':
        proj_rows = results
    else:
        proj_rows = results[results['project'] == project]

    fixcheck_time_tg = proj_rows['fixcheck_time_tg'].sum()
    fixcheck_time_ag = proj_rows['fixcheck_time_ag'].sum()
    ficcheck_time_te = proj_rows['fixcheck_time_te'].sum()

    print(f'project: {project} patches: {len(proj_rows)} test_gen_time: {fixcheck_time_tg} assertion_gen_time: {fixcheck_time_ag} test_execution_time: {ficcheck_time_te} total_time: {fixcheck_time_tg + fixcheck_time_ag + ficcheck_time_te}')

save_results_for_project(exec_time,'Chart',chart_row_latex)
save_results_for_project(exec_time,'Lang',lang_row_latex)
save_results_for_project(exec_time,'Math',math_row_latex)
save_results_for_project(exec_time,'Time',time_row_latex)
save_results_for_project(exec_time,'bf4j',bf4j_row_latex)
save_results_for_project(exec_time,'TOTAL',total_row_latex)

