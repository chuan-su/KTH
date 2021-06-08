import numpy as np
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

            rel_error = abs(grad_numerical - grad_analytic)  # / (abs(grad_analytic) + abs(grad_numerical))

            if (rel_error > 1e-6):
                print("numerical: {} analytic: {}, relative error: {:.3e}".format(grad_numerical, grad_analytic, rel_error))

if __name__ == '__main__':
    from cifar import Cifar10
    from network import Network

    cifar = Cifar10('data_batch_1')
    X, Y, y = cifar.dataset(20)


    network = Network(X.shape[0], 50, 10)


    grads = network.compute_gradients(X, y)

    dW1, db1, dW2, db2 = grads['W1'], grads['b1'], grads['W2'], grads['b2']

    grad_analytics = dW1
    W = network.params['W1']
    gradients_check(W, grad_analytics, lambda x, y: network.loss_and_accuracy(x, y))
    # grad_analytics = dw2
