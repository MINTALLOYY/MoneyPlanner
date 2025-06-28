# Money Planner

**Money Planner** is an Android app designed to help students manage their finances by organizing expenses and income into customizable categories. The app tracks the user's current balance and provides insightful visualizations through charts, allowing for a clear understanding of financial habits. Features include balance tracking, analytical charts, and a smart receipt scanner.

## Features

- 📊 **Balance Tracker** – Monitor your current financial status in real-time.
- 📈 **Charts & Analytics** – Visualize:
  - Balance over time
  - Total spending trends
  - Total earnings trends
  - Expense categories (pie chart)
  - Income sources (pie chart)
- 🧾 **Receipt Scanner** – Automatically extract expenses from receipts using OCR.
- 🧭 **Navigation Helper** – AI-assisted guidance through app functionality.
- 💾 **SQLite** – Database for storing all user transactions and categories

## Templates/Technologies Used

- **Android SDK + Kotlin/Java**
- **[MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)** – For displaying bar, line, and pie charts.
- **AWS Textract (via API)** – OCR for extracting text from receipts.
- **Gemini 2.0 Flash (via API)** – AI used for navigation and assistance.
- **[Dribbble](https://dribbble.com/shots/25435642-Feenance-Finance-Mobile-App)** – Site for developers to post their app concepts, used for inspiration for UI

## Prerequisites

- Android Studio (latest version recommended)
- Android SDK installed
- Git for version control

## Installation & Setup

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/MINTALLOYY/MoneyPlanner2.git
   ```
2. **Open in Android Studio:**
   
- Open Android Studio.
- Select "Open an Existing Project" and choose the cloned repository.

4. **Setup Virtual Device:**
   
- Go to AVD Manager and create a new Virtual Device (e.g., Pixel 5, API 33).

6. **Build the Project:**
   
- Sync Gradle files.
- Click Run to launch the app on your emulator.
