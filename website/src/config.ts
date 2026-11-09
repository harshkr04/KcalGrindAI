export interface FeatureItem {
  id: string
  title: string
  description: string
  badge?: string
  icon: string
}

export interface FAQItem {
  question: string
  answer: string
}

export const DESIGN_TOKENS = {
  dark: {
    bg: '#101211',
    bgStart: '#14201a',
    bgMid: '#0f1311',
    bgEnd: '#0d100e',
    surface: '#191d1b',
    surfaceElevated: '#222825',
    surfaceSecondary: '#1c221e',
    border: 'rgba(255, 255, 255, 0.08)',
    borderSubtle: '#343b36',
    borderHighlight: 'rgba(56, 199, 123, 0.4)',
    textPrimary: '#f4f6f4',
    textSecondary: '#b2bab5',
    textMuted: '#858f89',
    accent: '#38c77b',
    accentDeep: '#16834d',
    progressTrack: '#2b332e',
    calorieRing: '#38c77b',
    proteinRing: '#5390d9',
    carbsRing: '#a8b3ac',
    fatsRing: '#858f89',
  },
  light: {
    bg: '#f2f4f2',
    bgStart: '#eaf4ee',
    bgMid: '#f2f7f4',
    bgEnd: '#f7faf8',
    surface: '#ffffff',
    surfaceElevated: '#e7ebe8',
    surfaceSecondary: '#ebf0ec',
    border: 'rgba(0, 0, 0, 0.08)',
    borderSubtle: '#d3dad5',
    borderHighlight: 'rgba(22, 131, 77, 0.35)',
    textPrimary: '#171c19',
    textSecondary: '#505a53',
    textMuted: '#5e6962',
    accent: '#0f6b3f',
    accentDeep: '#16834d',
    progressTrack: '#dce3de',
    calorieRing: '#16834d',
    proteinRing: '#2b6cb0',
    carbsRing: '#505a53',
    fatsRing: '#68736b',
  },
} as const

// One unified demo day reconciled across every preview:
// Dal tadka & roti 420 + Chicken breast & rice 540 + Paneer bhurji & paratha 380 + Curd & fruit 283 = 1,623 kcal.
// Macros: 120g protein * 4 + 180g carbs * 4 + 47g fat * 9 = 480 + 720 + 423 = 1,623 kcal.
// Target: 2,150 kcal. Remaining: 527 kcal.
export const DEMO_DAY = {
  calorieTarget: 2150,
  calorieConsumed: 1623,
  calorieRemaining: 527,
  proteinGrams: 120,
  proteinTarget: 140,
  proteinCalories: 480,
  carbsGrams: 180,
  carbsTarget: 220,
  carbsCalories: 720,
  fatsGrams: 47,
  fatsTarget: 60,
  fatsCalories: 423,
  waterMlConsumed: 2250,
  waterMlTarget: 3000,
  stepsTaken: 8420,
  stepsTarget: 10000,
  meals: [
    { id: 'm1', name: 'Dal tadka & roti', calories: 420, mealType: 'Breakfast' },
    { id: 'm2', name: 'Chicken breast & rice', calories: 540, mealType: 'Lunch' },
    { id: 'm3', name: 'Paneer bhurji & paratha', calories: 380, mealType: 'Dinner' },
    { id: 'm4', name: 'Curd & fruit', calories: 283, mealType: 'Snack' },
  ],
} as const

export const NUTRITION_TARGETS = DEMO_DAY

export const APP_CONFIG = {
  name: 'KcalGrind',
  fullName: 'KcalGrind',
  tagline: 'Calorie and nutrition tracking built for Android',
  description:
    'Tracks calories and macros with food data from USDA and ICMR-NIN. Your food log is stored on your device and works offline.',
  contactEmail: 'support@kcalgrind.app',
  privacyEmail: 'privacy@kcalgrind.app',
  edition: 'Android Edition',
  githubRepo: 'https://github.com/kcalgrindai/kcalgrind',
  notMedicalAdvice:
    'KcalGrind provides nutritional information for personal habit tracking and is not medical advice. Consult a healthcare professional before making dietary changes.',
  icmrCitation:
    'Indian Food Composition Tables (IFCT 2017), National Institute of Nutrition (ICMR-NIN), Indian Council of Medical Research, Hyderabad.',
  appScreenshots: [
    {
      id: 'home',
      title: 'Home Dashboard',
      category: 'Core tracking',
      desc: 'Energy balance ring, macro progress bars, and streak counter.',
      src: '/assets/real_screenshots/home_dashboard.webp',
      alt: 'KcalGrind Home Dashboard interface showing 527 kcal remaining of 2,150 kcal target, macro progress, and 6-day streak on dark OLED display',
      width: 1080,
      height: 2400,
    },
    {
      id: 'diary',
      title: 'Food Diary',
      category: 'On-device log',
      desc: 'Daily meal logs with gram portion weights and calorie tallies.',
      src: '/assets/real_screenshots/food_diary.webp',
      alt: 'KcalGrind Food Diary screen displaying breakfast, lunch, dinner, and snack entries totaling 1,623 kcal with grams and macros',
      width: 1080,
      height: 2400,
    },
    {
      id: 'search',
      title: 'Food Search',
      category: 'USDA & ICMR-NIN',
      desc: 'Search Indian dishes, packaged foods, and USDA benchmarks offline.',
      src: '/assets/real_screenshots/food_search.webp',
      alt: 'KcalGrind Food Search interface showing search query for dal with IFCT raw staples and curated Indian recipe estimates',
      width: 1080,
      height: 2400,
    },
    {
      id: 'detail',
      title: 'Food Detail',
      category: 'Nutritional breakdown',
      desc: 'Macronutrient breakdown and micronutrient details per serving.',
      src: '/assets/real_screenshots/food_detail.webp',
      alt: 'KcalGrind Food Detail screen displaying calculated calories, protein, carbs, and fat per portion weight',
      width: 1080,
      height: 2400,
    },
    {
      id: 'coach',
      title: 'Sproutling AI Coach',
      category: 'Nutrition guidance',
      desc: 'Constructive suggestions on protein distribution and balanced meals.',
      src: '/assets/real_screenshots/ai_coach.webp',
      alt: 'Sproutling AI Coach conversation recommending high-protein tandoori chicken and moong dal dinner fitting remaining calories',
      width: 1080,
      height: 2400,
    },
    {
      id: 'insights',
      title: 'Weekly Trends',
      category: 'Analytics',
      desc: 'Consistency trends, calorie pacing, and daily adherence history.',
      src: '/assets/real_screenshots/insights.webp',
      alt: 'KcalGrind Weekly Insights screen displaying 7-day consistency and calorie adherence analytics',
      width: 1080,
      height: 2400,
    },
    {
      id: 'profile',
      title: 'Profile & Settings',
      category: 'Account control',
      desc: 'Account management, manual cloud backup sync, and complete account deletion.',
      src: '/assets/real_screenshots/profile.webp',
      alt: 'KcalGrind Profile and Settings screen showing 2,150 kcal goal, manual Sync now cloud backup, and account controls',
      width: 1080,
      height: 2400,
    },
  ],
  widgets: [
    {
      id: '4x2',
      name: '4Ã—2 Expanded Widget',
      desc: 'Energy ring, macro bars, quick water logging, and Sproutling companion.',
    },
    {
      id: '2x2',
      name: '2Ã—2 Quick Widget',
      desc: 'Compact square displaying remaining calories and one-tap log action.',
    },
    {
      id: '4x1',
      name: '4Ã—1 Minimal Bar',
      desc: 'Slim horizontal bar showing daily calorie progress and macro values.',
    },
  ],
}

