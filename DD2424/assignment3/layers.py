import numpy as np
from scipy.special import softmax
from IPython.core.debugger import set_trace


def affine_forward(W, X, b):
    out = None

    out = np.dot(W, X) + b

    cache = (W, X, b)

    return out, cache

def affine_backward(dout, cache):

    W, X, b = cache
    dw, dx, db = None, None, None

    dx = np.dot(W.T, dout)
    dw = np.dot(dout, X.T)

    db = np.sum(dout, axis = 1, keepdims=True)

    return dx, dw, db

def bn_forward(X, gamma, beta, running_mean, running_var, mode='train'):
    D, N = X.shape
    alpha = 0.9
    eps = 1e-5
    if mode == 'train':
        # mean
        mu = np.mean(X, axis=1, keepdims=True) # mu = np.reshape(mu, (D, 1))

        xmu = X - mu
        sq= xmu ** 2

        var = 1 /float(N) * np.sum(sq, axis=1, keepdims=True)

        sqrtvar = np.sqrt(var + eps)
        ivar = 1./sqrtvar
        xhat = xmu * ivar

        gammax = gamma * xhat
        out = gammax + beta

        if running_mean is None:
            running_mean = alpha * mu + (1 - alpha) * mu
        else:
            running_mean = alpha * running_mean + (1 - alpha) * mu

        if running_var is None:
            running_var = alpha * var + (1 - alpha) * var
        else:
            running_var = alpha * running_var + (1 - alpha) * var

        cache = (xhat, gamma, mu, xmu, ivar, sqrtvar, var, eps, running_mean, running_var)
    elif mode == 'test':
        x_nomalized = (X - running_mean) / np.sqrt(running_var + eps)
        out = gamma * x_nomalized + beta
        cache = None

    return out, cache

def bn_backward(dout, cache):

    xhat, gamma, mu, xmu, ivar, sqrtvar, var, eps, _ , _ = cache
    D, N = dout.shape

    dbeta = np.sum(dout, axis=1, keepdims=True)

    dgammax = dout
    dgamma = np.sum(dgammax * xhat, axis=1, keepdims=True)

    dxhat = dgammax * gamma
    divar = np.sum(dxhat * xmu, axis=1, keepdims=True)

    dxmu1 = dxhat * ivar

    dsqrtvar = -1. / (sqrtvar ** 2) * divar
    dvar = 0.5 * 1. / np.sqrt(var+eps) * dsqrtvar

    dsq = 1. /N * np.ones((D, N)) * dvar

    dxmu2 = 2 * xmu * dsq
    dx1 = 1 * (dxmu1 + dxmu2)
    dmu = -1 * np.sum(dxmu1+dxmu2, axis = 1, keepdims=True)
    dx2 = 1. /N * np.ones((D,N)) * dmu

    dx = dx1 + dx2
    return dx, dgamma, dbeta


def relu_forward(X):
    out = None

    out = np.maximum(0, X)
    cache = X

    return out, cache

def relu_backward(dout, cache):

    dx, x = None, cache

    dx = dout
    dx[x <= 0] = 0
    return dx

def softmax_forward(X):
    scores = X

    scores = np.apply_along_axis(softmax, 0, scores)
    cache = scores

    return scores, cache

def softmax_backward(X, y, cache):

    dscores = cache
    num_trains = X.shape[1]

    dscores[y, range(num_trains)] -= 1
    dscores /= num_trains

    return dscores
