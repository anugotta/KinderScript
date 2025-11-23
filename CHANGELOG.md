# Changelog

All notable changes to KinderScript will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.0] - 2025-11-23

### Added
- **Conditional Statements (if/else)**: Added support for conditional logic with `if` and `else` statements
  - Supports comparison operators: `==`, `!=`, `<`, `>`, `<=`, `>=`
  - Variables can be used in conditions
  - Supports nested if/else statements
- **Variable Assignment**: Added `set` command for storing values in variables
  - Supports both numeric and string values
  - Variables can be used in string interpolation and conditions
  - Syntax: `set $variableName = value`
- **Additional Math Operations**:
  - `modulo(a, b)` - Returns the remainder after division
  - `power(base, exponent)` - Performs exponentiation (e.g., 2^8 = 256)
- **Versioning System**: 
  - Added version constant (currently 1.2.0)
  - Version display with `--version` or `-v` flag
  - Version shown in welcome message

### Changed
- Updated error messages to be more descriptive
- Improved function parameter/argument validation
- Enhanced math operation error handling

### Documentation
- Added comprehensive examples for all new features (Examples 9-12)
- Updated Quick Reference table with new commands
- Updated Language Limitations section
- Enhanced README with complete feature documentation

## [1.1.0] - 2024-12-19

### Added
- Division by zero protection
- Empty math operations validation
- Function parameter/argument mismatch validation
- Comprehensive README with 8 complete examples
- Quick Reference table

### Changed
- Improved error messages for better debugging
- Enhanced error handling throughout the interpreter

## [1.0.0] - 2024-12-01

### Added
- Initial release of KinderScript
- Basic commands: `say`, `add()`, `subtract()`, `multiply()`, `divide()`
- Function definitions and calls with parameters
- Repeat loops (including nested loops)
- Variable substitution in strings
- Scope management for variables and functions

---

[1.2.0]: https://github.com/anuspillai/KinderScript/releases/tag/v1.2.0
[1.1.0]: https://github.com/anuspillai/KinderScript/releases/tag/v1.1.0
[1.0.0]: https://github.com/anuspillai/KinderScript/releases/tag/v1.0.0

