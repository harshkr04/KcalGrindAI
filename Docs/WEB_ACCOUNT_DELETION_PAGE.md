# KcalGrind — Account & Data Deletion Request Page

**URL:** `https://kcalgrind.app/delete-account`  
**Play Store Policy Compliance:** Google Play User Data & Account Deletion Policy (Requirement for apps supporting account creation)

---

## Web Page Copy & Structure

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Request Account & Data Deletion — KcalGrind</title>
  <link rel="stylesheet" href="/style.css">
</head>
<body class="bg-dark text-light">
  <main class="container max-w-2xl py-12 px-4">
    <h1>Delete Your KcalGrind Account & Data</h1>
    <p class="lead">
      At KcalGrind, you have complete ownership and control over your personal information and health data. 
      You can permanently delete your account and all associated records at any time.
    </p>

    <hr class="my-6">

    <section class="mb-8">
      <h2>Option 1: Instant In-App Deletion (Recommended)</h2>
      <p>If you have the KcalGrind app installed on your Android device, you can delete your account immediately:</p>
      <ol class="list-decimal pl-6 space-y-2">
        <li>Open the <strong>KcalGrind</strong> app on your device.</li>
        <li>Tap the <strong>Profile</strong> tab in the bottom navigation bar.</li>
        <li>Scroll down to the <strong>Danger Zone</strong> section and tap <strong>Delete Account</strong>.</li>
        <li>Read the confirmation summary explaining what data is permanently wiped.</li>
        <li>Tap <strong>Permanently Delete</strong> and confirm your password or biometric prompt if requested.</li>
      </ol>
      <p class="text-sm text-muted mt-2">
        <em>In-app deletion takes effect instantaneously: all cloud data, local database entries, active reminders, and authentication credentials are wiped immediately.</em>
      </p>
    </section>

    <section class="mb-8">
      <h2>Option 2: Request Deletion by Email (Without App Installed)</h2>
      <p>If you have uninstalled the app or cannot access your device, submit a deletion request via email:</p>
      <div class="card p-4 bg-surface border border-muted rounded-lg">
        <p><strong>Send an email to:</strong> <a href="mailto:support@kcalgrind.app">support@kcalgrind.app</a></p>
        <p><strong>Subject line:</strong> <code>Account Deletion Request - [Your Registered Email]</code></p>
        <p><strong>Email body must include:</strong></p>
        <ul class="list-disc pl-6">
          <li>Your registered account email address.</li>
          <li>A clear statement: <em>"Please permanently delete my KcalGrind account and purge all associated cloud and personal data."</em></li>
        </ul>
      </div>
      <p class="mt-3">
        <strong>Verification & Processing Timeframe:</strong> For your security, our support team will send a verification confirmation link to your registered email address. Once verified, all cloud records are permanently purged within <strong>48 to 72 hours</strong> (and no later than 30 days under GDPR and CCPA regulations).
      </p>
    </section>

    <section class="mb-8">
      <h2>Important Notice: Google Play Subscriptions</h2>
      <p class="alert alert-warning p-4 bg-surface border border-warning rounded-lg">
        <strong>Active Subscriptions:</strong> Any active Google Play subscription must be cancelled separately in Google Play (under Google Play Store &rarr; Profile &rarr; Payments & Subscriptions &rarr; Subscriptions) and is <strong>not cancelled</strong> by deleting your KcalGrind account. Google Play handles all billing and recurring subscriptions independently.
      </p>
    </section>

    <section class="mb-8">
      <h2>What Data is Permanently Deleted?</h2>
      <p>When your account is deleted, the following records are permanently erased from our production servers and cannot be recovered:</p>
      <ul class="list-disc pl-6 space-y-1">
        <li><strong>Profile & Identity:</strong> Email address, display name, age, height, baseline weight, goal weight, dietary preferences, and Firebase Authentication UID.</li>
        <li><strong>Nutrition & Diary Records:</strong> All logged meals, custom foods, saved meal templates, food log items, calorie and macro daily totals.</li>
        <li><strong>Health & Activity:</strong> Weight log history, water hydration entries, synchronized Health Connect step counts and active calorie burn records.</li>
        <li><strong>AI Interactions:</strong> AI Coach chat history, nutrition advice transcripts, and photo vision analysis records.</li>
        <li><strong>Subscriptions & Entitlements:</strong> Cloud subscription linkage and in-memory rate limits.</li>
      </ul>
    </section>

    <section class="mb-8">
      <h2>Data Retention Exceptions</h2>
      <p>
        We do not retain personal health, diary, or nutrition data after account deletion. Non-identifiable operational container logs are rotated and deleted automatically pursuant to standard cloud infrastructure retention (maximum 30 days).
      </p>
    </section>

    <footer class="mt-12 text-sm text-muted">
      <p>Questions? Contact us at <a href="mailto:support@kcalgrind.app">support@kcalgrind.app</a>.</p>
      <p><a href="/privacy">Privacy Policy</a> | <a href="/terms">Terms of Service</a></p>
    </footer>
  </main>
</body>
</html>
```

---

## Launch Blocker Verification Checklist
- [x] Web deletion documentation created in `Docs/WEB_ACCOUNT_DELETION_PAGE.md`.
- [x] URL endpoint `https://kcalgrind.app/delete-account` declared in Play Console Data Safety questionnaire.
- [x] In-app deletion flow implemented in `ProfileScreen` with `DeleteAccountUseCase`.
- [x] Email deletion process specified with SLA (48–72 hours).
- [x] Complete list of purged data entities documented.
