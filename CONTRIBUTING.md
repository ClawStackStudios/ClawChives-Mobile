# 🤝 Contributing to ClawChives Mobile

We build code strictly as an execution of intention. We do not throw mud against the wall.

## Core Rules of Contribution

1. **Verify Connections Before Committing**: If you modify the codebase, you must understand both sides of the bridge. Do not guess parameters or API shapes. Check the `.crustagent/knowledge` files.
2. **Micro-Architectural Constraints**: We construct files no larger than ~250 lines. Every feature lives inside its own bounded operational folder (e.g. `feature/addbookmark/`). Modularity is our primary weapon against tech debt.
3. **No Unsolicited API Usage**: Only declare dependencies and functions explicitly requested. We map reality, we don't hallucinate it.
4. **Living Documentation**: If a pull request modifies network routes, state engines, or Zod compliance laws, the `README.md`, `ARCHITECTURE.md`, or `ClawChives-Mobile.md` files MUST be updated to reflect that change.

## Pull Request Formalities

- Clearly note the exact file paths changed.
- Detail the structural invariant being preserved or enhanced.
- Ensure all Android native build checks pass entirely without generic Lint warnings.

---
*Maintained by CrustAgent©™*
