# How to Use Custom Agents in VSCode

## Quick Start

### 1. Access Custom Agents

In the Copilot chat panel:
1. Click the **Agent dropdown** (shows "Claude Sonnet 4.5")
2. Select **"Configure Custom Agents..."** from the menu
3. You'll see the custom agents listed from `.github/agents/`

### 2. Available Custom Agents

Your project now has **4 custom agents**:

#### 🔨 `@dev-story` - Feature Implementation
**Use for**: Implementing new features or user stories with multiple tasks
**Invocation**: Type `@dev-story` in chat
**Example**: 
```
@dev-story Implement credit card tracking feature with:
- Entity: CreditCard (number, bank, limit, balance)
- Service: CRUD operations + usage tracking
- Controller: REST endpoints
- Frontend: CreditCard page with list and add form
```

#### 🔍 `@code-review` - Code Review
**Use for**: Reviewing code changes, PRs, or specific files
**Invocation**: Select code in editor, then type `@code-review` in chat
**Example**:
```
@code-review Review the TaxCalculationService for:
- Correctness
- Security issues
- Performance problems
- Test coverage
```

#### 📝 `@tech-writer` - Documentation
**Use for**: Creating or updating API docs, module guides, developer documentation
**Invocation**: Type `@tech-writer` in chat
**Example**:
```
@tech-writer Create API documentation for the new portfolio endpoints
```

#### ⚡ `@quick-dev` - Quick Changes
**Use for**: Small bug fixes or minor features (1-3 files, <30 minutes)
**Invocation**: Type `@quick-dev` in chat
**Example**:
```
@quick-dev Add email validation to the registration form
```

#### ♻️ `@refactor` - Code Refactoring
**Use for**: Improving code structure without changing behavior
**Invocation**: Select code, then type `@refactor` in chat
**Example**:
```
@refactor Extract this 200-line method into smaller, focused methods
```

### 3. Agent vs Normal Chat

| When to Use | Mode |
|-------------|------|
| Quick question | Normal chat (no @agent) |
| Code explanation | Normal chat |
| Simple task | Normal chat |
| Structured workflow | **Custom Agent** |
| Complex feature | **@dev-story** |
| Code review | **@code-review** |

## How Agents Work

### Background Instructions
All agents automatically have access to:
- [.github/copilot-instructions.md](.github/copilot-instructions.md) - Project structure and standards

### Agent-Specific Behavior
Each agent adds specialized instructions on top of base instructions:
- **@dev-story**: Test-driven development workflow, story checklist
- **@code-review**: Review dimensions (security, performance, quality)
- **@tech-writer**: Documentation templates and writing standards
- **@quick-dev**: Fast implementation pattern, time limits
- **@refactor**: Refactoring patterns, safety checklist

## Tips for Best Results

### 1. Be Specific
❌ "Implement the feature"
✅ "Implement credit card tracking with entity, service, controller, and React page"

### 2. Provide Context
If working on existing code:
- Mention file name: "in TaxCalculationService"
- Select code in editor before invoking agent
- Reference related files

### 3. Set Expectations
Tell agent what you need:
- "Review for security issues only"
- "Implement with tests"
- "Quick fix without tests"

### 4. Use Right Agent
- **Complex**: @dev-story
- **Review**: @code-review
- **Docs**: @tech-writer
- **Quick**: @quick-dev
- **Cleanup**: @refactor

## Customizing Agents

Want to modify agent behavior? Edit files in `.github/agents/`:
- [dev-story.agent.md](.github/agents/dev-story.agent.md)
- [code-review.agent.md](.github/agents/code-review.agent.md)
- [tech-writer.agent.md](.github/agents/tech-writer.agent.md)
- [quick-dev.agent.md](.github/agents/quick-dev.agent.md)
- [refactor.agent.md](.github/agents/refactor.agent.md)

Changes take effect immediately in VSCode.

## Troubleshooting

### Agent not showing up?
1. Check file exists in `.github/agents/`
2. Verify YAML frontmatter is valid (name and description present)
3. Reload VSCode window: Cmd+Shift+P → "Reload Window"

### Agent not following instructions?
1. Check `.github/copilot-instructions.md` is loaded (shown in status bar)
2. Be more explicit in your request
3. Reference specific sections: "Follow the backend standards from copilot-instructions"

### Want to add more agents?
Create new `.agent.md` file in `.github/agents/` with:
```markdown
---
name: agent-name
description: "When to use this agent"
---

# Agent Title

Instructions for agent behavior...
```

---

**Next Step**: Click "Configure Custom Agents..." in VSCode to see your new agents! 🎉
