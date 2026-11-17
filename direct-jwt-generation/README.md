# Direct JWT Token Generation (Spring Boot 3.5.7, Java 17, JJWT 0.12.6)

Send a JSON payload `{ "username": "...", "role": "..." }` and receive a signed
**HS256** JWT. A second endpoint verifies a token (signature + expiry).

> **Honest status:** the code was written and checked for XML validity, folder/package
> matching and Java syntax, but it was **not compiled or run** by the author because the
> build environment had no access to Maven Central. Your first
> **Maven > Update Project** / build is the first real compile. If anything fails, see
> "Common errors" below.

---

## 1. Requirements
- JDK 17 (Eclipse/STS 4.x can use its own or one you installed)
- Spring Tools for Eclipse (STS) with built-in Maven support
- Internet access the first time (Maven downloads libraries)

## 2. Import into STS
1. Extract `direct-jwt-generation.zip` anywhere (e.g. `C:\work`).
2. Open STS. **File > Import... > Maven > Existing Maven Projects > Next**.
3. **Root Directory > Browse...** and select the inner folder
   `direct-jwt-generation` (the one that contains `pom.xml`). **Finish**.
4. Wait for the bottom-right progress bar to finish.
5. Right-click the project > **Maven > Update Project...** > tick *Force Update of Snapshots/Releases* > **OK**.
6. Set Java 17: right-click project > **Properties > Java Build Path > Libraries >
   JRE System Library > Edit... > Alternate JRE** and choose a JDK 17
   (add one under **Window > Preferences > Java > Installed JREs** if missing).
   Then also check **Java Compiler** is set to 17.

## 3. Run
Open `src/main/java/com/example/directjwt/DirectJwtApplication.java`,
right-click > **Run As > Spring Boot App** (or **Java Application**).
Wait for `Started DirectJwtApplication` in the Console. Port: **8080**.

## 4. Secret key (how it is configured)
`application.properties` contains:

```
jwt.secret=${JWT_SECRET:local-dev-only-secret-change-me-0123456789-abcdef}
```

* If the environment variable `JWT_SECRET` exists, it is used.
* Otherwise the fallback after the colon is used. **That fallback is for local learning only
  and is public in this ZIP - never use it in production.**
* HS256 needs **at least 32 characters**. A shorter secret stops the app at startup with a clear message.
* No secret is hardcoded in Java code.

Set your own secret in Eclipse: **Run > Run Configurations... > Spring Boot App >
DirectJwtApplication > Environment tab > New...** Name `JWT_SECRET`, value = any
random string of 32+ characters.
Or in PowerShell before running from a terminal: `$env:JWT_SECRET="my-very-long-random-secret-at-least-32-chars"`

Token lifetime: `jwt.expiration-minutes=60` (change as you like).

## 5. Test in Swagger UI
1. Open <http://localhost:8080/swagger-ui/index.html>
2. Expand **POST /jwt/generate** > **Try it out**.
3. Body:
   ```json
   { "username": "Afreen", "role": "ADMIN" }
   ```
4. **Execute**. Expect **200** and a response like:
   ```json
   {
     "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJBZnJlZW4i...<payload>.<signature>",
     "tokenType": "Bearer",
     "username": "Afreen",
     "role": "ADMIN",
     "issuedAt": "2026-11-17T10:00:00Z",
     "expiresAt": "2026-11-17T11:00:00Z"
   }
   ```
5. Copy the value of `token` (without quotes).
6. The token must have **three parts separated by dots**: `HEADER.PAYLOAD.SIGNATURE`.

### Inspect header and payload
Paste the token at <https://jwt.io> (only use test tokens there), or decode the first two parts
with the PowerShell script. Expected header: `{"alg":"HS256"}`.
Expected payload: `sub`, `username`, `role`, `iat`, `exp`.

### Verify a token
Swagger UI > **POST /jwt/verify** > Try it out:
```json
{ "token": "PASTE_TOKEN_HERE" }
```
* Valid token: **200** with `"valid": true`.
* Change one character of the signature (last part): **401** "Invalid token signature".
* Token older than its expiry: **401** "Token has expired".

### Error responses
| Situation | Status |
|---|---|
| Token generated / verified | 200 |
| `username` or `role` missing/blank, or `token` missing | 400 (with `fieldErrors`) |
| Body missing or not valid JSON | 400 |
| Tampered / wrong-secret / malformed / expired token on `/jwt/verify` | 401 |

## 6. PowerShell test script
Start the app, then from the folder containing `test-jwt.ps1`:
```
powershell -ExecutionPolicy Bypass -File .\test-jwt.ps1
```
It generates a token, checks 3 parts, decodes header/payload, verifies it, tests a tampered token
(401), missing role (400) and invalid JSON (400).

## 7. Automated tests (optional)
Right-click project > **Run As > JUnit Test**, or `mvn test`. They cover: three-part token,
claims, tampered signature, wrong key, expired token, and 400/401 API responses.

## 8. Common errors
| Problem | Fix |
|---|---|
| `Port 8080 was already in use` | Stop the other app, or set `server.port=8081` in `application.properties`. |
| `jwt.secret is too short` | Use a secret of 32+ characters in `JWT_SECRET`. |
| `release version 17 not supported` / compiler errors about Java version | Select a JDK 17+ (not a JRE 8/11) as in step 2.6. |
| Red errors on `io.jsonwebtoken` imports | Maven > Update Project, check internet/proxy, then Project > Clean. |
| `Could not transfer artifact` | Network/proxy blocks Maven Central; fix proxy in Window > Preferences > Maven > User Settings. |
| Swagger page 404 | Use exactly `http://localhost:8080/swagger-ui/index.html` and confirm the app is running. |
| Verify says 401 for a token from an earlier run | The secret changed (different `JWT_SECRET`) or the token expired. Generate a new one. |
| `pom.xml` "Content is not allowed in prolog" | The file must start exactly with `<?xml version="1.0" encoding="UTF-8"?>` with nothing before it. |

## 9. Project layout
```
direct-jwt-generation/
  pom.xml
  src/main/java/com/example/directjwt/
    DirectJwtApplication.java
    controller/JwtController.java      POST /jwt/generate, POST /jwt/verify
    service/JwtUtil.java               token generation + verification (JJWT)
    dto/ JwtRequest, JwtResponse, VerifyRequest, VerifyResponse, ErrorResponse
    config/JwtConfig.java              HS256 key bean + OpenAPI info
    exception/GlobalExceptionHandler.java
  src/main/resources/application.properties
  src/test/java/...                    JUnit tests
```
