-module(node2).
-export([start/1, start/2]).
-define(Stabilize, 1000).
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
  node(Id, Predecessor, Successor, storage:create()).

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


node(Id, Predecessor, Successor, Store) ->
  receive
    {key, Qref, Peer} ->
      Peer ! {Qref, Id},
      node(Id, Predecessor, Successor, Store);
    {notify, New} ->
      {Pred, UpdatedStore} = notify(New, Id, Predecessor, Store),
      node(Id, Pred, Successor, UpdatedStore);
    {request, Peer} ->
      request(Peer, Predecessor),
      node(Id, Predecessor, Successor, Store);
    {status, Pred} ->
      Succ = stabilize(Pred, Id, Successor),
      node(Id, Predecessor, Succ, Store);
    stabilize ->
      stabilize(Successor),
      node(Id, Predecessor, Successor, Store);
    {add, Key, Value, Qref, Client} ->
      Added = add(Key, Value, Qref, Client,
                  Id, Predecessor, Successor, Store),
      node(Id, Predecessor, Successor, Added);
    {lookup, Key, Qref, Client} ->
      lookup(Key, Qref, Client, Id, Predecessor, Successor, Store),
      node(Id, Predecessor, Successor, Store);
    {handover, Elements} ->
      Merged = storage:merge(Store, Elements),
      node(Id, Predecessor, Successor, Merged);
    probe ->
      create_probe(Id, Successor),
      node(Id, Predecessor, Successor, Store);
    {probe, Id, Nodes, T} ->
      remove_probe(T, Nodes),
      node(Id, Predecessor, Successor, Store);
    {probe, Ref, Nodes, T} ->
      forward_probe(Ref, T, Nodes, Id, Successor),
      node(Id, Predecessor, Successor, Store);
    status ->
      io:format("[node ~w]: Predecessor ~w, Successor ~w Store ~w~n", [Id, Predecessor, Successor, Store]),
      node(Id, Predecessor, Successor, Store)
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
          Xpid ! {request,self()},
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

notify({Nkey, Npid}, Id, Predecessor, Store) ->
  case Predecessor of
    nil ->
      Keep = handover(Id, Store, Nkey, Npid),
      {{Nkey, Npid}, Keep};
    {Pkey, _} ->
      case key:between(Nkey, Pkey, Id) of
        true ->
          Keep = handover(Id, Store, Nkey, Npid),
          {{Nkey, Npid}, Keep };
        false ->
          {Predecessor, Store}
      end
  end.

handover(Id, Store, Nkey, Npid) ->
  {Keep, Rest} = storage:split(Id, Nkey, Store),
  Npid ! {handover, Rest},
  Keep.


add(Key, Value, Qref, Client, Id, {Pkey, _}, {_, Spid}, Store) ->
  case key:between(Key, Pkey, Id) of
    true ->
      Client ! {Qref, ok},
      UpdatedStore = storage:add(Key, Value, Store),
      UpdatedStore;
    false ->
      Spid ! {add, Key, Value, Qref, Client},
      Store
  end.

lookup(Key, Qref, Client, Id, {Pkey, _}, Successor, Store) ->
  case key:between(Key, Pkey, Id) of
    true ->
      Result = storage:lookup(Key, Store),
      io:format("node ~w: lookup successful: ~w~n", [Id, Store]),
      Client ! {Qref, Result};
    false ->
      {_, Spid} = Successor,
      Spid ! {lookup, Key, Qref, Client}
  end.

create_probe(Id, Successor) ->
  {_, SPid} = Successor,
  Stime = erlang:system_time(micro_seconds),
  SPid ! {probe, Id, [Id], Stime}.

forward_probe(Ref, Time, Nodes, Id, Successor) ->
  {_, Spid} = Successor,
  Spid ! {probe, Ref, Nodes ++ [Id], Time}.

remove_probe(Stime, Nodes) ->
  Etime = erlang:system_time(micro_seconds),
  io:format("Took ~w ms to traverse the ring. ~n", [Etime - Stime]),
  io:format("~w Nodes are traversed: ~w. ~n", [length(Nodes), Nodes]).
