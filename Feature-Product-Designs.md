# Feature Flags System – Design & Implementation

## Purpose

The Feature Flags system allows us to:
- Enable / disable features without redeploying
- Protect production during bugs or partial failures
- Gradually roll out features
- Run safe experiments
- Perform instant rollbacks

This is a **core safety system**, not an optional enhancement.

---

## What Problems This Solves

- Bug found in production → disable feature instantly
- Partner API (AA / Broker) is unstable → turn off integration
- Incomplete feature merged → keep disabled
- Gradual rollout to users
- Emergency kill switch during incidents

---

## Scope of Feature Flags

Feature flags can control:

- APIs
- Background jobs
- UI features
- External integrations
- Calculations / logic paths
- Rate limits and thresholds

---

## Feature Flag Levels

| Level | Description |
|-----|------------|
| GLOBAL | Affects entire application |
| MODULE | Affects a specific module (AA, Portfolio, Reports) |
| API | Affects a specific API |
| JOB | Affects background jobs |
| USER | Affects specific users or roles |

---

## Naming Convention

Use **clear, descriptive, hierarchical keys**.