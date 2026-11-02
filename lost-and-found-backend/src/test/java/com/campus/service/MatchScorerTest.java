package com.campus.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class MatchScorerTest {

    private static String createGradientImage(int brightnessOffset) throws Exception {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
                int val = Math.min(255, Math.max(0, (x * 8) + brightnessOffset));
                int rgb = (val << 16) | (val << 8) | val;
                img.setRGB(x, y, rgb);
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", baos);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private static String createCheckerboardImage() throws Exception {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
                boolean black = ((x / 4) + (y / 4)) % 2 == 0;
                int rgb = black ? 0x000000 : 0xFFFFFF;
                img.setRGB(x, y, rgb);
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", baos);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    @Test
    @DisplayName("Lost wallet vs Found wallet produces correct weighted match breakdown")
    void testWalletMatch() {
        MatchScorer.Candidate lost = MatchScorer.Candidate.of(
                "Accessories",
                "Cafeteria",
                "Black leather wallet",
                "Bi-fold wallet, has a metro card and a photo inside.",
                null
        );

        MatchScorer.Candidate found = MatchScorer.Candidate.of(
                "Accessories",
                "Cafeteria",
                "Black wallet near canteen",
                "Found under a table, looks bi-fold, didn't check inside.",
                null
        );

        MatchScorer.Result result = MatchScorer.score(lost, found);

        assertEquals(40, result.category());
        assertEquals(20, result.location());
        assertTrue(result.keywords() > 0, "Keywords score should be > 0");
        assertFalse(result.imageUsed());
        assertTrue(result.isMatch());
        assertTrue(result.total() >= 75, "Total score should be >= 75, got: " + result.total());
        assertEquals(84, result.total());
        assertEquals("Category +40 \u00b7 Location +20 \u00b7 Keywords +11 \u00b7 Photo not compared", result.explain());
    }

    @Test
    @DisplayName("Same category only (different location, no shared words) is NOT a match")
    void testSameCategoryOnlyNotMatch() {
        MatchScorer.Candidate a = MatchScorer.Candidate.of("Electronics", "Library", "Laptop charger", "Black cable", null);
        MatchScorer.Candidate b = MatchScorer.Candidate.of("Electronics", "Gym", "Water bottle", "Steel flask", null);

        MatchScorer.Result result = MatchScorer.score(a, b);

        assertEquals(40, result.category());
        assertEquals(0, result.location());
        assertEquals(0, result.keywords());
        assertFalse(result.imageUsed());
        assertFalse(result.isMatch());
    }

    @Test
    @DisplayName("Same category and same location but no shared words is NOT a match")
    void testSameCategoryAndLocationNoSharedWordsNotMatch() {
        MatchScorer.Candidate a = MatchScorer.Candidate.of("Electronics", "Library", "Laptop charger", "Black cable", null);
        MatchScorer.Candidate b = MatchScorer.Candidate.of("Electronics", "Library", "Water bottle", "Steel flask", null);

        MatchScorer.Result result = MatchScorer.score(a, b);

        assertEquals(40, result.category());
        assertEquals(20, result.location());
        assertEquals(0, result.keywords());
        assertFalse(result.imageUsed());
        assertFalse(result.isMatch());
    }

    @Test
    @DisplayName("Different category but same location and identical text IS a match")
    void testDifferentCategorySameLocationIdenticalTextIsMatch() {
        MatchScorer.Candidate a = MatchScorer.Candidate.of("Books", "Auditorium", "Mathematics textbook", "Calculus volume edition", null);
        MatchScorer.Candidate b = MatchScorer.Candidate.of("Stationery", "Auditorium", "Mathematics textbook", "Calculus volume edition", null);

        MatchScorer.Result result = MatchScorer.score(a, b);

        assertEquals(0, result.category());
        assertEquals(20, result.location());
        assertEquals(25, result.keywords());
        assertTrue(result.isMatch());
        assertTrue(result.total() >= 50);
    }

    @Test
    @DisplayName("Plural/case differences still give keywords >= 20")
    void testPluralCaseDifferences() {
        MatchScorer.Candidate a = MatchScorer.Candidate.of("Bags", "Room 101", "Grey BACKPACK", "", null);
        MatchScorer.Candidate b = MatchScorer.Candidate.of("Bags", "Room 102", "grey backpacks", "", null);

        MatchScorer.Result result = MatchScorer.score(a, b);

        assertTrue(result.keywords() >= 20, "Keywords score should be >= 20, got: " + result.keywords());
    }

    @Test
    @DisplayName("Identical items with no photos score exactly 100")
    void testIdenticalItemsNoPhotosScore100() {
        MatchScorer.Candidate a = MatchScorer.Candidate.of("Electronics", "Library", "Silver MacBook Pro", "Sticker on top", null);
        MatchScorer.Candidate b = MatchScorer.Candidate.of("Electronics", "Library", "Silver MacBook Pro", "Sticker on top", null);

        MatchScorer.Result result = MatchScorer.score(a, b);

        assertFalse(result.imageUsed());
        assertEquals(100, result.total());
        assertTrue(result.isMatch());
    }

    @Test
    @DisplayName("A photo on only one item: imageUsed false and total unchanged (100 for identical text)")
    void testPhotoOnOnlyOneItem() throws Exception {
        String photo = createGradientImage(0);
        MatchScorer.Candidate a = MatchScorer.Candidate.of("Electronics", "Library", "Silver MacBook Pro", "Sticker on top", photo);
        MatchScorer.Candidate b = MatchScorer.Candidate.of("Electronics", "Library", "Silver MacBook Pro", "Sticker on top", null);

        MatchScorer.Result result = MatchScorer.score(a, b);

        assertFalse(result.imageUsed());
        assertEquals(100, result.total());
        assertTrue(result.isMatch());
    }

    @Test
    @DisplayName("Two similar generated gradient images give image = 15; gradient vs checkerboard gives image = 0")
    void testImageSimilarityScoring() throws Exception {
        String grad1 = createGradientImage(0);
        String grad2 = createGradientImage(10);
        String checker = createCheckerboardImage();

        MatchScorer.Candidate cGrad1 = MatchScorer.Candidate.of("Cat", "Loc", "Title", "Desc", grad1);
        MatchScorer.Candidate cGrad2 = MatchScorer.Candidate.of("Cat", "Loc", "Title", "Desc", grad2);
        MatchScorer.Candidate cChecker = MatchScorer.Candidate.of("Cat", "Loc", "Title", "Desc", checker);

        MatchScorer.Result resGrad = MatchScorer.score(cGrad1, cGrad2);
        assertTrue(resGrad.imageUsed());
        assertEquals(15, resGrad.image());

        MatchScorer.Result resChecker = MatchScorer.score(cGrad1, cChecker);
        assertTrue(resChecker.imageUsed());
        assertEquals(0, resChecker.image());
    }

    @Test
    @DisplayName("Similar photos can carry a match when the words differ")
    void testSimilarPhotosCanCarryMatch() throws Exception {
        String grad1 = createGradientImage(0);
        String grad2 = createGradientImage(10);

        MatchScorer.Candidate a = MatchScorer.Candidate.of("Electronics", "Auditorium", "Silver artifact", "Velvet pouch", grad1);
        MatchScorer.Candidate b = MatchScorer.Candidate.of("Electronics", "Cafeteria", "Golden trinket", "Leather casing", grad2);

        MatchScorer.Result result = MatchScorer.score(a, b);

        assertEquals(40, result.category());
        assertEquals(0, result.location());
        assertEquals(0, result.keywords());
        assertEquals(15, result.image());
        assertTrue(result.imageUsed());
        assertEquals(55, result.total());
        assertTrue(result.isMatch());
    }

    @Test
    @DisplayName("Corrupt image data gives a null hash")
    void testCorruptImageData() {
        assertNull(ImageHasher.averageHash("not-a-valid-image-string"));
        assertNull(ImageHasher.averageHash("data:image/png;base64,invalid!@#$base64"));
        assertNull(ImageHasher.averageHash(""));
        assertNull(ImageHasher.averageHash("   "));
        assertNull(ImageHasher.averageHash(null));
    }
}
