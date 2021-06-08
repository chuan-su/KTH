import numpy as np
from scipy.special import softmax
import matplotlib.pyplot as plt
plt.rcParams['figure.figsize'] = (14.0, 8.0)

from IPython.core.debugger import set_trace

class RNN:
    def __init__(self, hidden_stat_dim, input_size, output_size, sig=0.01):

        self.b = np.zeros((hidden_stat_dim, 1))
        self.c = np.zeros((output_size, 1))

        self.W = np.random.randn(hidden_stat_dim, hidden_stat_dim) * sig
        self.U = np.random.randn(hidden_stat_dim, input_size) * sig
        self.V = np.random.randn(output_size, hidden_stat_dim) * sig

        self.h = np.zeros((hidden_stat_dim, 1))

        self.input_size = input_size

        self.ada_W = np.zeros((hidden_stat_dim, hidden_stat_dim))
        self.ada_U = np.zeros((hidden_stat_dim, self.input_size))
        self.ada_V = np.zeros((output_size, hidden_stat_dim))
        self.ada_b = np.zeros((hidden_stat_dim, 1))
        self.ada_c = np.zeros((output_size, 1))

    def forward(self, x, y):
        seq_length = len(x)
        h = {}
        p = {}
        h[-1] = np.copy(self.h)
        loss = 0
        for t in range(seq_length):
            xt = np.zeros((self.input_size, 1))
            xt[x[t]] = 1

            at = np.dot(self.U, xt) + np.dot(self.W, h[t-1]) + self.b
            h[t] = np.tanh(at)
            ot = np.dot(self.V, h[t]) + self.c

            # p[t] = np.exp(ot) / np.sum(np.exp(ot))
            p[t] = np.apply_along_axis(softmax, 0, ot)
            loss += -np.log(p[t][y[t], 0])

        return loss, p, h

    def backward(self, x, y, p, h):
        db = np.zeros_like(self.b)
        dc = np.zeros_like(self.c)

        dW = np.zeros_like(self.W)
        dU = np.zeros_like(self.U)
        dV = np.zeros_like(self.V)

        dh_next = np.zeros_like(self.h)

        seq_length = len(x)

        for t in reversed(range(seq_length)):
            yt = np.zeros((self.input_size, 1))
            yt[y[t]] = 1

            xt = np.zeros((self.input_size, 1))
            xt[x[t]] = 1

            g = -(yt - p[t])
            dV += np.dot(g, h[t].T)
            dc += g

            dh = (1 - h[t]**2) * (np.dot(self.V.T, g) + dh_next)
            dU += np.dot(dh, xt.T)

            dW += np.dot(dh, h[t-1].T)
            db += dh

            dh_next = np.dot(self.W.T, dh)

        return dW, dU, dV, db, dc, h[-1]

    def update_parameters(self, dW, dU, dV, db, dc, h, learning_rate=0.1):
        dW = np.clip(dW, -5, 5)
        self.ada_W += dW * dW
        self.W += - learning_rate * dW / np.sqrt(self.ada_W + np.finfo(float).eps)

        dU = np.clip(dU, -5, 5)
        self.ada_U += dU * dU
        self.U += - learning_rate * dU / np.sqrt(self.ada_U + np.finfo(float).eps)

        dV = np.clip(dV, -5, 5)
        self.ada_V += dV * dV
        self.V += - learning_rate * dV / np.sqrt(self.ada_V + np.finfo(float).eps)


        db = np.clip(db, -5, 5)
        self.ada_b += db * db
        self.b += - learning_rate * db / np.sqrt(self.ada_b + np.finfo(float).eps)

        # Update c
        dc = np.clip(dc, -5, 5)
        self.ada_c += dc * dc
        self.c += - learning_rate * dc / np.sqrt(self.ada_c + np.finfo(float).eps)

        self.h = h

    def synthesize(self, h0, x0, n):
        synthesize_idx = []

        ht = h0
        xt = x0

        for t in range(n):
            at = np.dot(self.U, xt) + np.dot(self.W, ht) + self.b
            ht = np.tanh(at)
            ot = np.dot(self.V, ht) + self.c
            p = np.apply_along_axis(softmax, 0, ot)

            cp = np.cumsum(p)
            a = np.random.uniform()
            cpa = cp - a
            ixs = np.where(cpa > 0)
            ii = ixs[0][0]

            xt = np.zeros((self.input_size, 1))
            xt[ii] = 1

            synthesize_idx.append(ii)

        return synthesize_idx

    def train(self, n_epochs, seq_length, X):
        num_chars = len(X)

        book_chars = list(set(X))

        char_to_ind = { char:idx for idx, char in enumerate(book_chars) }
        ind_to_char = { idx:char for idx, char in enumerate(book_chars) }

        losses = []
        iters = []
        smooth_loss = -np.log(1.0/len(book_chars))*seq_length
        lowest_loss = None
        iter = 0
        # hprev = np.zeros((100, 1))
        f= open("guru99.txt","w+")
        for epoch in range(n_epochs):
            for i in range(num_chars // seq_length):

                x = [char_to_ind[char] for char in X[i * seq_length:(i + 1) * seq_length]]
                y = [char_to_ind[char] for char in X[i * seq_length + 1:(i + 1) * seq_length + 1]]

                if (i) % 10000 == 0:
                    x0 = np.zeros((self.input_size, 1))
                    x0[x[0]] = 1

                    synthesize_idx = self.synthesize(h0=self.h, x0=x0, n=200)
                    print("iter={} epoch={} n=200, text={}".format(iter * 100, epoch, ''.join([ind_to_char[i] for i in synthesize_idx])))

                loss, p, h = self.forward(x, y)
                dW, dU, dV, db, dc, h = self.backward(x, y, p, h)
                # hprev = h
                self.update_parameters(dW, dU, dV, db, dc, h)
                smooth_loss = smooth_loss * 0.999 + loss * 0.001

                if i % 100 == 0:
                    print("iter={} epoch={} smooth_loss={}".format(iter * 100, epoch, smooth_loss))
                    iters.append(iter * 100)
                    losses.append(smooth_loss)
                    iter += 1

                if lowest_loss == None or smooth_loss < lowest_loss:
                    lowest_loss = smooth_loss
                    x00 = np.zeros((self.input_size, 1))
                    x00[x[0]] = 1
                    synthesize_idx = self.synthesize(h0=self.h, x0=x00, n=1000)
                    f.write("iter={} epoch={}, loss={}, txt={}".format(iter * 100, epoch, lowest_loss,''.join([ind_to_char[i] for i in synthesize_idx])))


        plt.title('RNN Smooth Loss')
        plt.plot(iters, losses, 'b', label='smooth loss')
        plt.xlabel('iteration')
        plt.ylabel('smooth loss')
        plt.legend(loc='upper center', shadow=True)
        plt.savefig('fig.png')



if __name__ == '__main__':
    book_fname = '../dataset/goblet_book.txt'
    book_data = open(book_fname, 'r').read()
    book_chars = list(set(book_data))

    # char_to_ind = { char:idx for idx, char in enumerate(book_chars) }
    # ind_to_char = { idx:char for idx, char in enumerate(book_chars) }

    input_size = len(book_chars)
    output_size = input_size

    rnn = RNN(hidden_stat_dim=100, input_size=input_size, output_size=output_size)
    epoch, seq_length = 20, 25

    rnn.train(20, seq_length, book_data)
