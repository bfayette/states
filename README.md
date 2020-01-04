# States machine (Initial work. In Progress)
We try to build a simple tool to act as state machine manager. Here is how we want to solve this problem.

We view a state machine as an ordered structure defined by steps. Each step is materialized by one or more statuses. We borrow the notion of extended status from spring state machine to allow a step or a status to be repeated.

We may have to define technical states to represent non regular states that can be shared accross state machines : completed, In Progress, Invalid etc.

Our state machine is that can be said should be stateless. States is persisted within domain object.

We will not deal with persistence, guards or other specifics of state machine. We want to focus on how we move from a status A to a status B given some pre-recorded conditions.

We start from an existing tighly coupled to an existing approbation system. Refactor is be needed to make it useable in other state machine. Work in progress.
 * Any dependency to lib need to be removed. Just java standard 8 api. Original code was written in java6.
 * Classes and packages renamed are expected
 * Suggestions are welcomed
