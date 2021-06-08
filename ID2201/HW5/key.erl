-module(key).
-export([generate/0, between/3]).

generate() -> rand:uniform(1000000000).

between(_, From, From) -> true;
between(Key, From, To) when From < To -> (Key > From) and (Key =< To);
between(Key, From, To) when From > To -> (Key > From) or (Key =< To).
