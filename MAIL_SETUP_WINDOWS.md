# Forgot Password Mail Setup on Windows

Do not put Gmail App Passwords directly into source code. Use `.env` instead.

## Fast start

Open PowerShell in this project folder and run:

```powershell
.\run.ps1
```

The script will:

1. Ask for your Gmail username.
2. Ask for your Gmail App Password.
3. Remove spaces from the app password.
4. Create `.env`.
5. Run `docker compose up -d --build`.

## Verify variables inside backend

```powershell
docker exec sale_backend printenv | Select-String GMAIL
```

Expected:

```text
GMAIL_USERNAME=your_email@gmail.com
GMAIL_APP_PASSWORD=...
```

## Test forgot password

1. Make sure the email you enter exists in the database.
2. Open backend logs:

```powershell
docker logs -f sale_backend
```

3. Submit the forgot password form.
4. If RabbitMQ and SMTP are correct, `MailQueueListener` should send the email from `mail.queue`.

## RabbitMQ dashboard

Open:

```text
http://localhost:15672
```

Login:

```text
guest / guest
```

Check:

- `mail.queue`
- `mail.dlq`

If messages are in `mail.dlq`, RabbitMQ received the job but SMTP sending failed.
