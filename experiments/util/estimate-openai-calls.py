import os
import pandas as pd
import json
import javalang

results_dir = "fixcheck-output/defects-repairing"
DEFECT_REPAIRING_DATASET = os.getenv('DEFECT_REPAIRING_DATASET')
DATASET = "experiments/defect-repairing-subjects.csv"

def __get_start_end_for_node(node_to_find):
    start = None
    end = None
    for path, node in tree:
        if start is not None and node_to_find not in path:
            end = node.position
            return start, end
        if start is None and node == node_to_find:
            start = node.position
    return start, end


def __get_string(start, end):
    if start is None:
        return ""

    # positions are all offset by 1. e.g. first line -> lines[0], start.line = 1
    end_pos = None

    if end is not None:
        end_pos = end.line - 1

    lines = java_file_content.splitlines(True)
    string = "".join(lines[start.line:end_pos])
    string = lines[start.line - 1] + string

    # When the method is the last one, it will contain a additional brace
    if end is None:
        left = string.count("{")
        right = string.count("}")
        if right - left == 1:
            p = string.rfind("}")
            string = string[:p]

    return string

# Read the dataset csv
dataset_df = pd.read_csv(DATASET)

# Loop over all folders in results_dir
all_inputs_lenght = []
for subject_id in os.listdir(results_dir):
    if subject_id == 'correct_patches_reports.csv' or subject_id == 'incorrect_patches_reports.csv':
        continue
    print(f'Processing patch: {subject_id}')
    subject_config_json = os.path.join(DEFECT_REPAIRING_DATASET, f'tool/patches/INFO/{subject_id}.json')
    with open(subject_config_json) as f:
        patch_json = json.load(f)
    # Loop over all java files in the folder passing-tests
    passing_tests_dir = os.path.join(results_dir, subject_id, 'passing-tests')
    if not os.path.exists(passing_tests_dir):
        continue
    # Get the method name for the current patch. It is the value if the column target_test_methods for the row with id = subject_id
    target_test_method = dataset_df[dataset_df['id'] == subject_id]['target_test_methods'].values[0]
    print(f'Target test method: {target_test_method}')
    for java_file in os.listdir(passing_tests_dir):
        print(f'Processing java file: {java_file}')
        java_file_path = os.path.join(passing_tests_dir, java_file)
        # Load the file content as a string
        with open(java_file_path) as f:
            java_file_content = f.read()
            tree = javalang.parse.parse(java_file_content)
            # Find the method with name test
            for _, method in tree.filter(javalang.tree.MethodDeclaration):
                if method.name == target_test_method:
                    start, end = __get_start_end_for_node(method)
                    code = __get_string(start, end)
                    # Get the method code
                    #print(f'Method code: {code}')
                    # Get the number of words in the method
                    prompt_to_openai = "Given the following Java test case:\n " + code + " Produce as output assertions for the following test case:\n " + code
                    number_of_words = len(code.split())
                    print(f'Number of words method: {number_of_words}')
                    all_inputs_lenght.append(number_of_words)
                    break


print(f'All tests inputs lenght: {all_inputs_lenght}')
print(f'Number of tests inputs: {len(all_inputs_lenght)}')
print(f'Average number of words: {sum(all_inputs_lenght)/len(all_inputs_lenght)}')