export const FAQS: FAQItem[] = [
  {
    question: 'How is my nutrition data stored and synced?',
    answer:
      'KcalGrind handles data across two separate paths: (1) On-Device Storage (Default): Your food log, daily totals, and chat history are stored directly on your Android device in local SQLite (Room) and work completely offline. (2) Cloud Backup ("Sync now"): Cloud backup is strictly manual with no automatic background sync. When you are signed in and tap "Sync now" in Profile, the app uploads 9 entities from your local database to your private Supabase cloud backup: user profiles, nutrition goals, meal logs, food log items, weight entries, water logs, AI analyses, conversations, and messages (SyncRepositoryImpl.kt:80-118; server.js:1658-1770). Food diaries are stored in your cloud backup when you manually sync. Custom foods, saved meal templates, and recipes are stored locally on your device in v1 and are not uploaded to cloud backup. Signing into an existing account on a new install automatically restores your backed-up profile, nutrition goals, meals, weights, water logs, and coach chat.',
  },
  {
    question: 'What data is sent to Sproutling AI Coach and Google Gemini?',
    answer:
      'For AI features (meal photo analysis, voice descriptions, and Sproutling chat), requests are routed through our secure server proxy to Google\'s Gemini API under Google\'s Gemini API Terms (https://ai.google.dev/gemini-api/terms). Photo & Audio Endpoints: Meal photos and voice audio are processed strictly in-memory by Gemini and discarded upon request completion (server.js:357-410); the server does not store media files. Sproutling AI Coach: Each coach message transmits conversation history and user context (goal, target calories, consumed calories, remaining calories, and weekly trends) to Gemini (server.js:1460-1607). Our server records only an atomic daily quota counter in the ai_coach_daily_usage table (server.js:1570); AI endpoints do not store chat transcripts or media. Transcripts remain on your device and are only backed up to your private cloud storage if you manually tap "Sync now".',
  },
  {
    question: 'Where does food and nutrition data come from?',
    answer:
      'KcalGrind references USDA FoodData Central and the Indian Food Composition Tables (IFCT 2017) by ICMR-NIN for single-ingredient raw staples, alongside curated regional recipe estimates for traditional Indian dishes. It also queries Open Food Facts for packaged product barcodes.',
  },
  {
    question: 'How do the Android home screen widgets work?',
    answer:
      'Our native widgets run on your Android home screen in 4Ã—2, 2Ã—2, and 4Ã—1 sizes. They read your daily energy and macro totals from your on-device log, adapt to system dark and light themes, and include a privacy toggle to conceal figures when in public.',
  },
  {
    question: 'Does KcalGrind support Health Connect?',
    answer:
      'Yes. If enabled, KcalGrind reads daily step counts from Android Health Connect to account for daily energy expenditure. KcalGrind only reads these parameters and never modifies your Health Connect records.',
  },
  {
    question: 'How can I delete my account and data?',
    answer:
      'You can delete your account and all associated data at any time directly in the app by opening Profile â†’ Delete Account, or online at kcalgrind.app/delete-account. Deleting your account wipes your profile, food logs, goals, usage records, and synced backups across all systems. Active Google Play subscriptions must be cancelled separately in Google Play.',
  },
  {
    question: 'Who is Sproutling?',
    answer:
      'Sproutling is your nutrition coach in KcalGrind, offering constructive, practical guidance on meal balance, daily protein distribution, and consistent habits.',
  },
  {
    question: 'How does the Android closed beta work?',
    answer:
      'Submit your Google account email in the beta signup section to request access to our closed testing track on Google Play. Your email is stored securely solely to deliver your closed test invitation. It is retained only during testing and purged immediately upon release or when you request removal at privacy@kcalgrind.app.',
  },
]
