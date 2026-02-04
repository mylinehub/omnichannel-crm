# MyLineHub AI Email Service

Java 17 / Spring Boot 3.3.4 microservice that:

- Loads all active organization email accounts from Postgres.
- For `IMAP_IDLE` accounts:
  - Opens IMAP connections and listens using IDLE with polling fallback.
- For `SIEVE_HTTP` accounts:
  - Exposes `/api/email/inbound` for Dovecot Sieve (or any MTA) to push inbound messages.
- For each inbound email:
  - Runs language + heuristic detection using OpenAI (JSON-only contract).
  - Converts text to English for RAG (if needed).
  - Fetches RAG context from MyLineHub vector store using English text.
  - Calls OpenAI mini model to generate a reply in the **same language as the user** (using languageCode).
  - **Email writing instructions + footer are loaded from DB (SystemConfig)**.
  - Sends reply via per-account SMTP settings.
  - Sends an EmailReportDTO (with languageCode) to MyLineHub CRM (optional).

## Database

Configure only Postgres in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mylinehub_email
    username: postgres
    password: root
  jpa:
    hibernate:
      ddl-auto: update
```

Tables (via JPA `ddl-auto=update`):

- `organization_email_account`
- `system_config`

## SystemConfig keys (with SQL INSERT examples)

Required keys:

```sql
INSERT INTO system_config(config_key, config_value) VALUES
('OPENAI_BASE_URL', 'https://api.openai.com'),
('OPENAI_MODEL', 'gpt-4o-mini'),
('OPENAI_API_KEY', 'sk-REPLACE_ME'),
('MYLINEHUB_BASE_URL', 'https://app.mylinehub.com:8080'),
('MYLINEHUB_LOGIN_URL', '/login'),
('MYLINEHUB_LOGIN_USERNAME', 'systemUser'),
('MYLINEHUB_LOGIN_PASSWORD', 'systemPass'),
('RAG_VECTOR_STORE_URL', '/vectorSearch'),
('EMAIL_REPORT_URL', '/email/reportFromAi');
```

### Email instruction template (comes from DB)

Add **email-writing instructions + footer** as a template with `${organization}` and `${languageCode}` placeholders:

```sql
INSERT INTO system_config(config_key, config_value) VALUES
('EMAIL_SYSTEM_PROMPT_TEMPLATE',
'You are an email assistant for the organization ''${organization}''.
- Always reply as a human representative of the company.
- Use clear, polite, concise language.
- Never mention that you are an AI or automated system.
- Use the RAG context (provided in English) to answer accurately where possible.
- The detected user language code is: ''${languageCode}''.
  * If ''en'' -> reply in English.
  * If ''Romanized'' or ''Mixed'' -> reply in clear Romanized Hindi (Hindi in English letters).
  * If ''Unknown'' -> reply in simple English.
At the end of every email, add:
Regards,
${organization} Team');
```

The Java code (`OpenAiEmailComposerServiceImpl`) will:

- Load this template from `SystemConfig`.
- Replace `${organization}` with the organization name from `OrganizationEmailAccount`.
- Replace `${languageCode}` with the value from `LanguageHeuristicResult.language`.
- Send it to OpenAI as the **system prompt**.

So you can change tone / style / footer / multi-language rules by editing this row only.

### Language heuristic prompt template (comes from DB)

We also store the **language + heuristic detection instructions** in DB:

```sql
INSERT INTO system_config(config_key, config_value) VALUES
('LANGUAGE_HEURISTIC_PROMPT_TEMPLATE',
'You are a language & intent detector. Return only JSON per LanguageAndHeuristicCheckResponse.
Rules:
1. language: One of ''en'', ''Romanized'', ''Mixed'', or ''Unknown'' (symbols/emojis only).
   - ''en'' = English written properly.
   - ''Romanized'' = Hindi or another language written using English letters.
   - ''Mixed'' = English + Romanized combined.
2. englishTranslation: always provide the fully corrected English version of the message.
   Set to null ONLY if the original message is already correct English without typos, informal spelling, or grammar issues.
3. customerStillWriting: true ONLY if the message clearly looks incomplete (for example, a sentence abruptly cut or ending mid-word).
   If there is any doubt, set false.
4. noFurtherTextRequired: true ONLY if the message is a brief acknowledgment like ''ok'', ''yes'', ''thanks'', ''done'', or ''got it''.
   If unsure, set false.
5. calculationRequired: true if the message mentions or implies pricing, quote, cost, total, seats, recharge, plan, payment, estimation, or number of channels.
   If there are numbers and pricing keywords even with slight doubt, set true.
6. customerMaybeAskingDemoVideo: true if the message asks for demo, tutorial, walkthrough, how-to, or video explanation.
   If there is slight doubt, set true; only false if clearly not asking for video.
7. Maintain response consistency - if the user''s message is in Romanized or Hindi, prefer the same language style in AI replies for continuity.
IMPORTANT: Correct typos, slang, and half-written words before judgment. Identify the user''s intent and tone first.
STRICT: customerStillWriting and noFurtherTextRequired must be true only if 100% certain.
Output: A single clean JSON object following LanguageAndHeuristicCheckResponse - no commentary, extra text, or formatting beyond JSON.
Downstream assistants will use the ''language'' field to choose reply language; ''englishTranslation'' is only for understanding and RAG and must NOT be used to decide reply language.');
```

`LanguageHeuristicServiceImpl` will:

- Load this template from DB as system prompt.
- Send the raw email body as `user` content.
- Parse the JSON string from the model into `LanguageHeuristicResult`.

Then:

- `englishTranslation` (if present) is used as input to RAG.
- `language` is used to instruct the reply language.
- `language` is also sent in `EmailReportDTO.language` to CRM for analytics.

## organization_email_account examples

IMAP_IDLE account (e.g. Gmail with app-password):

```sql
INSERT INTO organization_email_account(
  organization_name, email_address, email_vendor, connection_type,
  imap_host, imap_port, imap_ssl, imap_username, imap_password,
  smtp_host, smtp_port, smtp_starttls, smtp_username, smtp_password,
  active, created_at, updated_at
) VALUES (
  'MyLineHub',
  'support@mylinehub.com',
  'GMAIL',
  'IMAP_IDLE',
  'imap.gmail.com', 993, true, 'support@mylinehub.com', 'app-password',
  'smtp.gmail.com', 587, true, 'support@mylinehub.com', 'app-password',
  true, now(), now()
);
```

SIEVE_HTTP account (mail server pushes via HTTP):

```sql
INSERT INTO organization_email_account(
  organization_name, email_address, email_vendor, connection_type,
  active, created_at, updated_at
) VALUES (
  'MyLineHub',
  'support@mylinehub.com',
  'CUSTOM',
  'SIEVE_HTTP',
  true, now(), now()
);
```

Then configure your mail server to POST inbound emails to:

```text
POST http://<this-service-host>:9090/api/email/inbound
Content-Type: application/json

{
  "from": "customer@example.com",
  "to": "support@mylinehub.com",
  "subject": "Question about pricing",
  "bodyText": "Hello, I want to know about your pricing...",
  "bodyHtml": null
}
```

The service will:

1. Look up `organization_email_account` by `to` address.
2. Run language + heuristic detection on `bodyText` / `bodyHtml`.
3. Convert to English (if needed) and fetch RAG context for that English text.
4. Generate reply via OpenAI using system instructions from `EMAIL_SYSTEM_PROMPT_TEMPLATE` and languageCode.
5. Send reply via SMTP using that account's settings.
6. Send a report (including languageCode) to MyLineHub CRM (if `EMAIL_REPORT_URL` is configured).
