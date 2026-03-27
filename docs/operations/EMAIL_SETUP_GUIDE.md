# 📧 Email Service Setup Guide

**Last Updated**: February 6, 2026  
**Status**: ✅ Configured and Ready

---

## 📋 Overview

The PI System uses **JavaMailSender** with Gmail SMTP for sending email notifications including:
- Budget monthly reports
- Subscription renewal reminders
- Policy expiry alerts
- EMI due reminders
- Tax deadline alerts

---

## 🚀 Quick Setup

### Step 1: Enable 2-Step Verification

1. Go to your Google Account: https://myaccount.google.com
2. Navigate to **Security**
3. Enable **2-Step Verification**
4. Complete the setup process

### Step 2: Generate App Password

1. Go to **App passwords**: https://myaccount.google.com/apppasswords
2. Select app: **Mail**
3. Select device: **Other (Custom name)**
4. Enter name: **PI System**
5. Click **Generate**
6. Copy the 16-character password (e.g., `abcd efgh ijkl mnop`)

### Step 3: Configure Environment Variables

Edit your `.env` file:

```bash
# Email Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD="acuh wvvh llrb ldcj"  # Remove spaces from app password
MAIL_FROM=noreply@pi-system.com
MAIL_ENABLED=true
```

**Important**: 
- Use the **App Password**, NOT your regular Gmail password
- Remove all spaces from the app password
- The App Password is case-insensitive

### Step 4: Restart Application

```bash
# Stop the application
# Restart to load new environment variables
./gradlew bootRun
```

---

## 🧪 Testing Email Service

### Test 1: Send Test Email via API

```bash
curl -X POST http://localhost:8082/api/v1/budget/1/email-report \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "reportType": "MONTHLY",
    "month": "FEBRUARY",
    "year": 2026
  }'
```

**Expected Response**:
```json
{
  "status": "success",
  "message": "Report sent to user@example.com"
}
```

### Test 2: Check Application Logs

Look for these log entries:
```
Email sent successfully to: user@example.com
```

### Test 3: Verify Email Received

1. Check inbox for email from configured sender
2. Subject should match the report type
3. Body should contain budget details

---

## 🔧 Configuration Reference

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `MAIL_HOST` | `smtp.gmail.com` | SMTP server hostname |
| `MAIL_PORT` | `587` | SMTP server port (TLS) |
| `MAIL_USERNAME` | - | Gmail address |
| `MAIL_PASSWORD` | - | Gmail App Password (16 chars) |
| `MAIL_FROM` | `noreply@pi-system.com` | Sender email address |
| `MAIL_ENABLED` | `true` | Enable/disable email service |

### Application.yml Configuration

```yaml
spring:
  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000
    from-email: ${MAIL_FROM:noreply@pi-system.com}
    enabled: ${MAIL_ENABLED:true}
```

---

## 📨 Email Features Implemented

### 1. Budget Monthly Reports

**Trigger**: Manual API call  
**Endpoint**: `POST /api/v1/budget/{userId}/email-report`  
**Content**:
```
Monthly Budget Report

Total Income: ₹50,000.00
Total Expenses: ₹35,000.00
Net Savings: ₹15,000.00

Expense Breakdown:
  Food: ₹8,000.00
  Transport: ₹5,000.00
  Entertainment: ₹3,000.00
  ...
```

### 2. Subscription Renewal Reminders

**Trigger**: Scheduled job (daily at 8:00 AM)  
**Condition**: Subscription renews within 7 days  
**Content**:
```
Subject: Subscription Renewal Reminder

Reminder: Your Netflix subscription (₹499.00) will renew on 2026-02-15
```

### 3. Policy Expiry Alerts

**Trigger**: Scheduled job (daily at 9:00 AM)  
**Condition**: Policy expires within 30 days  
**Content**:
```
Subject: Insurance Policy Expiry Alert

Your Life Insurance policy (Policy #123456) expires on 2026-03-01.
Please renew to avoid coverage lapse.
```

### 4. EMI Due Reminders

