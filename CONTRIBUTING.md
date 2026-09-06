# Contributing to SAFEx

Thank you for considering contributing to SAFEx! This document outlines the process and guidelines for contributing.

## 🌟 How to Contribute

### Reporting Bugs

1. Check if the bug has already been reported in [Issues](../../issues)
2. If not, create a new issue with:
   - Clear title and description
   - Steps to reproduce
   - Expected vs actual behavior
   - Android version and device info
   - Screenshots if applicable

### Suggesting Features

1. Check [Issues](../../issues) for existing feature requests
2. Create a new issue with:
   - Clear description of the feature
   - Use cases and benefits
   - Possible implementation approach
   - Any mockups or examples

### Code Contributions

#### Before You Start

1. Fork the repository
2. Create a new branch: `git checkout -b feature/your-feature-name`
3. Check existing issues or create one to discuss your changes

#### Development Setup

1. **Prerequisites**
   - Android Studio Meerkat or later
   - JDK 17
   - Android SDK (API 26-34)

2. **Clone and Setup**
   ```bash
   git clone https://github.com/yourusername/safex.git
   cd safex
   ```

3. **Add API Keys** (for testing)
   Create `local.properties`:
   ```properties
   OPENAI_API_KEY=your_test_key
   GOOGLE_STT_API_KEY=your_test_key
   VIRUSTOTAL_API_KEY=your_test_key
   ```

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```

#### Code Guidelines

**Architecture**
- Follow Clean Architecture principles
- Maintain clear separation of concerns:
  - `data/` - Data sources (database, network, datastore)
  - `domain/` - Business logic (models, use cases, interfaces)
  - `presentation/` - UI (screens, viewmodels, components)

**Kotlin Style**
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Keep functions small and focused
- Add KDoc comments for public APIs

**Compose UI**
- Use Material 3 components
- Follow the existing theme system
- Ensure accessibility (contentDescription, semantics)
- Keep composables small and reusable

**Dependency Injection**
- Use Hilt for DI
- Follow the existing module structure
- Provide clear scopes (@Singleton, @ViewModelScoped)

**Testing**
- Add unit tests for business logic
- Add UI tests for critical user flows
- Aim for meaningful coverage, not just numbers

**Git Commit Messages**
Follow conventional commits:
```
feat: Add QR code batch scanning
fix: Resolve crash on call recording
docs: Update setup instructions
refactor: Simplify fraud detection logic
test: Add tests for URL scanner
```

#### Pull Request Process

1. **Update Your Branch**
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. **Ensure Quality**
   - Build succeeds: `./gradlew build`
   - No lint errors: `./gradlew lint`
   - Tests pass: `./gradlew test`
   - Code formatted properly

3. **Create Pull Request**
   - Clear title describing the change
   - Reference related issues: "Fixes #123"
   - Describe what changed and why
   - Include screenshots for UI changes
   - List any breaking changes

4. **Code Review**
   - Address reviewer feedback promptly
   - Keep discussions constructive
   - Be patient - reviews take time

5. **After Approval**
   - Squash commits if requested
   - Wait for maintainer to merge

## 📝 Code Review Checklist

Before submitting, verify:

- [ ] Code follows project architecture
- [ ] No hardcoded strings (use strings.xml)
- [ ] Proper error handling
- [ ] No memory leaks (check lifecycle)
- [ ] Accessibility labels added
- [ ] Runtime permissions handled
- [ ] Background work properly scoped
- [ ] Database migrations if schema changed
- [ ] API keys not committed
- [ ] Documentation updated
- [ ] Tests added/updated

## 🎨 UI/UX Guidelines

- Follow Material 3 design guidelines
- Use the existing color palette (Navy/Cyan)
- Maintain consistent spacing and typography
- Support both light and dark themes
- Test on different screen sizes
- Ensure 48dp minimum touch targets
- Add loading and error states
- Provide meaningful feedback

## 🔒 Security Guidelines

- Never commit API keys or secrets
- Use encrypted storage for sensitive data
- Validate all user inputs
- Follow secure network practices (HTTPS only)
- Handle permissions responsibly
- Test security features thoroughly

## 🌍 Localization

- All user-facing strings go in `strings.xml`
- Use string parameters for dynamic content
- Consider RTL language support
- Test with different languages

## 📚 Documentation

When adding features:
- Update README.md if user-facing
- Add KDoc comments for public APIs
- Update relevant docs/ files
- Include code examples where helpful

## 🤝 Community Guidelines

- Be respectful and inclusive
- Help others learn and grow
- Provide constructive feedback
- Celebrate contributions
- Follow the [Code of Conduct](CODE_OF_CONDUCT.md)

## ❓ Questions?

- Check [docs/](docs/) folder for detailed documentation
- Ask in GitHub Discussions
- Open an issue for clarification

## 📄 License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing to SAFEx! Together we can make digital safety accessible to everyone. 🛡️
