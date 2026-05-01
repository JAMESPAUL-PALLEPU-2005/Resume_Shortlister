📄 Resume Ranking and Classification System

🚀 Overview

The Resume Ranking and Classification System is a Java-based desktop application designed to automate the resume screening process. It parses resumes, extracts key information, evaluates candidates based on user-defined criteria, and ranks them to assist recruiters in efficient decision-making.

🎯 Features

📂 Bulk resume processing (PDF, DOC, DOCX)

🔍 Automatic text extraction using Apache PDFBox and Apache POI

🧠 Keyword-based information extraction (skills, education, experience, contact details)

✅ Candidate classification (Shortlisted / Rejected)

📊 Weighted scoring and ranking of candidates

📁 Automatic folder organization of results

📄 Summary file generation with shortlisted candidate details

🖥️ User-friendly GUI (Java Swing)


🛠️ Tech Stack

Language: Java

Libraries: Apache PDFBox, Apache POI

GUI: Java Swing

Build Tools (Optional): Maven / Gradle



💻 System Requirements

Software
JDK 8 or higher (JDK 11/17 recommended)

IDE: Eclipse / IntelliJ / NetBeans

OS: Windows / macOS / Linux

Hardware
Processor: Intel i3 / Ryzen 3 or higher

RAM: 4 GB (8 GB recommended)

Storage: 500 MB minimum


⚙️ How It Works
User selects a folder containing resumes

Defines search criteria (skills, experience, etc.)

System processes each resume:

Parses document

Extracts keywords

Evaluates against criteria

Candidates are:

Shortlisted or Rejected

Shortlisted candidates are:

Ranked based on score

Output is generated:

Organized folders


Summary file with contact details

📁 Project Structure
app/

 ├── Application.java
 
 ├── ResumeScorer.java
 
 ├── Candidate.java
 
 ├── SkillWeight.java
 
 ├── Parser/
 
 ├── Extractor/
 
 ├── Engine/
 
 └── Output/
 
▶️ How to Run
Clone/download the project

Open in IDE (Eclipse/IntelliJ)

Add required libraries (PDFBox, POI)

Run Application.java

Use GUI to select folder and execute


📈 Future Enhancements

NLP-based resume analysis

Machine learning-based ranking

Web-based version (Spring Boot + React)

Database integration

Email notifications

Cloud deployment


🤝 Contribution

Contributions are welcome! Feel free to fork and improve the system.


📜 License
This project is for academic and educational purposes.
