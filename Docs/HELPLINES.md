# Mental Health & Crisis Helplines

KcalGrind provides immediate access to compassionate, confidential, and professional crisis support resources. The application never provides advice on self-harm, extreme caloric restriction, prolonged fasting, or purging.

---

## Verified Regional Helplines

### 1. India (Device Locale: `IN`)
- **Emergency Services**: `112`  
  *Official Portal*: [https://112.gov.in/](https://112.gov.in/)  
  *Verified Date*: November 2026. Pan-India single emergency response number for police, medical, and fire emergencies.
- **Tele-MANAS**: `14416` or `1800-891-4416`  
  *Official Portal*: [https://telemanas.mohfw.gov.in/](https://telemanas.mohfw.gov.in/)  
  *Verified Date*: November 2026. 24/7 National Tele Mental Health Programme of India, Ministry of Health and Family Welfare (Govt. of India). Free, toll-free, multi-lingual support.
- **KIRAN Mental Health Helpline**: `1800-599-0019`  
  *Official Portal*: [https://disabilityaffairs.gov.in/](https://disabilityaffairs.gov.in/)  
  *Verified Date*: November 2026. 24/7 mental health rehabilitation helpline by Department of Empowerment of Persons with Disabilities, Ministry of Social Justice and Empowerment (Govt. of India).
- **AASRA**: `+91 98204 66726`  
  *Official Portal*: [http://www.aasra.info/](http://www.aasra.info/)  
  *Verified Date*: November 2026. 24/7 suicide prevention and emotional distress helpline.
- **Vandrevala Foundation**: `+91 9999 666 555`  
  *Official Portal*: [https://www.vandrevalafoundation.com/](https://www.vandrevalafoundation.com/)  
  *Verified Date*: November 2026. 24/7 free psychological counseling and crisis intervention via phone and WhatsApp.

---

### 2. United States (Device Locale: `US`)
- **Emergency Services**: `911`  
  *Official Portal*: [https://www.911.gov/](https://www.911.gov/)  
  *Verified Date*: November 2026. Nationwide emergency dispatcher for medical and life-threatening emergencies.
- **988 Suicide & Crisis Lifeline**: Call or Text `988`  
  *Official Portal*: [https://988lifeline.org/](https://988lifeline.org/)  
  *Verified Date*: November 2026. Free, confidential 24/7 mental health crisis and suicide prevention line funded by SAMHSA.
- **ANAD (National Association of Anorexia Nervosa and Associated Disorders) Helpline**: `1-888-375-7767`  
  *Official Portal*: [https://anad.org/get-help/eating-disorders-helpline/](https://anad.org/get-help/eating-disorders-helpline/)  
  *Verified Date*: November 2026. Dedicated national eating disorders support helpline (Monday–Friday). *Note on organization naming*: NEDA (National Eating Disorders Association) closed its phone/chat helpline in 2023; ANAD is the established, active national eating disorder support organization.
- **Crisis Text Line**: Text `HOME` to `741741`  
  *Official Portal*: [https://www.crisistextline.org/](https://www.crisistextline.org/)  
  *Verified Date*: November 2026. Free, 24/7 text support with trained crisis volunteer counselors.
- **The Trevor Project**: Call `1-866-488-7386` or text `START` to `678-678`  
  *Official Portal*: [https://www.thetrevorproject.org/](https://www.thetrevorproject.org/)  
  *Verified Date*: November 2026. 24/7 crisis intervention and suicide prevention for LGBTQ youth.

---

### 3. United Kingdom (Device Locale: `GB` / `UK`)
- **Emergency Services**: `999` (or `112`)  
  *Official Portal*: [https://www.nhs.uk/nhs-services/urgent-and-emergency-care-services/when-to-call-999/](https://www.nhs.uk/nhs-services/urgent-and-emergency-care-services/when-to-call-999/)  
  *Verified Date*: November 2026. Immediate emergency medical, police, or rescue services.
- **Samaritans**: `116 123`  
  *Official Portal*: [https://www.samaritans.org/how-we-can-help/contact-samaritan/](https://www.samaritans.org/how-we-can-help/contact-samaritan/)  
  *Verified Date*: November 2026. 24/7 free, non-judgmental emotional support across the UK and ROI.
- **Beat Eating Disorders Helpline**: `0808 801 0677` (Adults) / `0808 801 0711` (Youth)  
  *Official Portal*: [https://www.beateatingdisorders.org.uk/get-support/helplines/](https://www.beateatingdisorders.org.uk/get-support/helplines/)  
  *Verified Date*: November 2026. UK's national eating disorder charity providing helpline support 365 days a year.
- **Shout Crisis Text Line**: Text `SHOUT` to `85258`  
  *Official Portal*: [https://giveusashout.org/](https://giveusashout.org/)  
  *Verified Date*: November 2026. Free, 24/7 text service for anyone in crisis in the UK.

---

### 4. International / Default Fallback (All Other Regions)
For devices set to any other country code, KcalGrind provides universal emergency contacts and globally accessible resources:
- **Emergency**: `112` (Universal emergency number in Europe and GSM networks worldwide) or `911` (Americas).
- **Samaritans**: `116 123`
- **988 Lifeline**: `988`
- **ANAD Eating Disorders Helpline**: `+1-888-375-7767`
- **Tele-MANAS**: `+91 14416`
- **Crisis Text Line**: Text `HOME` to `741741` or international affiliates via [crisistextline.org](https://www.crisistextline.org/).

---

## Region Selection Implementation

The user's region is determined dynamically from the operating system's configured locale:
```kotlin
val countryCode = java.util.Locale.getDefault().country.uppercase()
```
- **"IN"**: Formats response with India Emergency (112), Tele-MANAS, KIRAN, AASRA, and Vandrevala Foundation.
- **"GB" or "UK"**: Formats response with UK Emergency (999), Samaritans (116 123), Beat Eating Disorders, and Shout.
- **"US"**: Formats response with US Emergency (911), 988 Suicide & Crisis Lifeline, ANAD Eating Disorders Helpline, and Crisis Text Line.
- **Default / All Others**: Formats response with international emergency guidance (112 / 911 / 999) along with globally accessible helplines.

---

## Client & Server Safety Policy

1. **Client-Side Pre-Flight Check**: When user input contains crisis, self-harm, severe fasting, or eating-disorder keywords (`checkClientCrisisKeywords`), the client intercepts the request immediately.
2. **Zero Quota Consumption**: Because the crisis response is handled locally, **no API request is transmitted to the backend**, and the user's daily message quota is never decremented.
3. **Offline & 503 Safety Net**: If a message fails due to network outage or a 503 error, the client verifies if crisis keywords are present and shows the supportive helpline message rather than a standard offline budget summary.
