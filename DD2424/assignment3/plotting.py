from cifar import Cifar10
from network import Network
import numpy as np
from IPython.core.debugger import set_trace
import matplotlib.pyplot as plt
plt.rcParams['figure.figsize'] = (14.0, 8.0)

def plot_cost_accuracy(fig, **plt_data):
    epochs_arr = plt_data['t']
    # Cost
    plt.subplot(1,2,1)
    plt.title('Cost')
    plt.plot(epochs_arr, plt_data['ts_cost'], 'r-',label='training set')
    plt.plot(epochs_arr, plt_data['vs_cost'], 'b-',label='validation set')
    plt.legend(loc='upper center', shadow=True)
    plt.xlabel('Step')
    plt.ylabel('Cost')

    # Accuracy
    plt.subplot(1,2,2)
    plt.title('Accuracy')
    plt.plot(epochs_arr, plt_data['ts_accuracy'], 'r-',label='training set')
    plt.plot(epochs_arr, plt_data['vs_accuracy'], 'b-',label='validation set')
    plt.legend(loc='upper center', shadow=True)
    plt.xlabel('Step')
    plt.ylabel('Accuracy')
    plt.show()
    plt.savefig(fig)

def train_network(layers, reg, ns, cycle, bn=False, sig=None):
    X, Xv, y, yv = Cifar10.load_all()

    layer_sizes = [X.shape[0]]

    for ls in layers:
        layer_sizes.append(ls)

    layer_sizes.append(10)

    network = Network(layer_sizes, bn=bn, sig=sig)
    plt_data = network.train(X, y, Xv, yv, ns, cycle, 100, reg)

    return plt_data

if __name__ == '__main__':
    # 3 LAYERS with Batch Norm 54.2%
    plt_data = train_network([50,50], reg=0.005, ns=2250, cycle=2, bn=True)
    plot_cost_accuracy('img/3_layer_bn.png',**plt_data)

    # 3 LAYERS without Batch Norm 53.84%
    plt_data = train_network([50,50], reg=0.005, ns=2250, cycle=2, bn=False)
    plot_cost_accuracy('img/3_layer_nobn.png',**plt_data)

    ## 6 LAYERS with Batch Norm 54.06%
    plt_data = train_network([50, 30, 20, 20, 10], reg=0.005, ns=2250, cycle=2, bn=True)
    plot_cost_accuracy('img/6_layer_bn.png',**plt_data)

    ## 6 LAYERS without Batch Norm 51.34%
    plt_data = train_network([50, 30, 20, 20, 10], reg=0.005, ns=2250, cycle=2, bn=False)
    plot_cost_accuracy('img/6_layer_nobn.png',**plt_data)

    ## 3 layer reg=0.0057, accuracy=54.9%
    plt_data = train_network([50,50], reg=0.0057, ns=2250, cycle=2, bn=True)
    plot_cost_accuracy('img/3_layer_bn_best.png',**plt_data)

    ## sensitive to initialization

    ## sig=1e-1 BN 52.96%
    plt_data = train_network([50,50], reg=0.0057, ns=900, cycle=2, bn=True, sig=1e-1)
    plot_cost_accuracy('img/sig1_layer_bn.png',**plt_data)

    ## sig=1e-1 no BN 51.7%
    plt_data = train_network([50,50], reg=0.0057, ns=900, cycle=2, bn=False, sig=1e-1)
    plot_cost_accuracy('img/sig1_layer_nobn.png',**plt_data)

    ## sig=1e-3 BN 53.82%
    plt_data = train_network([50,50], reg=0.0057, ns=900, cycle=2, bn=True, sig=1e-3)
    plot_cost_accuracy('img/sig3_layer_bn.png',**plt_data)

    ## sig=1e-3 no BN 9.82%
    plt_data = train_network([50,50], reg=0.0057, ns=900, cycle=2, bn=False, sig=1e-3)
    plot_cost_accuracy('img/sig3_layer_nobn.png',**plt_data)

    ## sig=1e-4 BN 52.98%
    plt_data = train_network([50,50], reg=0.0057, ns=900, cycle=2, bn=True, sig=1e-4)
    plot_cost_accuracy('img/sig4_layer_bn.png',**plt_data)

    ## sig=1e-4 no BN 9.82%
    plt_data = train_network([50,50], reg=0.0057, ns=900, cycle=2, bn=False, sig=1e-4)
    plot_cost_accuracy('img/sig4_layer_nobn.png',**plt_data)
