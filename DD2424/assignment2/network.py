import numpy as np
import math as math
from scipy.special import softmax
from IPython.core.debugger import set_trace

class Network:
    def __init__(self, input_size, hidden_size, output_size):
        """
        - input_size: the dimension D of the input data
        """
        mu = 0
        std = 1 / np.sqrt(input_size)
        std1 = 1 / np.sqrt(hidden_size)

        np.random.seed(200)
        self.params = {}

        self.params['W1'] = np.random.normal(mu, std, (hidden_size, input_size))
        self.params['b1'] = np.zeros((hidden_size,1))
        self.params['W2'] = np.random.normal(mu, std1, (output_size, hidden_size))
        self.params['b2'] = np.zeros((output_size, 1))

    def compute_gradients(self, X, y, reg=0.01):
        W1, b1 = self.params['W1'], self.params['b1']
        W2, b2 = self.params['W2'], self.params['b2']

        D, N = X.shape
        num_trains = X.shape[1]

        scores = None
        # set_trace()
        H1 = np.maximum(0, np.dot(W1, X) + b1)

        H2 = np.dot(W2, H1) + b2

        scores = H2


        probs = np.apply_along_axis(softmax, 0, scores)

        # Backward pass
        grads = {}


        s = probs
        s[y, range(num_trains)] -= 1
        s /= num_trains


        dW2 = np.dot(s, H1.T)
        db2 = np.sum(s, axis = 1, keepdims=True)


        hidden = np.dot(W2.T, s)
        hidden[H1 <= 0] = 0

        dW1 = np.dot(hidden, X.T)
        db1 = np.sum(hidden, axis = 1, keepdims=True)


        grads['W2'] = dW2 + 2 * reg * W2
        grads['b2'] = db2
        grads['W1'] = dW1 + 2 * reg * W1
        grads['b1'] = db1

        return grads

    def loss_and_accuracy(self, X, y, reg=0.01):
        W1, b1 = self.params['W1'], self.params['b1']
        W2, b2 = self.params['W2'], self.params['b2']

        num_trains = X.shape[1]

        scores = None

        H1 = np.maximum(0, np.dot(W1, X) + b1)
        H2 = np.dot(W2, H1) + b2
        scores = H2

        probs = np.apply_along_axis(softmax, 0, scores)

        correct_logprobs = -np.log(probs[y, range(num_trains)])
        data_loss = np.sum(correct_logprobs) / num_trains

        reg_loss = reg * (np.sum(W1 * W1) + np.sum(W2 * W2))
        loss = data_loss + reg_loss


        predicted_class = np.argmax(probs, axis=0)
        accuracy = np.mean(predicted_class == y)

        return loss, accuracy

    def update_parameters(self, grads, learning_rate):

        self.params['W1'] -= learning_rate * grads['W1']
        self.params['W2'] -= learning_rate * grads['W2']
        self.params['b1'] -= learning_rate * grads['b1']
        self.params['b2'] -= learning_rate * grads['b2']


    def train(self, X, y, X_validation, y_validation, ns = 800, cycle = 3, batch_size = 100, reg=0.01):
        plt_data = {'ts_cost': [],'ts_accuracy':[],'vs_cost': [],'vs_accuracy':[], 't': []}

        num_examples = X.shape[1]

        # l = 0.01
        eta_min = 1e-5
        eta_max = 1e-1

        # learning_rate = eta_min
        t = 1
        while (t <= cycle * 2 * ns):
            for batch_start in range(0, num_examples, batch_size):

                l = (t-1) // (2 * ns)

                if (t >= 2*l*ns and t <= (2*l+1)*ns):
                    learning_rate = eta_min + (t - (2*l*ns)) / ns * (eta_max - eta_min)

                if (t >= (2*l+1)*ns and t <= 2*(l+1)*ns):
                    learning_rate = eta_max - (t - (2*l+1)*ns) / ns * (eta_max - eta_min)

                batch_end = batch_start + batch_size
                mini_batch = X[:, batch_start:batch_end]


                grads = self.compute_gradients(mini_batch, y[batch_start:batch_end], reg)

                self.update_parameters(grads, learning_rate)

                if (t == 1 or t % (2 * ns // 9) == 0 or t == cycle * 2 * ns): # measure 10 times per cycle
                    ts_cost, ts_accuracy = self.loss_and_accuracy(X, y, reg)
                    vs_cost, vs_accuracy = self.loss_and_accuracy(X_validation, y_validation, reg)

                    plt_data['ts_cost'].append(ts_cost)
                    plt_data['ts_accuracy'].append(ts_accuracy)

                    plt_data['vs_cost'].append(vs_cost)
                    plt_data['vs_accuracy'].append(vs_accuracy)
                    plt_data['t'].append(t)
                    print("t={} cycle={} lr={} ts_cost={} ts_accuracy={} , vs_cost={} vs_accuracy={}".format(t,l, learning_rate, ts_cost, ts_accuracy, vs_cost, vs_accuracy))
                t += 1

        return plt_data


if __name__ == '__main__':
    from IPython.core.debugger import set_trace
    from cifar import Cifar10

    cifar = Cifar10('data_batch_1')
    validation_set = Cifar10('test_batch')

    X, Y, y = cifar.dataset(10000)
    Xv,Yv,yv = validation_set.dataset(5000)


    network = Network(X, Y, y, 0.01)
    network.train(X, y, Xv, yv, 500, 1)
