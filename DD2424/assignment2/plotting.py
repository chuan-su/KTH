from cifar import Cifar10
from network import Network
import numpy as np

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

def plot1():
    cifar = Cifar10('data_batch_1')
    validation_set = Cifar10('data_batch_2')
    test_set = Cifar10('test_batch')

    X, Y, y = cifar.dataset(10000)
    Xv,Yv,yv = validation_set.dataset(10000)
    Xt,Yt,yt = test_set.dataset(10000)

    X_mean = np.mean(X)
    Xv_mean = np.mean(Xv)
    Xt_mean = np.mean(Xt)

    X -= X_mean
    Xv -= Xv_mean
    Xt -= Xt_mean

    X /= np.std(X)
    Xv /= np.std(Xv)
    Xt /= np.std(Xt)

    network = Network(X.shape[0], 50, 10)
    plt_data = network.train(X, y, Xv, yv, 500, 1)

    cost, accuracy = network.loss_and_accuracy(Xt, yt)
    print("final accuracy {} ".format(accuracy))
    return plt_data

def plot2():
    cifar = Cifar10('data_batch_1')
    validation_set = Cifar10('data_batch_2')
    test_set = Cifar10('test_batch')

    X, Y, y = cifar.dataset(10000)
    Xv,Yv,yv = validation_set.dataset(10000)
    Xt,Yt,yt = test_set.dataset(10000)

    X_mean = np.mean(X)
    Xv_mean = np.mean(Xv)
    Xt_mean = np.mean(Xt)

    X -= X_mean
    Xv -= Xv_mean
    Xt -= Xt_mean

    X /= np.std(X)
    Xv /= np.std(Xv)
    Xt /= np.std(Xt)

    network = Network(X.shape[0], 50, 10)
    plt_data = network.train(X, y, Xv, yv, 800, 3)
    cost, accuracy = network.loss_and_accuracy(Xt, yt)
    print("final accuracy {} ".format(accuracy))
    return plt_data

def plot3():
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

    network = Network(X.shape[0], 50, 10)
    plt_data = network.train(X, y, Xv, yv, 1000, 3, 100, 0.00096)
    return plt_data

if __name__ == '__main__':
    # plt_data = plot1()
    # plot_cost_accuracy('img/fig1.png',**plt_data)

    plt_data = plot2()
    plot_cost_accuracy('img/fig2.png',**plt_data)

    # plt_data = plot3()
    # plot_cost_accuracy('img/final.png',**plt_data)
