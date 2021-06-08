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

    @classmethod
    def load_all(cls):
        cifar = Cifar10('data_batch_1')
        validation_set = Cifar10('test_batch')

        X, Y, y = cifar.dataset(10000)
        Xv,Yv,yv = validation_set.dataset(5000)

        for i, dataset in enumerate(['data_batch_2','data_batch_3', 'data_batch_4', 'data_batch_5']):
            X1, Y1, y1 = Cifar10(dataset).dataset(10000)
            X = np.concatenate((X, X1), axis=1)
            Y = np.concatenate((Y, Y1), axis=1)
            y = np.concatenate((y, y1), axis=None)

        X_mean = np.mean(X, axis=1, keepdims=True)
        Xv_mean = np.mean(Xv, axis=1, keepdims=True)

        X -= X_mean
        X /= np.std(X, axis=1, keepdims=True)

        Xv -= Xv_mean
        Xv /= np.std(Xv, axis=1, keepdims=True)

        return  X, Xv, y, yv

if __name__ == '__main__':
    cifar = Cifar10('data_batch_1')
    X, Y, y = cifar.dataset(20)
    print("X shape {}".format(X.shape))
    print("X shape {}".format(Y.shape))
    print("X shape {}".format(y.shape))