**Trigger**: Scheduled job (daily at 8:30 AM)  
**Condition**: EMI due within 3 days  
**Content**:
```
Subject: EMI Payment Reminder

Your loan EMI of ₹15,000.00 is due on 2026-02-10.
Loan: Home Loan (#HL-001)
```

### 5. Tax Deadline Alerts

**Trigger**: Scheduled job (daily at 10:00 AM)  
**Condition**: Tax deadline within 15 days  
**Content**:
```
Subject: Tax Filing Deadline Reminder

Advance Tax installment deadline: 2026-03-15
Estimated tax liability: ₹50,000.00
```

---

## 🐛 Troubleshooting

### Issue 1: "Authentication failed" Error

**Cause**: Using regular Gmail password instead of App Password

**Solution**:
1. Generate App Password as described in Step 2
2. Ensure 2-Step Verification is enabled
3. Update `MAIL_PASSWORD` in `.env`
4. Restart application

---

### Issue 2: "Email service is disabled or not configured"

**Cause**: `MAIL_ENABLED` is set to `false` or email credentials not configured

**Solution**:
```bash
# In .env file
MAIL_ENABLED=true
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

**Check logs**:
```
Email service is disabled or not configured. Skipping email to: user@example.com
```

---

### Issue 3: Emails Not Received (No Error in Logs)

**Possible Causes**:
1. Email in spam folder
2. Incorrect recipient email
3. Gmail rate limiting

**Solutions**:
1. Check spam/junk folder
2. Verify user email in database
3. Check Gmail sending limits (500 emails/day for free accounts)
4. Whitelist sender email

---

### Issue 4: Connection Timeout

**Cause**: Firewall blocking SMTP port 587

**Solution**:
```bash
# Test SMTP connectivity
telnet smtp.gmail.com 587

# If connection fails, check firewall rules
# Or try alternative port 465 (SSL)
MAIL_PORT=465
```

Update `application.yml`:
```yaml
spring:
  mail:
    port: 465
    properties:
      mail:
        smtp:
          ssl:
            enable: true
```

---

### Issue 5: "Invalid Addresses" Error

**Cause**: Malformed email address

**Solution**:
- Verify email format in database
- Check for extra spaces or special characters
- Validate email with regex before sending

---

## 📊 Rate Limits

### Gmail Free Account Limits

| Limit Type | Value |
|------------|-------|
| **Daily sending limit** | 500 emails/day |
| **Per-minute limit** | ~20 emails/minute |
| **Recipients per message** | 500 recipients |
| **Attachment size** | 25 MB |

**Note**: Exceeding limits results in temporary blocking (1-24 hours)

### Handling Rate Limits

**Strategy 1: Batch Processing**
```java
// Send emails in batches with delays
for (int i = 0; i < emails.size(); i++) {
    emailService.sendEmail(...);
    if ((i + 1) % 20 == 0) {
        Thread.sleep(60000); // Wait 1 minute after 20 emails
    }
}
```

**Strategy 2: Queue-Based Sending**
```java
// Use message queue (Redis/RabbitMQ)
// Process emails asynchronously with rate limiting
```

---

## 🔒 Security Best Practices

### 1. Never Commit Credentials

❌ **DON'T**:
```yaml
spring:
  mail:
    username: myemail@gmail.com
    password: abcd1234efgh5678
```

✅ **DO**:
```yaml
spring:
  mail:
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
```

### 2. Use App Passwords

- ✅ Use Gmail App Password (16 characters)
- ❌ Never use regular Gmail password
- ✅ Revoke App Password if compromised

### 3. Rotate App Passwords Regularly

- Generate new App Password every 6 months
- Update `.env` file
- Revoke old App Password in Google Account

### 4. Secure .env File

```bash
# Set proper file permissions
chmod 600 .env

