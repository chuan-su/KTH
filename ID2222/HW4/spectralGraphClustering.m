function [idx] = spectralGraphClustering(A,k)
[n,~] = size(A);
D = zeros(n,n);
for i = 1:n
    D(i,i) = sum(A(i,:),2); % get ith row of A and sum the row
end

D_inv = D^(-1/2);
L = D_inv*A*D_inv;

[eig_v,eig_D] = eig(L);
X = fliplr(eig_v(:,n-k+1:n));

[row,col] = size(X);
Y = zeros(size(X));

for(i=1:row)
   for(j=1:col)
       XrowSquared = X(i,:).^2;
       Y(i,j) = X(i,j)/sum(XrowSquared)^(1/2);
   end
end

idx = kmeans(Y,k);
end

