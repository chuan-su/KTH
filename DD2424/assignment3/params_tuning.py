import numpy as np
import pandas as pd
import math as math
from IPython.core.debugger import set_trace

from cifar import Cifar10
from network import Network


def coarse_search(ns, cycle):

    regs = [0.0746, 0.0220, 0.0075, 0.0116, 0.0432, 0.0094, 0.0910, 0.0594, 0.0037, 0.0028, 0.0009]
    print("regs={}".format(regs))

    result = {'accuracy': [], 'cost': [], 'best_accuracy':[], 'reg': []}

    print("ns={} cycle={}".format(ns, cycle))

    X, Xv, y, yv = Cifar10.load_all()

    for i, reg in enumerate(regs):
        layer_sizes = [X.shape[0], 50,50,10]
        network = Network(layer_sizes, bn=True)

        r = network.train(X, y, Xv, yv, ns, cycle, 100, reg)

        result['accuracy'].append(r['vs_accuracy'][-1])
        result['best_accuracy'].append(max(r['vs_accuracy']))
        result['cost'].append(r['vs_cost'][-1])
        result['reg'].append(reg)

    return result


def fine_search(ns, cycle):
    # 0.0094 / 0.0057
    regs = np.linspace(0.001, 0.015, 10)
    print("regs {}".format(regs))

    result = {'accuracy': [], 'cost': [], 'best_accuracy':[], 'reg': []}

    print("ns={} cycle={}".format(ns, cycle))

    X, Xv, y, yv = Cifar10.load_all()

    for i, reg in enumerate(regs):
        layer_sizes = [X.shape[0], 50,50,10]
        network = Network(layer_sizes, bn=True)

        r = network.train(X, y, Xv, yv, ns, cycle, 100, reg)

        result['accuracy'].append(r['vs_accuracy'][-1])
        result['best_accuracy'].append(max(r['vs_accuracy']))
        result['cost'].append(r['vs_cost'][-1])
        result['reg'].append(reg)

    return result

if __name__ == '__main__':
    result = coarse_search(ns=2250, cycle=2)
    pd.DataFrame(result).to_csv('coarse.csv', sep='\t', encoding='utf-8')

    result = fine_search(ns=2250, cycle=2)
    pd.DataFrame(result).to_csv('fine_search.csv', sep='\t', encoding='utf-8')
