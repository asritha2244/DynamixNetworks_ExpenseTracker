# ExpenseTracker Project

A Java Swing Expense Tracker application with features requested:

FEATURES:
- Add income & expenses
- Categorize transactions (Food, Rent, Entertainment, etc.)
- View transaction history (table with sorting)
- Generate reports: Daily, Monthly, Yearly
- Date filtering (From — To)
- Save / Load transactions to CSV
- Attractive color theme + Dark mode toggle
- Categories dropdown

## Run in VS Code / Terminal (Linux, macOS, Windows with Java installed)

Make sure Java (JDK) is installed and `javac` and `java` are available on PATH.

From project root:

Compile:
```
javac src/ExpenseTracker.java
```

Run:
```
java -cp src ExpenseTracker
```

Alternatively, in VS Code:
- Open the project folder.
- Compile the Java file or use the Java extension to run `ExpenseTracker.java`.
- Ensure the working directory when running is the project root so file chooser starts at a sensible location.

## Files
- `src/ExpenseTracker.java` — main application (single-file Swing app)
- `README.md` — this file

## Notes / Tips
- Use "Save to CSV" to export your transactions and "Load from CSV" to reload them.
- Date fields accept `yyyy-MM-dd` format and the UI provides pickers.
- If you need an executable jar, you can create one with `jar` commands (not included by default).

Good luck with your submission — I included the requested features and an attractive UI with dark/light themes.
