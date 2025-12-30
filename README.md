# Jasypt Helper

[![JetBrains Plugin](https://img.shields.io/badge/Plugin-Jasypt%20Helper-blue?logo=jetbrains)](https://plugins.jetbrains.com/plugin/your-plugin-id)
![License](https://img.shields.io/badge/License-Proprietary-orange)

An IntelliJ IDEA plugin to **encrypt and decrypt configuration values** in YAML, YML, and Properties files
using [Jasypt](http://www.jasypt.org/).

> 🔒 **This plugin does not store, log, or transmit your password.**

## ✨ Features

- Right-click any `.yaml`, `.yml`, or `.properties` file in **Project View** or **Editor**
- Encrypt plaintext values wrapped in `DEC(...)` → `ENC(...)`
- Decrypt encrypted values wrapped in `ENC(...)` → plaintext
- Supports multiple Jasypt algorithms (e.g., `PBEWITHHMACSHA512ANDAES_256`)

## 🚀 Usage

1. In IntelliJ IDEA, right-click a config file (`.yaml`, `.yml`, or `.properties`)
2. Select **"Jasypt Encrypt/Decrypt"** from the context menu
3. Enter your **password** and choose an **algorithm**
4. Click:
   - **Encrypt**: Converts `DEC(your_value)` → `ENC(encrypted_data)`
   - **Decrypt**: Converts `ENC(encrypted_data)` → `DEC(your_value)`

### Example

Before encryption (`application.yml`):
```yaml
database:
  password: DEC(mySecret123)
