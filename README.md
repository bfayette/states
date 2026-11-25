# States Manager

A Java state machine library for managing complex **multi-step approval workflows** and state transitions.

## Overview

**States Manager** is a lightweight Java library that provides a flexible framework for managing state machines with:
- Defined states and transitions
- Condition-based transition validation
- Approval workflow support (analyst → manager → approver)
- Event-driven state changes
- Comprehensive test coverage

## Features

- **State Machine Core**: Generic state machine implementation with transition tracking
- **Multi-Step Approval Workflow**: Native support for sequential approval processes with multiple stakeholders
  - Analyst → Manager → Approver flow (3+ step approvals)
  - Rework cycles: REJECTED → IN_PROGRESS → (re-submit) → APPROVED
  - Skip approval after rework: REJECTED → APPROVED
  - Recommendation paths: SUBMITTED → RECOMMENDED → APPROVED
  - Direct approval: SUBMITTED → APPROVED (single step)
- **Conditional Transitions**: Validate transitions based on custom conditions per approval step
- **Event Tracking**: Track all state transitions with complete event history
- **Serializable**: State machine state can be serialized for persistence and recovery

## Installation

### Maven

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>org.state.machine</groupId>
    <artifactId>stateMachine</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### Gradle

```groovy
implementation 'org.state.machine:stateMachine:0.0.1-SNAPSHOT'
```

## Visual Workflow

Below is the approval workflow diagram showing states and transition events. The workflow supports **multi-step approvals** where multiple stakeholders approve in sequence:

```
SUBMITTED → IN_PROGRESS → REJECTED → RECOMMENDED → APPROVED
  |                |                  |                |
  |  SEND          |  REWORK          |  APPROVE       |  APPROVE
  v                v                  v                v
SUBMITTED → IN_PROGRESS → REJECTED → RECOMMENDED → APPROVED
  |                |                  |                |
  |  REJECT        |  REJECT          |  REJECT        |
  v                v                  v                v
REJECTED ←──────────┘                ←─────────────┘
       ^                              ^
       |  RECOMMEND                   |  REJECT (revision)
       v                              v
RECOMMENDED ←────────────────────────┘
```

### Multi-Step Approval Paths

The library supports several progression paths across the approval workflow:

**Core Approval Path (2-step):**
1. **SUBMITTED** → **IN_PROGRESS** (via `SEND` event) - Initial submission
2. **SUBMITTED** → **RECOMMENDED** (via `RECOMMEND` event) - Analyst recommends action
3. **SUBMITTED** → **APPROVED** (via `APPROVE` event) - Direct manager approval
4. **SUBMITTED** → **REJECTED** (via `REJECT` event) - Manager rejects submission

**Rework & Revision Path (after rejection):**
5. **REJECTED** → **IN_PROGRESS** (via `REWORK` event) - Implement fixes
6. **REJECTED** → **APPROVED** (via `APPROVE` event) - Skip approval after rework (*second approb step*)
7. **REJECTED** → **REJECTED** (via `REJECT` event) - Request further revisions

**Recommendation Path (2-step with analyst input):**
8. **RECOMMENDED** → **REJECTED** (via `REJECT` event) - Reject analyst recommendation
9. **RECOMMENDED** → **APPROVED** (via `APPROVE` event) - Approve analyst recommendation (*second approb step*)
10. **RECOMMENDED** → **RECOMMENDED** (via `REJECT` event) - Request revisions on recommendation

### Event Cheat Sheet

| Event | From → To | Description |
|-------|-----------|-------------|
| **SEND** | IN_PROGRESS → SUBMITTED | Submit work for review - moves work into approval flow |
| **RECOMMEND** | SUBMITTED → RECOMMENDED | Analyst recommends action/next steps - first step in recommendation path |
| **APPROVE** | SUBMITTED → APPROVED | Manager/approver approves submission - *first approb step* |
| **APPROVE** | RECOMMENDED → APPROVED | Approve analyst recommendation - *second approb step* |
| **APPROVE** | REJECTED → APPROVED | Skip approval after rework - *second approb step* |
| **REJECT** | SUBMITTED → REJECTED | Manager rejects submission |
| **REJECT** | RECOMMENDED → REJECTED | Reject analyst recommendation |
| **REJECT** | REJECTED → IN_PROGRESS | Rework after rejection - return to development |
| **REJECT** | REJECTED → APPROVED | Skip approval after rework bypass full approval |
| **REJECT** | APPROVED → REJECTED | Request revisions on approved work |
| **REWORK** | REJECTED → IN_PROGRESS | Implement fixes and return to in-progress state |

