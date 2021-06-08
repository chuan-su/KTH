function [A] = readGraph(filename)
%UNTITLED6 Summary of this function goes here
%   Detailed explanation goes here
E = csvread(filename);
col1 = E(:,1);
col2 = E(:,2);
max_ids = max(max(col1,col2));
As= sparse(col1, col2, 1, max_ids, max_ids); 
A = full(As);
end

