package app;

import documentReader.*;
import documentReader.exceptions.UnsupportedFileException;
import org.apache.commons.io.FileUtils;
import profileDataExtractor.*;
import rules.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Core application logic for parsing, scoring, and classifying resume profiles.
 * Reads resumes from a folder, scores them against weighted skill criteria,
 * applies selection rules, and sorts them into ShortListed and Rejected directories.
 */
public class Application {

    public Application() {
    }

    /**
     * Parses all resume files in the given folder, scores and classifies them.
     *
     * @param resumeFolder  the folder containing resume files
     * @param selectionRule the rule to apply for classification
     * @param skillWeights  list of skills with weights for scoring
     * @return sorted list of all candidates with scores (descending by score)
     */
    public List<Candidate> parseProfiles(File resumeFolder, Rule selectionRule, List<SkillWeight> skillWeights) {
        String folderLocation = resumeFolder.getAbsolutePath();
        FirstLineExtractor candidateNameExtractor = new FirstLineExtractor();

        // Create output directories upfront
        File shortlistedDir = new File(folderLocation + File.separator + "ShortListedProfiles");
        File rejectedDir = new File(folderLocation + File.separator + "RejectedProfiles");
        shortlistedDir.mkdirs();
        rejectedDir.mkdirs();

        // Guard against null (invalid directory)
        File[] files = resumeFolder.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("No files found in the selected directory.");
            return Collections.emptyList();
        }

        // Create the scorer
        ResumeScorer scorer = new ResumeScorer(skillWeights);

        List<Candidate> shortlistedCandidates = new ArrayList<>();
        List<Candidate> rejectedCandidates = new ArrayList<>();

        for (File file : files) {
            // Skip directories and output files
            if (file.isDirectory() || file.getName().startsWith("_DONE")) continue;

            try {
                String documentText = MainDocumentReader.getDocumentText(file, getDocumentReadersList());

                // Extract candidate name from the first trustworthy line; fall back to file name.
                String candidateName = candidateNameExtractor.getData(documentText);
                if (candidateName == null || candidateName.trim().isEmpty() || "Unable to find data.".equals(candidateName)) {
                    candidateName = formatCandidateNameFromFile(file.getName());
                }

                // Score this resume
                Candidate candidate = scorer.score(candidateName, file.getName(), documentText);

                if (selectionRule.interpret(documentText)) {
                    shortlistedCandidates.add(candidate);
                    FileUtils.copyFile(file, getDestinationForSelectedProfile(file, folderLocation));
                } else {
                    rejectedCandidates.add(candidate);
                    FileUtils.copyFile(file, getDestinationForRejectedProfile(file, folderLocation));
                }
            } catch (UnsupportedFileException e) {
                System.out.println("Skipping unsupported file: " + file.getName());
            } catch (Exception e) {
                System.err.println("Error processing file: " + file.getName());
                e.printStackTrace();
            }
        }

        // Sort both lists by score (highest first)
        Collections.sort(shortlistedCandidates);
        Collections.sort(rejectedCandidates);

        // Write ranked summary file
        File summaryFile = new File(folderLocation + File.separator + "_DONE" + getTimeStamp() + ".txt");
        String summaryContent = buildSummary(shortlistedCandidates, rejectedCandidates, skillWeights);
        writeToFile(summaryFile, summaryContent);

        // Move summary to shortlisted folder
        try {
            File destination = new File(shortlistedDir, summaryFile.getName());
            Files.move(summaryFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Could not move summary file to ShortListedProfiles folder.");
            e.printStackTrace();
        }

        System.out.println("Classification complete. Shortlisted: " + shortlistedCandidates.size()
                + ", Rejected: " + rejectedCandidates.size());

        // Return all candidates sorted by score for UI display
        List<Candidate> allCandidates = new ArrayList<>(shortlistedCandidates);
        allCandidates.addAll(rejectedCandidates);
        Collections.sort(allCandidates);
        return allCandidates;
    }

    private String formatCandidateNameFromFile(String fileName) {
        String baseName = fileName;
        int extensionIndex = baseName.lastIndexOf('.');
        if (extensionIndex > 0) {
            baseName = baseName.substring(0, extensionIndex);
        }

        String cleaned = baseName
                .replaceAll("(?i)\\b(resume|cv|profile|candidate|updated|final)\\b", " ")
                .replaceAll("[_\\-.]+", " ")
                .replaceAll("\\s+", " ")
                .trim();

        return cleaned.isEmpty() ? fileName : cleaned;
    }

    /**
     * Backward-compatible overload: no scoring, existing behavior.
     */
    public void parseProfiles(File resumeFolder, Rule selectionRule) {
        parseProfiles(resumeFolder, selectionRule, Collections.emptyList());
    }