## Quick Start

### Basic State Machine Setup

```java
import org.state.machine.StateMachine;
import org.state.machine.IStatus;
import org.state.machine.EVENT_NAME;
import org.state.machine.Status;
import org.state.machine.approbation.*;
import org.state.machine.IEvent;

// Create a state machine with built-in approval workflow
List<ApprCondition> conditions = StateMachineHelper.create1AnalystThenManagerConditions(
    1, new ArrayList<>()
);

StateMachine sm = StateMachineHelper.create(conditions);

// Or create from builder
StateMachine.Builder builder = StateMachine.builder(false);
builder.transition(Status.SUBMITTED, EVENT_NAME.SEND, Status.SUBMITTED);
StateMachine sm = builder.build();
```

### Using the State Machine

```java
Summary summary = new Summary("USER-123", Status.SUBMITTED);
Event<Summary> event = new Event<>(EVENT_NAME.APPROVE.getEventName(), summary);
event.setArgument("user", new User("john_doe"));
event.setArgument("approbator", new User("manager1"));

try {
    RealizedTransition transition = sm.nextState(event);
    System.out.println("Transition allowed: " + transition.getNextStatus());
} catch (StateMachineException e) {
    System.err.println("Transition denied: " + e.getMessage());
}
```

### Approval Workflow Example (Multi-Approb Steps)

The library includes a pre-configured approval workflow supporting multi-step approvals
with the following progression paths:

### Core Approval Path
1. **SUBMITTED** → **IN_PROGRESS** (via `SEND` event) - Initial submission
2. **SUBMITTED** → **RECOMMENDED** (via `RECOMMEND` event) - Analyst recommends action
3. **SUBMITTED** → **APPROVED** (via `APPROVE` event) - Direct manager approval
4. **SUBMITTED** → **REJECTED** (via `REJECT` event) - Manager rejects submission

### Rework & Revision Path (after rejection)
5. **REJECTED** → **IN_PROGRESS** (via `REWORK` event) - Implement fixes
6. **REJECTED** → **APPROVED** (via `APPROVE` event) - Skip approval after rework *(second approb step)*
7. **REJECTED** → **REJECTED** (via `REJECT` event) - Request further revisions

### Recommendation Path
8. **RECOMMENDED** → **REJECTED** (via `REJECT` event) - Reject analyst recommendation
9. **RECOMMENDED** → **APPROVED** (via `APPROVE` event) - Approve analyst recommendation *(second approb step)*
10. **RECOMMENDED** → **RECOMMENDED** (via `REJECT` event) - Request revisions on recommendation

### Full Transition Matrix
| From \ To | SUBMITTED | IN_PROGRESS | REJECTED | RECOMMENDED | APPROVED |
|-----------|-----------|-------------|----------|-------------|----------|
| **SUBMITTED** | - | **SEND** | **REJECT** | **RECOMMEND** | **APPROVE** |
| **IN_PROGRESS** | **SEND** | - | - | - | - |
| **REJECTED** | - | **REWORK** | - | **REJECT** | **APPROVE** |
| **RECOMMENDED** | - | - | **REJECT** | - | **APPROVE** |
| **APPROVED** | - | - | **REJECT** | - | - |

### Event Descriptions
- **SEND** (`IN_PROGRESS → SUBMITTED`) - Move work from in-progress to submitted for review
- **RECOMMEND** (`SUBMITTED → RECOMMENDED`) - Analyst recommends action/next steps
- **APPROVE** (`SUBMITTED → APPROVED` or `RECOMMENDED → APPROVED`) - Manager/approver approves
  - First approb step: from SUBMITTED
  - Second approb step: from REJECTED or RECOMMENDED (after rework/analysis)
- **REJECT** (`SUBMITTED/RECOMMENDED → REJECTED`, `REJECTED → IN_PROGRESS/APPROVED`) - Reject at any stage
  - From SUBMITTED: reject submission
  - From RECOMMENDED: reject recommendation  
  - From REJECTED: rework (→ IN_PROGRESS) or skip approval (→ APPROVED)
