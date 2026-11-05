# Fall Error Client (Minecraft 1.21.11, Fabric)

Modules: Aim Assist, Trigger Bot, Kill Aura, Auto Mace, Auto Spear, Auto Clicker, Reach, Sprint, Fly, Speed,
Step, Auto Totem, ESP Players, ESP Item, ESP Chest, Xray (ore highlight), Fullbright, Friends (/friend add|remove|list|clear or middle-click) + Settings tab (Interface accent/opacity, HUD).

## Get the jar (no installs needed)
1. Unzip, create a free GitHub account + NEW repository.
2. Upload the CONTENTS of this folder (including the hidden .github folder).
3. Actions tab -> "Build Fall Error Client" -> Run workflow.
4. Download "FallErrorClient-jar" from Artifacts, unzip, the .jar inside is the client.

## Install
Fabric Loader 0.18.x + Fabric API for 1.21.11 -> .minecraft/mods. Right Shift opens the menu.

## Discord Rich Presence (shows logo, PlayerName, server IP / world, version, "Made by NoMoreLimits")
1. Go to https://discord.com/developers/applications -> New Application -> name it "Fall Error Client".
2. Copy the **Application ID** (General Information).
3. Rich Presence -> Art Assets -> upload `discord_logo.png` and name it exactly `logo`.
4. Open `src/main/java/fallerror/discord/DiscordRpc.java` and replace YOUR_APPLICATION_ID with your Application ID.
5. Build as usual. Toggle it in the Settings tab ("Discord RPC"; you can hide username / server IP there).
Discord app must be running on the same PC. Art assets can take a few minutes to appear.