    /**
     * Builds a formatted summary string with rankings, scores, and matched skills.
     */
    private String buildSummary(List<Candidate> shortlisted, List<Candidate> rejected, List<SkillWeight> skillWeights) {
        StringBuilder sb = new StringBuilder();

        sb.append("═══════════════════════════════════════════════════\n");
        sb.append("         RESUME CLASSIFICATION REPORT\n");
        sb.append("═══════════════════════════════════════════════════\n");
        sb.append("Generated: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n\n");

        // Scoring criteria
        sb.append("── SCORING CRITERIA ──\n");
        if (!skillWeights.isEmpty()) {
            int totalWeight = skillWeights.stream().mapToInt(SkillWeight::getWeight).sum();
            for (SkillWeight sw : skillWeights) {
                sb.append(String.format("  • %-20s  Weight: %d / %d\n", sw.getSkillName(), sw.getWeight(), totalWeight));
            }
            sb.append(String.format("\n  Formula: Score = (Matched Weight / %d) × 100\n\n", totalWeight));
        } else {
            sb.append("  No weighted scoring applied.\n\n");
        }

        // Shortlisted section
        sb.append("SHORTLISTED CANDIDATES DETAILS\n\n");
        if (shortlisted.isEmpty()) {
            sb.append("  No candidates shortlisted.\n\n");
        } else {
            int rank = 1;
            profileDataExtractor.ContactExtractor contactExtractor = new profileDataExtractor.ContactExtractor();
            profileDataExtractor.EmailIDExtractor emailExtractor = new profileDataExtractor.EmailIDExtractor();
            for (Candidate c : shortlisted) {
                String name = c.getName();
                if (name == null || name.trim().isEmpty() || name.equals(c.getFileName())) {
                    name = "Details not provided";
                }
                
                String contact = contactExtractor.getData(c.getResumeText());
                if (contact == null || contact.trim().isEmpty() || contact.contains("Unable to find data.")) {
                    contact = "Details not provided";
                }
                
                String email = emailExtractor.getData(c.getResumeText());
                if (email == null || email.trim().isEmpty() || email.contains("Unable to find data.")) {
                    email = "Details not provided";
                }
                
                sb.append(String.format("%d. Name: %s\n", rank++, name));
                sb.append(String.format("   Contact Number: %s\n", contact));
                sb.append(String.format("   Email Address: %s\n\n", email));
            }
        }

        // Rejected section
        sb.append("── REJECTED CANDIDATES (" + rejected.size() + ") ──\n");
        if (rejected.isEmpty()) {
            sb.append("  No candidates rejected.\n");
        } else {
            sb.append(String.format("  %-4s %-30s %-10s %s\n", "Rank", "Name", "Score", "Matched Skills"));
            sb.append("  " + "─".repeat(80) + "\n");
            int rank = 1;
            for (Candidate c : rejected) {
                sb.append(String.format("  %-4d %-30s %5.1f%%    %s\n",
                        rank++, truncate(c.getName(), 28), c.getScore(), String.join(", ", c.getMatchedSkills())));
            }
        }
        sb.append("\n═══════════════════════════════════════════════════\n");

        return sb.toString();
    }

    /**
     * Truncates a string to maxLen characters, adding "..." if truncated.
     */
    private String truncate(String text, int maxLen) {
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 3) + "...";
    }



    private String getTimeStamp() {
        return new SimpleDateFormat("yyyy_MM_dd_HH-mm-ss").format(new Date());
    }

    private void writeToFile(File file, String data) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(data);
        } catch (IOException e) {
            System.err.println("Error writing summary file: " + file.getName());
            e.printStackTrace();
        }
    }

    private File getDestinationForRejectedProfile(File file, String folderLocation) {
        return new File(folderLocation + File.separator + "RejectedProfiles" + File.separator + file.getName());
    }

    private File getDestinationForSelectedProfile(File file, String folderLocation) {
        return new File(folderLocation + File.separator + "ShortListedProfiles" + File.separator + file.getName());
    }

    private ArrayList<DataExtractor> getDataExtractors() {
        ArrayList<DataExtractor> dataExtractors = new ArrayList<>();
        dataExtractors.add(new FirstLineExtractor());
        // ExperienceExtractor disabled — planned as future enhancement
        // dataExtractors.add(new ExperienceExtractor());
        dataExtractors.add(new ContactExtractor());
        dataExtractors.add(new EmailIDExtractor());
        return dataExtractors;
    }

    private List<DocumentReader> getDocumentReadersList() {
        List<DocumentReader> readerList = new ArrayList<>();
        readerList.add(new DocxReader());
        readerList.add(new PDFReader());
        readerList.add(new DocReader());
        return readerList;
    }
}
