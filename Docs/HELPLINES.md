# Mental Health & Crisis Helplines

KcalGrind provides immediate access to compassionate, confidential, and professional crisis support resources. The application suppresses all calorie, meal, and persona responses and displays verified emergency contacts and crisis helplines immediately whenever self-harm, extreme restriction, or eating disorder cues are encountered.

Default region priority: **India (Primary + 112) is displayed first regardless of user locale**, followed by United States & Canada, United Kingdom, and International resources.

---

## India (Primary Support)

| Service / Resource | Verified Contact | Type / Focus | Official Source URL | Verification Date |
| :--- | :--- | :--- | :--- | :--- |
| **National Emergency** | `112` | Emergency Services (Police, Medical, Fire) | [112.gov.in](https://112.gov.in/) | November 2026 |
| **AASRA** | `+91 98204 66726` | 24/7 Suicide Prevention & Crisis Distress | [aasra.info](http://www.aasra.info/) | November 2026 |
| **Vandrevala Foundation** | `+91 9999 666 555` | 24/7 Free Psychological Counselling & Crisis Intervention (Phone & WhatsApp) | [vandrevalafoundation.com](https://www.vandrevalafoundation.com/) | November 2026 |
| **Tele-MANAS (MoHFW)** | `14416` or `1800-891-4416` | 24/7 Multi-lingual Govt. Mental Health Support | [telemanas.mohfw.gov.in](https://telemanas.mohfw.gov.in/) | November 2026 |
| **KIRAN (MSJE)** | `1800-599-0019` | 24/7 Govt. Mental Health Rehabilitation | [disabilityaffairs.gov.in](https://disabilityaffairs.gov.in/) | November 2026 |

---

## United States & Canada

| Service / Resource | Verified Contact | Type / Focus | Official Source URL | Verification Date |
| :--- | :--- | :--- | :--- | :--- |
| **Emergency Services** | `911` | Immediate Life Threat / Medical Emergency | [911.gov](https://www.911.gov/) | November 2026 |
| **988 Suicide & Crisis Lifeline** | Call or Text `988` | 24/7 Suicide & Mental Health Crisis Support | [988lifeline.org](https://988lifeline.org/) | November 2026 |
| **ANAD (Eating Disorders)** | `1-888-375-7767` | Anorexia Nervosa & Associated Disorders Helpline | [anad.org](https://anad.org/) | November 2026 |
| **Crisis Text Line** | Text `HOME` to `741741` | 24/7 Crisis Counselor Support via SMS | [crisistextline.org](https://www.crisistextline.org/) | November 2026 |
| **The Trevor Project** | `1-866-488-7386` (Text `START` to `678-678`) | 24/7 LGBTQ Youth Suicide Prevention | [thetrevorproject.org](https://www.thetrevorproject.org/) | November 2026 |

---

## United Kingdom

| Service / Resource | Verified Contact | Type / Focus | Official Source URL | Verification Date |
| :--- | :--- | :--- | :--- | :--- |
| **Emergency Services** | `999` | Immediate Emergency & Ambulance | [nhs.uk/when-to-call-999](https://www.nhs.uk/nhs-services/urgent-and-emergency-care-services/when-to-call-999/) | November 2026 |
| **Samaritans** | `116 123` | 24/7 Free Listening & Crisis Support | [samaritans.org](https://www.samaritans.org/) | November 2026 |
| **Beat (Eating Disorders)** | `0808 801 0677` | UK National Eating Disorders Helpline | [beateatingdisorders.org.uk](https://beateatingdisorders.org.uk/) | November 2026 |
| **NHS Mental Health** | `111` | Non-Emergency Urgent Medical / Mental Health Advice | [111.nhs.uk](https://111.nhs.uk/) | November 2026 |

---

## International & Global Directory

| Service / Resource | Verified Link / Portal | Coverage | Official Source URL | Verification Date |
| :--- | :--- | :--- | :--- | :--- |
| **Befrienders Worldwide** | Global directory of emotional support centers | Worldwide | [befrienders.org](https://www.befrienders.org/) | November 2026 |
| **Find A Helpline** | Free, confidential support from local helplines in 130+ countries | Worldwide | [findahelpline.com](https://findahelpline.com/) | November 2026 |
| **International Association for Suicide Prevention (IASP)** | Crisis centers & helpline directory | Worldwide | [iasp.info/resources/Crisis_Centres](https://www.iasp.info/resources/Crisis_Centres/) | November 2026 |

---

## Regional Priority Selection Logic

Regardless of device locale, client code (`AiChatViewModel.kt`) and server proxy (`server.js`) format crisis messages with **India primary resources (`112`, AASRA, Vandrevala Foundation, Tele-MANAS)** placed at the very top. Supplementary international sections for the US/Canada, UK, and global directories appear beneath to ensure every user has guaranteed access to support immediately.
