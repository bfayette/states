# States Manager

A Java state machine library for managing complex approval workflows and state transitions.

## Overview

**States Manager** is a lightweight Java library that provides a flexible framework for managing state machines with:
- Defined states and transitions
- Condition-based transition validation
- Approval workflow support (analyst → manager → approver)
- Event-driven state changes
- Comprehensive test coverage

## Features

- **State Machine Core**: Generic state machine implementation with transition tracking
- **Approval Workflow**: Built-in support for multi-step approval processes (analyst → manager → approver)
- **Conditional Transitions**: Validate transitions based on custom conditions
- **Event Tracking**: Track all state transitions with event history
- **Serializable**: State machine state can be serialized for persistence

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

Below is the approval workflow diagram showing states and transition events:

```
SUBMITTED        IN_PROGRESS        REJECTED        RECOMMENDED        APPROVED
  |                |                  |                |                |
  |  SEND          |  REWORK          |  APPROVE       |  APPROVE       |
  v                v                  v                v                v
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

### Event Cheat Sheet

| Event | From → To | Description |
|-------|-----------|-------------|
| **SEND** | IN_PROGRESS → SUBMITTED | Submit work for review |
| **RECOMMEND** | SUBMITTED → RECOMMENDED | Analyst recommends action |
| **APPROVE** | SUBMITTED → APPROVED | Manager approves submission |
| **APPROVE** | RECOMMENDED → APPROVED | Approve analyst recommendation |
| **REJECT** | SUBMITTED → REJECTED | Manager rejects submission |
| **REJECT** | RECOMMENDED → REJECTED | Reject analyst recommendation |
| **REJECT** | REJECTED → IN_PROGRESS | Rework after rejection |
| **REJECT** | REJECTED → APPROVED | Skip approval after rework |
| **REJECT** | APPROVED → REJECTED | Request revisions on approved |
| **REWORK** | REJECTED → IN_PROGRESS | Implement rework fixes |

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

### Approval Workflow Example

The library includes a pre-configured approval workflow supporting:

- **SUBMITTED** → **IN_PROGRESS** (via SEND event)
- **SUBMITTED** → **RECOMMENDED** (via RECOMMEND event)
- **SUBMITTED** → **APPROVED** (via APPROVE event)
- **SUBMITTED** → **REJECTED** (via REJECT event)
- **REJECTED** → **IN_PROGRESS** (via REWORK event)
- **REJECTED** → **APPROVED** (via second APPROVE event)
- **RECOMMENDED** → **REJECTED** (via REJECT event)
- **RECOMMENDED** → **APPROVED** (via APPROVE event)
- **APPROVED** → **REJECTED** (via REJECT event for revisions)

Default transition events:
- `SEND` - Move from IN_PROGRESS to SUBMITTED
- `RECOMMEND` - Move from SUBMITTED to RECOMMENDED
- `APPROVE` - Move from SUBMITTED to APPROVED (or RECOMMENDED to APPROVED)
- `REJECT` - Move from SUBMITTED/RECOMMENDED to REJECTED; from REJECTED to IN_PROGRESS/APPROVED
- `REWORK` - Move from REJECTED back to IN_PROGRESS

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