import numpy as np
from scipy.special import softmax
from IPython.core.debugger import set_trace

class LinearLayer:
    def __init__(self, w, b, reg=0.01):
        self.w = w
        self.b = b
        self.reg = reg

    def forward(self, X):
        self.X = X
        return np.dot(self.w, self.X) + self.b

    def backpropate(self, gradients):
        dw = np.dot(gradients, self.X.T)
        db = np.sum(gradients, axis = 1, keepdims=True)
        return dw /self.X.shape[1] + 2 * self.reg * self.w, db / self.X.shape[1]

class HiddenLayer:
    def __init__(self, dimension, h = 50, reg = 0.01):
        mu = 0
        std = 1 / np.sqrt(dimension)
        np.random.seed(0)
        w = np.random.normal(mu, std, (h, dimension))
        b = np.zeros((h, 1))
        self.h = h
        self.linear = LinearLayer(w, b)
        self.reg = reg

    def forward(self, X):
        self.hidden = self.linear.forward(X)
        # set_trace()
        return np.maximum(0, self.hidden)

    def backpropate(self, gradients):
        gradients[self.hidden <= 0] = 0
        dw, db = self.linear.backpropate(gradients)
        # dw += (2 * self.reg * self.linear.w)
        return dw, db

    def update_parameter(self, dw, db, learning_rate):
        self.linear.w -= learning_rate * dw
        self.linear.b -= learning_rate * db

class SoftmaxLayer:
    def __init__(self, dimension, h = 50, reg = 0.01):
        mu = 0
        std = 1 / np.sqrt(h)
        np.random.seed(0)
        w = np.random.normal(mu, std, (dimension, h))
        b = np.zeros((dimension, 1))

        self.linear = LinearLayer(w, b)
        self.reg = reg

    def forward(self, hidden_layer):
        scores = self.linear.forward(hidden_layer)

        # compute the class probabilities
        # exp_scores = np.exp(scores)
        # self.probs = exp_scores / np.sum(exp_scores, axis=0, keepdims=True)
        self.probs = np.apply_along_axis(softmax, 0, scores)
        # set_trace()
        return self.probs


    def backpropate(self, y, num_examples):

        # compute gradients
        dscores = self.probs
        dscores[y, range(num_examples)] -= 1.0
        # dscores = dscores / num_examples

        dw, db = self.linear.backpropate(dscores)

        # dw += (2 * self.reg * self.linear.w)

        dhidden = np.dot(self.linear.w.T, dscores)
        return dw, db, dhidden

    def update_parameter(self, dw, db, learning_rate):
        self.linear.w -= learning_rate * dw
        self.linear.b -= learning_rate * db

if __name__ == '__main__':
    from IPython.core.debugger import set_trace
    from cifar import Cifar10

    cifar = Cifar10('data_batch_1')
    X, Y, y = cifar.dataset(20)
    print("X shape {}".format(X.shape))
    print("Y shape {}".format(Y.shape))
    print("y shape {}".format(y.shape))
