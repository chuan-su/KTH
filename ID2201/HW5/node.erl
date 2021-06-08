-module(node).
-export([start/1, start/2]).
-define(Stabilize, 100).
-define(Timeout, 5).

start(Id) ->
  start(Id, nil).

start(Id, Peer) ->
  timer:start(),
  spawn(fun() -> init(Id, Peer) end).

init(Id, Peer) ->
  Predecessor = nil,
  {ok, Successor} = connect(Id, Peer),
  schedule_stabilize(),
  node(Id, Predecessor, Successor).

connect(Id, nil) ->
  {ok, {Id, self()}};

connect(_Id, Peer) ->
  Qref = make_ref(),
  Peer ! {key, Qref, self()},
  receive
    {Qref, Skey} ->
      {ok, {Skey, Peer}}
    after ?Timeout ->
        io:format("Time out: no response~n",[])
    end.

node(Id, Predecessor, Successor) ->
  receive
    {key, Qref, Peer} ->
      Peer ! {Qref, Id},
      node(Id, Predecessor, Successor);
    {notify, New} ->
      Pred = notify(New, Id, Predecessor),
      node(Id, Pred, Successor);
    {request, Peer} ->
      request(Peer, Predecessor),
      node(Id, Predecessor, Successor);
    {status, Pred} ->
      Succ = stabilize(Pred, Id, Successor),
      node(Id, Predecessor, Succ);
    stabilize ->
      %% io:format("[node ~w] start stabilizing ~n", [Id]),
      stabilize(Successor),
      node(Id, Predecessor, Successor);
    probe ->
      create_probe(Id, Successor),
      node(Id, Predecessor, Successor);
    {probe, Id, Nodes, T} ->
      remove_probe(T, Nodes),
      node(Id, Predecessor, Successor);
    {probe, Ref, Nodes, T} ->
      forward_probe(Ref, T, Nodes, Id, Successor),
      node(Id, Predecessor, Successor)
    end.


stabilize(Pred, Id, Successor) ->
  {Skey, Spid} = Successor,
  case Pred of
    nil ->
      Spid ! {notify, {Id, self()} },
      Successor;
    {Id, _} ->
      Successor;
    {Skey, _} ->
      Spid ! {notify, {Id, self()} },
      Successor;
    {Xkey, Xpid} ->
      case key:between(Xkey, Id, Skey) of
        true ->
          Xpid ! {request, self()},
          Pred;
        false ->
          Spid ! {notify, {Id, self()} },
          Successor
      end
  end.

schedule_stabilize() ->
  timer:send_interval(?Stabilize, self(), stabilize).

stabilize({_, Spid}) ->
  Spid ! {request, self()}.

request(Peer, Predecessor) ->
  case Predecessor of
    nil ->
      Peer ! {status, nil};
    {Pkey, Ppid} ->
      Peer ! {status, {Pkey, Ppid}}
  end.

notify({Nkey, Npid}, Id, Predecessor) ->
  case Predecessor of
    nil ->
      io:format("[node ~w]: accept ~w as Pred as mine is nil ~n",[Id, Nkey]),
      {Nkey, Npid};
    {Pkey, _} ->
      case key:between(Nkey, Pkey, Id) of
        true ->
          io:format("[node ~w]: accept ~w as Pred as it is between ~n",[Id, Nkey]),
          {Nkey, Npid};
        false ->
          Predecessor
      end
  end.

create_probe(Id, Successor) ->
  {_, Spid} = Successor,
  Stime = erlang:system_time(micro_seconds),
  Spid ! {probe, Id, [Id], Stime}.

forward_probe(Ref, Time, Nodes, Id, Successor) ->
  {_, Spid} = Successor,
  Spid ! {probe, Ref, Nodes ++ [Id], Time}.

remove_probe(Stime, Nodes) ->
  Etime = erlang:system_time(micro_seconds),
  io:format("Took ~w ms to traverse the ring. ~n", [Etime - Stime]),
  io:format("~w Nodes are traversed: ~w. ~n", [length(Nodes), Nodes]).
