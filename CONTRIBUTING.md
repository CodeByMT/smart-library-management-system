# Contributing to Smart Library Management System

Thank you for your interest in contributing! This document provides guidelines and instructions for contributing to the project.

## Code of Conduct

- Be respectful and inclusive
- Provide constructive feedback
- Focus on the code, not the person
- Help create a welcoming environment for all contributors

## Getting Started

### 1. Fork and Clone
```bash
git clone https://github.com/<your-username>/SmartLibraryManagementSystem.git
cd SmartLibraryManagementSystem
```

### 2. Create a Feature Branch
```bash
git checkout -b feature/your-feature-name
# OR for bug fixes
git checkout -b bugfix/bug-description
```

### 3. Make Your Changes
- Follow the coding standards (see below)
- Keep changes focused and atomic
- Write meaningful commit messages

### 4. Test Your Changes
```bash
# Compile
javac -d bin -cp src src/**/*.java

# Run tests
java -cp bin tests.LibrarySystemTestSuite

# Manual testing
java -cp bin main.Main
```

### 5. Submit a Pull Request
- Provide a clear description of changes
- Reference related issues
- Include before/after examples if applicable

---

## Coding Standards

### Code Style
- **Naming**: Use meaningful, descriptive names
  - Classes: PascalCase (e.g., `LibraryService`)
  - Methods: camelCase (e.g., `issueItem()`)
  - Constants: UPPER_SNAKE_CASE (e.g., `MAX_BORROW_LIMIT`)

- **Indentation**: 4 spaces (not tabs)
- **Line Length**: Max 100 characters
- **Braces**: Same-line opening (Java style)

### Example Format
```java
/**
 * Brief description of what this method does.
 * 
 * <p>More detailed explanation if needed. Explain parameters,
 * return values, and any exceptions thrown.</p>
 *
 * @param paramName description of parameter
 * @return description of return value
 * @throws ExceptionType when this is thrown and why
 */
public void methodName(String paramName) throws ExceptionType {
    // Implementation
}
```

### Design Principles

1. **Single Responsibility**: Each class should have one reason to change
2. **DRY (Don't Repeat Yourself)**: Extract common code to utilities
3. **SOLID Principles**: Follow SOLID whenever possible
4. **Type Safety**: Use enums instead of magic strings
5. **Validation**: Validate at service layer, not in domain models

### Required Practices

- ✅ Add JavaDoc comments to public methods
- ✅ Use descriptive commit messages
- ✅ Include unit tests for new features
- ✅ Ensure code compiles without warnings
- ✅ Follow existing code patterns in the project

---

## Commit Message Guidelines

Use clear, concise commit messages:

```
[TYPE] Brief description (50 chars max)

Detailed explanation if needed (wrap at 72 chars).
Explain WHAT and WHY, not HOW.

- Bullet points for multiple changes
- Reference issues: Fixes #123
- Include relevant context

Example types:
- feat: New feature
- fix: Bug fix
- refactor: Code restructuring
- docs: Documentation changes
- test: Adding/updating tests
- style: Code style changes
```

### Good Commit Examples
```
✅ fix: Prevent duplicate reservations by same user
✅ docs: Update README with architecture diagram
✅ refactor: Extract validation logic to InputValidator
```

### Poor Commit Examples
```
❌ fixed stuff
❌ updated code
❌ changes
❌ asdf
```

---

## Types of Contributions Welcome

### 🎨 Features
- New functionality that adds value
- Improvements to existing features
- Better user experience enhancements

### 🐛 Bug Fixes
- Report issues with clear reproduction steps
- Include expected vs actual behavior
- Provide relevant code snippets

### 📚 Documentation
- Improve README clarity
- Add code examples
- Create architecture diagrams
- Update JavaDoc comments

### ✅ Testing
- Add edge case tests
- Improve test coverage
- Add integration tests
- Performance testing

### ♻️ Refactoring
- Reduce code complexity
- Improve maintainability
- Enhance readability
- Apply design patterns

---

## Issue Reporting

When reporting issues, include:

1. **Title**: Clear, concise description
2. **Environment**: Java version, OS
3. **Reproduction Steps**: Exact steps to reproduce
4. **Expected Behavior**: What should happen
5. **Actual Behavior**: What actually happens
6. **Screenshots/Logs**: If applicable
7. **Suggested Fix**: If you have ideas (optional)

### Example Issue
```
Title: Duplicate user registration doesn't throw exception

Environment: Java 11, Windows 10

Reproduction:
1. Run application
2. Register user "U001" - Success
3. Register user "U001" again - No error thrown

Expected: IllegalArgumentException for duplicate ID
Actual: User added without error

Suggested Fix: Check if user exists before adding
```

---

## Pull Request Process

### Before Submitting
- [ ] Code compiles without errors or warnings
- [ ] All tests pass
- [ ] JavaDoc added for public methods
- [ ] Commit messages are clear
- [ ] No commented-out code
- [ ] No debug print statements

### PR Template
```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Refactoring
- [ ] Documentation

## Related Issues
Fixes #(issue number)

## Testing
How was this tested?

## Changes Proposed
- Bullet point 1
- Bullet point 2
- Bullet point 3

## Screenshots (if applicable)
Before/after screenshots

## Checklist
- [ ] Code follows style guidelines
- [ ] JavaDoc added for new methods
- [ ] Tests added/updated
- [ ] No new warnings generated
- [ ] Changelog updated
```

---

## Project Structure Notes

Maintain this structure when adding files:

```
src/
├── models/          - Domain objects and data models
├── services/        - Business logic and orchestration
├── interfaces/      - Contracts and abstractions
├── exceptions/      - Custom exception types
├── utils/           - Utility and helper classes
├── main/            - Entry point (Main.java only)
└── tests/           - Test classes
```

---

## Performance Considerations

When contributing, consider:

1. **Algorithm Efficiency**
   - Use appropriate data structures
   - Prefer HashMap over ArrayList for lookups
   - Document O() complexity for algorithms

2. **Memory Usage**
   - Close file handles properly
   - Avoid unnecessary object creation
   - Use StringBuilder for string concatenation

3. **User Experience**
   - Provide meaningful error messages
   - Add progress feedback for long operations
   - Handle edge cases gracefully

---

## Questions?

- Check existing issues and discussions
- Read the README and JavaDoc
- Review similar code in the project
- Ask in a new issue if still unclear

---

## Recognition

Contributors will be:
- Added to the README contributors section
- Mentioned in release notes
- Credited in commit history

Thank you for contributing! 🎉
