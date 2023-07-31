
def precision(tp, fp):
    if tp + fp == 0:
        return 0
    return round(tp / (tp + fp), 2)

def recall(tp, fn):
    if tp + fn == 0:
        return 0
    return round(tp / (tp + fn), 2)

def accuracy(tp, tn, fp, fn):
    if tp + tn + fp + fn == 0:
        return 0
    return round((tp + tn) / (tp + tn + fp + fn),2)

def f1_score(precision, recall):
    if precision + recall == 0:
        return 0
    return round(2 * precision * recall / (precision + recall),2)

patch_sim_tp = 62
patch_sim_tn = 29
patch_sim_fp = 0
patch_sim_fn = 48
patch_sim_precision = precision(patch_sim_tp, patch_sim_fp)
patch_sim_recall = recall(patch_sim_tp, patch_sim_fn)
patch_sim_accuracy = accuracy(patch_sim_tp, patch_sim_tn, patch_sim_fp, patch_sim_fn)
patch_sim_f1 = f1_score(patch_sim_precision, patch_sim_recall)

shiboboleth_tp = 101
shiboboleth_tn = 23
shiboboleth_fp = 6
shiboboleth_fn = 9
shioboleth_precision = precision(shiboboleth_tp, shiboboleth_fp)
shiboboleth_recall = recall(shiboboleth_tp, shiboboleth_fn)
shiboboleth_accuracy = accuracy(shiboboleth_tp, shiboboleth_tn, shiboboleth_fp, shiboboleth_fn)
shiboboleth_f1 = f1_score(shioboleth_precision, shiboboleth_recall)

fixcheck_tp = 62
fixcheck_tn = 17
fixcheck_fp = 10
fixcheck_fn = 63
fixcheck_precision = precision(fixcheck_tp, fixcheck_fp)
fixcheck_recall = recall(fixcheck_tp, fixcheck_fn)
fixcheck = accuracy(fixcheck_tp, fixcheck_tn, fixcheck_fp, fixcheck_fn)
fixcheck_f1 = f1_score(fixcheck_precision, fixcheck_recall)

# Print latex rows
print('TP &', shiboboleth_tp, '&', patch_sim_tp , '&', fixcheck_tp, '\\\\')
print('TN &', shiboboleth_tn, '&', patch_sim_tn , '&', fixcheck_tn, '\\\\')
print('FP &', shiboboleth_fp, '&', patch_sim_fp , '&', fixcheck_fp, '\\\\')
print('FN &', shiboboleth_fn, '&', patch_sim_fn , '&', fixcheck_fn, '\\\\')
print('\midrule')
print('Precision &', shioboleth_precision, '&', patch_sim_precision , '&', fixcheck_precision, '\\\\')
print('Recall &', shiboboleth_recall, '&', patch_sim_recall , '&', fixcheck_recall, '\\\\')
print('Accuracy &', shiboboleth_accuracy, '&', patch_sim_accuracy , '&', fixcheck, '\\\\')
print('F1-score &', shiboboleth_f1, '&', patch_sim_f1 , '&', fixcheck_f1, '\\\\')

