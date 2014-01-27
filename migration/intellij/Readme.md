TODO - document how to use migration maps and where to copy them to.


When migrating code that depends on proteus framework:
If intellij has compiled your files and detected errors, the migration will not work correctly (at least in my experiences).  The best way to perform the migration is to close all of the files in intellij, and then restart intellij.  This effectively cleans the workspace such that none of the files have been compiled.  Once intellij is open, DO NOT OPEN ANY FILES.  Just run the migration (Refactor -> Migrate).  You will need to run the migration multiple times in a row because it will not pick up some items on the first run through.

After you are satisfied that migration has worked, do a Build -> Rebuild Project.  This will compile all files in your workspace and you will be able to go through the remaining compile errors.

These are some useful text replacements that can be done to your workspace to help fix remaining compile errors.  The default shortcut for this replace command is ctrl+shift+r.

replace "protected void init" with "public void init"
