package profileDataExtractor;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for data extractors (ContactExtractor, EmailIDExtractor, FirstLineExtractor).
 */
public class ExtractorTest {

    @Test
    public void testEmailExtraction() {
        EmailIDExtractor extractor = new EmailIDExtractor();
        String doc = "Name: John Doe\nEmail: john.doe@example.com\nPhone: 1234567890";
        String result = extractor.getData(doc);
        Assert.assertTrue(result.contains("john.doe@example.com"),
                "Should extract email address. Got: " + result);
    }

    @Test
    public void testEmailExtraction_NotFound() {
        EmailIDExtractor extractor = new EmailIDExtractor();
        String doc = "Name: John Doe\nNo email here.";
        String result = extractor.getData(doc);
        Assert.assertEquals(result, "Unable to find data.");
    }

    @Test
    public void testContactExtraction_TenDigit() {
        ContactExtractor extractor = new ContactExtractor();
        String doc = "Contact: 9876543210";
        String result = extractor.getData(doc);
        Assert.assertTrue(result.contains("9876543210"),
                "Should extract 10-digit number. Got: " + result);
    }

    @Test
    public void testContactExtraction_WithCountryCode() {
        ContactExtractor extractor = new ContactExtractor();
        String doc = "Phone: +91 9876543210";
        String result = extractor.getData(doc);
        Assert.assertNotEquals(result, "Unable to find data.",
                "Should extract number with country code");
    }

    @Test
    public void testFirstLineExtractor() {
        FirstLineExtractor extractor = new FirstLineExtractor();
        String doc = "JOHN DOE\nSoftware Engineer\nExperience: 5 years";
        String result = extractor.getData(doc);
        Assert.assertEquals(result, "JOHN DOE");
    }

    @Test
    public void testFirstLineExtractor_IgnoresTemplatePlaceholders() {
        FirstLineExtractor extractor = new FirstLineExtractor();
        String doc = "[Type Text] [Type Text] [Type Text]\nJennifer M. Conte\nSpring Boot Developer";
        String result = extractor.getData(doc);
        Assert.assertEquals(result, "Jennifer M. Conte");
    }

    @Test
    public void testFirstLineExtractor_ReturnsNotFoundForOnlyTemplateContent() {
        FirstLineExtractor extractor = new FirstLineExtractor();
        String doc = "[Type Text] [Type Text] [Type Text]\nSoftware Engineer\nSkills";
        String result = extractor.getData(doc);
        Assert.assertEquals(result, "Unable to find data.");
    }
}
