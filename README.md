# Discord Music Bot 🎵

> Bot de música para Discord desarrollado en Java con JDA y LavaPlayer.

---

## ⚠️ Proyecto Deprecado

Este proyecto ha sido deprecado debido a cambios y restricciones relacionadas con la reproducción de contenido de YouTube y políticas de terceros.

El código permanecerá público únicamente con fines educativos y de referencia.

No se garantiza mantenimiento, soporte ni funcionamiento correcto del bot.

---

## ¿Qué es?

Bot de música para servidores de Discord que permite reproducir audio desde YouTube y otras fuentes mediante comandos slash (`/`). Desarrollado completamente en Java usando la librería JDA para la integración con Discord y LavaPlayer para la gestión del audio.

## Tecnologías utilizadas

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 17 | Lenguaje principal |
| [JDA](https://github.com/discord-jda/JDA) | 5.x | Integración con la API de Discord |
| [LavaPlayer](https://github.com/lavalink-devs/lavaplayer) | 2.2.2 | Reproducción y gestión de audio |
| [YouTube Source Plugin](https://github.com/lavalink-devs/youtube-source) | 1.18.0 | Soporte para YouTube |
| SLF4J + Logback | 2.0.12 / 1.4.14 | Sistema de logging |
| dotenv-java | 3.0.0 | Carga de variables de entorno |
| Maven | 3.9 | Gestión de dependencias y build |
| Docker | — | Contenerización |

## Características

- Reproducción de música desde YouTube (URL o búsqueda por texto)
- Cola de reproducción con hasta 200 canciones
- Modos de repetición: canción, cola completa o desactivado
- Control de volumen (0–100)
- Búsqueda y salto a un momento específico de la canción (`/seek`)
- Mezcla aleatoria de la cola (`/shuffle`)
- Comandos de información del servidor y perfil de usuario
- Soporte para Docker y docker-compose
- Logging con rotación diaria de archivos

## Comandos

### 🎵 Música

| Comando | Descripción |
|---|---|
| `/play <url o búsqueda>` | Reproduce una canción o la añade a la cola |
| `/skip` | Salta a la siguiente canción |
| `/stop` | Detiene la reproducción y desconecta el bot |
| `/pause` | Pausa la reproducción |
| `/resume` | Reanuda la reproducción |
| `/queue` | Muestra la cola de reproducción |
| `/nowplaying` | Muestra la canción en curso |
| `/volume <0-100>` | Ajusta el volumen |
| `/shuffle` | Mezcla aleatoriamente la cola |
| `/loop <off\|track\|queue>` | Activa o desactiva el modo de repetición |
| `/seek <tiempo>` | Salta a un momento específico (mm:ss o segundos) |
| `/remove <posición>` | Elimina una canción de la cola |
| `/clear` | Limpia toda la cola |

### ℹ️ Información

| Comando | Descripción |
|---|---|
| `/info` | Información del servidor |
| `/me` | Tu perfil de usuario |
| `/connect` | Usuarios conectados en el canal de voz |
| `/ping` | Latencia del bot |
| `/help` | Lista todos los comandos disponibles |

## Estructura del proyecto

```
src/main/java/com/bot/music/
├── core/
│   ├── BotMain.java              # Punto de entrada, inicialización de JDA
│   └── SlashCommandConfig.java   # Registro de slash commands
├── audio/
│   ├── PlayerManager.java        # Singleton central del reproductor
│   ├── GuildMusicManager.java    # Gestor de música por servidor
│   ├── TrackScheduler.java       # Cola y ciclo de vida de pistas
│   └── AudioPlayerSendHandler.java # Puente LavaPlayer ↔ JDA
├── commands/
│   ├── music/
│   │   ├── PlayCommand.java      # Comando /play
│   │   └── MusicCommands.java    # Resto de comandos de música
│   └── info/
│       └── InfoCommands.java     # Comandos de información
├── listeners/
│   └── EventListener.java        # Enrutador de eventos y slash commands
└── utils/
    └── EmbedUtils.java           # Construcción de mensajes embed
```

## Configuración

1. Crea un archivo `.env` en la raíz del proyecto basándote en `.env.example`:

```env
DISCORD_TOKEN=tu_token_aqui
BOT_ACTIVITY=Music 🎵
BOT_STATUS=ONLINE
LOG_LEVEL=INFO
```

2. Obtén tu token en el [Discord Developer Portal](https://discord.com/developers/applications).

## Ejecución

### Con Maven

```bash
mvn clean package -DskipTests
java -jar target/discord-music-bot-*.jar
```

### Con Docker

```bash
docker-compose up -d
```

## Permisos necesarios

El bot requiere los siguientes permisos en Discord:

- Ver canales
- Enviar mensajes
- Conectar (voz)
- Hablar (voz)

## Licencia

Este proyecto es de código abierto con fines educativos. No se ofrece soporte activo.
