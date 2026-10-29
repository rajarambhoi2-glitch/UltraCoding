package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProjectEntity::class,
        FileEntity::class,
        ChatMessageEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CodeForgeDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao
    abstract fun fileDao(): FileDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: CodeForgeDatabase? = null

        fun getDatabase(context: Context): CodeForgeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CodeForgeDatabase::class.java,
                    "codeforge_ai_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: CodeForgeDatabase) {
            val projectDao = database.projectDao()
            val fileDao = database.fileDao()
            val settingsDao = database.appSettingsDao()

            // Initialize default settings
            settingsDao.saveSettings(
                AppSettingsEntity(
                    id = 1,
                    provider = "gemini",
                    geminiApiKey = "",
                    openAiApiKey = "",
                    claudeApiKey = "",
                    customEndpoint = "https://api.openai.com/v1/chat/completions",
                    customApiKey = "",
                    fontSize = 14,
                    tabSize = 2,
                    autoSave = true,
                    sendCodeToAiToggle = true,
                    languageResponseMode = "hinglish"
                )
            )

            // Seed Project 1: Web App
            val webProjectId = "proj_web_starter"
            projectDao.insertOrUpdate(
                ProjectEntity(
                    id = webProjectId,
                    name = "Smart Web App",
                    description = "Responsive HTML5/CSS3/JS app with interactive counter & Live Preview",
                    templateType = "HTML_SITE",
                    createdAt = System.currentTimeMillis(),
                    lastModified = System.currentTimeMillis()
                )
            )

            fileDao.insertOrUpdate(
                FileEntity(
                    id = "file_html_1",
                    projectId = webProjectId,
                    name = "index.html",
                    path = "index.html",
                    content = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>CodeForge AI Demo</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <div class="container">
    <div class="badge">CodeForge AI Mobile IDE</div>
    <h1>Build Fast. Debug Smarter.</h1>
    <p>Tap Live Preview below to view changes in real-time!</p>
    
    <div class="card">
      <div id="counter-val">0</div>
      <div class="btn-group">
        <button id="dec-btn" class="btn secondary">-</button>
        <button id="reset-btn" class="btn outline">Reset</button>
        <button id="inc-btn" class="btn primary">+</button>
      </div>
    </div>

    <div class="ai-box">
      <h3>AI Bug Finder Tip</h3>
      <p id="tip-text">AI Scanner checks syntax, XSS risk and logic bugs automatically.</p>
    </div>
  </div>
  <script src="script.js"></script>
</body>
</html>""",
                    language = "html",
                    isDirty = false
                )
            )

            fileDao.insertOrUpdate(
                FileEntity(
                    id = "file_css_1",
                    projectId = webProjectId,
                    name = "style.css",
                    path = "style.css",
                    content = """* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

body {
  background: #0f172a;
  color: #f8fafc;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
}

.container {
  max-width: 420px;
  width: 100%;
  text-align: center;
}

.badge {
  display: inline-block;
  background: rgba(56, 189, 248, 0.15);
  color: #38bdf8;
  padding: 4px 12px;
  border-radius: 9999px;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 12px;
  border: 1px solid rgba(56, 189, 248, 0.3);
}

h1 {
  font-size: 26px;
  font-weight: 700;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #38bdf8, #818cf8);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

p {
  color: #94a3b8;
  font-size: 14px;
  margin-bottom: 24px;
}

.card {
  background: #1e293b;
  border: 1px solid #334155;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.5);
  margin-bottom: 20px;
}

#counter-val {
  font-size: 54px;
  font-weight: 800;
  color: #38bdf8;
  margin-bottom: 16px;
}

.btn-group {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.btn {
  padding: 12px 20px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: transform 0.1s;
}

.btn:active {
  transform: scale(0.96);
}

.primary {
  background: #0284c7;
  color: white;
}

.secondary {
  background: #334155;
  color: white;
}

.outline {
  background: transparent;
  color: #94a3b8;
  border: 1px solid #475569;
}

.ai-box {
  background: rgba(129, 140, 248, 0.1);
  border: 1px dashed rgba(129, 140, 248, 0.4);
  border-radius: 12px;
  padding: 14px;
  text-align: left;
}

.ai-box h3 {
  font-size: 13px;
  color: #a78bfa;
  margin-bottom: 4px;
}

.ai-box p {
  font-size: 12px;
  color: #cbd5e1;
  margin-bottom: 0;
}""",
                    language = "css",
                    isDirty = false
                )
            )

            fileDao.insertOrUpdate(
                FileEntity(
                    id = "file_js_1",
                    projectId = webProjectId,
                    name = "script.js",
                    path = "script.js",
                    content = """// CodeForge Interactive Script
let count = 0;
const counterDisplay = document.getElementById("counter-val");
const incBtn = document.getElementById("inc-btn");
const decBtn = document.getElementById("dec-btn");
const resetBtn = document.getElementById("reset-btn");

function updateDisplay() {
  counterDisplay.textContent = count;
  if (count > 0) {
    counterDisplay.style.color = "#38bdf8";
  } else if (count < 0) {
    counterDisplay.style.color = "#f43f5e";
  } else {
    counterDisplay.style.color = "#94a3b8";
  }
}

incBtn.addEventListener("click", () => {
  count++;
  updateDisplay();
});

decBtn.addEventListener("click", () => {
  count--;
  updateDisplay();
});

resetBtn.addEventListener("click", () => {
  count = 0;
  updateDisplay();
});

console.log("CodeForge web preview initialized successfully!");""",
                    language = "javascript",
                    isDirty = false
                )
            )

            // Seed Project 2: Python Algorithms
            val pythonProjectId = "proj_python_algo"
            projectDao.insertOrUpdate(
                ProjectEntity(
                    id = pythonProjectId,
                    name = "Python Algorithms",
                    description = "Data structures, search algorithms and test cases",
                    templateType = "PYTHON_SCRIPT",
                    createdAt = System.currentTimeMillis() - 10000,
                    lastModified = System.currentTimeMillis() - 5000
                )
            )

            fileDao.insertOrUpdate(
                FileEntity(
                    id = "file_py_1",
                    projectId = pythonProjectId,
                    name = "main.py",
                    path = "main.py",
                    content = """# CodeForge Python Algorithms Demo
import time
from typing import List, Optional

def binary_search(arr: List[int], target: int) -> Optional[int]:
    \"\"\"Perform binary search on a sorted integer list.\"\"\"
    low = 0
    high = len(arr) - 1

    while low <= high:
        mid = (low + high) // 2
        guess = arr[mid]
        if guess == target:
            return mid
        elif guess > target:
            high = mid - 1
        else:
            low = mid + 1
    return None

def main():
    numbers = [2, 5, 8, 12, 16, 23, 38, 56, 72, 91]
    target = 23
    
    start_time = time.time()
    index = binary_search(numbers, target)
    duration = (time.time() - start_time) * 1000
    
    if index is not None:
        print(f"Target {target} found at index: {index} in {duration:.4f} ms")
    else:
        print(f"Target {target} not found in array.")

if __name__ == "__main__":
    main()
""",
                    language = "python",
                    isDirty = false
                )
            )
        }
    }
}
