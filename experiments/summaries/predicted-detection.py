import sys
import pandas as pd
import os


SCORE_THRESHOLD = 0.8

assertion_generation = sys.argv[1]  # One of no-assertion, previous-assertion or llm-assertion

target_ids = [
    1,
    2,
    4,
    5,
    6,
    7,
    8,
    9,
    10,
    11,
    12,
    13,
    14,
    15,
    16,
    17,
    18,
    19,
    20,
    21,
    22,
    23,
    24,
    25,
    26,
    27,
    28,
    29,
    30,
    31,
    32,
    33,
    34,
    36,
    37,
    38,
    44,
    45,
    46,
    47,
    48,
    49,
    51,
    53,
    54,
    55,
    58,
    59,
    62,
    63,
    64,
    65,
    66,
    67,
    68,
    69,
    72,
    73,
    74,
    75,
    76,
    77,
    78,
    79,
    80,
    81,
    82,
    83,
    84,
    88,
    89,
    90,
    91,
    92,
    93,
    150,
    151,
    152,
    153,
    154,
    155,
    157,
    158,
    159,
    160,
    161,
    162,
    163,
    165,
    166,
    167,
    168,
    169,
    170,
    171,
    172,
    173,
    174,
    175,
    176,
    177,
    180,
    181,
    182,
    183,
    184,
    185,
    186,
    187,
    188,
    189,
    191,
    192,
    193,
    194,
    195,
    196,
    197,
    198,
    199,
    201,
    202,
    203,
    204,
    205,
    206,
    207,
    208,
    209,
    210,
    "HDRepair1",
    "HDRepair3",
    "HDRepair4",
    "HDRepair5",
    "HDRepair6",
    "HDRepair7",
    "HDRepair8",
    "HDRepair9",
    "HDRepair10",
]

results_dir = "fixcheck-output/defects-repairing"
parent = os.path.dirname(os.getcwd())
root_dir = os.path.dirname(parent)
result_dir = os.path.join(root_dir, results_dir)

target_patches = ["Patch" + str(id) for id in target_ids]
prediction_dict = {"Patch ID": [], "FixCheck Prediction": []}

for patch in target_patches:
    score_file = os.path.join(results_dir, f'{patch}/{assertion_generation}/scores-failing-tests.csv')

    prediction_dict["Patch ID"].append(patch)

    if os.path.exists(score_file):
        score_df = pd.read_csv(score_file)
        max_score = score_df["score"].max()
        prediction_dict["FixCheck Prediction"].append(0 if max_score >= SCORE_THRESHOLD else 1)
    else:
        print(f'{patch} do not exist')
        prediction_dict["FixCheck Prediction"].append("Not Found")


df = pd.DataFrame(data=prediction_dict)
df.to_csv(f'{results_dir}/{assertion_generation}-prediction-detection.csv')
