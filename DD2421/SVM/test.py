import numpy as np
import random, math
from scipy.optimize import minimize
import matplotlib.pyplot as plt
from cvxopt.solvers import qp
from cvxopt.base import matrix


# kernel functions
def linerkernel(x, y, c=0):
    return np.dot(x, y)


def polinomialkernel(x, y, n=2):
    return (np.dot(x, y) + 1)**n


def rbfkernel(x, y, temp_gamma=0):
    d = np.subtract(x, y)
    d2 = np.power(d, 2)
    if temp_gamma == 0:
        gamma = 1/len(x)
    else:
        gamma = temp_gamma
    temp = np.exp(-(gamma*d2))
    return temp

# generate test data
def generate_class(seed=True):
    if seed:
        np.random.seed(3000)
    class1 = np.concatenate((np.random.randn(10, 2) * 0.2 + [1.5, 0.5],
                             np.random.randn(10, 2) * 0.2 + [-1.5, 0.5]))
    class2 = np.random.randn(20, 2) * 0.2 + [0.0, -0.5]
    return class1, class2


# generate test data
def generate_data(class1, class2):
    inputs = np.concatenate((class1, class2))
    targets = np.concatenate((np.ones(class1.shape[0]), -np.ones(class2.shape[0])))
    permute = list(range(len(inputs)))
    random.shuffle(permute)
    # inputs = inputs[permute, :]
    inputs = inputs[permute]
    targets = targets[permute]
    return inputs, targets


# build kernel matrix
def kernel(w, x, kernel_fun=linerkernel):
    out = kernel_fun(w, x)
    return out


def train(X, y, kernelfun, C=0):
    m,n = X.shape
    y = y.reshape(-1,1) * 1.
    X_dash = y * X

    H = kernelfun(X_dash, X_dash.T, C)
    #Converting into cvxopt format
    P = matrix(H)
    q = matrix(-np.ones((m, 1)))
    if C > 0:
        G = matrix(np.vstack((np.eye(m)*-1,np.eye(m))))
        h = matrix(np.hstack((np.zeros(m), np.ones(m) * C)))
        A = matrix(y.reshape(1, -1))
        b = matrix(np.zeros(1))
    else:
        G = matrix(-np.eye(m))
        h = matrix(np.zeros(m))
        A = matrix(y.reshape(1, -1))
        b = matrix(np.zeros(1))

    #Run solver
    sol = qp(P, q, G, h, A, b)
    alphas = list(sol['x'])
    support_vectors = []
    for i in range(0, len(alphas)):
        if alphas[i] > 1.e-5:
            support_vectors.append((alphas[i],(X[i])[0], (X[i])[1], y[i][0]))
    return support_vectors


# plot data
def draw(class1, class2, support_vectors, kernel_fun):
    plt.plot([p[0] for p in class1],
             [p[1] for p in class1],
             'b.')
    plt.plot([p[0] for p in class2],
             [p[1] for p in class2],
             'r.')
    xgrid = np.linspace(-4, 4)
    ygrid = np.linspace(-4, 4)
    grid = [xgrid[0], xgrid[-1], ygrid[0], ygrid[-1]]
    plt.axis(grid)

    grid2 = matrix([[indicator(support_vectors, x, y, kernel_fun) for y in ygrid] for x in xgrid])
    plt.contour(xgrid, ygrid, grid2, (-1.0, 0.0, 1.0), colors=('red', 'black', 'blue'), linewidths=(1, 3, 1))
    plt.savefig('svmplot.pdf')
    plt.show()


def indicator(svs, x, y, kernel_fun=linerkernel):
    ind = 0.0
    for i in range(0, len(svs)):
        ind += (svs[i])[0] * (svs[i])[3] * kernel_function([x, y], np.transpose([(svs[i])[1], (svs[i])[2]]))
    return ind


def plot_boundaries(support_vectors, kernel_fun):
    xrange = np.arange(-4, 4, 0.05)
    yrange = np.arange(-4, 4, 0.05)
    grid = matrix([[indicator(support_vectors, x, y, kernel_fun) for y in yrange] for x in xrange])
    plt.contour(xrange, yrange, grid, (-1.0, 0.0, 1.0), colors=('red', 'black', 'blue'), linewidths=(1, 3, 1))


if __name__ == '__main__':
    class_1, class_2 = generate_class()
    Inputs, Targets = generate_data(class_1, class_2)
    kernel_function = linerkernel
    kernel_function = polinomialkernel
    #kernel_function = rbfkernel
    svs = train(Inputs, Targets, kernel_function)
    draw(class_1, class_2, svs, kernel_function)
