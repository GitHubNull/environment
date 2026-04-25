# Environment

[![Java Version](https://img.shields.io/badge/Java-17-blue)](https://openjdk.org/)
[![Maven Central](https://img.shields.io/maven-central/v/net.portswigger.burp.extensions/montoya-api)](https://mvnrepository.com/artifact/net.portswigger.burp.extensions/montoya-api)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**Environment** is a Burp Suite extension that automatically replaces user-defined variables in HTTP requests using the `{{variableName}}` marker syntax. It supports multiple auto-update types (UUID, timestamp, random numbers, incrementing numbers, Groovy scripts, etc.) and is ideal for penetration testing and API automation.

[中文版](README.md)

---

## Features

- **Parameter Management**: Add, edit, delete, and view parameters with a table view supporting multi-column sorting and keyword search.
- **Auto-Update Mechanism**: Supports UUID, timestamp, SHA1-of-timestamp, random numbers, random text, incrementing numbers, and Groovy script-based updates.
- **Flexible Configuration**: Three parameter types (TEXT, NUMBER, ALL); configurable default values, length, description; enable/disable and persistence toggles.
- **Full Request Processing**: Automatically processes variable markers in URL paths, query parameters, and request bodies.
- **Data Persistence**: Uses SQLite for local storage; parameters are automatically restored after restart.
- **YAML Import/Export**: Export parameter configurations to YAML or import from YAML files.
- **Groovy Script Extension**: Supports external Groovy scripts for custom parameter generation logic.
- **User-Friendly UI**: Swing-based GUI with double-click view and move-up/move-down ordering.

---

## Tech Stack

- **Language**: Java 17
- **Build Tool**: Maven
- **Key Dependencies**:
  - [Montoya API](https://portswigger.net/burp/extender/api): Burp Suite extension API
  - [Groovy](https://groovy-lang.org/): Dynamic script execution
  - [SQLite JDBC](https://github.com/xerial/sqlite-jdbc): Local data persistence
  - [SnakeYAML](https://bitbucket.org/snakeyaml/snakeyaml): YAML import/export
  - [Apache Commons IO](https://commons.apache.org/proper/commons-io/) / [Codec](https://commons.apache.org/proper/commons-codec/): File and encoding utilities

---

## Installation & Build

### Requirements

- JDK 17 or higher
- Maven 3.x
- Burp Suite Professional / Community Edition

### Build

1. Clone the repository:
   ```bash
   git clone https://github.com/GitHubNull/environment.git
   cd environment
   ```

2. Build with Maven:
   ```bash
   mvn clean package
   ```

3. The fat JAR will be located in the `target` directory, named `environment-1.0_yyyyMMdd_HHmm.jar`.

### Install into Burp Suite

1. Open Burp Suite, go to **Extensions → Installed → Add**.
2. Select **Extension type** as **Java**.
3. Click **Select file** and choose the generated JAR from the `target` directory.
4. Click **Next** to complete installation. The **environment** tab will appear automatically.

---

## Usage

### Add a Parameter

1. Click the **Add** button.
2. Fill in the dialog:
   - **arg name**: Parameter name (letters, digits, underscores only; must start with a letter or underscore).
   - **arg type**: TEXT / NUMBER / ALL.
   - **auto update type**: UUID, TIMESTAMP, RANDOM_NUMBER, etc.
   - **arg length**: Length (for RANDOM_NUMBER / RANDOM_TEXT).
   - **defaultValue**: Default value (used as start value for INCREMENT_NUMBER).
   - **arg value**: Current value.
   - **code path**: Groovy script path (for Groovy_CODE only).
   - **description**: Description.
   - **enabled**: Whether the parameter is active.
   - **persistent**: Whether to persist to the local database.
3. Click **OK**.

### Edit a Parameter

1. Select the parameter row in the table.
2. Click **Edit**, or **double-click** the row.
3. Modify and click **OK**.

### Delete Parameters

1. Select one or more rows.
2. Click **Remove** and confirm.

### Search Parameters

Enter a keyword in the search box and click **Query** to filter by parameter name (regex supported).

### Move Parameters

Select a row and click **Move Up** or **Move Down** to reorder.

### Import / Export

- **Import**: Click **Import**, choose a YAML file. Duplicate names will be skipped.
- **Export**: Click **Export**, choose a save path. All parameters will be exported to a YAML file.

---

## Variable Marker Syntax

Use `{{variableName}}` in HTTP requests to mark variables for automatic replacement:

```http
GET /api/user/{{user_id}}?token={{session_token}} HTTP/1.1
Host: example.com
X-Timestamp: {{timestamp}}

{"request_id": "{{request_uuid}}"}
```

The extension automatically replaces markers with the latest parameter values before the request is sent.

---

## Auto-Update Types

| Type | Description | Length Required | Example |
|------|-------------|-----------------|---------|
| NONE | No auto-update | No | - |
| UUID | Random UUID | No | `550e8400-e29b-41d4-a716-446655440000` |
| TIMESTAMP | Current timestamp (ms) | No | `1714032000000` |
| SHA1_OF_TIMESTAMP | SHA1 of current timestamp | No | `a9993e36...` |
| RANDOM_NUMBER | Random digits of given length | Yes | `83749201` |
| RANDOM_TEXT | Random lowercase letters | Yes | `abcxyz` |
| INCREMENT_NUMBER | Incrementing number | No | `1, 2, 3...` |
| Groovy_CODE | Custom logic via Groovy script | No | Custom |

---

## Groovy Script Example

Create a file named `modify.groovy`:

```groovy
def modify(Map<String, String> params) {
    def name = params.keySet().iterator().next()
    def value = params.get(name)
    return "custom_" + value + "_" + System.currentTimeMillis()
}
```

When adding a parameter, select **auto update type** as `Groovy_CODE` and specify the script path.

---

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository.
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m 'Add some feature'`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request.

---

## License

This project is licensed under the [MIT License](LICENSE).

---

## Security Disclaimer

This project is intended for security research and authorized testing only. Ensure you have legal authorization before testing any target system. The developers are not responsible for any direct or indirect damages caused by misuse of this tool. See [SECURITY.md](SECURITY.md) for details.

---

## Contact

- GitHub Issues: [https://github.com/GitHubNull/environment/issues](https://github.com/GitHubNull/environment/issues)

---

## Related Documents

- [Tutorial](doc/tutorial.md)
- [Chinese README](README.md)
- [AGENT.md](AGENT.md)
