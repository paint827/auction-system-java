package vn.auction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SampleTest {

    @Test
    @DisplayName("Kiểm tra môi trường kiểm thử JUnit 5")
    void testSanity() {
        Assertions.assertTrue(true, "Môi trường JUnit 5 hoạt động bình thường.");
    }
}

