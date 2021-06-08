import numpy as np
from cifar import Cifar10
from network import Network
from IPython.core.debugger import set_trace

def gradients_check(W, grad_analytics, compute_cost):
    h = 1e-5
    # result = np.zeros_like(W)
    for i in range(W.shape[0]):
        for j in range(W.shape[1]):
            tmp = W[i,j]

            W[i,j] = tmp + h
            c1, _ = compute_cost(X, y)

            W[i,j] = tmp - h
            c2, _ = compute_cost(X, y)

            W[i,j] = tmp # reset

            grad_numerical = (c1 - c2) / (2*h)
            grad_analytic = grad_analytics[i,j]

            rel_error = abs(grad_numerical - grad_analytic) # / max(abs(grad_analytic), abs(grad_numerical))

            if (rel_error > 1e-6):
                print("numerical: {} analytic: {}, relative error: {:.3e}".format(grad_numerical, grad_analytic, rel_error))

def gradients_check_bn(X, y, reg=0.000):

    layer_sizes = [X.shape[0], 50, 50, 10]
    network = Network(layer_sizes, bn=True)

    grads, bn_grads = network.compute_gradients(X, y, reg)

    dW1, db1, dW2, db2, dW3, db3 = grads[0]['dW'], grads[0]['db'], grads[1]['dW'], grads[1]['db'], grads[2]['dW'], grads[2]['db']

    dgamma1, dbeta1, dgamma2, dbeta2 = bn_grads[0]['dgamma'], bn_grads[0]['dbeta'], bn_grads[1]['dgamma'], bn_grads[1]['dbeta']

    gamma = network.bn_params[1]['gamma']
    gamma_analytics = dgamma1

    gradients_check(gamma, gamma_analytics, lambda x, y: network.loss_and_accuracy(x, y, reg, mode='train'))

    gamma = network.bn_params[0]['gamma']
    gamma_analytics = dgamma2

    gradients_check(gamma, gamma_analytics, lambda x, y: network.loss_and_accuracy(x, y, reg, mode='train'))

    W = network.params[2]['W']
    W_analytics = dW1
    gradients_check(W, W_analytics, lambda x, y: network.loss_and_accuracy(x, y, reg, mode='train'))

    W = network.params[1]['W']
    W_analytics = dW2
    gradients_check(W, W_analytics, lambda x, y: network.loss_and_accuracy(x, y, reg, mode='train'))

    W = network.params[0]['W']
    W_analytics = dW3
    gradients_check(W, W_analytics, lambda x, y: network.loss_and_accuracy(x, y, reg, mode='train'))

    b = network.params[0]['b']
    b_analytics = db3
    gradients_check(b, b_analytics, lambda x, y: network.loss_and_accuracy(x, y, reg, mode='train'))

    b = network.params[1]['b']
    b_analytics = db2
    gradients_check(b, b_analytics, lambda x, y: network.loss_and_accuracy(x, y, reg, mode='train'))

    b = network.params[2]['b']
    b_analytics = db1
    gradients_check(b, b_analytics, lambda x, y: network.loss_and_accuracy(x, y, reg, mode='train'))


def gradients_check_no_bn(X, y, reg=0.005):
    layer_sizes = [X.shape[0], 50, 50, 10]
    network = Network(layer_sizes, bn=False)

    grads, bn_grads = network.compute_gradients(X, y, reg)

    dW1, db1, dW2, db2, dW3, db3 = grads[0]['dW'], grads[0]['db'], grads[1]['dW'], grads[1]['db'], grads[2]['dW'], grads[2]['db']

    W = network.params[2]['W']
    W_analytics = dW1
    gradients_check(W, W_analytics, lambda x, y: network.loss_and_accuracy(x, y, reg))

    W = network.params[1]['W']
    W_analytics = dW2
    gradients_check(W, W_analytics, lambda x, y: network.loss_and_accuracy(x, y, reg))

    W = network.params[0]['W']
    W_analytics = dW3
    gradients_check(W, W_analytics, lambda x, y: network.loss_and_accuracy(x, y, reg))

    b = network.params[0]['b']
    b_analytics = db3
    gradients_check(b, b_analytics, lambda x, y: network.loss_and_accuracy(x, y, reg))

    b = network.params[1]['b']
    b_analytics = db2
    gradients_check(b, b_analytics, lambda x, y: network.loss_and_accuracy(x, y, reg))

    b = network.params[2]['b']
    b_analytics = db1
    gradients_check(b, b_analytics, lambda x, y: network.loss_and_accuracy(x, y, reg))

if __name__ == '__main__':

    cifar = Cifar10('data_batch_1')
    X, Y, y = cifar.dataset(20)

    gradients_check_bn(X, y, reg=0.005)
    gradients_check_no_bn(X, y, reg=0.005)
