-module(gms4).
-export([start/1, start/2]).
-define(timeout, 200).
-define(arghh, 1000).

start(Id) ->
  Rnd = rand:uniform(1000),
  Self = self(),
  {ok, spawn_link(fun() -> init(Id, Rnd, Self) end)}.

start(Id, Grp) ->
  Rnd = rand:uniform(500),
  Self = self(),
  {ok, spawn_link(fun() -> init(Id, Rnd, Grp, Self) end)}.


init(Id, Rnd, Master) ->
  rand:seed(exsplus, {Rnd, Rnd, Rnd}),
  leader(Id, Master, 0, [], [Master],[]).

init(Id, Rnd, Grp, Master) ->
  rand:seed(exsplus, {Rnd, Rnd, Rnd}),
  Self = self(),
  Grp ! {join, Master, Self},
  receive
    {view, N, Peers, Group, Queue} ->
      Master ! {view, Group},
      [Leader | Slaves] = Peers,
      slave(Id, Master, Leader, N, {view, N, Peers, Group, Queue}, Slaves, Group)
  after ?timeout ->
      Master ! {error, "no reply from leader"}
  end.

leader(Id, Master, N, Slaves, Group, Queue) ->
  receive
    {mcast, Msg} ->
      S = N + 1, % S - total number of messages has sent to group g.

      Message = {msg, S, Msg},

      bcast(Id, erlang:append_element(Message, Queue), Slaves), % piggy-backs the value S and history-queue onto the message.

      Master ! Msg,

      NewQueue = lists:append(Queue, [Msg]),
      io:format("Queue length is ~w: {length} ~n", [length(NewQueue)]),
      leader(Id, Master, S, Slaves, Group, NewQueue);
    {join, Wrk, Peer}  ->
      S = N + 1, % S - total number of messages has sent to group g.

      Slaves2 = lists:append(Slaves, [Peer]),
      Group2 = lists:append(Group, [Wrk]),

      View = {view, S, [self() | Slaves2], Group2},

      bcast(Id, erlang:append_element(View, Queue), Slaves2),  % piggy-backs the value S and history queue on to the message.

      Master  ! {view, Group2},

      NewQueue = lists:append(Queue, [{view, Group2}]),
      io:format("Queue length is ~w: {length} ~n", [length(NewQueue)]),
      leader(Id, Master, S, Slaves2, Group2, NewQueue);
    stop ->
      ok
  end.

% N: the sequence number of the latest group message leader has delivered.
slave(Id, Master, Leader, N, Last, Slaves, Group) ->
  erlang:monitor(process, Leader),
  receive
    {mcast, Msg} ->
      Leader ! {mcast, Msg},
      slave(Id, Master, Leader, N, Last, Slaves, Group);
    {join, Wrk, Peer}  ->
      Leader ! {join, Wrk, Peer},
      slave(Id, Master, Leader, N, Last, Slaves, Group);
    {msg, S, _, _} when S < (N + 1) -> % old message, discard it.
      slave(Id, Master, Leader, N, Last, Slaves, Group);
    {msg, S, Msg, Queue}  when S == (N + 1) ->
      Master ! Msg,
      slave(Id, Master, Leader, S, {msg, S, Msg, Queue}, Slaves, Group); % if S > N +1, places the message in the hold-bak queue until the intervening message have been delivered and S=N+1
    {msg, S, Msg, Queue}  when S > (N + 1) ->
      io:format("OBS! have missed ~w: {msg} message from leader~n", [S - (N +1)]),
      MissedMessages = lists:sublist(Queue, (N+1), (S - (N + 1))),
      lists:foreach(fun(M) -> Master ! M end, MissedMessages),
      Master ! Msg,
      slave(Id, Master, Leader, N, {msg, S, Msg, Queue}, Slaves, Group);
    {view, S, _, _, _ } when S < (N + 1) ->
      slave(Id, Master, Leader, N, Last, Slaves, Group);
    {view, S, Peers, Group2, Queue} when S == (N + 1) ->
      Master ! {view, Group2},
      [Leader2 | Slaves2] =  Peers,
      slave(Id, Master, Leader2, S, {view, S, Peers, Group2, Queue}, Slaves2, Group2);
    {view, S, Peers, Group2, Queue}  when S > (N + 1) ->
      io:format("OBS! have missed ~w: {view} message from leader~n", [S - (N +1)]),
      MissedMessages = lists:sublist(Queue, (N+1), (S - (N + 1))),
      lists:foreach(fun(M) -> Master ! M end, MissedMessages),
      Master ! {view, Group2},
      slave(Id, Master, Leader, N, {view, S, Peers, Group2, Queue}, Slaves, Group);
    {'DOWN', _Ref, process, Leader, _Reason} ->
      election(Id, Master, N, Last, Slaves, Group);
    stop ->
      ok
  end.

bcast(Id, Msg, Nodes) ->
  lists:foreach(fun(Node) -> Node ! Msg, crash(Id)  end, Nodes).

crash(Id) ->
  case rand:uniform(?arghh) of
    ?arghh ->
      io:format("leader ~w: crash~n", [Id]),
      exit(no_luck);
    _ ->
      ok
  end.

election(Id, Master, N, Last, Slaves, Group) ->
  Self = self(),
  case Slaves of
    [Self|Rest] ->
      bcast(Id, Last, Rest),
      bcast(Id, {view, N, Slaves, Group, erlang:element(size(Last), Last)}, Rest),
      Master ! {view, Group},
      leader(Id, Master, N, Rest, Group, erlang:element(size(Last), Last));
    [Leader|Rest] ->
      erlang:monitor(process, Leader),
      slave(Id, Master, Leader, N, Last, Rest, Group)
  end.
