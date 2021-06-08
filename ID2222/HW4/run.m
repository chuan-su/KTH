function [] = run(filename,k,fig)
%RUN Summary of this function goes here
%   Detailed explanation goes here
A = readGraph(filename);
idx = spectralGraphClustering(A,k);

G = graph(A);

figure(fig)
h = plot(G,'Layout','force');

colors = ['g','r','m','k','c','w'];

for i=1:k
    highlight(h,find(idx==i),'NodeColor',colors(i));
end

end