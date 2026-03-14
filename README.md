# Duel Plugin (Paper/Spigot)

A starter Minecraft duel plugin with:

- `/duel <player>` to send duel requests
- `/accept` to accept a request
- `/deny` to deny a request
- Automatic win handling on death or disconnect

## Build

```bash
mvn package
```

You can also use Gradle if your local environment supports it.

## Install

1. Build the plugin jar.
2. Copy `target/duel-plugin-1.0.0.jar` into your server `plugins/` folder.
3. Restart server.

## Next features you may want

- Configurable duel arenas
- Duel countdown and kit selection
- ELO / ranked duels
- Duel cooldowns and anti-spam