- **REWORK** (`REJECTED → IN_PROGRESS`) - Implement fixes and return to in-progress state

**Key point**: The `APPROVE` event is the workhorse of the multi-step approval workflow - it can be used multiple times at different stages:

- **First approb step**: from `SUBMITTED` (initial manager/approver approval)
- **Second approb step**: from `REJECTED` (skip approval after rework) or from `RECOMMENDED` (approve analyst recommendation)

This design embodies the 80/20 principle: a single `APPROVE` event covers the majority of workflow scenarios while remaining flexible enough for sequential approval processes (analyst → manager → approver), rework cycles, and revision paths.

## API Components

### Core Interfaces

- `IStatus` - Status representation with description and sequence
- `IEvent` - Event with ID and entity
- `ITransition` - Realized transition with condition and next status
- `IEntity` - Entity that can have status and transitions

### Main Classes

- `StateMachine` - Core state machine with Builder pattern
- `StateMachineException` - Exception for invalid transitions
- `Summary` - Summary entity holding status and transition history
- `Event` - Event entity with arguments (user, approbator)

### Approval Workflow Classes

- `ApprCondition` - Approval condition with role and sequence
- `Role` - Roles: ANALYST, MANAGER, APPROVER
- `Status` - Status values: SUBMITTED, IN_PROGRESS, REJECTED, RECOMMENDED, APPROVED
- `EVENT_NAME` - Event names: SEND, RECOMMEND, APPROVE, REJECT, REWORK

### Helper Class

- `StateMachineHelper` - Factory methods for creating common state machine configurations, especially approval workflows

## Design Decisions

### Stateless Architecture

The state machine is **stateless** by design - each `nextState()` call is independent and does not maintain internal mutable state between invocations. This design choice provides several benefits:

- **Thread-safe**: Multiple threads can operate on the same `StateMachine` instance without synchronization concerns
- **Predictable**: Transition outcomes depend only on the current state and input event, not on hidden history
- **Composable**: Easy to chain operations, persist state between runs, or recreate from stored state
- **Testable**: No need to reset or clear state between tests

State is managed externally through `Summary` objects, which capture the entity's status and transition history. Each call to `sm.nextState(event)` returns a new `RealizedTransition` based on the current state, without modifying the `StateMachine` instance itself.

### No Threadpool Design

The library deliberately avoids a built-in threadpool or executor service for the following reasons:

- **Flexibility**: Users can integrate the state machine with their own concurrency model (Spring, Vert.x, custom executors, etc.)
- **Simplicity**: No hidden threading behavior to reason about or configure
- **Control**: Callers decide when and how state machine operations execute, including batching, rate-limiting, or transaction boundaries
- **Determinism**: Easier to test and debug since there are no race conditions or internal scheduling non-determinism

The `StateMachine` class implements `Serializable` and is designed to be instantiated once and used repeatedly, with state managed through `Summary` objects that can be saved and restored between runs.

## Testing

The project includes comprehensive tests in `src/test/java/` covering:
- Basic state transitions
- Approval workflow scenarios
- Condition validation
- Edge cases and error handling

Run tests with Maven:
```bash
mvn test
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Project Structure

```
src/main/java/org/state/machine/
├── StateMachine.java        - Core state machine implementation
├── IStatus.java             - Status interface
├── IEvent.java              - Event interface
├── ITransition.java         - Transition interface
├── IEntity.java             - Entity interface
├── Conditions.java          - Condition interface and implementations
├── StateMachineException.java - Exception class
├── approbation/             - Approval workflow package
│   ├── StateMachineHelper.java - Helper factory methods
│   ├── Summary.java           - Summary entity
│   ├── Event.java             - Event class
│   ├── Status.java            - Status enum with ExtendedStatus
│   ├── User.java              - User class
│   ├── IStatus.java           - IStatus implementation
│   ├── EVENT_NAME.java        - Event name enum
│   ├── Role.java              - Role enum
│   ├── ApprCondition.java     - Approval condition
│   ├── Conditions.java        - Condition implementations
│   └── Summary.java           - Summary class
└── test/                    - Test source files
```

## Development

- **JDK**: 1.8 (source/target) / 21 (toolchain)
- **Build**: Maven
- **Dependencies**: Apache Commons Lang3, Apache Commons Collections4, JUnit 4, Mockito, Lombok