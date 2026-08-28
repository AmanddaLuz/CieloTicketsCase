# CieloTickets agent instructions

- Read `AGENTS.md` and the related spec or ADR before changing code.
- Use Kotlin, native XML, ViewBinding and unidirectional state flow.
- Keep `minSdk 24`, `targetSdk 29` and money as `Long` cents.
- Keep Android, Room and Cielo outside the pure domain layer.
- Use interfaces with `Impl` classes for repositories and use cases.
- Keep Views passive: render state and dispatch events only.
- Prefer reusable XML components when a real second use exists.
- Never include credentials or payment data in code, logs or QR Codes.
- Follow `.github/skills/pr-quality/SKILL.md`.

