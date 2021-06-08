import pickle
import numpy as np
from sklearn import preprocessing

class Cifar10:
    def __init__(self, file):
        self.file = '../dataset/' + file

    def dataset(self, n = 10000):
        with open(self.file, 'rb') as fo:
            data = pickle.load(fo, encoding='bytes')
        # image data N*3027
        X = data[b'data'].astype('float32')
        X = np.divide(X,255)
        # labels array len 10000
        y = np.array(data[b'labels'])

        # one hot labels
        label_encoder = preprocessing.LabelBinarizer()
        label_encoder.fit(np.arange(10))
        Y = label_encoder.transform(data[b'labels'])
        return np.transpose(X)[:, 0:n], np.transpose(Y)[:, 0:n], y[0:n]

if __name__ == '__main__':
    cifar = Cifar10('data_batch_1')
    X, Y, y = cifar.dataset(20)
    print("X shape {}".format(X.shape))
    print("X shape {}".format(Y.shape))
    print("X shape {}".format(y.shape))
