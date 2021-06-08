-module(http).
-export([parse_request/1, ok/1, get/1]).

parse_request(R) ->
  {Request, R1} = request_line(R),
  {Headers, R2} = headers(R1),
  {Body,_} = message_body(R2),
  {Request, Headers, Body}.

ok(Body) ->
  "HTTP/1.1 200 OK\r\n" ++ "\r\n" ++ Body.

get(URI) ->
    "GET " ++ URI ++ " HTTP/1.1\r\n" ++ "\r\n".

request_line([$G, $E, $T, 32 |R]) ->
  {URI, R1} = request_uri(R),
  {Ver, R2} = http_version(R1),
  [13,10|R3] = R2,
  {{get, URI, Ver},R3}.


request_uri([32|R]) ->
  {[], R};
request_uri([H|R]) ->
  {Rest, R0} = request_uri(R),
  {[H|Rest],R0}.

http_version([$H, $T, $T, $P, $/, $1, $., $1 |R]) ->
  {v11, R};
http_version([$H, $T, $T, $P, $/, $1, $., $0 |R]) ->
  {v10, R}.

headers([13,10|R]) ->
  {[],R};
headers(R) ->
  {Header,R0} = header(R),
  {Rest,R1} = headers(R0),
  {[Header|Rest],R1}.

header([13,10|R]) ->
  {[],R};
header([C|R]) ->
  {Rest, R0} = header(R),
  {[C|Rest],R0}.

message_body(R) ->
  {R,[]}.