# Add to .gitignore
echo ".env" >> .gitignore
```

---

## 🚀 Production Deployment

### Using AWS SES (Recommended for Production)

**Benefits**:
- Higher sending limits (50,000 emails/day)
- Better deliverability
- Lower cost (€0.10 per 1,000 emails)
- Bounce/complaint handling

**Configuration**:
```bash
# .env for production
MAIL_HOST=email-smtp.us-east-1.amazonaws.com
MAIL_PORT=587
MAIL_USERNAME=your-ses-smtp-username
MAIL_PASSWORD=your-ses-smtp-password
MAIL_FROM=noreply@yourdomain.com
```

**Setup Steps**:
1. Verify domain in AWS SES
2. Move out of SES sandbox (request production access)
3. Generate SMTP credentials
4. Update environment variables

---

### Using SendGrid (Alternative)

**Configuration**:
```bash
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=your-sendgrid-api-key
MAIL_FROM=noreply@yourdomain.com
```

---

## 🎨 HTML Email Templates (Future Enhancement)

### Create Template Directory

```bash
mkdir -p src/main/resources/templates/emails
```

### Sample HTML Template

**File**: `src/main/resources/templates/emails/budget-report.html`

```html
<!DOCTYPE html>
<html>
<head>
    <style>
        body { font-family: Arial, sans-serif; }
        .header { background-color: #4CAF50; color: white; padding: 20px; }
        .content { padding: 20px; }
        .footer { background-color: #f1f1f1; padding: 10px; text-align: center; }
    </style>
</head>
<body>
    <div class="header">
        <h1>Monthly Budget Report</h1>
    </div>
    <div class="content">
        <p>Dear {{userName}},</p>
        <p>Here's your budget summary for {{month}} {{year}}:</p>
        <table>
            <tr>
                <td><strong>Total Income:</strong></td>
                <td>₹{{totalIncome}}</td>
            </tr>
            <tr>
                <td><strong>Total Expenses:</strong></td>
                <td>₹{{totalExpenses}}</td>
            </tr>
            <tr>
                <td><strong>Net Savings:</strong></td>
                <td>₹{{netSavings}}</td>
            </tr>
        </table>
    </div>
    <div class="footer">
        <p>© 2026 PI System. All rights reserved.</p>
    </div>
</body>
</html>
```

### Implement HTML Email Service

```java
@Autowired
private TemplateEngine templateEngine;

public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
    try {
        Context context = new Context();
        context.setVariables(variables);
        String htmlContent = templateEngine.process(templateName, context);
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
        log.info("HTML email sent successfully to: {}", to);
    } catch (Exception e) {
        log.error("Failed to send HTML email to: {}. Error: {}", to, e.getMessage());
    }
}
```

---

## 📈 Monitoring & Logging

### Email Delivery Metrics

Track these metrics in production:
- Total emails sent
- Successful deliveries
- Failed deliveries
- Bounce rate
- Complaint rate

### Log Examples

**Successful Email**:
```
2026-02-06 10:30:45 INFO  EmailService - Email sent successfully to: user@example.com
```

**Failed Email**:
```
2026-02-06 10:30:45 ERROR EmailService - Failed to send email to: user@example.com. Error: Authentication failed
```

**Email Disabled**:
```
2026-02-06 10:30:45 WARN  EmailService - Email service is disabled or not configured. Skipping email to: user@example.com
```

---

## ✅ Verification Checklist

Before going to production:

- [ ] Gmail App Password generated
- [ ] Environment variables configured in `.env`
- [ ] Email service enabled (`MAIL_ENABLED=true`)
- [ ] Test email sent successfully
- [ ] Email received in inbox (not spam)
- [ ] Subscription reminders working
- [ ] Budget reports sending correctly
- [ ] Email logs showing successful delivery
- [ ] Rate limits configured (if needed)
- [ ] Bounce handling implemented
- [ ] Unsubscribe link added (if required)
- [ ] Production email provider selected (AWS SES/SendGrid)
- [ ] Domain verified for production provider

---

## 📞 Support

For issues with email setup:

1. Check application logs: `logs/application.log`
2. Verify Gmail account settings
3. Test SMTP connectivity: `telnet smtp.gmail.com 587`
4. Review Gmail App Password documentation
5. Check firewall/network settings

---

**Setup Complete!** ✅  
Your email service is now configured and ready to send notifications.
