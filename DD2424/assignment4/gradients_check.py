import numpy as np
from rnn import RNN

def gradients_check(W, grad_analytics, compute_cost):
    h = 1e-5
    for i in range(W.shape[0]):
        for j in range(W.shape[1]):
            tmp = W[i,j]

            W[i,j] = tmp + h
            c1, _, _ = compute_cost(x, y)

            W[i,j] = tmp - h
            c2, _, _ = compute_cost(x, y)

            W[i,j] = tmp # reset

            grad_numerical = (c1 - c2) / (2*h)
            grad_analytic = grad_analytics[i,j]

            rel_error = abs(grad_numerical - grad_analytic) # / max(abs(grad_analytic), abs(grad_numerical))

            if (rel_error > 1e-6):
                print("numerical: {} analytic: {}, relative error: {:.3e}".format(grad_numerical, grad_analytic, rel_error))

if __name__ == '__main__':

    book_fname = '../dataset/goblet_book.txt'
    book_data = open('../dataset/goblet_book.txt', 'r').read()
    book_chars = list(set(book_data))

    char_to_ind = { char:idx for idx, char in enumerate(book_chars) }
    ind_to_char = { idx:char for idx, char in enumerate(book_chars) }

    x = [char_to_ind[char] for char in book_data[0:25]]
    y = [char_to_ind[char] for char in book_data[1:26]]

    rnn = RNN(hidden_stat_dim=100, input_size=len(book_chars), output_size=len(book_chars))

    loss, p, h = rnn.forward(x, y)
    dW, dU, dV, db, dc, _ = rnn.backward(x, y, p, h)


    W = rnn.W
    W_analytics = dW

    gradients_check(W, W_analytics, lambda x, y: rnn.forward(x, y))

    U = rnn.U
    U_analytics = dU

    gradients_check(U, U_analytics, lambda x, y: rnn.forward(x, y))

    V = rnn.V
    V_analytics = dV

    gradients_check(V, V_analytics, lambda x, y: rnn.forward(x, y))

    b = rnn.b
    b_analytics = db

    gradients_check(b, b_analytics, lambda x, y: rnn.forward(x, y))

    c = rnn.c
    c_analytics = dc

    gradients_check(c, c_analytics, lambda x, y: rnn.forward(x, y))
