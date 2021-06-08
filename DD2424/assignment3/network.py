import numpy as np
import math as math
import functools

from layers import *
from scipy.special import softmax
from IPython.core.debugger import set_trace

class Network:
    def __init__(self, layer_sizes, bn=True, sig=None):
        """
        - layer_size: [the dimension D of the input data, hidden layer size, ...., output size]
        """
        mu = 0
        np.random.seed(200)

        self.params = []
        self.bn_params = []
        self.bn = bn

        self.layer_num = len(layer_sizes) - 1;

        for idx, (input_size, output_size) in enumerate(zip(layer_sizes, layer_sizes[1:])):
            print("layer={} input={} output={}".format(idx, input_size, output_size))

            param = {}
            std = np.sqrt(2 / (input_size + output_size)) if sig is None else sig
            param['W'] = np.random.normal(mu, std, (output_size, input_size))
            # param['W'] /= 2
            param['b'] = np.zeros((output_size,1))

            if bn and idx < self.layer_num - 1:
                self.bn_params.append({'gamma': np.ones((output_size, 1)), 'beta': np.zeros((output_size, 1)), 'running_mean': None, 'running_var': None})

            self.params.append(param)

    def evaluate(self, input, mode='train', reg=0.01, layer_idx = 0, cache=[]):

        W, b = self.params[layer_idx]['W'], self.params[layer_idx]['b']

        if layer_idx == self.layer_num - 1:
            fn_out, fn_cache = affine_forward(W, input, b)
            score, softmax_cache = softmax_forward(fn_out)
            if mode == 'train':
                cache.append({'fn': fn_cache})
            return score, cache

        # forward
        affine_out, affine_cache = affine_forward(W, input, b)

        if self.bn:
            bn_param = self.bn_params[layer_idx]
            gamma, beta, running_mean, running_var = bn_param['gamma'], bn_param['beta'], bn_param['running_mean'], bn_param['running_var']

            bn_out, bn_cache = bn_forward(affine_out, gamma, beta, running_mean, running_var, mode)
            relu_out, relu_cache = relu_forward(bn_out)
        else:
            relu_out, relu_cache = relu_forward(affine_out)

        if mode == 'train':
            c = {'affine': affine_cache, 'relu': relu_cache}
            if self.bn:
                _, _, mu, xmu, _, _, var, _, rm, rv = bn_cache
                bn_param['running_mean'] = rm
                bn_param['running_var'] = rv
                c['bn'] = bn_cache
            cache.append(c)

        return self.evaluate(relu_out, mode, reg, layer_idx + 1, cache)

    def back_prop(self, dout, N, caches, reg, layer_idx, grads=[], bn_grads=[]):

        param, cache = self.params[layer_idx], caches[layer_idx]

        if layer_idx == self.layer_num - 1:
            dx, dW, db = affine_backward(dout, cache['fn'])
            grads.append({'dW': dW + 2 * reg * param['W'], 'db': db })

            return self.back_prop(dx, N, caches, reg, layer_idx-1, grads, bn_grads)
        elif layer_idx >= 0:
            relu_backward_out = relu_backward(dout, cache['relu'])
            if self.bn:
                bn_backward_out, dgamma, dbeta = bn_backward(relu_backward_out, cache['bn'])
                affine_backward_out, dW, db = affine_backward(bn_backward_out, cache['affine'])
            else:
                affine_backward_out, dW, db = affine_backward(relu_backward_out, cache['affine'])

            grads.append({'dW': dW + 2 * reg * param['W'], 'db': db})

            if self.bn:
                bn_grads.append({'dgamma': dgamma, 'dbeta': dbeta})

            return self.back_prop(affine_backward_out, N, caches, reg, layer_idx-1, grads, bn_grads)
        else:
            return grads, bn_grads

    def compute_gradients(self, X, y, reg=0.005):

        D, N = X.shape

        out, caches = self.evaluate(X, mode='train', reg=reg, layer_idx=0, cache=[])

        dout = softmax_backward(X, y, out)
        grads, bn_grads = self.back_prop(dout, N, caches, reg, self.layer_num - 1, [], [])

        return grads, bn_grads

    def loss_and_accuracy(self, X, y, reg=0.005, mode='test'):
        num_trains = X.shape[1]

        probs, caches = self.evaluate(X, mode, reg=reg, layer_idx=0, cache=[])
        correct_logprobs = -np.log(probs[y, range(num_trains)])
        data_loss = np.sum(correct_logprobs) / num_trains

        sum = functools.reduce(lambda s, param: s + np.sum(param['W'] * param['W']), self.params, 0)
        reg_loss = reg * sum
        loss = data_loss + reg_loss


        predicted_class = np.argmax(probs, axis=0)
        accuracy = np.mean(predicted_class == y)

        return loss, accuracy

    def update_parameters(self, grads, bn_grads,learning_rate):

        for idx in range(len(self.params)):
            self.params[idx]['W'] -= learning_rate * grads[-(idx + 1)]['dW']
            self.params[idx]['b'] -= learning_rate * grads[-(idx + 1)]['db']

        for idx in range(len(self.bn_params)):
            self.bn_params[idx]['gamma'] -= learning_rate * bn_grads[-(idx+1)]['dgamma']
            self.bn_params[idx]['beta'] -= learning_rate * bn_grads[-(idx+1)]['dbeta']

    def train(self, X, y, X_validation, y_validation, ns = 800, cycle = 2, batch_size = 100, reg=0.005):
        plt_data = {'ts_cost': [],'ts_accuracy':[],'vs_cost': [],'vs_accuracy':[], 't': []}
        num_examples = X.shape[1]

        # l = 0.01
        eta_min = 1e-5
        eta_max = 1e-1

        # learning_rate = eta_min
        t = 1
        while (t <= cycle * 2 * ns):
            indexes = np.random.permutation(num_examples)

            for batch_start in range(0, num_examples, batch_size):

                l = (t-1) // (2 * ns)

                if (t >= 2*l*ns and t <= (2*l+1)*ns):
                    learning_rate = eta_min + (t - (2*l*ns)) / ns * (eta_max - eta_min)

                if (t >= (2*l+1)*ns and t <= 2*(l+1)*ns):
                    learning_rate = eta_max - (t - (2*l+1)*ns) / ns * (eta_max - eta_min)

                batch_end = batch_start + batch_size
                b = indexes[batch_start: batch_end]

                mini_batch = X[:, b]

                grads, bn_grads = self.compute_gradients(mini_batch, y[b], reg)

                self.update_parameters(grads, bn_grads,learning_rate)

                if (t == 1 or t % (2 * ns // 10) == 0 or t == cycle * 2 * ns): # measure 10 times per cycle
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
