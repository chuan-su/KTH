import numpy as np
import pandas as pd
import math as math
from IPython.core.debugger import set_trace

from cifar import Cifar10
from network import Network

cifar = Cifar10('data_batch_1')
validation_set = Cifar10('test_batch')

X, Y, y = cifar.dataset(10000)
Xv,Yv,yv = validation_set.dataset(5000)

for i, dataset in enumerate(['data_batch_2','data_batch_3', 'data_batch_4', 'data_batch_5']):
    X1, Y1, y1 = Cifar10(dataset).dataset(10000)
    X = np.concatenate((X, X1), axis=1)
    Y = np.concatenate((Y, Y1), axis=1)
    y = np.concatenate((y, y1), axis=None)

X_mean = np.mean(X)
Xv_mean = np.mean(Xv)

X -= X_mean
Xv -= Xv_mean

X /= np.std(X)
Xv /= np.std(Xv)


def coarse_search(ts, cycle):
    regs = [0.0746, 0.0220, 0.0075, 0.0116, 0.0432, 0.0094, 0.0910, 0.0594, 0.0037, 0.0028, 0.0009]
    result = {'accuracy': [], 'cost': [], 'best_accuracy':[], 'reg': []}
    print("ts={} cycle={}".format(ts, cycle))
    for i, reg in enumerate(regs):
        network = Network(X.shape[0], 50, 10)
        r = network.train(X, y, Xv, yv, ts, cycle,100, reg)
        result['accuracy'].append(r['vs_accuracy'][-1])
        result['best_accuracy'].append(max(r['vs_accuracy']))
        result['cost'].append(r['vs_cost'][-1])
        result['reg'].append(reg)
    return result


def fine_search(ts, cycle):
    regs = np.linspace(0.0006, 0.0015, 6) # [0.0006 , 0.00078, 0.00096, 0.00114, 0.00132, 0.0015 ]
    print("regs {}".format(regs))

    result = {'accuracy': [], 'cost': [], 'best_accuracy':[], 'reg': []}
    for i, reg in enumerate(regs):
        network = Network(X.shape[0], 50, 10)
        r = network.train(X, y, Xv, yv, ts, cycle,100, reg)
        result['accuracy'].append(r['vs_accuracy'][-1])
        result['best_accuracy'].append(max(r['vs_accuracy']))
        result['cost'].append(r['vs_cost'][-1])
        result['reg'].append(reg)
    return result

if __name__ == '__main__':
    # result = coarse_search(1000, 2)
    # pd.DataFrame(result).to_csv('coarse.csv', sep='\t', encoding='utf-8')

    result = fine_search(1000, 2)
    pd.DataFrame(result).to_csv('fine2.csv', sep='\t', encoding='utf-8')
